package com.threethan.questpatcher.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.apk.axml.aXMLDecoder;
import com.apk.axml.aXMLEncoder;
import com.threethan.questpatcher.R;
import com.threethan.questpatcher.fragments.APKExplorerFragment;
import com.threethan.questpatcher.utils.APKExplorer;
import com.threethan.questpatcher.utils.AppData;
import com.threethan.questpatcher.utils.Common;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 25, 2021
 */
public class TextEditorActivity extends AppCompatActivity {

    private static final String find0 = "android:extractNativeLibs=\"false\"";
    private static final String replace0 = "android:extractNativeLibs=\"true\"";
    private static final String find1 = "<application";
    private static final String replace1 = "<uses-permission android:name=\"com.oculus.permission.PLAY_AUDIO_BACKGROUND\"/>\n" +
            "<uses-permission android:name=\"com.oculus.permission.RECORD_AUDIO_BACKGROUND\"/>\n" +
            "<application";
    private static final String check = "com.oculus.permission.RECORD_AUDIO_BACKGROUND";


    private AppCompatEditText mText;
    private LinearLayoutCompat mProgressLayout;
    public static final String PATH_INTENT = "path";
    private String mTextContents = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texteditor);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mSave = findViewById(R.id.save);
        mProgressLayout = findViewById(R.id.progress_layout);
        MaterialTextView mTitle = findViewById(R.id.title);
        mText = findViewById(R.id.text);

        AppData.toggleKeyboard(1, mText, this);

        String mPath = getIntent().getStringExtra(PATH_INTENT);

        mText.setTextColor(sThemeUtils.isDarkTheme(this) ? Color.WHITE : Color.BLACK);

        if (mPath != null) {
            mTitle.setText(new File(mPath).getName());
        }

        new sExecutor() {
            private boolean invalid = false;
            private String text = null;
            @Override
            public void onPreExecute() {
                mProgressLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void doInBackground() {
                assert mPath != null;
                if (APKExplorer.isBinaryXML(mPath)) {
                    try (FileInputStream inputStream = new FileInputStream(mPath)) {
                        text = new aXMLDecoder().decode(inputStream).trim();
                    } catch (Exception e) {
                        invalid = true;
                    }
                } else {
                    text = sFileUtils.read(new File(mPath));
                }
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPostExecute() {

                // CHANGE: PATCH STRING AUTOMATICALLY
                if (text != null) {
                    if (text.contains(check)) {
                        finish();
                        APKExplorerFragment.current.fail();
                        return;
                    } else {
                        text = text.replace(find1, replace1);
                    }

                    text = text.replace(find0, replace0);
                    mText.setText(text);
                    mTextContents = text;
                }
                if (invalid) {
                    assert mPath != null;
                    sCommonUtils.toast(getString(R.string.xml_decode_failed, new File(mPath).getName()), TextEditorActivity.this).show();
                }
                mProgressLayout.setVisibility(View.GONE);
                mSave.callOnClick();
            }
        }.execute();

//        mSave.setVisibility(View.VISIBLE);

        mSave.setOnClickListener(v -> {
            if (mText == null || mText.getText() != null && mText.getText().toString().isEmpty()) return;
            new sExecutor() {
                private boolean invalid = false;
                private final String text = mText.getText().toString().trim();
                @Override
                public void onPreExecute() {
                    mProgressLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void doInBackground() {
                    assert mPath != null;
                    if (APKExplorer.isBinaryXML(mPath)) {
                        if (isXMLValid(text)) {
                            try (FileOutputStream fos = new FileOutputStream(mPath)) {
                                aXMLEncoder aXMLEncoder = new aXMLEncoder();
                                byte[] bs = aXMLEncoder.encodeString(TextEditorActivity.this, text);
                                fos.write(bs);
                            } catch (IOException | XmlPullParserException ignored) {
                            }
                        } else {
                            invalid = true;
                        }
                    } else {
                        sFileUtils.create(text, new File(mPath));
                        if (mPath.contains("classes") && mPath.contains(".dex")) {
                            try {
                                JSONObject jsonObject = new JSONObject(sFileUtils.read(new File(getCacheDir(), Common.getAppID() + "/.aeeBackup/appData")));
                                jsonObject.put("smali_edited", true);
                                sFileUtils.create(jsonObject.toString(), new File(getCacheDir(), Common.getAppID() + "/.aeeBackup/appData"));
                            } catch (JSONException ignored) {
                            }
                        }
                    }
                }

                @Override
                public void onPostExecute() {
                    if (invalid) {
                        sCommonUtils.toast(getString(R.string.xml_corrupted), TextEditorActivity.this).show();
                    }
                    mProgressLayout.setVisibility(View.GONE);
                    finish();
                    APKExplorerFragment.current.succeed();
                }
            }.execute();
        });

        mBack.setOnClickListener(v -> onBackPressed());
    }

    private static boolean isXMLValid(String xmlString) {
        try {
            SAXParserFactory.newInstance().newSAXParser().getXMLReader().parse(new InputSource(new StringReader(xmlString)));
            return true;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return false;
        }
    }

    /** @noinspection deprecation*/
    @Override
    public void onBackPressed() {
        if (mTextContents != null && mText.getText() != null && !mTextContents.equals(mText.getText().toString())) {
            new MaterialAlertDialogBuilder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.text_editor)
                    .setMessage(getString(R.string.discard_message))
                    .setCancelable(false)
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    })
                    .setPositiveButton(R.string.discard, (dialogInterface, i) -> finish()).show();
            return;
        }
        super.onBackPressed();
    }

}