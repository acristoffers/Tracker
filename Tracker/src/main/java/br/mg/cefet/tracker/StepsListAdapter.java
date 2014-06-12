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

package br.mg.cefet.tracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import br.mg.cefet.tracker.backend.Correios;
import br.mg.cefet.tracker.backend.Package;

public class StepsListAdapter extends BaseAdapter {

    private List<Correios.Step> steps;
    private LayoutInflater layoutInflater;

    public StepsListAdapter(Context context, Package pkg) {
        steps = pkg.getSteps();
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return steps.size();
    }

    @Override
    public Object getItem(int i) {
        return steps.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.listview_item_steps, null);
        }

        Correios.Step step = steps.get(i);

        TextView title = (TextView) view.findViewById(R.id.title);
        if (title != null) {
            title.setText(step.title);
        }

        TextView date = (TextView) view.findViewById(R.id.date);
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat();
            String dateS = sdf.format(step.date);
            date.setText(dateS);
        }

        TextView local = (TextView) view.findViewById(R.id.local);
        if (local != null) {
            local.setText(step.local);
        }

        TextView description = (TextView) view.findViewById(R.id.description);
        if (description != null) {
            description.setText(step.description);
        }

        return view;
    }

}
