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
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.method.LinkMovementMethod;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import me.acristoffers.tracker.BackupAgent;
import me.acristoffers.tracker.Package;
import me.acristoffers.tracker.R;
import me.acristoffers.tracker.adapters.PackageListAdapter;
import me.acristoffers.tracker.fragments.BlankFragment;
import me.acristoffers.tracker.fragments.PackageDetailsFragment;
import me.acristoffers.tracker.fragments.PackageEditFragment;
import me.acristoffers.tracker.fragments.PackageListFragment;

public class PackageListActivity extends AppCompatActivity implements PackageListAdapter.OnCardViewClickedListener, ActionMode.Callback {

    private static final ArrayList<Package> selection = new ArrayList<>();
    private static boolean isTablet;
    private PackageListFragment packageListFragment = null;
    private Package longClickPackage = null;
    private ActionMode actionMode = null;

    public static boolean isTablet() {
        return isTablet;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_list);

        final FragmentManager manager = getSupportFragmentManager();
        if (manager != null) {
            packageListFragment = (PackageListFragment) manager.findFragmentById(R.id.package_list);
        }

        updateIsTablet();

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref != null) {
            int versionCode = 0;

            try {
                PackageManager packageManager = getPackageManager();
                String packageName = getPackageName();
                PackageInfo info = packageManager.getPackageInfo(packageName, 0);
                versionCode = info.versionCode;
            } catch (Exception e) {
                e.printStackTrace();
            }

            final boolean canRate = sharedPref.getBoolean("can_rate", true);
            final boolean didRate = sharedPref.getInt("did_rate", 0) > 0;
            final boolean appUpdated = sharedPref.getInt("do_not_rate", 0) < versionCode;
            final int times = sharedPref.getInt("rate_times", 0) + 1;

            final SharedPreferences.Editor editor = sharedPref.edit();
            if (editor != null) {
                editor.putInt("rate_times", times);
                editor.apply();
            }

            if (!didRate && (times > 5) && (canRate || appUpdated)) {
                showRateDialog();
            }
        }

        BackupAgent.restoreIfNotBackingUp(this);
    }

    private void updateIsTablet() {
        isTablet = findViewById(R.id.package_details) != null;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_package_list, menu);

        MenuItem item = menu.findItem(R.id.toggle_inactive);

        final boolean showInactive = packageListFragment.isShowInactive();

        item.setTitle(showInactive ? R.string.hide_inactive : R.string.show_inactive);
        item.setIcon(showInactive ? R.drawable.ic_visibility_off_black_48dp : R.drawable.ic_visibility_black_48dp);

        item = menu.findItem(R.id.search);
        if (item != null) {
            final SearchView searchView = (SearchView) item.getActionView();
            if (searchView != null) {
                searchView.setQueryHint(getResources().getString(R.string.filter));

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(final String query) {
                        filter(query);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(final String newText) {
                        // Workaround this:
                        // http://stackoverflow.com/questions/9327826/searchviews-oncloselistener-doesnt-work/12975254#12975254
                        if (newText.isEmpty()) {
                            filter(null);
                        } else {
                            filter(newText);
                        }

                        return true;
                    }
                });

                searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                    @Override
                    public boolean onClose() {
                        filter(null);
                        return true;
                    }
                });
            }
        }

        return true;
    }

    private void filter(final String query) {
        final View view = packageListFragment.getView();
        if (view != null) {
            final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
            if (recyclerView != null) {
                final PackageListAdapter adapter = (PackageListAdapter) recyclerView.getAdapter();
                if (adapter != null) {
                    adapter.filterPackages(query);
                }
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateIsTablet();
        packageListFragment.reloadData();

        if (PackageListAdapter.isSelecting) {
            startActionMode(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateIsTablet();
        packageListFragment.reloadData();

        if (PackageListAdapter.isSelecting) {
            startActionMode(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case R.id.check_for_updates:
                packageListFragment.checkForUpdates();
                return true;

            case R.id.toggle_inactive:
                final boolean showInactive = packageListFragment.toggleShowInactive();

                item.setTitle(showInactive ? R.string.hide_inactive : R.string.show_inactive);
                item.setIcon(showInactive ? R.drawable.ic_visibility_off_black_48dp : R.drawable.ic_visibility_black_48dp);

                return true;

            case R.id.about:
                about();
                return true;

            case R.id.settings:
                final Intent intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("InflateParams")
    private void about() {
        final View about = View.inflate(this, R.layout.about, null);

        final PackageManager manager = getPackageManager();
        final String packageName = getPackageName();
        int versionCode = 0;
        String versionName = "";

        try {
            PackageInfo info = manager.getPackageInfo(packageName, 0);
            versionCode = info.versionCode;
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        final TextView version = (TextView) about.findViewById(R.id.version);
        if (version != null) {
            final String versionText = getString(R.string.version, versionName, versionCode);
            version.setText(versionText);
        }

        final TextView issues = (TextView) about.findViewById(R.id.issues);
        if (issues != null) {
            issues.setMovementMethod(LinkMovementMethod.getInstance());
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setView(about);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setNeutralButton(R.string.rate_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String packageName = getPackageName();
                final Uri uri = Uri.parse("market://details?id=" + packageName);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                dialogInterface.dismiss();
            }
        });

        final AlertDialog aboutDialog = builder.create();
        aboutDialog.show();
    }

    @Override
    public void onCardViewClicked(final me.acristoffers.tracker.Package pkg) {
        if (PackageListAdapter.isSelecting) {
            if (selection.contains(pkg)) {
                final ArrayList<Package> removeList = new ArrayList<>();
                for (final Package p : selection) {
                    if (p.equals(pkg)) {
                        removeList.add(p);
                    }
                }
                selection.removeAll(removeList);
            } else {
                selection.add(pkg);
            }

            if (actionMode != null) {
                final Menu menu = actionMode.getMenu();
                if (menu != null) {
                    final MenuItem menuItem = menu.findItem(R.id.edit);
                    if (menuItem != null) {
                        menuItem.setVisible(selection.size() == 1);
                    }
                }

                if (selection.isEmpty()) {
                    actionMode.finish();
                }
            }
        } else {
            if (isTablet) {
                final Bundle args = new Bundle();
                args.putString(PackageDetailsActivity.PACKAGE_CODE, pkg.getCod());

                final PackageDetailsFragment fragment = new PackageDetailsFragment();
                fragment.setArguments(args);

                final FragmentManager supportFragmentManager = getSupportFragmentManager();
                if (supportFragmentManager != null) {
                    final FragmentTransaction transaction = supportFragmentManager.beginTransaction();
                    transaction.replace(R.id.package_details, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }

                packageListFragment.reloadData();
            } else {
                final Intent intent = new Intent(this, PackageDetailsActivity.class);
                intent.putExtra(PackageDetailsActivity.PACKAGE_CODE, pkg.getCod());
                startActivity(intent);
            }
        }
    }

    @Override
    public void onCardViewLongClicked(final me.acristoffers.tracker.Package pkg) {
        if (longClickPackage == null) {
            longClickPackage = pkg;
            selection.add(pkg);
            startActionMode(this);
        }
    }

    @Override
    public boolean onCreateActionMode(final ActionMode actionMode, final Menu menu) {
        final MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.menu_package_long_click, menu);

        this.actionMode = actionMode;

        final ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }

        final View addButton = findViewById(R.id.addButton);
        if (addButton != null) {
            addButton.setVisibility(View.INVISIBLE);
        }

        return true;
    }

    @Override
    public boolean onPrepareActionMode(final ActionMode actionMode, final Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode actionMode, final MenuItem menuItem) {
        boolean result = false;

        switch (menuItem.getItemId()) {
            case R.id.remove:
                final String body = getString(R.string.confirm_delete, longClickPackage.getName());

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.are_you_sure);
                builder.setMessage(body);

                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for (final Object o : selection) {
                            final Package pkg = (Package) o;
                            pkg.remove();
                        }

                        packageListFragment.reloadData();

                        if (isTablet) {
                            final FragmentManager supportFragmentManager = getSupportFragmentManager();
                            final FragmentTransaction transaction = supportFragmentManager.beginTransaction();
                            transaction.replace(R.id.package_details, new BlankFragment());
                            transaction.commit();
                        }

                        dialogInterface.dismiss();
                        actionMode.finish();
                    }
                });

                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                final AlertDialog dialog = builder.create();
                dialog.show();

                return true;

            case R.id.set_active:
                for (final Package pkg : selection) {
                    pkg.setActive(true);
                    pkg.save();
                }

                packageListFragment.reloadData();

                result = true;
                break;

            case R.id.set_inactive:
                for (final Package pkg : selection) {
                    pkg.setActive(false);
                    pkg.save();
                }

                packageListFragment.reloadData();

                result = true;
                break;

            case R.id.edit:
                if (isTablet) {
                    final Bundle args = new Bundle();
                    args.putString(PackageDetailsActivity.PACKAGE_CODE, selection.get(0).getCod());

                    final PackageEditFragment fragment = new PackageEditFragment();
                    fragment.setArguments(args);

                    final FragmentManager supportFragmentManager = getSupportFragmentManager();
                    if (supportFragmentManager != null) {
                        final FragmentTransaction transaction = supportFragmentManager.beginTransaction();
                        transaction.replace(R.id.package_details, fragment);
                        transaction.commit();
                    }
                } else {
                    final Intent intent = new Intent(this, PackageEditActivity.class);
                    intent.putExtra(PackageDetailsActivity.PACKAGE_CODE, selection.get(0).getCod());
                    startActivity(intent);
                }

                result = true;
                break;
        }

        actionMode.finish();

        return result;
    }

    @Override
    public void onDestroyActionMode(final ActionMode actionMode) {
        longClickPackage = null;

        final ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.show();
        }

        final View addButton = findViewById(R.id.addButton);
        if (addButton != null) {
            addButton.setVisibility(View.VISIBLE);
        }

        PackageListAdapter.isSelecting = false;
        selection.clear();
        this.actionMode = null;

        packageListFragment.reloadData();
    }

    private void showRateDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setMessage(R.string.rate_me);

        builder.setPositiveButton(R.string.rate_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(PackageListActivity.this);
                if (sharedPref != null) {
                    final SharedPreferences.Editor editor = sharedPref.edit();
                    if (editor != null) {
                        editor.putBoolean("can_rate", false);

                        try {
                            final PackageManager manager = getPackageManager();
                            final String packageName = getPackageName();
                            final PackageInfo info = manager.getPackageInfo(packageName, 0);
                            editor.putInt("did_rate", info.versionCode);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        editor.apply();
                    }
                }

                /*
                 * This seems like non-sense, as we know the name of the package
                 * but makes our life easier if we want to change it later
                 * as it's only necessary to change two places: the manifest and gradle build file
                 * (and to copy&paste code into other projects ;)
                 */
                final String packageName = getPackageName();
                final Uri uri = Uri.parse("market://details?id=" + packageName);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));

                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton(R.string.dont_rate, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(PackageListActivity.this);
                if (sharedPref != null) {
                    final SharedPreferences.Editor editor = sharedPref.edit();
                    if (editor != null) {
                        editor.putBoolean("can_rate", false);

                        try {
                            final PackageManager manager = getPackageManager();
                            final String packageName = getPackageName();
                            final PackageInfo info = manager.getPackageInfo(packageName, 0);
                            editor.putInt("do_not_rate", info.versionCode);
                            editor.putInt("rate_times", 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        editor.apply();
                    }
                }

                dialogInterface.dismiss();
            }
        });

        builder.setNeutralButton(R.string.later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(PackageListActivity.this);
                if (sharedPref != null) {
                    final SharedPreferences.Editor editor = sharedPref.edit();
                    if (editor != null) {
                        editor.putInt("rate_times", 0);
                        editor.apply();
                    }
                }

                dialogInterface.dismiss();
            }
        });

        final AlertDialog aboutDialog = builder.create();
        aboutDialog.show();
    }

}
