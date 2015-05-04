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

package me.acristoffers.tracker.fragments;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import me.acristoffers.tracker.Package;
import me.acristoffers.tracker.R;
import me.acristoffers.tracker.activities.PackageDetailsActivity;
import me.acristoffers.tracker.activities.PackageEditActivity;
import me.acristoffers.tracker.activities.PackageListActivity;
import me.acristoffers.tracker.adapters.StepListAdapter;

public class PackageDetailsFragment extends Fragment {

    private Package pkg = null;
    private Activity activity;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupUI();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_package_details, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setupUI();
    }

    @Override
    public void onStart() {
        super.onStart();
        setupUI();
    }

    private void setupUI() {
        activity = getActivity();
        View view = getView();

        if (activity == null || view == null) {
            return;
        }

        String code = null;

        Bundle arguments = getArguments();
        if (arguments != null) {
            code = arguments.getString(PackageDetailsActivity.PACKAGE_CODE);
        }

        if (code == null || code.isEmpty()) {
            Intent intent = activity.getIntent();
            if (intent != null) {
                code = intent.getStringExtra(PackageDetailsActivity.PACKAGE_CODE);
            }
        }

        if (code == null || code.isEmpty()) {
            return;
        }

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.steps);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);

        pkg = new Package(code, activity);

        StepListAdapter stepListAdapter = new StepListAdapter(pkg, activity);
        recyclerView.setAdapter(stepListAdapter);

        TextView textView = (TextView) view.findViewById(R.id.name);
        textView.setText(pkg.getName());

        textView = (TextView) view.findViewById(R.id.code);
        textView.setText(pkg.getCod());
        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboardManager != null) {
                    String code = pkg.getCod();

                    ClipData clipData = ClipData.newPlainText(code, code);
                    clipboardManager.setPrimaryClip(clipData);

                    Toast toast = Toast.makeText(activity, R.string.code_copied, Toast.LENGTH_SHORT);
                    toast.show();

                    return true;
                }

                return false;
            }
        });

        if (!pkg.getSteps().isEmpty()) {
            textView = (TextView) view.findViewById(R.id.emptyStepView);
            textView.setVisibility(View.INVISIBLE);
        }

        NotificationManager nm = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(pkg.getId());
    }

    @Override
    public void onResume() {
        super.onResume();
        setupUI();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_package_view, menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.remove:
                String body = getString(R.string.confirm_delete, pkg.getName());

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.are_you_sure);
                builder.setMessage(body);

                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        pkg.remove();

                        if (PackageListActivity.isTablet()) {
                            PackageListActivity listActivity = (PackageListActivity) activity;
                            FragmentManager fragmentManager = listActivity.getSupportFragmentManager();
                            if (fragmentManager != null) {
                                FragmentTransaction transaction = fragmentManager.beginTransaction();
                                transaction.replace(R.id.package_details, new BlankFragment());
                                transaction.commit();
                            }
                        } else {
                            activity.finish();
                        }

                        dialogInterface.dismiss();
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

            case R.id.edit:
                if (PackageListActivity.isTablet()) {
                    Bundle args = new Bundle();
                    args.putString(PackageDetailsActivity.PACKAGE_CODE, pkg.getCod());

                    PackageEditFragment fragment = new PackageEditFragment();
                    fragment.setArguments(args);

                    PackageListActivity listActivity = (PackageListActivity) activity;
                    FragmentManager supportFragmentManager = listActivity.getSupportFragmentManager();
                    if (supportFragmentManager != null) {
                        FragmentTransaction transaction = supportFragmentManager.beginTransaction();
                        transaction.replace(R.id.package_details, fragment);
                        transaction.commit();
                    }
                } else {
                    Intent intent = new Intent(activity, PackageEditActivity.class);
                    intent.putExtra(PackageDetailsActivity.PACKAGE_CODE, pkg.getCod());
                    startActivity(intent);
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
