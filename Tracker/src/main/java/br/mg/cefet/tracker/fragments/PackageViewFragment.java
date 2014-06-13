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

package br.mg.cefet.tracker.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import br.mg.cefet.tracker.R;
import br.mg.cefet.tracker.activities.PackageEditActivity;
import br.mg.cefet.tracker.activities.PackageListActivity;
import br.mg.cefet.tracker.adapters.StepsListAdapter;
import br.mg.cefet.tracker.backend.Package;

public class PackageViewFragment extends Fragment {

    Package pkg = null;
    Activity activity = null;
    View view;

    public void setPackage(Package pkg) {
        this.pkg = pkg;
    }

    @Override
    public void onAttach(Activity activity) {
        this.activity = activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_package_view, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;
        reload();

        if (!PackageListActivity.isTablet) {
            View v = view.findViewById(R.id.button_bar);
            if (v != null) {
                v.setVisibility(View.GONE);
            }

        } else {
            Button edit = (Button) view.findViewById(R.id.edit);
            if (edit != null) {
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(activity, PackageEditActivity.class);
                        intent.putExtra(PackageEditActivity.EXTRA_PACKAGE_CODE, pkg.getCod());
                        startActivity(intent);
                    }
                });
            }

            Button remove = (Button) view.findViewById(R.id.remove);
            if (remove != null) {
                remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String body = getString(R.string.confirm_delete);
                        body = String.format(body, pkg.getName());

                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle(R.string.are_you_sure);
                        builder.setMessage(body);

                        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                pkg.remove();
                                PackageListActivity listActivity = (PackageListActivity) activity;
                                listActivity.reloadAndSelectNone();
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
                    }
                });
            }
        }

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        reload();
    }

    public void reload() {
        pkg = new Package(pkg.getCod(), activity);

        TextView name = (TextView) view.findViewById(R.id.name);
        if (name != null) {
            name.setText(pkg.getName());
        }

        TextView cod = (TextView) view.findViewById(R.id.code);
        if (cod != null) {
            cod.setText(pkg.getCod());
        }

        ListView listView = (ListView) view.findViewById(R.id.steps);
        if (listView != null) {
            listView.setAdapter(new StepsListAdapter(activity, pkg));
        }
    }

}