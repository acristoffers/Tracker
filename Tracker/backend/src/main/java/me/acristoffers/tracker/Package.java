/*
 * Copyright (c) 2014 Álan Crístoffer
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package me.acristoffers.tracker;

import android.content.Context;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Package implements Correios.SyncDone {

    private final WeakReference<Context> context;
    private final WeakReference<StatusReady> listener;
    private final String code;
    private final ArrayList<Correios.Step> steps = new ArrayList<>();
    private int id = -1;
    private String name = "";
    private boolean active = false;
    private Date timeCreated = new Date();
    private Date timeUpdated = new Date();

    public Package(final String cod, final Context context, final StatusReady listener) {
        this.context = new WeakReference<>(context);
        this.listener = new WeakReference<>(listener);
        this.code = cod;

        final Store store = new Store(context);
        steps.clear();
        steps.addAll(store.getSteps(cod));

        final HashMap<String, Object> pkg = store.getPackage(cod);

        if (pkg.containsKey(Store.KEY_NAME)) {
            name = (String) pkg.get(Store.KEY_NAME);
        }

        if (pkg.containsKey(Store.KEY_ACTIVE)) {
            active = (boolean) pkg.get(Store.KEY_ACTIVE);
        }

        if (pkg.containsKey(Store.KEY_ID)) {
            id = (int) pkg.get(Store.KEY_ID);
        }

        if (pkg.containsKey(Store.KEY_TIME_CREATED)) {
            timeCreated = (Date) pkg.get(Store.KEY_TIME_CREATED);
        }

        if (pkg.containsKey(Store.KEY_TIME_UPDATED)) {
            timeUpdated = (Date) pkg.get(Store.KEY_TIME_UPDATED);
        }
    }

    public static ArrayList<Package> allPackages(final Context context) {
        final ArrayList<Package> packages = new ArrayList<>();
        final Store store = new Store(context);

        final ArrayList<String> codes = store.allCodes();
        for (final String code : codes) {
            Package pkg = new Package(code, context, null);
            packages.add(pkg);
        }

        return packages;
    }

    public Date getTimeUpdated() {
        return timeUpdated;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public void checkForStatusUpdates() {
        final Correios correios = new Correios(code, this);
        correios.sync();
    }

    public void remove() {
        final Store store = new Store(context.get());
        store.removePackage(this);
    }

    @Override
    public void finishedSyncing(final boolean success, final Correios correios) {
        final ArrayList<Correios.Step> steps = correios.getSteps();
        if (success && steps != null && !steps.isEmpty()) {
            this.steps.clear();
            this.steps.addAll(steps);
        }

        if (listener.get() != null) {
            listener.get().statusUpdated(this);
        }
    }

    public void save() {
        final Store store = new Store(context.get());
        store.updatePackage(this);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public String getCod() {
        final Correios correios = new Correios(code, this);
        return correios.getCod();
    }

    public int getId() {
        return id;
    }

    public ArrayList<Correios.Step> getSteps() {
        return steps;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(final Object o) {
        try {
            final Package pkg = (Package) o;
            final String code = pkg.getCod();
            return getCod().equalsIgnoreCase(code);
        } catch (Exception e) {
            return false;
        }
    }

    public interface StatusReady {
        void statusUpdated(Package pkg);
    }
}
