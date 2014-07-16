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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import br.mg.cefet.tracker.R;
import me.acristoffers.tracker.activities.PackageEditActivity;
import me.acristoffers.tracker.activities.PackageListActivity;
import me.acristoffers.tracker.adapters.StepsListAdapter;
import me.acristoffers.tracker.backend.Package;

public class PackageViewFragment extends Fragment {

    String code = null;
    Package pkg = null;
    Activity activity = null;
    View view = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_package_view, null);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        reload();
    }

    @Override
    public void onResume() {
        super.onResume();
        reload();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(PackageEditActivity.EXTRA_PACKAGE_CODE, code);
    }

    public void reload() {
        if (code == null) {
            final Bundle arguments = getArguments();
            if (arguments != null) {
                code = arguments.getString(PackageEditActivity.EXTRA_PACKAGE_CODE);
            }
        }

        if (activity != null && view != null && code != null) {
            pkg = new Package(code, activity);

            TextView name = (TextView) view.findViewById(R.id.name);
            if (name != null) {
                name.setText(pkg.getName());
            }

            TextView cod = (TextView) view.findViewById(R.id.code);
            if (cod != null) {
                cod.setText(pkg.getCod());
                cod.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        ClipboardManager clipboardManager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                        if (clipboardManager != null) {
                            TextView tv = (TextView) view;
                            String code = tv.getText().toString();
                            ClipData clipData = ClipData.newPlainText(code, code);
                            clipboardManager.setPrimaryClip(clipData);

                            Toast toast = Toast.makeText(activity, R.string.code_copied, Toast.LENGTH_SHORT);
                            toast.show();

                            return true;
                        }

                        return false;
                    }
                });
            }

            ListView listView = (ListView) view.findViewById(R.id.steps);
            if (listView != null) {
                View emptyView = view.findViewById(R.id.emptyStepView);
                listView.setEmptyView(emptyView);
                listView.setAdapter(new StepsListAdapter(activity, pkg));
            }

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
                            String body = getString(R.string.confirm_delete, pkg.getName());

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
        }
    }

}
