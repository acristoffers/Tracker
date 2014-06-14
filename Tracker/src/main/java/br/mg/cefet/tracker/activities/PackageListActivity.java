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

package br.mg.cefet.tracker.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import br.mg.cefet.tracker.AlarmReceiver;
import br.mg.cefet.tracker.R;
import br.mg.cefet.tracker.adapters.PackageListAdapter;
import br.mg.cefet.tracker.backend.Package;
import br.mg.cefet.tracker.fragments.PackageViewFragment;

public class PackageListActivity extends FragmentActivity implements Package.StatusReady {

    private static final int REQUEST_SAVE_PACKAGE = 0;
    public static boolean isTablet = false;
    private static boolean showingInactive = false;
    private EditText searchPackage = null;
    private Package searchingForPackage = null;
    private AlertDialog dialog = null;
    private int updating = 0;
    private PackageListAdapter packageListAdapter;

    @Override
    protected void onRestart() {
        super.onRestart();
        packageListAdapter.updatePackageList();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.slide_out_right);

        View view = findViewById(R.id.container);
        if (view != null) {
            view.requestFocus();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.toggle_inactive);
        if (showingInactive) {
            item.setTitle(R.string.hide_inactive);
        } else {
            item.setTitle(R.string.show_inactive);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toggle_inactive: {
                showingInactive = !showingInactive;

                ListView listView = (ListView) findViewById(R.id.packages);
                if (listView != null) {
                    if (showingInactive) {
                        packageListAdapter = new PackageListAdapter(this, true);
                        item.setTitle(R.string.hide_inactive);
                    } else {
                        packageListAdapter = new PackageListAdapter(this, false);
                        item.setTitle(R.string.show_inactive);
                    }

                    listView.setAdapter(packageListAdapter);
                    packageListAdapter.updatePackageList();

                    return true;
                }
            }

            case R.id.check_for_updates: {
                List<String> codes = Package.getCodes(this);
                updating = codes.size();

                for (String code : codes) {
                    Package pkg = new Package(code, this);
                    pkg.setListener(this);
                    pkg.checkForStatusUpdates();
                }

                Toast toast = Toast.makeText(this, R.string.checking_for_updates, Toast.LENGTH_LONG);
                toast.show();

                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED && searchingForPackage.getName().isEmpty()) {
            searchingForPackage.remove();
        }

        searchingForPackage = null;

        packageListAdapter.updatePackageList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_list_root);

        isTablet = (findViewById(R.id.view_fragment) != null);

        packageListAdapter = new PackageListAdapter(this, showingInactive);

        searchPackage = (EditText) findViewById(R.id.search_package);
        if (searchPackage != null) {
            searchPackage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        searchForPackage();
                        return true;
                    }

                    return false;
                }
            });
        }

        ListView listView = (ListView) findViewById(R.id.packages);
        if (listView != null) {
            View view = findViewById(R.id.emptyView);

            listView.setEmptyView(view);
            listView.setAdapter(packageListAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Package pkg = (Package) packageListAdapter.getItem(i);

                    if (isTablet) {
                        Bundle bundle = new Bundle();
                        bundle.putString(PackageEditActivity.EXTRA_PACKAGE_CODE, pkg.getCod());

                        PackageViewFragment viewFragment = new PackageViewFragment();
                        viewFragment.setArguments(bundle);

                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.view_fragment, viewFragment, "viewFragment")
                                .commit();
                    } else {
                        Intent intent = new Intent(PackageListActivity.this, PackageViewActivity.class);
                        intent.putExtra(PackageEditActivity.EXTRA_PACKAGE_CODE, pkg.getCod());

                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.abc_fade_out);
                    }
                }
            });
        }

        AlarmReceiver.setAlarm(this);
    }

    @SuppressLint("InflateParams")
    private void searchForPackage() {
        if (searchingForPackage == null) {
            String cod = getSearchPackageText();

            if (cod.length() != 13) {
                Toast toast = Toast.makeText(this, R.string.invalid_tracking_number, Toast.LENGTH_LONG);
                toast.show();
                return;
            }

            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_searching, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(view);

            dialog = builder.create();
            dialog.show();

            searchingForPackage = new Package(cod, this);
            searchingForPackage.setListener(this);
            searchingForPackage.checkForStatusUpdates();
        }
    }

    private String getSearchPackageText() {
        String text = "";

        if (searchPackage == null) {
            searchPackage = (EditText) findViewById(R.id.search_package);
        }

        if (searchPackage != null) {
            Editable editable = searchPackage.getText();
            if (editable != null) {
                text = editable.toString();
            }
        }

        return text;
    }

    @Override
    protected void onResume() {
        super.onResume();
        packageListAdapter.updatePackageList();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.slide_out_right);

        View view = findViewById(R.id.container);
        if (view != null) {
            view.requestFocus();
        }
    }

    @Override
    public void statusUpdated(Package pkg) {
        if (dialog != null) {
            dialog.dismiss();

            dialog = null;

            if (pkg.getSteps().isEmpty()) {
                searchingForPackage = null;

                Toast toast = Toast.makeText(this, R.string.package_not_found, Toast.LENGTH_LONG);
                toast.show();
            } else {
                searchPackage.setText("");

                pkg.save();

                Intent intent = new Intent(this, PackageEditActivity.class);
                intent.putExtra(PackageEditActivity.EXTRA_PACKAGE_CODE, pkg.getCod());
                startActivityForResult(intent, REQUEST_SAVE_PACKAGE);
                overridePendingTransition(R.anim.slide_in_right, R.anim.abc_fade_out);
            }
        } else {
            pkg.save();
            updating--;
            if (updating == 0) {
                Toast toast = Toast.makeText(this, R.string.finished_checking_for_updates, Toast.LENGTH_LONG);
                toast.show();
                packageListAdapter.updatePackageList();
            }
        }
    }

    public void reloadAndSelectNone() {
        packageListAdapter.updatePackageList();
        getSupportFragmentManager()
                .beginTransaction()
                .remove(getSupportFragmentManager().findFragmentByTag("viewFragment"))
                .commit();
    }

}
