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
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import me.acristoffers.tracker.R;
import me.acristoffers.tracker.adapters.PackageListAdapter;
import me.acristoffers.tracker.fragments.PackageDetailsFragment;
import me.acristoffers.tracker.fragments.PackageListFragment;

public class PackageListActivity extends AppCompatActivity implements PackageListAdapter.OnCardViewClickedListener {

    public static boolean isTablet;
    private PackageListFragment packageListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_list);

        FragmentManager supportFragmentManager = getSupportFragmentManager();
        packageListFragment = (PackageListFragment) supportFragmentManager.findFragmentById(R.id.package_list);

        updateIsTablet();
    }

    private void updateIsTablet() {
        isTablet = findViewById(R.id.package_details) != null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity_phone_tablet, menu);
        return true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateIsTablet();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateIsTablet();
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

                if (showInactive) {
                    item.setTitle(R.string.hide_inactive);
                } else {
                    item.setTitle(R.string.show_inactive);
                }

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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateIsTablet();
    }

    @Override
    public void onCardViewClicked(me.acristoffers.tracker.Package pkg) {
        if (isTablet) {
            Bundle args = new Bundle();
            args.putString(PackageDetailsActivity.PACKAGE_CODE, pkg.getCod());

            PackageDetailsFragment fragment = new PackageDetailsFragment();
            fragment.setArguments(args);

            FragmentManager supportFragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = supportFragmentManager.beginTransaction();
            transaction.replace(R.id.package_details, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            Intent intent = new Intent(this, PackageDetailsActivity.class);
            intent.putExtra(PackageDetailsActivity.PACKAGE_CODE, pkg.getCod());
            startActivity(intent);
        }
    }
}
