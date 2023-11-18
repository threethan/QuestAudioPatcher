package com.threethan.questpatcher.utils.dialogs;

import android.content.Context;

import com.apk.editor.R;
import com.threethan.questpatcher.utils.tasks.SaveBundletoDownloads;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 30, 2023
 */
public class ShareBundleDialog {

    private final MaterialAlertDialogBuilder mDialogBuilder;

    public ShareBundleDialog(String path, Context context) {
        mDialogBuilder = new MaterialAlertDialogBuilder(context)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(context.getString(R.string.share_message, new File(path).getName()))
                .setNegativeButton(context.getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(context.getString(R.string.share), (dialog, id) ->
                        new SaveBundletoDownloads(path, false, context).execute());
    }

    public void show() {
        mDialogBuilder.show();
    }

}