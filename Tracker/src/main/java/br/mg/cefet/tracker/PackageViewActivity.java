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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import br.mg.cefet.tracker.backend.Package;

public class PackageViewActivity extends ActionBarActivity {

    private Package pkg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_view);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        String code = getIntent().getStringExtra(PackageAddActivity.EXTRA_PACKAGE_CODE);
        pkg = new br.mg.cefet.tracker.backend.Package(code, this);

        if (!pkg.getName().isEmpty()) {
            setTitle(pkg.getName());
        }

        TextView name = (TextView) findViewById(R.id.name);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_package_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit: {
                Intent intent = new Intent(this, PackageAddActivity.class);
                intent.putExtra(PackageAddActivity.EXTRA_PACKAGE_CODE, pkg.getCod());
                startActivity(intent);
                return true;
            }

            case R.id.remove: {
                pkg.remove();
                finish();
                return true;
            }

            case android.R.id.home: {
                Intent intent = new Intent(this, MainActivity.class);
                NavUtils.navigateUpTo(this, intent);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

}
