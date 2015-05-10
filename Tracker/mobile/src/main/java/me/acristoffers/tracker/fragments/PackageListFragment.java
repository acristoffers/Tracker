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
    private SwipeRefreshLayout swipeRefreshLayout = null;
    private int updating = 0;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return getActivity().getLayoutInflater().inflate(R.layout.fragment_package_list, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Activity activity = getActivity();
        final View view = getView();

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

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);

        final PackageListAdapter recyclerViewAdapter = new PackageListAdapter(activity, (PackageListActivity) activity);
        recyclerView.setAdapter(recyclerViewAdapter);

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

        final Button button = (Button) view.findViewById(R.id.addButton);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final View view = View.inflate(activity, R.layout.search_package, null);

                    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(R.string.search_for_package);
                    builder.setView(view);

                    final EditText code = (EditText) view.findViewById(R.id.code);
                    if (code != null) {
                        code.addTextChangedListener(new TrackCodeFormattingTextWatcher(code));
                    }

                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final EditText code = (EditText) view.findViewById(R.id.code);
                            final EditText name = (EditText) view.findViewById(R.id.name);

                            Package p = null;

                            if (code != null) {
                                Editable editable = code.getText();
                                if (editable != null) {
                                    String s = editable.toString();
                                    p = new Package(s, getActivity(), null);
                                }
                            }

                            if (name != null && p != null) {
                                final Editable editable = name.getText();
                                if (editable != null) {
                                    final String s = editable.toString();
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

                    final AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        final PackageListAdapter adapter = (PackageListAdapter) recyclerView.getAdapter();
        adapter.filterPackages();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        recyclerView = null;
        swipeRefreshLayout = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        final PackageListAdapter adapter = (PackageListAdapter) recyclerView.getAdapter();
        adapter.filterPackages();
    }

    public void checkForUpdates() {
        BackupAgent.restoreIfNotBackingUp(getActivity());

        final ArrayList<Package> packages = Package.allPackages(getActivity());
        for (final Package pkg : packages) {
            if (!pkg.isActive()) {
                continue;
            }

            updating++;
            new Package(pkg.getCod(), getActivity(), this).checkForStatusUpdates();
        }

        swipeRefreshLayout.setRefreshing(updating > 0);
    }

    @Override
    public void statusUpdated(final Package pkg) {
        pkg.save();

        updating--;

        final PackageListAdapter adapter = (PackageListAdapter) recyclerView.getAdapter();
        adapter.filterPackages();

        swipeRefreshLayout.setRefreshing(updating > 0);
    }

    public boolean isShowInactive() {
        final PackageListAdapter recyclerViewAdapter = (PackageListAdapter) recyclerView.getAdapter();
        return recyclerViewAdapter.getShowInactive();
    }

    public boolean toggleShowInactive() {
        final PackageListAdapter recyclerViewAdapter = (PackageListAdapter) recyclerView.getAdapter();
        recyclerViewAdapter.setShowInactive(!recyclerViewAdapter.getShowInactive());

        return recyclerViewAdapter.getShowInactive();
    }

    public void reloadData() {
        final PackageListAdapter recyclerViewAdapter = (PackageListAdapter) recyclerView.getAdapter();
        recyclerViewAdapter.filterPackages();
    }
}
