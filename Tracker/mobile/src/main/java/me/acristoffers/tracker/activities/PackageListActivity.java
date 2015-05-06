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
import android.text.method.LinkMovementMethod;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import me.acristoffers.tracker.Package;
import me.acristoffers.tracker.R;
import me.acristoffers.tracker.adapters.PackageListAdapter;
import me.acristoffers.tracker.fragments.BlankFragment;
import me.acristoffers.tracker.fragments.PackageDetailsFragment;
import me.acristoffers.tracker.fragments.PackageEditFragment;
import me.acristoffers.tracker.fragments.PackageListFragment;

public class PackageListActivity extends AppCompatActivity implements PackageListAdapter.OnCardViewClickedListener, ActionMode.Callback {

    private static boolean isTablet;
    private PackageListFragment packageListFragment = null;
    private Package longClickPackage = null;
    private ArrayList<Package> selection = new ArrayList<>();
    private ActionMode actionMode = null;

    public static boolean isTablet() {
        return isTablet;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_list);

        FragmentManager manager = getSupportFragmentManager();
        if (manager != null) {
            packageListFragment = (PackageListFragment) manager.findFragmentById(R.id.package_list);
        }

        updateIsTablet();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref != null) {
            boolean canRate = sharedPref.getBoolean("can_rate", true);
            if (canRate) {
                int times = sharedPref.getInt("rate_times", 0) + 1;

                if (times > 5) {
                    showRateDialog();
                }

                SharedPreferences.Editor editor = sharedPref.edit();
                if (editor != null) {
                    editor.putInt("rate_times", times);
                    editor.apply();
                }
            }
        }
    }

    private void updateIsTablet() {
        isTablet = findViewById(R.id.package_details) != null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_package_list, menu);

        return true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateIsTablet();
        selection.clear();
        PackageListAdapter.isSelecting = false;
        packageListFragment.reloadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateIsTablet();
        selection.clear();
        PackageListAdapter.isSelecting = false;
        packageListFragment.reloadData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.check_for_updates:
                packageListFragment.checkForUpdates();
                return true;

            case R.id.toggle_inactive:
                boolean showInactive = packageListFragment.toggleShowInactive();

                item.setTitle(showInactive ? R.string.hide_inactive : R.string.show_inactive);
                item.setIcon(showInactive ? R.drawable.ic_visibility_off_black_48dp : R.drawable.ic_visibility_black_48dp);

                return true;

            case R.id.about:
                about();
                return true;

            case R.id.settings:
                Intent intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("InflateParams")
    public void about() {
        View about = View.inflate(this, R.layout.about, null);

        PackageManager manager = getPackageManager();
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
    public void onCardViewClicked(me.acristoffers.tracker.Package pkg) {
        if (PackageListAdapter.isSelecting) {
            if (selection.contains(pkg)) {
                ArrayList<Package> removeList = new ArrayList<>();
                for (Package p : selection) {
                    if (p.equals(pkg)) {
                        removeList.add(p);
                    }
                }
                selection.removeAll(removeList);
            } else {
                selection.add(pkg);
            }

            if (actionMode != null) {
                Menu menu = actionMode.getMenu();
                if (menu != null) {
                    MenuItem menuItem = menu.findItem(R.id.edit);
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
                Bundle args = new Bundle();
                args.putString(PackageDetailsActivity.PACKAGE_CODE, pkg.getCod());

                PackageDetailsFragment fragment = new PackageDetailsFragment();
                fragment.setArguments(args);

                FragmentManager supportFragmentManager = getSupportFragmentManager();
                if (supportFragmentManager != null) {
                    FragmentTransaction transaction = supportFragmentManager.beginTransaction();
                    transaction.replace(R.id.package_details, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }

                packageListFragment.reloadData();
            } else {
                Intent intent = new Intent(this, PackageDetailsActivity.class);
                intent.putExtra(PackageDetailsActivity.PACKAGE_CODE, pkg.getCod());
                startActivity(intent);
            }
        }
    }

    @Override
    public void onCardViewLongClicked(me.acristoffers.tracker.Package pkg) {
        if (longClickPackage == null) {
            longClickPackage = pkg;
            selection.add(pkg);
            startActionMode(this);
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.menu_package_long_click, menu);

        this.actionMode = actionMode;

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }

        View addButton = findViewById(R.id.addButton);
        if (addButton != null) {
            addButton.setVisibility(View.INVISIBLE);
        }

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        boolean result = false;
        final ActionMode mode = actionMode;

        switch (menuItem.getItemId()) {
            case R.id.remove:
                String body = getString(R.string.confirm_delete, longClickPackage.getName());

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.are_you_sure);
                builder.setMessage(body);

                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for (Object o : selection) {
                            Package pkg = (Package) o;
                            pkg.remove();
                        }

                        packageListFragment.reloadData();

                        if (isTablet) {
                            FragmentManager supportFragmentManager = getSupportFragmentManager();
                            FragmentTransaction transaction = supportFragmentManager.beginTransaction();
                            transaction.replace(R.id.package_details, new BlankFragment());
                            transaction.commit();
                        }

                        dialogInterface.dismiss();
                        mode.finish();
                    }
                });

                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;

            case R.id.toggle_active:
                for (Package pkg : selection) {
                    pkg.setActive(!pkg.isActive());
                    pkg.save();
                }

                packageListFragment.reloadData();

                result = true;
                break;

            case R.id.edit:
                if (isTablet) {
                    Bundle args = new Bundle();
                    args.putString(PackageDetailsActivity.PACKAGE_CODE, selection.get(0).getCod());

                    PackageEditFragment fragment = new PackageEditFragment();
                    fragment.setArguments(args);

                    FragmentManager supportFragmentManager = getSupportFragmentManager();
                    if (supportFragmentManager != null) {
                        FragmentTransaction transaction = supportFragmentManager.beginTransaction();
                        transaction.replace(R.id.package_details, fragment);
                        transaction.commit();
                    }
                } else {
                    Intent intent = new Intent(this, PackageEditActivity.class);
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
    public void onDestroyActionMode(ActionMode actionMode) {
        longClickPackage = null;

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.show();
        }

        View addButton = findViewById(R.id.addButton);
        if (addButton != null) {
            addButton.setVisibility(View.VISIBLE);
        }

        PackageListAdapter.isSelecting = false;
        selection.clear();
        this.actionMode = null;

        packageListFragment.reloadData();
    }

    private void showRateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setMessage(R.string.rate_me);

        builder.setPositiveButton(R.string.rate_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(PackageListActivity.this);
                if (sharedPref != null) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    if (editor != null) {
                        editor.putBoolean("can_rate", false);
                        editor.apply();
                    }
                }

                /*
                 * This seems like non-sense, as we know the name of the package
                 * but makes our life easier if we want to change it later
                 * as it's only necessary to change two places: the manifest and gradle build file
                 * (and to copy&paste code into other projects ;)
                 */
                String packageName = getPackageName();
                Uri uri = Uri.parse("market://details?id=" + packageName);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));

                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton(R.string.dont_rate, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(PackageListActivity.this);
                if (sharedPref != null) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    if (editor != null) {
                        editor.putBoolean("can_rate", false);
                        editor.apply();
                    }
                }

                dialogInterface.dismiss();
            }
        });

        builder.setNeutralButton(R.string.later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(PackageListActivity.this);
                if (sharedPref != null) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    if (editor != null) {
                        editor.putInt("rate_times", 0);
                        editor.apply();
                    }
                }

                dialogInterface.dismiss();
            }
        });

        AlertDialog aboutDialog = builder.create();
        aboutDialog.show();
    }

}
