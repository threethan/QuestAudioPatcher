package com.threethan.questpatcher.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threethan.questpatcher.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on November 07, 2021
 */
public class APKDetailsAdapter extends RecyclerView.Adapter<APKDetailsAdapter.ViewHolder> {

    private static List<String> data;

    public APKDetailsAdapter(List<String> data) {
        APKDetailsAdapter.data = data;
    }

    @NonNull
    @Override
    public APKDetailsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_apkdetails, parent, false);
        return new APKDetailsAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull APKDetailsAdapter.ViewHolder holder, int position) {
        holder.mText.setText(data.get(position));
        holder.mCard.setCardBackgroundColor(sThemeUtils.isDarkTheme(holder.mCard.getContext()) ? Color.DKGRAY : Color.LTGRAY);
        holder.mCard.setStrokeColor(sThemeUtils.isDarkTheme(holder.mCard.getContext()) ? Color.DKGRAY : Color.LTGRAY);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView mCard;
        private final MaterialTextView mText;

        public ViewHolder(View view) {
            super(view);
            this.mCard = view.findViewById(R.id.card);
            this.mText = view.findViewById(R.id.text);
        }
    }

}