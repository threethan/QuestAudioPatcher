package com.threethan.questpatcher.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.threethan.questpatcher.BuildConfig;
import com.threethan.questpatcher.R;
import com.threethan.questpatcher.adapters.AboutAdapter;
import com.threethan.questpatcher.utils.APKEditorUtils;

import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sSerializableItems;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class AboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_about, container, false);

        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), sCommonUtils.getOrientation(requireActivity()) == Configuration.ORIENTATION_LANDSCAPE ? 3 : 2));
        AboutAdapter mRecycleViewAdapter = new AboutAdapter(getData());
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        return mRootView;
    }

    private List<sSerializableItems> getData() {
        List<sSerializableItems> mData = new ArrayList<>();
        mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.mipmap.ic_launcher, requireActivity()), getString(R.string.app_name), getString(R.string.version, BuildConfig.VERSION_NAME),null));
        mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_github, requireActivity()), getString(R.string.source_code), getString(R.string.report_issue),"https://github.com/threethan/QuestAudioPatcher"));
        return mData;
    }
    
}