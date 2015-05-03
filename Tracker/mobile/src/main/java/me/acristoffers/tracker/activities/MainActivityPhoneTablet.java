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

package me.acristoffers.tracker.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import me.acristoffers.tracker.AlarmReceiver;
import me.acristoffers.tracker.Package;
import me.acristoffers.tracker.R;
import me.acristoffers.tracker.TrackCodeFormattingTextWatcher;
import me.acristoffers.tracker.adapters.PackageListAdapter;

public class MainActivityPhoneTablet extends AppCompatActivity implements Package.StatusReady {
    private RecyclerView recyclerView;
    private int updating = 0;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onResume() {
        super.onResume();

        PackageListAdapter adapter = (PackageListAdapter) recyclerView.getAdapter();
        adapter.filterPackages();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        PackageListAdapter adapter = (PackageListAdapter) recyclerView.getAdapter();
        adapter.filterPackages();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_package_list);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        AlarmReceiver.setAlarm(this);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        if (recyclerView == null) {
            finish();
            System.exit(0);
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        final RecyclerView.Adapter recyclerViewAdapter = new PackageListAdapter(this);
        recyclerView.setAdapter(recyclerViewAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        if (swipeRefreshLayout == null) {
            finish();
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

        Button button = (Button) findViewById(R.id.addButton);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final View view = View.inflate(MainActivityPhoneTablet.this, R.layout.search_package, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityPhoneTablet.this);
                    builder.setTitle(R.string.search_for_package);
                    builder.setView(view);

                    EditText code = (EditText) view.findViewById(R.id.code);
                    code.addTextChangedListener(new TrackCodeFormattingTextWatcher(code));

                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            EditText code = (EditText) view.findViewById(R.id.code);
                            EditText name = (EditText) view.findViewById(R.id.name);

                            Package p = null;

                            if (code != null) {
                                Editable editable = code.getText();
                                if (editable != null) {
                                    String s = editable.toString();
                                    p = new Package(s, MainActivityPhoneTablet.this);
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

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity_phone_tablet, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.check_for_updates:
                checkForUpdates();
                return true;

            case R.id.toggle_inactive:
                PackageListAdapter recyclerViewAdapter = (PackageListAdapter) recyclerView.getAdapter();
                recyclerViewAdapter.setShowInactive(!recyclerViewAdapter.getShowInactive());

                if (recyclerViewAdapter.getShowInactive()) {
                    item.setTitle(R.string.hide_inactive);
                } else {
                    item.setTitle(R.string.show_inactive);
                }

                return true;

            case R.id.about:
                about();
                return true;

            case R.id.settings:
                Intent intent = new Intent(this, Preferences.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void checkForUpdates() {
        swipeRefreshLayout.setRefreshing(true);

        List<Package> packages = Package.allPackages(this);
        for (Package pkg : packages) {
            if (!pkg.isActive()) {
                continue;
            }

            updating++;
            pkg.setListener(this);
            pkg.checkForStatusUpdates();
        }
    }

    @SuppressLint("InflateParams")
    public void about() {
        View about = View.inflate(this, R.layout.about, null);

        PackageManager manager = this.getPackageManager();
        String packageName = getPackageName();
        int versionCode = 0;
        String versionName = "";

        try {
            PackageInfo info = manager.getPackageInfo(packageName, 0);
            versionCode = info.versionCode;
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView version = (TextView) about.findViewById(R.id.version);
        if (version != null) {
            String versionText = getString(R.string.version, versionName, versionCode);
            version.setText(versionText);
        }

        TextView issues = (TextView) about.findViewById(R.id.issues);
        if (issues != null) {
            issues.setMovementMethod(LinkMovementMethod.getInstance());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setView(about);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog aboutDialog = builder.create();
        aboutDialog.show();
    }

    @Override
    public void statusUpdated(Package pkg) {
        pkg.save();

        updating--;
        if (updating == 0) {
            swipeRefreshLayout.setRefreshing(false);

            PackageListAdapter adapter = (PackageListAdapter) recyclerView.getAdapter();
            adapter.filterPackages();
        } else {
            swipeRefreshLayout.setRefreshing(true);
        }
    }

}
