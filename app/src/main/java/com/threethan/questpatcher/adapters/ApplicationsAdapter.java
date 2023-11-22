package com.threethan.questpatcher.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.threethan.questpatcher.R;
import com.threethan.questpatcher.activities.ImageViewActivity;
import com.threethan.questpatcher.utils.APKEditorUtils;
import com.threethan.questpatcher.utils.Common;
import com.threethan.questpatcher.utils.dialogs.ExportOptionsDialog;
import com.threethan.questpatcher.utils.dialogs.SigningOptionsDialog;
import com.threethan.questpatcher.utils.recyclerViewItems.PackageItems;
import com.threethan.questpatcher.utils.tasks.ExploreAPK;
import com.threethan.questpatcher.utils.tasks.ExportApp;
import com.threethan.questpatcher.utils.tasks.ResignAPKs;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class ApplicationsAdapter extends RecyclerView.Adapter<ApplicationsAdapter.ViewHolder> {

    private static List<PackageItems> data;
    private static Activity mActivity;

    public ApplicationsAdapter(List<PackageItems> data, Activity activity) {
        ApplicationsAdapter.data = data;
        ApplicationsAdapter.mActivity = activity;
    }

    @NonNull
    @Override
    public ApplicationsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view, parent, false);
        return new ViewHolder(rowItem);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(@NonNull ApplicationsAdapter.ViewHolder holder, int position) {
        try {
            data.get(position).loadAppIcon(holder.mAppIcon);
            if (Common.getSearchWord() != null && Common.isTextMatched(data.get(position).getPackageName(), Common.getSearchWord())) {
                holder.mAppID.setText(APKEditorUtils.fromHtml(data.get(position).getPackageName().replace(Common.getSearchWord(), "<b><i><font color=\"" +
                        Color.RED + "\">" + Common.getSearchWord() + "</font></i></b>")));
            } else {
                holder.mAppID.setText(data.get(position).getPackageName());
            }
            if (Common.getSearchWord() != null && Common.isTextMatched(data.get(position).getAppName(), Common.getSearchWord())) {
                holder.mAppName.setText(APKEditorUtils.fromHtml(data.get(position).getAppName().replace(Common.getSearchWord(),
                        "<b><i><font color=\"" + Color.RED + "\">" + Common.getSearchWord() + "</font></i></b>")));
            } else {
                holder.mAppName.setText(data.get(position).getAppName());
            }
            holder.mVersion.setText(data.get(position).getAppVersion());
            holder.mCard.setCardBackgroundColor(sThemeUtils.isDarkTheme(holder.mCard.getContext()) ? Color.DKGRAY : Color.LTGRAY);
            holder.mCard.setStrokeColor(sThemeUtils.isDarkTheme(holder.mCard.getContext()) ? Color.DKGRAY : Color.LTGRAY);
            holder.mSize.setVisibility(View.VISIBLE);
            holder.mVersion.setVisibility(View.VISIBLE);
            holder.mCard.setOnLongClickListener(v -> {
                if (sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, v.getContext()) && sCommonUtils.getString("exportAPKsPath", "externalFiles",
                        v.getContext()).equals("internalStorage")) {
                    sPermissionUtils.requestPermission(
                            new String[] {
                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                            },(Activity) v.getContext());
                    return true;
                }
                if (APKEditorUtils.isFullVersion(v.getContext())) {
                    if (sCommonUtils.getString("exportAPKs", null, v.getContext()) == null) {
                        new ExportOptionsDialog(data.get(position).getPackageName(), false, (Activity) v.getContext()).show();
                    } else if (sCommonUtils.getString("exportAPKs", null, v.getContext()).equals(v.getContext().getString(R.string.export_storage))) {
                        new ExportApp(data.get(position).getPackageName(), v.getContext()).execute();
                    } else {
                        if (!sCommonUtils.getBoolean("firstSigning", false, v.getContext())) {
                            new SigningOptionsDialog(data.get(position).getPackageName(), false, v.getContext()).show();
                        } else {
                            new ResignAPKs(data.get(position).getPackageName(), false, false, (Activity) v.getContext()).execute();
                        }
                    }
                } else {
                    new MaterialAlertDialogBuilder(v.getContext())
                            .setTitle(data.get(position).getAppName())
                            .setMessage(v.getContext().getString(R.string.export_app_question, data.get(position).getAppName()))
                            .setNegativeButton(R.string.cancel, (dialog, id) -> {
                            })
                            .setPositiveButton(R.string.export, (dialog, id) ->
                                    new ExportApp(data.get(position).getPackageName(), v.getContext()).execute()
                            ).show();
                }
                return false;
            });
        } catch (NullPointerException ignored) {}
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AppCompatImageButton mAppIcon;
        private final MaterialCardView mCard;
        private final MaterialTextView mAppID, mAppName, mSize, mVersion;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mCard = view.findViewById(R.id.card);
            this.mAppIcon = view.findViewById(R.id.icon);
            this.mAppName = view.findViewById(R.id.title);
            this.mAppID = view.findViewById(R.id.description);
            this.mSize = view.findViewById(R.id.size);
            this.mVersion = view.findViewById(R.id.version);
        }

        @Override
        public void onClick(View view) {
            new ExploreAPK(data.get(getAdapterPosition()).getPackageName(), null, null, view.getContext()).execute();
        }
    }

}