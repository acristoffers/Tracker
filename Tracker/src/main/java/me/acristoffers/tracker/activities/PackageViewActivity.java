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

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import br.mg.cefet.tracker.R;
import me.acristoffers.tracker.backend.Package;
import me.acristoffers.tracker.fragments.PackageViewFragment;

public class PackageViewActivity extends ActionBarActivity {

    private Package pkg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_fragment);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        String code;

        Intent intent = getIntent();
        if (intent != null) {
            code = intent.getStringExtra(PackageEditActivity.EXTRA_PACKAGE_CODE);
        } else if (savedInstanceState != null) {
            code = savedInstanceState.getString(PackageEditActivity.EXTRA_PACKAGE_CODE);
        } else {
            finish();
            return;
        }

        pkg = new Package(code, this);

        if (!pkg.getName().isEmpty()) {
            setTitle(pkg.getName());
        }

        Bundle bundle = new Bundle();
        bundle.putString(PackageEditActivity.EXTRA_PACKAGE_CODE, pkg.getCod());

        PackageViewFragment fragment = new PackageViewFragment();
        fragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, fragment, "viewFragment")
                .commit();

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(pkg.getId());
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
                Intent intent = new Intent(this, PackageEditActivity.class);
                intent.putExtra(PackageEditActivity.EXTRA_PACKAGE_CODE, pkg.getCod());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.abc_fade_out);
                return true;
            }

            case R.id.remove: {
                String body = getString(R.string.confirm_delete, pkg.getName());

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.are_you_sure);
                builder.setMessage(body);

                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        pkg.remove();
                        finish();
                        overridePendingTransition(R.anim.abc_fade_in, R.anim.slide_out_right);
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
            }

            case android.R.id.home: {
                Intent intent = new Intent(this, PackageListActivity.class);
                NavUtils.navigateUpTo(this, intent);
                overridePendingTransition(R.anim.abc_fade_in, R.anim.slide_out_right);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

}
