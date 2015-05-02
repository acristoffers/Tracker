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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import me.acristoffers.tracker.Package;
import me.acristoffers.tracker.R;
import me.acristoffers.tracker.adapters.StepListAdapter;

public class PackageDetails extends AppCompatActivity {

    public static final String PACKAGE_CODE = "pkg_cod";
    private Package pkg = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_package_details);

        String code;

        Intent intent = getIntent();
        if (intent != null) {
            code = intent.getStringExtra(PACKAGE_CODE);
        } else if (savedInstanceState != null) {
            code = savedInstanceState.getString(PACKAGE_CODE);
        } else {
            finish();
            return;
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.steps);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        pkg = new Package(code, this);
        StepListAdapter stepListAdapter = new StepListAdapter(pkg, this);
        recyclerView.setAdapter(stepListAdapter);

        TextView textView = (TextView) findViewById(R.id.name);
        textView.setText(pkg.getName());

        textView = (TextView) findViewById(R.id.code);
        textView.setText(pkg.getCod());
        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboardManager != null) {
                    TextView tv = (TextView) view;
                    String code = tv.getText().toString();
                    ClipData clipData = ClipData.newPlainText(code, code);
                    clipboardManager.setPrimaryClip(clipData);

                    Toast toast = Toast.makeText(PackageDetails.this, R.string.code_copied, Toast.LENGTH_SHORT);
                    toast.show();

                    return true;
                }

                return false;
            }
        });

        if (!pkg.getSteps().isEmpty()) {
            textView = (TextView) findViewById(R.id.emptyStepView);
            textView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_package_view, menu);
        return true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        pkg = new Package(pkg.getCod(), this);
        TextView textView = (TextView) findViewById(R.id.name);
        textView.setText(pkg.getName());
    }

    @Override
    protected void onResume() {
        super.onResume();

        pkg = new Package(pkg.getCod(), this);
        TextView textView = (TextView) findViewById(R.id.name);
        textView.setText(pkg.getName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.remove) {
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

        if (id == R.id.edit) {
            Intent intent = new Intent(this, PackageEdit.class);
            intent.putExtra(PackageDetails.PACKAGE_CODE, pkg.getCod());

            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
