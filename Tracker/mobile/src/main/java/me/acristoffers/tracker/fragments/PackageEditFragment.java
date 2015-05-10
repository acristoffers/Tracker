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

package me.acristoffers.tracker.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import me.acristoffers.tracker.Package;
import me.acristoffers.tracker.R;
import me.acristoffers.tracker.activities.PackageDetailsActivity;
import me.acristoffers.tracker.activities.PackageListActivity;

public class PackageEditFragment extends Fragment {

    private Package pkg = null;
    private View view = null;

    @Override
    public void onDestroy() {
        super.onDestroy();

        pkg = null;
        view = null;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupUI();
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        setupUI();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        setupUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupUI();
    }

    private void setupUI() {
        final Activity activity = getActivity();
        view = getView();

        if (activity == null || view == null) {
            return;
        }

        String code = null;

        final Bundle arguments = getArguments();
        if (arguments != null) {
            code = arguments.getString(PackageDetailsActivity.PACKAGE_CODE);
        }

        if (code == null || code.isEmpty()) {
            final Intent intent = activity.getIntent();
            if (intent != null) {
                code = intent.getStringExtra(PackageDetailsActivity.PACKAGE_CODE);
            }
        }

        if (code == null || code.isEmpty()) {
            return;
        }

        pkg = new Package(code, activity, null);

        final EditText editText = (EditText) view.findViewById(R.id.name);
        if (editText != null) {
            editText.setText(pkg.getName());
        }

        final SwitchCompat switchCompat = (SwitchCompat) view.findViewById(R.id.active);
        if (switchCompat != null) {
            switchCompat.setChecked(pkg.isActive());
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return getActivity().getLayoutInflater().inflate(R.layout.fragment_package_edit, container, false);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_package_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();

        if (id == R.id.save) {
            String name = "";
            boolean active = true;

            final EditText editText = (EditText) view.findViewById(R.id.name);
            if (editText != null) {
                final Editable editable = editText.getText();
                if (editable != null) {
                    name = editable.toString();
                }
            }

            final SwitchCompat switchCompat = (SwitchCompat) view.findViewById(R.id.active);
            if (switchCompat != null) {
                active = switchCompat.isChecked();
            }

            pkg.setName(name);
            pkg.setActive(active);
            pkg.save();

            if (PackageListActivity.isTablet()) {
                final PackageListActivity listActivity = (PackageListActivity) getActivity();
                listActivity.onCardViewClicked(pkg);
            } else {
                getActivity().finish();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
