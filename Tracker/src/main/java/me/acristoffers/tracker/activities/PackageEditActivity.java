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

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import br.mg.cefet.tracker.R;
import me.acristoffers.tracker.adapters.StepsListAdapter;
import me.acristoffers.tracker.backend.Package;

public class PackageEditActivity extends Activity {

    public static final String EXTRA_PACKAGE_CODE = "package_code";

    private Package pkg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (PackageListActivity.isTablet) {
            setTheme(android.R.style.Theme_Dialog);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_edit);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        String code;

        Intent intent = getIntent();
        if (intent != null) {
            code = intent.getStringExtra(EXTRA_PACKAGE_CODE);
        } else if (savedInstanceState != null) {
            code = savedInstanceState.getString(EXTRA_PACKAGE_CODE);
        } else {
            finish();
            return;
        }

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

        CheckBox active = (CheckBox) findViewById(R.id.active);
        if (active != null) {
            active.setChecked(pkg.isActive());
        }

        ListView listView = (ListView) findViewById(R.id.steps);
        if (listView != null) {
            View view = findViewById(R.id.emptyStepView);
            listView.setEmptyView(view);
            listView.setAdapter(new StepsListAdapter(this, pkg));
        }

        if (!PackageListActivity.isTablet) {
            View view = findViewById(R.id.button_bar);
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        } else {
            Button cancel = (Button) findViewById(R.id.cancel);
            if (cancel != null) {
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
            }

            Button save = (Button) findViewById(R.id.save);
            if (save != null) {
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText name = (EditText) findViewById(R.id.name);
                        if (name != null) {
                            Editable editable = name.getText();
                            if (editable != null) {
                                String s = editable.toString();
                                if (s.isEmpty()) {
                                    Toast toast = Toast.makeText(PackageEditActivity.this, R.string.please_type_name, Toast.LENGTH_LONG);
                                    toast.show();
                                    return;
                                } else {
                                    pkg.setName(s);
                                }
                            }
                        }

                        CheckBox active = (CheckBox) findViewById(R.id.active);
                        if (active != null) {
                            pkg.setActive(active.isChecked());
                        }

                        pkg.save();

                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.slide_out_right);
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
                            Toast toast = Toast.makeText(PackageEditActivity.this, R.string.please_type_name, Toast.LENGTH_LONG);
                            toast.show();
                            return true;
                        } else {
                            pkg.setName(s);
                        }
                    }
                }

                CheckBox active = (CheckBox) findViewById(R.id.active);
                if (active != null) {
                    pkg.setActive(active.isChecked());
                }

                pkg.save();

                setResult(RESULT_OK);
                finish();
                overridePendingTransition(R.anim.abc_fade_in, R.anim.slide_out_right);

                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigateUp() {
        setResult(RESULT_CANCELED);
        finish();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.slide_out_right);
        return true;
    }

}
