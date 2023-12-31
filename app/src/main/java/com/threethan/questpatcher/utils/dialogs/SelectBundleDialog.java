package com.threethan.questpatcher.utils.dialogs;

import android.app.Activity;

import com.threethan.questpatcher.R;
import com.threethan.questpatcher.utils.SplitAPKInstaller;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 30, 2023
 */
public class SelectBundleDialog {

    private final MaterialAlertDialogBuilder mDialogBuilder;

    public SelectBundleDialog(String path, boolean exit, Activity activity) {
        mDialogBuilder = new MaterialAlertDialogBuilder(activity)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.split_apk_installer)
                .setMessage(activity.getString(R.string.install_bundle_question))
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    if (exit) {
                        activity.finish();
                    }
                })
                .setPositiveButton(R.string.yes, (dialogInterface, i) ->
                        SplitAPKInstaller.handleAppBundle(exit, path, activity)
                );
    }

    public void show() {
        mDialogBuilder.show();
    }

}