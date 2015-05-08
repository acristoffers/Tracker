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
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

import me.acristoffers.tracker.AlarmReceiver;
import me.acristoffers.tracker.BackupAgent;
import me.acristoffers.tracker.Package;
import me.acristoffers.tracker.R;
import me.acristoffers.tracker.TrackCodeFormattingTextWatcher;
import me.acristoffers.tracker.activities.PackageListActivity;
import me.acristoffers.tracker.adapters.PackageListAdapter;

public class PackageListFragment extends Fragment implements Package.StatusReady {
    private RecyclerView recyclerView = null;
    private int updating = 0;
    private SwipeRefreshLayout swipeRefreshLayout = null;
    private Activity activity = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return getActivity().getLayoutInflater().inflate(R.layout.fragment_package_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();
        View view = getView();

        if (view == null || activity == null) {
            return;
        }

        PreferenceManager.setDefaultValues(activity, R.xml.preferences, false);

        AlarmReceiver.setAlarm(activity);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        if (recyclerView == null) {
            activity.finish();
            System.exit(0);
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);

        final PackageListAdapter recyclerViewAdapter = new PackageListAdapter(activity);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setListener((PackageListActivity) activity);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        if (swipeRefreshLayout == null) {
            activity.finish();
            System.exit(0);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (updating == 0) {
                    checkForUpdates();
                }
            }
        });

        Button button = (Button) view.findViewById(R.id.addButton);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final View view = View.inflate(activity, R.layout.search_package, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(R.string.search_for_package);
                    builder.setView(view);

                    EditText code = (EditText) view.findViewById(R.id.code);
                    if (code != null) {
                        code.addTextChangedListener(new TrackCodeFormattingTextWatcher(code));
                    }

                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            EditText code = (EditText) view.findViewById(R.id.code);
                            EditText name = (EditText) view.findViewById(R.id.name);

                            me.acristoffers.tracker.Package p = null;

                            if (code != null) {
                                Editable editable = code.getText();
                                if (editable != null) {
                                    String s = editable.toString();
                                    p = new Package(s, activity);
                                }
                            }

                            if (name != null && p != null) {
                                Editable editable = name.getText();
                                if (editable != null) {
                                    String s = editable.toString();
                                    p.setName(s);
                                    p.setActive(true);
                                    p.save();
                                    checkForUpdates();
                                }
                            }

                            dialogInterface.dismiss();
                        }
                    });

                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        PackageListAdapter adapter = (PackageListAdapter) recyclerView.getAdapter();
        adapter.filterPackages();
    }

    @Override
    public void onResume() {
        super.onResume();

        PackageListAdapter adapter = (PackageListAdapter) recyclerView.getAdapter();
        adapter.filterPackages();
    }

    public void checkForUpdates() {
        BackupAgent.restoreIfNotBackingUp(getActivity());

        ArrayList<Package> packages = Package.allPackages(getActivity());
        for (Package pkg : packages) {
            if (!pkg.isActive()) {
                continue;
            }

            updating++;
            pkg.setListener(this);
            pkg.checkForStatusUpdates();
        }

        swipeRefreshLayout.setRefreshing(updating > 0);
    }

    @Override
    public void statusUpdated(Package pkg) {
        pkg.save();

        updating--;

        PackageListAdapter adapter = (PackageListAdapter) recyclerView.getAdapter();
        adapter.filterPackages();

        swipeRefreshLayout.setRefreshing(updating > 0);
    }

    public boolean toggleShowInactive() {
        PackageListAdapter recyclerViewAdapter = (PackageListAdapter) recyclerView.getAdapter();
        recyclerViewAdapter.setShowInactive(!recyclerViewAdapter.getShowInactive());

        return recyclerViewAdapter.getShowInactive();
    }

    public void reloadData() {
        PackageListAdapter recyclerViewAdapter = (PackageListAdapter) recyclerView.getAdapter();
        recyclerViewAdapter.filterPackages();
    }
}
