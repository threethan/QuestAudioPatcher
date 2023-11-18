package com.threethan.questpatcher;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.viewpager.widget.ViewPager;

import com.threethan.questpatcher.R;
import com.threethan.questpatcher.activities.SettingsActivity;
import com.threethan.questpatcher.fragments.APKsFragment;
import com.threethan.questpatcher.fragments.AboutFragment;
import com.threethan.questpatcher.fragments.ApplicationsFragment;
import com.threethan.questpatcher.fragments.ProjectsFragment;
import com.threethan.questpatcher.utils.Common;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import in.sunilpaulmathew.crashreporter.Utils.CrashReporter;
import in.sunilpaulmathew.sCommon.Adapters.sPagerAdapter;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class MainActivity extends AppCompatActivity {

    private boolean mExit;
    private final Handler mHandler = new Handler();

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set App Language
        sThemeUtils.setLanguage(this);
        setContentView(R.layout.activity_main);

        new CrashReporter("E-Mail: apkeditor@protonmail.com", this).initialize();

        BottomNavigationView mBottomNav = findViewById(R.id.bottom_navigation);
        AppCompatImageButton mSettings = findViewById(R.id.settings_menu);
        ViewPager mViewPager = findViewById(R.id.view_pager);

        sPagerAdapter adapter = new sPagerAdapter(getSupportFragmentManager());

        adapter.AddFragment(new ApplicationsFragment(), null);
        adapter.AddFragment(new ProjectsFragment(), null);
        adapter.AddFragment(new APKsFragment(), null);
        adapter.AddFragment(new AboutFragment(), null);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int i, final float v, final int i2) {
            }
            @Override
            public void onPageSelected(int position) {
                Objects.requireNonNull(mViewPager.getAdapter()).notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(final int i) {
            }
        });

        mViewPager.setAdapter(adapter);

        mBottomNav.setOnItemSelectedListener(
                menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.nav_apps:
                            mViewPager.setCurrentItem(0);
                            break;
                        case R.id.nav_apks:
                            mViewPager.setCurrentItem(2);
                            break;
                        case R.id.nav_about:
                            mViewPager.setCurrentItem(3);
                            break;
                    }
                    Objects.requireNonNull(mViewPager.getAdapter()).notifyDataSetChanged();
                    return false;
                }
        );

        if (savedInstanceState == null) {
            mViewPager.setCurrentItem(0);
        }

        mSettings.setOnClickListener(v -> {
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
        });
    }

    @Override
    public void onBackPressed() {
        if (Common.isBusy()) return;
        if (Common.getSearchWord() != null) {
            if (Common.getAPKsSearchWord() != null && Common.getAPKsSearchWord().getVisibility() == View.VISIBLE) {
                Common.getAPKsSearchWord().setVisibility(View.GONE);
                Common.getAPKsTitle().setVisibility(View.VISIBLE);
                Common.getAPKsSearchWord().setText(null);
            } else if (Common.getAppsSearchWord() != null && Common.getAppsSearchWord().getVisibility() == View.VISIBLE) {
                Common.getAppsSearchWord().setVisibility(View.GONE);
                Common.getAppsTitle().setVisibility(View.VISIBLE);
                Common.getAppsSearchWord().setText(null);
            } else if (Common.getProjectsSearchWord() != null && Common.getProjectsSearchWord().getVisibility() == View.VISIBLE) {
                Common.getProjectsSearchWord().setVisibility(View.GONE);
                Common.getProjectsTitle().setVisibility(View.VISIBLE);
                Common.getProjectsSearchWord().setText(null);
            }
            Common.setSearchWord(null);
            return;
        }
        if (mExit) {
            mExit = false;
            finish();
        } else {
            sCommonUtils.snackBar(findViewById(android.R.id.content), getString(R.string.press_back)).show();
            mExit = true;
            mHandler.postDelayed(() -> mExit = false, 2000);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (Common.getSearchWord() != null) {
            Common.setSearchWord(null);
        }
    }

}