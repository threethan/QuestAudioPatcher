package com.threethan.questpatcher.utils.tasks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import com.apk.editor.R;
import com.threethan.questpatcher.activities.APKTasksActivity;
import com.threethan.questpatcher.utils.APKData;
import com.threethan.questpatcher.utils.APKEditorUtils;
import com.threethan.questpatcher.utils.Common;
import com.threethan.questpatcher.utils.SplitAPKInstaller;
import com.threethan.questpatcher.utils.ZipAlign;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 28, 2023
 */
public class SignAPK extends sExecutor {

    private final Activity mActivity;
    private File mBackUpPath = null, mBuildDir = null, mExportPath = null, mTMPZip = null;
    private File mParent;

    public SignAPK(Activity activity) {
        mActivity = activity;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        mExportPath = new File(mActivity.getCacheDir(), Common.getAppID());
        mTMPZip = new File(mActivity.getCacheDir(), "tmp.apk");
        Common.setFinishStatus(false);
        Common.isCancelled(false);
        Common.isBuilding(true);
        Common.setStatus(null);
        Intent apkTasks = new Intent(mActivity, APKTasksActivity.class);
        mActivity.startActivity(apkTasks);
        Common.setStatus(mActivity.getString(R.string.preparing_apk, Common.getAppID()));

        mBuildDir = new File(mExportPath, ".aeeBuild");
        mBackUpPath = new File(mExportPath, ".aeeBackup");
        if (mBuildDir.exists()) {
            sFileUtils.delete(mBuildDir);
        }
        sFileUtils.mkdir(mBuildDir);

        if (mTMPZip.exists()) {
            sFileUtils.delete(mTMPZip);
        }
    }
    List<String> apks = new ArrayList<>();
    @SuppressLint("StringFormatInvalid")
    @Override
    public void doInBackground() {
        Common.setStatus(mActivity.getString(R.string.preparing_source));
        APKData.prepareSource(mBuildDir, mExportPath, mBackUpPath, mActivity);
        if (Common.getError() > 0) {
            return;
        }
        APKEditorUtils.zip(mBuildDir, mTMPZip);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Common.setStatus(mActivity.getString(R.string.zip_aligning));
            try {
                RandomAccessFile apkUnaligned = new RandomAccessFile(mTMPZip, "r");
                FileOutputStream apkAligned = new FileOutputStream(new File(mActivity.getCacheDir(), "tmp_zipAligned.apk"));
                ZipAlign.alignZip(apkUnaligned, apkAligned);
                mTMPZip = new File(mActivity.getCacheDir(), "tmp_zipAligned.apk");
                sFileUtils.delete(new File(mActivity.getCacheDir(), "tmp.apk"));
            } catch (IOException ignored) {
            }
        }
        if (sPackageUtils.isPackageInstalled(Common.getAppID(), mActivity) && APKData.isAppBundle(sPackageUtils
                .getSourceDir(Common.getAppID(), mActivity))) {
            mParent = new File(APKData.getExportAPKsPath(mActivity), Common.getAppID() + "_aee-signed");
            if (mParent.exists()) {
                sFileUtils.delete(mParent);
            }
            sFileUtils.mkdir(mParent);
            for (String mSplits : APKData.splitApks(sPackageUtils.getSourceDir(Common.getAppID(), mActivity))) {
                if (!new File(mSplits).getName().equals("base.apk")) {
                    Common.setStatus(mActivity.getString(R.string.signing, new File(mSplits).getName()));
                    APKData.signApks(new File(mSplits), new File(mParent, new File(mSplits).getName()), mActivity);
                    apks.add(new File(mParent, new File(mSplits).getName()).getAbsolutePath());
                }
            }
            Common.setStatus(mActivity.getString(R.string.signing, "base.apk"));
            APKData.signApks(mTMPZip, new File(mParent, "base.apk"), mActivity);
            apks.add( new File(mParent, "base.apk").getAbsolutePath() );

        } else {
            mParent = new File(APKData.getExportAPKsPath(mActivity), Common.getAppID() + "_aee-signed.apk");
            if (mParent.exists()) {
                sFileUtils.delete(mParent);
            }
            Common.setStatus(mActivity.getString(R.string.signing, mParent.getName()));
            APKData.signApks(mTMPZip, mParent, mActivity);
        }
        if (Common.isCancelled()) {
            sFileUtils.delete(mParent);
        }
        installPackage(Common.getAppID(), mActivity);
    }

    @Override
    public void onPostExecute() {
        sFileUtils.delete(mTMPZip);
        sFileUtils.delete(mBuildDir);
        if (!Common.isFinished()) {
            Common.setFinishStatus(true);
        }
        mActivity.finish();
    }

    public void installPackage(String packageName, Activity mActivity) {
        if (isPackageInstalled(packageName, mActivity)) {
            // Prompt uninstall
            Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
            intent.setData(Uri.parse("package:" + packageName));
            mActivity.startActivity(intent);

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!isPackageInstalled(packageName, mActivity)) {
                        tryInstall(mActivity);
                        cancel();
                    }
                }
            }, 500, 500);
        } else {
            tryInstall(mActivity);
        }
    }
    private void tryInstall(Activity mActivity) {
        if (apks.isEmpty())
            SplitAPKInstaller.installAPK(true, mParent, mActivity);
        else
            SplitAPKInstaller.installSplitAPKs(true, apks, null, mActivity);
    }
    public boolean isPackageInstalled(String targetPackage, Activity activity){
        PackageManager pm=activity.getPackageManager();
        try {
            PackageInfo ignored=pm.getPackageInfo(targetPackage,PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }
}