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

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import br.mg.cefet.tracker.backend.Package;

public class PackageAddActivity extends ActionBarActivity {

    public static final String EXTRA_PACKAGE_CODE = "package_code";

    private Package pkg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_add);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        String code = getIntent().getStringExtra(EXTRA_PACKAGE_CODE);
        pkg = new Package(code, this);

        if (!pkg.getName().isEmpty()) {
            setTitle(pkg.getName());
        }

        EditText name = (EditText) findViewById(R.id.name);
        if (name != null) {
            name.setText(pkg.getName());
        }

        TextView cod = (TextView) findViewById(R.id.code);
        if (cod != null) {
            cod.setText(code);
        }

        ListView listView = (ListView) findViewById(R.id.steps);
        if (listView != null) {
            listView.setAdapter(new StepsListAdapter(this, pkg));
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_package_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save: {
                EditText name = (EditText) findViewById(R.id.name);
                if (name != null) {
                    Editable editable = name.getText();
                    if (editable != null) {
                        String s = editable.toString();
                        if (s.isEmpty()) {
                            Toast toast = Toast.makeText(PackageAddActivity.this, R.string.please_type_name, Toast.LENGTH_LONG);
                            toast.show();
                            return true;
                        } else {
                            pkg.setName(s);
                        }
                    }
                }

                pkg.save();

                setResult(RESULT_OK);
                finish();

                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigateUp() {
        setResult(RESULT_CANCELED);
        finish();
        return true;
    }

}
