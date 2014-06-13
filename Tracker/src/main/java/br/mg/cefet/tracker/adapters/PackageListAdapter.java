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

package br.mg.cefet.tracker.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import br.mg.cefet.tracker.R;
import br.mg.cefet.tracker.backend.Correios;
import br.mg.cefet.tracker.backend.Package;

public class PackageListAdapter extends BaseAdapter implements Package.StatusReady {

    private final Context context;
    private boolean showInactive = false;
    private List<Package> packageList;
    private LayoutInflater layoutInflater;

    public PackageListAdapter(Context context, boolean showInactive) {
        this.context = context;
        this.showInactive = showInactive;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        updatePackageList();
    }

    public void updatePackageList() {
        packageList = new ArrayList<>();

        List<String> codes = Package.getCodes(context);
        for (String code : codes) {
            Package pkg = new Package(code, context);
            if (!pkg.getActive() && !showInactive) {
                continue;
            }

            pkg.setListener(this);
            packageList.add(pkg);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return packageList.size();
    }

    @Override
    public Object getItem(int i) {
        return packageList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.listview_item_packages, null);
        }

        final Package pkg = packageList.get(i);

        String name = pkg.getName();
        String code = pkg.getCod();
        String title = "";
        String date = "";

        List<Correios.Step> steps = pkg.getSteps();
        if (steps.size() > 0) {
            Correios.Step step = steps.get(0);
            SimpleDateFormat sdf = new SimpleDateFormat();
            title = step.title;
            date = sdf.format(step.date);
        }

        TextView textView = (TextView) view.findViewById(R.id.name);
        if (textView != null) {
            textView.setText(name);
        }

        textView = (TextView) view.findViewById(R.id.code);
        if (textView != null) {
            textView.setText(code);
        }

        textView = (TextView) view.findViewById(R.id.title);
        if (textView != null) {
            textView.setText(title);
        }

        textView = (TextView) view.findViewById(R.id.date);
        if (textView != null) {
            textView.setText(date);
        }

        return view;
    }

    @Override
    public void statusUpdated(Package pkg) {
        updatePackageList();
    }
}
