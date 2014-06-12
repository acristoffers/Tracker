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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import br.mg.cefet.tracker.backend.Package;

public class MainActivity extends Activity implements Package.StatusReady {

    private static final int REQUEST_SAVE_PACKAGE = 0;
    private EditText searchPackage = null;
    private Package searchingForPackage = null;
    private AlertDialog dialog = null;
    private PackageListAdapter packageListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        packageListAdapter = new PackageListAdapter(this);

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
            listView.setAdapter(packageListAdapter);
        }
    }

    private void searchForPackage() {
        if (searchingForPackage == null) {
            String cod = getSearchPackageText();

            if (cod.length() != 13) {
                Toast toast = Toast.makeText(this, R.string.invalid_tracking_number, Toast.LENGTH_LONG);
                toast.show();
                return;
            }

            searchingForPackage = new Package(cod, this);
            searchingForPackage.setListener(this);
            searchingForPackage.checkForStatusUpdates();

            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_searching, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(view);

            dialog = builder.create();
            dialog.show();
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
    protected void onRestart() {
        packageListAdapter.updatePackageList();
        super.onRestart();
    }

    @Override
    protected void onResume() {
        packageListAdapter.updatePackageList();
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            searchingForPackage.remove();
        }

        searchingForPackage = null;

        packageListAdapter.updatePackageList();
    }

    @Override
    public void statusUpdated(Package pkg) {
        dialog.dismiss();

        dialog = null;

        if (pkg.getSteps().isEmpty()) {
            searchingForPackage = null;

            Toast toast = Toast.makeText(this, R.string.package_not_found, Toast.LENGTH_LONG);
            toast.show();
        } else {
            searchPackage.setText("");

            pkg.save();

            Intent intent = new Intent(this, PackageAddActivity.class);
            intent.putExtra(PackageAddActivity.EXTRA_PACKAGE_CODE, pkg.getCod());
            startActivityForResult(intent, REQUEST_SAVE_PACKAGE);
        }
    }

}
