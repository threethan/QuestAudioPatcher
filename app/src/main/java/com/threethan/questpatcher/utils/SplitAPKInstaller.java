package com.threethan.questpatcher.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import com.threethan.questpatcher.R;
import com.threethan.questpatcher.activities.InstallerActivity;
import com.threethan.questpatcher.activities.InstallerFilePickerActivity;
import com.threethan.questpatcher.services.InstallerService;

import java.io.File;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.InstallerUtils.sInstallerParams;
import in.sunilpaulmathew.sCommon.InstallerUtils.sInstallerUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class SplitAPKInstaller {

    private static File[] getFilesList(File dir) {
        return Objects.requireNonNull(dir).listFiles();
    }

    private static Intent getCallbackIntent(Context context) {
        return new Intent(context, InstallerService.class);
    }

    private static long getTotalSize(String path) {
        int totalSize = 0;
        if (path != null) {
            for (String mSplits : APKData.splitApks(path)) {
                File mFile = new File(mSplits);
                if (mFile.exists() && mSplits.endsWith(".apk")) {
                    totalSize += mFile.length();
                }
            }
        } else if (Common.getAPKList().size() > 0) {
            for (String string : Common.getAPKList()) {
                if (sFileUtils.exist(new File(string))) {
                    File mFile = new File(string);
                    if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                        totalSize += mFile.length();
                    }
                }
            }
        }
        return totalSize;
    }

    public static void handleAppBundle(boolean exit, String path, Activity activity) {
        new sExecutor() {
            private final File mFile = new File(activity.getExternalCacheDir(), "splits");
            private ProgressDialog mProgressDialog;

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setMessage(activity.getString(R.string.preparing_bundle_install, new File(path).getName()));
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setIcon(R.mipmap.ic_launcher);
                mProgressDialog.setTitle(R.string.app_name);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();

                if (sFileUtils.exist(mFile)) {
                    sFileUtils.delete(mFile);
                }
            }

            @Override
            public void doInBackground() {
                APKEditorUtils.unzip(path, mFile.getAbsolutePath());
                for (File files : getFilesList(mFile)) {
                    if (files.isFile() && files.getName().endsWith(".apk")) {
                        Common.setPath(mFile.getAbsolutePath());
                    } else if (files.isDirectory()) {
                        for (File dirs : getFilesList(new File(mFile, files.getName()))) {
                            if (dirs.isFile() && dirs.getName().endsWith(".apk")) {
                                Common.setPath(new File(mFile, dirs.getName()).getAbsolutePath());
                            }
                        }
                    }
                }
            }

            @Override
            public void onPostExecute() {
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                Common.getAPKList().clear();
                Intent installer = new Intent(activity, InstallerFilePickerActivity.class);
                installer.putExtra(InstallerFilePickerActivity.TITLE_INTENT, activity.getString(R.string.select_apk));
                activity.startActivity(installer);

                if (exit) {
                    activity.finish();
                }
            }
        }.execute();
    }

    public static void installSplitAPKs(boolean exit, List<String> apks, String path, Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                sCommonUtils.saveString("installationStatus", "waiting", activity);
                Intent installIntent = new Intent(activity, InstallerActivity.class);
                installIntent.putExtra(InstallerActivity.HEADING_INTENT, activity.getString(R.string.split_apk_installer));
                installIntent.putExtra(InstallerActivity.PATH_INTENT, path);
                activity.startActivity(installIntent);
            }

            @Override
            public void doInBackground() {
                int sessionId;
                final sInstallerParams installParams = sInstallerUtils.makeInstallParams(getTotalSize(path));
                sessionId = sInstallerUtils.runInstallCreate(installParams, activity);
                try {
                    if (path != null) {
                        for (String mSplits : APKData.splitApks(path)) {
                            File mFile = new File(mSplits);
                            if (mFile.exists()) {
                                sInstallerUtils.runInstallWrite(mFile.length(), sessionId, mFile.getName(), mFile.toString(), activity);
                            }
                        }
                    } else {
                        for (String string : apks) {
                            if (sFileUtils.exist(new File(string))) {
                                File mFile = new File(string);
                                if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                                    sInstallerUtils.runInstallWrite(mFile.length(), sessionId, mFile.getName(), mFile.toString(), activity);
                                }
                            }
                        }
                    }
                } catch (NullPointerException ignored) {}
                sInstallerUtils.doCommitSession(sessionId, getCallbackIntent(activity), activity);
            }

            @Override
            public void onPostExecute() {
                if (exit) {
                    activity.finish();
                }
            }
        }.execute();
    }

    public static void installAPK(boolean exit, File APK, Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                sCommonUtils.saveString("installationStatus", "waiting", activity);
                Intent installIntent = new Intent(activity, InstallerActivity.class);
                installIntent.putExtra(InstallerActivity.HEADING_INTENT, activity.getString(R.string.apk_installer));
                installIntent.putExtra(InstallerActivity.PATH_INTENT, APK.getAbsolutePath());
                activity.startActivity(installIntent);
            }

            @Override
            public void doInBackground() {
                int sessionId;
                final sInstallerParams installParams = sInstallerUtils.makeInstallParams(APK.length());
                sessionId = sInstallerUtils.runInstallCreate(installParams, activity);
                try {
                    sInstallerUtils.runInstallWrite(APK.length(), sessionId, APK.getName(), APK.getAbsolutePath(), activity);
                } catch (NullPointerException ignored) {}
                sInstallerUtils.doCommitSession(sessionId, getCallbackIntent(activity), activity);
            }

            @Override
            public void onPostExecute() {
                if (exit) {
                    activity.finish();
                }
            }
        }.execute();
    }

}