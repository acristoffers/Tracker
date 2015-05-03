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

package me.acristoffers.tracker.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import me.acristoffers.tracker.Correios;
import me.acristoffers.tracker.Package;
import me.acristoffers.tracker.R;
import me.acristoffers.tracker.activities.PackageDetails;

public class PackageListAdapter extends RecyclerView.Adapter implements Package.StatusReady {

    private Activity context = null;
    private List<Package> packages = new ArrayList<>();
    private boolean showInactive = false;
    private LayoutInflater layoutInflater;

    public PackageListAdapter(Activity context) {
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        filterPackages();
    }

    @Override
    @SuppressLint("InflateParams")
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.package_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        View view = holder.itemView;
        ViewHolder viewHolder = (ViewHolder) holder;

        final Package pkg = packages.get(position);

        String name = pkg.getName();
        String code = pkg.getCod();
        String title;
        String date = "";
        String local = "";

        List<Correios.Step> steps = pkg.getSteps();
        if (steps.size() > 0) {
            Correios.Step step = steps.get(0);
            DateFormat sdf = SimpleDateFormat.getDateTimeInstance();
            title = step.title;
            local = step.local;
            date = sdf.format(step.date);
        } else {
            title = view.getResources().getString(R.string.empty_steps);
        }

        TextView textView = viewHolder.getName();
        if (textView != null) {
            textView.setText(name);
        }

        textView = viewHolder.getCode();
        if (textView != null) {
            textView.setText(code);
        }

        textView = viewHolder.getTitle();
        if (textView != null) {
            textView.setText(title);
        }

        textView = viewHolder.getDate();
        if (textView != null) {
            textView.setText(date);
        }

        textView = viewHolder.getLocal();
        if (textView != null) {
            textView.setText(local);
        }

        view = viewHolder.getLayout();
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PackageDetails.class);
                intent.putExtra(PackageDetails.PACKAGE_CODE, pkg.getCod());

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return packages.size();
    }

    @Override
    public void statusUpdated(Package pkg) {
        filterPackages();
    }

    public boolean getShowInactive() {
        return showInactive;
    }

    public void setShowInactive(boolean showInactive) {
        if (showInactive != this.showInactive) {
            this.showInactive = showInactive;
            filterPackages();
        }
    }

    public void filterPackages() {
        packages.clear();

        List<Package> allPackages = Package.allPackages(context);
        for (Package pkg : allPackages) {
            if (pkg.getName().isEmpty()) {
                pkg.remove();
                continue;
            }

            if (!pkg.isActive() && !showInactive) {
                continue;
            }

            pkg.setListener(this);
            packages.add(pkg);
        }

        notifyDataSetChanged();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name, code, title, date, local;
        private View layout;

        public ViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.name);
            code = (TextView) itemView.findViewById(R.id.code);
            title = (TextView) itemView.findViewById(R.id.title);
            date = (TextView) itemView.findViewById(R.id.date);
            local = (TextView) itemView.findViewById(R.id.local);
            layout = itemView.findViewById(R.id.layout);
        }

        public TextView getName() {
            return name;
        }

        public TextView getCode() {
            return code;
        }

        public TextView getTitle() {
            return title;
        }

        public TextView getDate() {
            return date;
        }

        public TextView getLocal() {
            return local;
        }

        public View getLayout() {
            return layout;
        }
    }
}
