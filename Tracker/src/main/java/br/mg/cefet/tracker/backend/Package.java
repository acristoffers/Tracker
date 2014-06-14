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

package br.mg.cefet.tracker.backend;

import android.content.Context;

import java.util.Date;
import java.util.List;

public class Package implements Correios.SyncDone {

    private Correios correios = null;
    private Store store = null;
    private StatusReady listener = null;

    private int id = -1;
    private String name;
    private boolean active;
    private List<Correios.Step> steps;
    private Date timeCreated;
    private Date timeUpdated;

    public Package(String cod, Context context) {
        store = new Store(context);
        steps = store.getSteps(cod);
        name = store.getName(cod);
        active = store.getActive(cod);
        id = store.getId(cod);
        correios = new Correios(cod, this);
        timeCreated = store.getTimeCreated(cod);
        timeUpdated = store.getTimeUpdated(cod);
    }

    public static List<String> getCodes(Context context) {
        return Store.getCodes(context);
    }

    public Date getTimeUpdated() {
        return timeUpdated;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public void checkForStatusUpdates() {
        correios.sync();
    }

    public void remove() {
        store.removePackage(this);
    }

    @Override
    public void finishedSyncing(boolean success) {
        List<Correios.Step> steps = correios.getSteps();
        if (success && steps != null) {
            this.steps = steps;
        }

        if (listener != null) {
            listener.statusUpdated(this);
        }
    }

    public void save() {
        store.updatePackage(this);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCod() {
        return correios.getCod();
    }

    public int getId() {
        return id;
    }

    public List<Correios.Step> getSteps() {
        return steps;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setListener(StatusReady listener) {
        this.listener = listener;
    }

    public interface StatusReady {
        public void statusUpdated(Package pkg);
    }

}
