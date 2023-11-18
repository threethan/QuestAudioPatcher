package com.threethan.questpatcher.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.threethan.questpatcher.adapters.InstallerFilePickerAdapter;
import com.threethan.questpatcher.utils.APKData;
import com.threethan.questpatcher.utils.APKExplorer;
import com.threethan.questpatcher.utils.Common;
import com.threethan.questpatcher.utils.SplitAPKInstaller;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 21, 2021
 */
public class InstallerFilePickerActivity extends AppCompatActivity {

    private LinearLayoutCompat mProgressLayout;
    private InstallerFilePickerAdapter mRecycleViewAdapter;
    private MaterialTextView mTitle;
    private RecyclerView mRecyclerView;
    public static final String TITLE_INTENT = "title";

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installerfilepicker);

        AppCompatImageButton mBack = findViewById(R.id.back);
        mTitle = findViewById(R.id.title);
        AppCompatImageButton mSortButton = findViewById(R.id.sort);
        Common.initializeView(findViewById(android.R.id.content), R.id.select);
        mProgressLayout = findViewById(R.id.progress_layout);
        mRecyclerView = findViewById(R.id.recycler_view);

        mBack.setOnClickListener(v -> super.onBackPressed());

        if (Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,this)) {
            LinearLayoutCompat mPermissionLayout = findViewById(R.id.permission_layout);
            MaterialCardView mPermissionGrant = findViewById(R.id.grant_card);
            mPermissionLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            mPermissionGrant.setOnClickListener(v -> sPermissionUtils.requestPermission(
                    new String[] {
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },this));
            return;
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, APKExplorer.getSpanCount(this)));
        mRecycleViewAdapter = new InstallerFilePickerAdapter(APKExplorer.getData(getFilesList(), false, this));
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        if (getIntent().getStringExtra(TITLE_INTENT) != null) {
            mTitle.setText(getIntent().getStringExtra(TITLE_INTENT));
        } else {
            mTitle.setText(Common.getPath().equals(Environment.getExternalStorageDirectory().toString() + File.separator) ? getString(R.string.sdcard) : new File(Common.getPath()).getName());
        }

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (new File(APKExplorer.getData(getFilesList(), false, this).get(position)).isDirectory()) {
                Common.setPath(APKExplorer.getData(getFilesList(), false, this).get(position));
                reload(this);
            } else if (APKExplorer.getData(getFilesList(), false, this).get(position).endsWith(".apks") || APKExplorer.getData(getFilesList(), false,
                    this).get(position).endsWith(".apkm") || APKExplorer.getData(getFilesList(), false, this).get(position).endsWith(".xapk")) {
                new MaterialAlertDialogBuilder(this)
                        .setMessage(getString(R.string.bundle_install_question, new File(APKExplorer.getData(getFilesList(), false, this).get(position)).getName()))
                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        })
                        .setPositiveButton(getString(R.string.install), (dialogInterface, i) -> SplitAPKInstaller.handleAppBundle(true, APKExplorer.getData(
                                getFilesList(), false, this).get(position), this)).show();
            } else if (APKExplorer.getData(getFilesList(), false, this).get(position).endsWith(".apk")) {
                if (Common.getAPKList().contains(APKExplorer.getData(getFilesList(), false, this).get(position))) {
                    Common.getAPKList().remove(APKExplorer.getData(getFilesList(), false, this).get(position));
                } else {
                    Common.getAPKList().add(APKExplorer.getData(getFilesList(), false, this).get(position));
                }
                mRecycleViewAdapter.notifyItemChanged(position);
                Common.getSelectCard().setVisibility(Common.getAPKList().isEmpty() ? View.GONE : View.VISIBLE);
            } else {
                sCommonUtils.snackBar(findViewById(android.R.id.content), getString(R.string.wrong_extension, ".apks/.apkm/.xapk")).show();
            }
        });

        Common.getSelectCard().setOnClickListener(v -> {
            if (APKData.findPackageName(this) != null) {
                if (Common.getAPKList().size() > 1) {
                    APKExplorer.handleAPKs(true, this);
                } else {
                    Intent intent = new Intent(this, APKInstallerActivity.class);
                    intent.putExtra("apkFilePath", Common.getAPKList().get(0));
                    startActivity(intent);
                    finish();
                }
            } else {
                sCommonUtils.snackBar(findViewById(android.R.id.content), getString(R.string.installation_status_bad_apks)).show();
            }
        });

        mSortButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, mSortButton);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_order)).setCheckable(true)
                    .setChecked(sCommonUtils.getBoolean("az_order", true, this));
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    sCommonUtils.saveBoolean("az_order", !sCommonUtils.getBoolean("az_order", true, this), this);
                    reload(this);
                }
                return false;
            });
            popupMenu.show();
        });
    }

    private File[] getFilesList() {
        if (!Common.getPath().endsWith(File.separator)) {
            Common.setPath(Common.getPath() + File.separator);
        }
        return new File(Common.getPath()).listFiles();
    }

    private void reload(Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                APKExplorer.getData(getFilesList(), false, activity).clear();
                mProgressLayout.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }

            @Override
            public void doInBackground() {
                mRecycleViewAdapter = new InstallerFilePickerAdapter(APKExplorer.getData(getFilesList(), false, activity));
            }

            @Override
            public void onPostExecute() {
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                if (getIntent().getStringExtra(TITLE_INTENT) != null) {
                    mTitle.setText(getIntent().getStringExtra(TITLE_INTENT));
                } else {
                    mTitle.setText(Common.getPath().equals(Environment.getExternalStorageDirectory().toString() + File.separator) ? getString(R.string.sdcard)
                            : new File(Common.getPath()).getName());
                }
                if (Common.getAPKList().isEmpty()) {
                    Common.getSelectCard().setVisibility(View.GONE);
                } else {
                    Common.getSelectCard().setVisibility(View.VISIBLE);
                }
                mRecyclerView.setVisibility(View.VISIBLE);
                mProgressLayout.setVisibility(View.GONE);
            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && Build.VERSION.SDK_INT < 30 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            this.recreate();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Common.isFinished()) {
            Common.setFinishStatus(false);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (Common.getPath().equals(getCacheDir().getPath() + "/splits/")) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(getString(R.string.installation_cancel_message))
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    })
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> finish()).show();
        } else if (Common.getPath().equals(Environment.getExternalStorageDirectory().toString() + File.separator)) {
            super.onBackPressed();
        } else {
            Common.setPath(Objects.requireNonNull(new File(Common.getPath()).getParentFile()).getPath());
            Common.getAPKList().clear();
            reload(this);
        }
    }

}