package com.threethan.questpatcher.activities;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;

import com.apk.editor.R;
import com.threethan.questpatcher.utils.APKExplorer;
import com.threethan.questpatcher.utils.Common;
import com.threethan.questpatcher.utils.Projects;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;

import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class ImageViewActivity extends AppCompatActivity {

    public static final String PATH_INTENT = "path";

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageview);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mMenu = findViewById(R.id.export);
        AppCompatImageView mImage = findViewById(R.id.image);
        MaterialTextView mTitle = findViewById(R.id.title);
        String path = getIntent().getStringExtra(PATH_INTENT);

        if (path != null) {
            mTitle.setText(new File(path).getName());
            mImage.setImageURI(APKExplorer.getIconFromPath(path));
        } else {
            mTitle.setText(sPackageUtils.getAppName(Common.getAppID(), this));
            mImage.setImageDrawable(sPackageUtils.getAppIcon(Common.getAppID(), this));
        }

        mMenu.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.export_question)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(getString(R.string.export), (dialog, id) -> {
                            if (sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,this)) {
                                sPermissionUtils.requestPermission(
                                        new String[] {
                                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        },this);
                            } else {
                                String mExportPath;
                                if (Build.VERSION.SDK_INT < 29) {
                                    sFileUtils.mkdir(new File(Projects.getExportPath(this), Common.getAppID()));
                                    mExportPath = Projects.getExportPath(this) + "/" + Common.getAppID();
                                } else {
                                    mExportPath = Projects.getExportPath(this);
                                }
                                if (path != null) {
                                    APKExplorer.saveImage(BitmapFactory.decodeFile(path), mExportPath + "/" + new File(path).getName(), this);
                                } else {
                                    APKExplorer.saveImage(APKExplorer.drawableToBitmap(mImage.getDrawable()), mExportPath + "/icon.png", this);
                                }
                                new MaterialAlertDialogBuilder(this)
                                        .setMessage(getString(R.string.export_complete_message, mExportPath))
                                        .setPositiveButton(getString(R.string.cancel), (dialog1, id1) -> {
                                        }).show();
                            }
                        }
                ).show()
        );

        mBack.setOnClickListener(v -> finish());
    }

}