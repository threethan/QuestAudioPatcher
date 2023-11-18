package com.threethan.questpatcher.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.threethan.questpatcher.activities.TextEditorActivity;
import com.threethan.questpatcher.adapters.APKExplorerAdapter;
import com.threethan.questpatcher.utils.APKEditorUtils;
import com.threethan.questpatcher.utils.APKExplorer;
import com.threethan.questpatcher.utils.Common;
import com.threethan.questpatcher.utils.tasks.DeleteFile;
import com.threethan.questpatcher.utils.tasks.DeleteProject;
import com.threethan.questpatcher.utils.tasks.ExportToStorage;
import com.threethan.questpatcher.utils.tasks.SignAPK;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 05, 2021
 */
public class APKExplorerFragment extends androidx.fragment.app.Fragment {

    private MaterialTextView mTitle;
    private LinearLayoutCompat mProgressLayout;
    private RecyclerView mRecyclerView;
    private APKExplorerAdapter mRecycleViewAdapter;
    private AppCompatImageButton saveBtn;
    public static APKExplorerFragment current;
    public void fail() {
        AlertDialog alertDialog =  new MaterialAlertDialogBuilder(getActivity())
                .setNegativeButton(R.string.cancel, (DialogInterface.OnClickListener) (d, w) -> {
                    getActivity().finish();
                    d.dismiss();
                }).create();
        alertDialog.setTitle(R.string.cancelling);
        alertDialog.setMessage(getString(R.string.already_patched));
        alertDialog.show();
    }
    public void failManifest() {
        AlertDialog alertDialog =  new MaterialAlertDialogBuilder(getActivity())
                .setNegativeButton(R.string.cancel, (DialogInterface.OnClickListener) (d, w) -> {
                    getActivity().finish();
                    d.dismiss();
                }).create();
        alertDialog.setTitle(R.string.cancelling);
        alertDialog.setMessage(getString(R.string.no_manifest));
        alertDialog.show();
    }
    public void succeed() {
        saveBtn.callOnClick();
    }
    @SuppressLint("StringFormatInvalid")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_apkexplorer, container, false);

        AppCompatImageButton mBack = mRootView.findViewById(R.id.back);
        AppCompatImageButton mSave = mRootView.findViewById(R.id.save);
        // EDITED
        saveBtn = mSave;

        AppCompatImageButton mSortButton = mRootView.findViewById(R.id.sort);
        mTitle = mRootView.findViewById(R.id.title);
        MaterialTextView mError = mRootView.findViewById(R.id.error_status);
        mProgressLayout = mRootView.findViewById(R.id.progress_layout);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        String mAppName = APKExplorer.getAppName(Common.getPath() + "/.aeeBackup/appData");
        Common.setAppID(new File(Common.getPath()).getName());
        mTitle.setText(getString(R.string.root));

        mBack.setOnClickListener(v -> retainDialog());

        mSave.setOnClickListener(v -> {
            new SignAPK(requireActivity()).execute();
        });

        if (APKEditorUtils.isFullVersion(requireActivity())) {
            mSave.setVisibility(View.VISIBLE);
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), APKExplorer.getSpanCount(requireActivity())));

        // CHANGED: AUTOMATE
        current = this;
        Intent amintent;
        boolean hasOpened = false;
        amintent = new Intent(requireActivity(), TextEditorActivity.class);
        for (String file : APKExplorer.getData(getFilesList(), true, requireActivity())) {
            if (file != null && file.endsWith("AndroidManifest.xml")) {
                amintent.putExtra(TextEditorActivity.PATH_INTENT, file);
                startActivity(amintent);
                hasOpened = true;
                break;
            }
        }
        if (!hasOpened) failManifest();

        mSortButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), mSortButton);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_order)).setCheckable(true)
                    .setChecked(sCommonUtils.getBoolean("az_order", true, requireActivity()));
            if (Common.getFiles() != null && Common.getFiles().size() > 0) {
                menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.export_selected_files));
                if (APKEditorUtils.isFullVersion(requireActivity())) {
                    menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.delete_selected_files));
                }
            }
            if (APKEditorUtils.isFullVersion(requireActivity()) && !Objects.requireNonNull(new File(Common.getPath()).getParentFile()).getPath().equals(requireActivity().getCacheDir().getPath())) {
                menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.delete_folder));
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    sCommonUtils.saveBoolean("az_order", !sCommonUtils.getBoolean("az_order", true, requireActivity()), requireActivity());
                    reload(requireActivity());
                } else if (item.getItemId() == 1) {
                    if (Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, v.getContext())) {
                        sPermissionUtils.requestPermission(
                                new String[] {
                                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                }, (Activity) v.getContext());
                    } else {
                        new ExportToStorage(null, Common.getFiles(), Common.getAppID(), requireActivity()).execute();
                    }
                } else if (item.getItemId() == 2) {
                    for (File file : Common.getFiles()) {
                        if (file.exists()) {
                            new DeleteFile(file, v.getContext()) {

                                @Override
                                public void onPostExecute() {
                                    reload(requireActivity());
                                }
                            }.execute();
                        }
                    }
                } else if (item.getItemId() == 3) {
                    new DeleteFile(new File(Common.getPath()), v.getContext()) {

                        @Override
                        public void onPostExecute() {
                            Common.setPath(Objects.requireNonNull(new File(Common.getPath()).getParentFile()).getPath());
                            Common.clearFilesList();
                            reload(requireActivity());
                        }
                    }.execute();
                }
                return false;
            });
            popupMenu.show();
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (Objects.requireNonNull(new File(Common.getPath()).getParentFile()).getPath().equals(requireActivity().getCacheDir().getPath())) {
                    retainDialog();
                } else {
                    Common.setPath(Objects.requireNonNull(new File(Common.getPath()).getParentFile()).getPath());
                    Common.clearFilesList();
                    reload(requireActivity());
                }
            }
        });

        return mRootView;
    }
    private void retainDialog() {
        if (sCommonUtils.getString("projectAction", null, requireActivity()) == null) {
            new MaterialAlertDialogBuilder(requireActivity())
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.save_projects_question)
                    .setNeutralButton(getString(R.string.cancel), (dialog, id) -> {
                    })
                    .setNegativeButton(getString(R.string.discard), (dialog, id) -> {
                        new DeleteProject(new File(requireActivity().getCacheDir(), Common.getAppID()), requireActivity()).execute();
                        requireActivity().finish();
                    })
                    .setPositiveButton(getString(R.string.save), (dialog, id) -> requireActivity().finish()).show();
        } else if (sCommonUtils.getString("projectAction", null, requireActivity()).equals(getString(R.string.delete))) {
            new DeleteProject(new File(requireActivity().getCacheDir(), Common.getAppID()), requireActivity()).execute();
            requireActivity().finish();
        } else {
            requireActivity().finish();
        }
    }

    private File[] getFilesList() {
        return new File(Common.getPath()).listFiles();
    }

    private void reload(Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                mRecyclerView.setVisibility(View.GONE);
                mProgressLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void doInBackground() {
                mRecycleViewAdapter = new APKExplorerAdapter(APKExplorer.getData(getFilesList(), true, activity), replaceFile);
            }

            @Override
            public void onPostExecute() {
                if (Common.getAppID() != null) {
                    mTitle.setText(Common.getPath().equals(new File(activity.getCacheDir(), Objects.requireNonNull(Common.getAppID()))
                            .getAbsolutePath()) ? getString(R.string.root) : new File(Common.getPath()).getName());
                }
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mProgressLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }.execute();
    }

    ActivityResultLauncher<Intent> replaceFile = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    Uri uriFile = data.getData();

                    if (uriFile != null) {
                        new MaterialAlertDialogBuilder(requireActivity())
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.app_name)
                                .setMessage(getString(R.string.replace_file_question, new File(Common.getFileToReplace()).getName()))
                                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                                })
                                .setPositiveButton(R.string.replace, (dialog, id) -> {
                                    sFileUtils.copy(uriFile, new File(Common.getFileToReplace()), requireActivity());
                                    if (Common.getFileToReplace().contains("classes") && Common.getFileToReplace().contains(".dex")) {
                                        try {
                                            JSONObject jsonObject = new JSONObject(sFileUtils.read(new File(requireActivity().getCacheDir(), Common.getAppID() + "/.aeeBackup/appData")));
                                            jsonObject.put("smali_edited", true);
                                            sFileUtils.create(jsonObject.toString(), new File(requireActivity().getCacheDir(), Common.getAppID() + "/.aeeBackup/appData"));
                                        } catch (JSONException ignored) {
                                        }
                                    }
                                    reload(requireActivity());
                                }).show();
                    }
                }
            }
    );

}