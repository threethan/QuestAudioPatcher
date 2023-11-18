package com.threethan.questpatcher.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.apk.editor.R;
import com.threethan.questpatcher.utils.APKData;
import com.threethan.questpatcher.utils.Common;
import com.threethan.questpatcher.utils.recyclerViewItems.PackageItems;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.threethan.questpatcher.utils.tasks.SignAPK;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class InstallerActivity extends AppCompatActivity {

    private AppCompatImageButton mIcon;
    private View mCancel, mOpen;
    private MaterialTextView mStatus, mTitle;
    private ProgressBar mProgress;
    private Thread mRefreshThread = null;

    public static final String HEADING_INTENT = "heading", PATH_INTENT = "path";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installer);

        mIcon = findViewById(R.id.icon);
        mProgress = findViewById(R.id.progress);
        mOpen = findViewById(R.id.open);
        mCancel = findViewById(R.id.cancel);
        MaterialTextView mHeading = findViewById(R.id.heading);
        mTitle = findViewById(R.id.title);
        mStatus = findViewById(R.id.status);

        String path = getIntent().getStringExtra(PATH_INTENT);
        if (path != null) {
            try {
                Common.setPackageName(Objects.requireNonNull(sAPKUtils.getPackageName(path, this)));
                mTitle.setText(sAPKUtils.getAPKName(path, this));
                mIcon.setImageDrawable(sAPKUtils.getAPKIcon(path, this));
            } catch (NullPointerException ignored) {}
        } else {
            Common.setPackageName(APKData.findPackageName(this));
            mTitle.setText(getName());
            mIcon.setImageDrawable(getIcon());
        }

        mHeading.setText(getIntent().getStringExtra(HEADING_INTENT));

        mOpen.setOnClickListener(v -> {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(Common.getPackageName());
            if (launchIntent != null) {
                startActivity(launchIntent);
                finish();
            }
        });

        mCancel.setOnClickListener(v -> {
            if (sFileUtils.exist(new File(getCacheDir(),"splits"))) {
                sFileUtils.delete(new File(getCacheDir(),"splits"));
            }
            super.onBackPressed();
        });

        refreshStatus(this);
    }

    public void refreshStatus(InstallerActivity activity) {
        mRefreshThread = new RefreshThread(activity);
        mRefreshThread.start();
    }

    private CharSequence getName() {
        CharSequence name = null;
        for (String mAPKs : Common.getAPKList()) {
            if (sAPKUtils.getAPKName(mAPKs, this) != null) {
                name = sAPKUtils.getAPKName(mAPKs, this);
            }
        }
        return name;
    }

    private Drawable getIcon() {
        Drawable icon = null;
        for (String mAPKs : Common.getAPKList()) {
            if (sAPKUtils.getAPKIcon(mAPKs, this) != null) {
                icon = sAPKUtils.getAPKIcon(mAPKs, this);
            }
        }
        return icon;
    }

    @Override
    public void onBackPressed() {
        if (sCommonUtils.getString("installationStatus", "waiting", this).equals("waiting")) {
            return;
        }
        if (sCommonUtils.getString("installationStatus", "waiting", this).equals(getString(R.string.installation_status_success))) {
            Common.getPackageData().add(new PackageItems(
                    Common.getPackageName(),
                    this
            ));
        }
        if (sFileUtils.exist(new File(getCacheDir(),"splits"))) {
            sFileUtils.delete(new File(getCacheDir(),"splits"));
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (mRefreshThread != null) {
            try {
                mRefreshThread.interrupt();
            } catch(Exception ignored) {}
        }
        super.onDestroy();
    }

    private static class RefreshThread extends Thread {
        WeakReference<InstallerActivity> mInstallerActivityRef;
        RefreshThread(InstallerActivity activity) {
            mInstallerActivityRef = new WeakReference<>(activity);
        }
        @SuppressLint("StringFormatInvalid")
        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    Thread.sleep(500);
                    final InstallerActivity activity = mInstallerActivityRef.get();
                    if(activity == null){
                        break;
                    }
                    activity.runOnUiThread(() -> {
                        String installationStatus = sCommonUtils.getString("installationStatus", "waiting", activity);
                        if (installationStatus.equals("waiting")) {
                            try {
                                if (activity.getIntent().getStringExtra(PATH_INTENT) != null) {
                                    activity.mStatus.setText(activity.getString(R.string.installing, sAPKUtils.getAPKName(activity.getIntent().getStringExtra(PATH_INTENT), activity)));
                                } else {
                                    activity.mStatus.setText(activity.getString(R.string.installing, getName()));
                                }
                            } catch (NullPointerException ignored) {}
                        } else {
                            activity.mStatus.setText(installationStatus);
                            activity.mProgress.setVisibility(View.GONE);
                            activity.mCancel.setVisibility(View.VISIBLE);
                            if (installationStatus.equals(activity.getString(R.string.installation_status_success))) {
                                try {
                                    activity.mTitle.setText(sPackageUtils.getAppName(Common.getPackageName(), activity));
                                    activity.mIcon.setImageDrawable(sPackageUtils.getAppIcon(Common.getPackageName(), activity));
                                    activity.mOpen.setVisibility(View.VISIBLE);
                                } catch (NullPointerException ignored) {}
                            }
                        }
                    });
                }
            } catch (InterruptedException ignored) {}
        }
    }

}