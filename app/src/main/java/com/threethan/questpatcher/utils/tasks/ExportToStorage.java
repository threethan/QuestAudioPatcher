package com.threethan.questpatcher.utils.tasks;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;

import com.threethan.questpatcher.R;
import com.threethan.questpatcher.utils.APKData;
import com.threethan.questpatcher.utils.Common;
import com.threethan.questpatcher.utils.Projects;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 28, 2023
 */
public class ExportToStorage extends sExecutor {

    private final Context mContext;
    private final File mSourceFile;
    private final List<File> mSourceFiles;
    private ProgressDialog mProgressDialog;
    private final String mFolder;
    private String mExportPath = null;

    public ExportToStorage(File sourceFile, List<File> sourceFiles, String folder, Context context) {
        mSourceFile = sourceFile;
        mSourceFiles = sourceFiles;
        mFolder = folder;
        mContext = context;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(mContext.getString(R.string.exporting, mSourceFile != null && mSourceFile.exists() ? mSourceFile.getName() : ""));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(R.string.app_name);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    public void doInBackground() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            sFileUtils.mkdir(new File(Projects.getExportPath(mContext), mFolder));
            mExportPath = Projects.getExportPath(mContext) + "/" + Common.getAppID();
        } else {
            mExportPath = Projects.getExportPath(mContext);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (mSourceFiles != null && mSourceFiles.size() > 0) {
                for (File file : mSourceFiles) {
                    if (file.exists()) {
                        APKData.saveToDownload(file, file.getName(), mContext);
                    }
                }
            } else {
                APKData.saveToDownload(mSourceFile, mSourceFile.getName(), mContext);
            }
        } else {
            if (mSourceFiles != null && mSourceFiles.size() > 0) {
                for (File file : mSourceFiles) {
                    if (file.exists()) {
                        sFileUtils.copy(file, new File(mExportPath, file.getName()));
                    }
                }
            } else {
                sFileUtils.copy(mSourceFile, new File(mExportPath, mSourceFile.getName()));
            }
        }
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPostExecute() {
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException ignored) {
        }
        new MaterialAlertDialogBuilder(mContext)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(mContext.getString(R.string.export_complete_message, mExportPath))
                .setPositiveButton(mContext.getString(R.string.cancel), (dialog1, id1) -> {
                }).show();
    }

}