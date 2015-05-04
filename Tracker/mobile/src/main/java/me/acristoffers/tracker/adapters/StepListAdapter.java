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
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import me.acristoffers.tracker.Correios;
import me.acristoffers.tracker.Package;
import me.acristoffers.tracker.R;

public class StepListAdapter extends RecyclerView.Adapter {

    private Context context = null;
    private Package pkg = null;
    private ArrayList<Correios.Step> steps = new ArrayList<>();
    private LayoutInflater layoutInflater = null;

    public StepListAdapter(Package pkg, Context context) {
        this.pkg = pkg;
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        updateSteps();
    }

    // Create new views
    @Override
    @SuppressLint("InflateParams")
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.steps_item, null);
        return new ViewHolder(view);
    }

    // Recycle views
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;

        final Correios.Step step = steps.get(steps.size() - position - 1);

        String title;
        String date = "";
        String local = "";

        DateFormat sdf = SimpleDateFormat.getDateTimeInstance();
        title = step.title;
        local = step.local;
        date = sdf.format(step.date);

        TextView textView = viewHolder.getTitle();
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
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    public void updateSteps() {
        pkg = new Package(pkg.getCod(), context);
        steps = pkg.getSteps();
        notifyDataSetChanged();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title, date, local;

        public ViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.title);
            date = (TextView) itemView.findViewById(R.id.date);
            local = (TextView) itemView.findViewById(R.id.local);
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
    }
}
