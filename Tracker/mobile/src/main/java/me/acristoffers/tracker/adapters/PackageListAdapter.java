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
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.CardView;
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

public class PackageListAdapter extends RecyclerView.Adapter implements Package.StatusReady {

    public static boolean isSelecting = false;
    private static boolean showInactive = false;
    private Activity context = null;
    private ArrayList<Package> packages = new ArrayList<>();
    private LayoutInflater layoutInflater = null;
    private OnCardViewClickedListener listener = null;
    private ArrayList<Package> selection = new ArrayList<>();

    public PackageListAdapter(Activity context) {
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        filterPackages();
    }

    @Override
    @SuppressLint("InflateParams")
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.package_item, parent, false);

        CardView cardView = (CardView) view;
        cardView.setMaxCardElevation(view.getResources().getDimension(R.dimen.card_elevation_selected));

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final View view = holder.itemView;
        ViewHolder viewHolder = (ViewHolder) holder;

        if (!isSelecting) {
            selection.clear();
        }

        final Package pkg = packages.get(position);

        String name = pkg.getName();
        String code = pkg.getCod();
        String title;
        String date = "";
        String local = "";

        List<Correios.Step> steps = pkg.getSteps();
        if (steps.size() > 0) {
            Correios.Step step = steps.get(steps.size() - 1);
            DateFormat sdf = SimpleDateFormat.getDateTimeInstance();
            title = step.title;
            local = step.local;
            date = sdf.format(step.date);
        } else {
            title = view.getResources().getString(R.string.empty_steps);
        }

        View v = viewHolder.getInactiveIcon();
        if (v != null) {
            v.setVisibility(pkg.isActive() ? View.GONE : View.VISIBLE);
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

        View layout = viewHolder.getLayout();
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    if (isSelecting) {
                        if (selection.contains(pkg)) {
                            List<Package> removeList = new ArrayList<>();
                            for (Package p : selection) {
                                if (p.equals(pkg)) {
                                    removeList.add(p);
                                }
                            }
                            selection.removeAll(removeList);
                        } else {
                            selection.add(pkg);
                        }

                        notifyDataSetChanged();
                    }

                    listener.onCardViewClicked(pkg);
                }
            }
        });

        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null && !isSelecting) {
                    listener.onCardViewLongClicked(pkg);
                    isSelecting = true;
                    selection.add(pkg);
                    notifyDataSetChanged();
                }

                return true;
            }
        });

        CardView cardView = (CardView) view;

        if (selection.contains(pkg)) {
            Resources resources = view.getResources();
            cardView.setCardElevation(resources.getDimension(R.dimen.card_elevation_selected));
            cardView.setCardBackgroundColor(resources.getColor(R.color.primary_light));
        } else {
            cardView.setCardElevation(view.getResources().getDimension(R.dimen.card_elevation));
            cardView.setCardBackgroundColor(Color.WHITE);
        }
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
        if (showInactive != PackageListAdapter.showInactive) {
            PackageListAdapter.showInactive = showInactive;
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

    public void filterPackages(String query) {
        if (query == null) {
            filterPackages();
            return;
        }

        packages.clear();

        List<Package> allPackages = Package.allPackages(context);
        for (Package pkg : allPackages) {
            if (pkg.getName().isEmpty()) {
                pkg.remove();
                continue;
            }

            String queryUpperCase = query.toUpperCase();
            String pkgNameUpperCase = pkg.getName().toUpperCase();
            String pkgCodeUpperCase = pkg.getCod().toUpperCase();
            if (!pkgNameUpperCase.contains(queryUpperCase) && !pkgCodeUpperCase.contains(queryUpperCase)) {
                continue;
            }

            pkg.setListener(this);
            packages.add(pkg);
        }

        notifyDataSetChanged();
    }

    public void setListener(OnCardViewClickedListener listener) {
        this.listener = listener;
    }

    public interface OnCardViewClickedListener {
        void onCardViewClicked(Package pkg);

        void onCardViewLongClicked(Package pkg);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name, code, title, date, local;
        private View layout;
        private View inactiveIcon;

        public ViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.name);
            code = (TextView) itemView.findViewById(R.id.code);
            title = (TextView) itemView.findViewById(R.id.title);
            date = (TextView) itemView.findViewById(R.id.date);
            local = (TextView) itemView.findViewById(R.id.local);
            layout = itemView.findViewById(R.id.layout);
            inactiveIcon = itemView.findViewById(R.id.inactive_icon);
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

        public View getInactiveIcon() {
            return inactiveIcon;
        }
    }
}
