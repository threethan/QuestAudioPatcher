package com.threethan.questpatcher.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import androidx.appcompat.widget.AppCompatEditText;

import com.threethan.questpatcher.utils.recyclerViewItems.PackageItems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class AppData {

    public static List<PackageItems> getRawData(ProgressBar progressBar, Context context) {
        List<PackageItems> mData = new ArrayList<>();
        List<ApplicationInfo> packages = context.getPackageManager().getInstalledApplications(0);
        for (ApplicationInfo packageInfo: packages) {
            progressBar.setMax(packages.size());
            try {
                PackageItems pi = new PackageItems(
                        packageInfo.packageName,
                        context);
                // If PackageItems construction survived, then add
                mData.add(pi);
            } catch (Exception ignored) {
            }
            if (progressBar.getProgress() < packages.size()) {
                progressBar.setProgress(progressBar.getProgress() + 1);
            }
        }
        return mData;
    }

    public static List<PackageItems> getData(Context context) {
        List<PackageItems> mData = new ArrayList<>();
        try {
            boolean mAppType;
            for (PackageItems packageItem : Common.getPackageData()) {
                if (sCommonUtils.getString("appTypes", "all", context).equals("system")) {
                    mAppType = sPackageUtils.isSystemApp(packageItem.getPackageName(), context);
                } else if (sCommonUtils.getString("appTypes", "all", context).equals("user")) {
                    mAppType = !sPackageUtils.isSystemApp(packageItem.getPackageName(), context);
                } else {
                    mAppType = true;
                }
                if (mAppType) {
                    if (Common.getSearchWord() == null) {
                        mData.add(packageItem);
                    } else if (Common.isTextMatched(packageItem.getAppName(), Common.getSearchWord())
                            || Common.isTextMatched(packageItem.getPackageName(), Common.getSearchWord())) {
                        mData.add(packageItem);
                    }
                }
            }
            if (sCommonUtils.getBoolean("sort_name", false, context)) {
                Collections.sort(mData, (lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getAppName(), rhs.getAppName()));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && sCommonUtils.getBoolean("sort_size", false, context)) {
                Collections.sort(mData, Comparator.comparingLong(PackageItems::getAPKSize));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && sCommonUtils.getBoolean("sort_installed", false, context)) {
                Collections.sort(mData, Comparator.comparingLong(PackageItems::getInstalledTime));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && sCommonUtils.getBoolean("sort_updated", false, context)) {
                Collections.sort(mData, Comparator.comparingLong(PackageItems::getUpdatedTime));
            } else {
                Collections.sort(mData, (lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getPackageName(), rhs.getPackageName()));
            }
            if (!sCommonUtils.getBoolean("az_order", true, context)) {
                Collections.reverse(mData);
            }
        } catch (NullPointerException ignored) {}
        return mData;
    }

    public static PackageInfo getPackageInfo(String packageName, Context context) {
        try {
            return context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (Exception ignored) {
        }
        return null;
    }

    /*
     * Based on the work of https://github.com/ZenerDeveloper
     * Ref: https://github.com/SmartPack/PackageManager/commit/1ac499d0ed8922c02875df029ead80a17f1c40e1
     */
    public static void toggleKeyboard(int mode, AppCompatEditText textView, Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mode == 1) {
            if (textView.requestFocus()) {
                imm.showSoftInput(textView, InputMethodManager.SHOW_IMPLICIT);
            }
        } else {
            imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
        }
    }

}
