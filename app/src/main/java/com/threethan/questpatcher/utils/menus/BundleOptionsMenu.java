package com.threethan.questpatcher.utils.menus;

import android.view.Menu;
import android.view.View;

import androidx.appcompat.widget.PopupMenu;

import com.apk.editor.R;
import com.threethan.questpatcher.utils.dialogs.ShareBundleDialog;
import com.threethan.questpatcher.utils.tasks.SaveBundletoDownloads;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on February 03, 2023
 */
public class BundleOptionsMenu extends PopupMenu {

    public BundleOptionsMenu(String bundlePath, View view) {
        super(view.getContext(), view);
        Menu menu = getMenu();
        menu.add(Menu.NONE, 0, Menu.NONE, R.string.share);
        menu.add(Menu.NONE, 1, Menu.NONE, R.string.save_to_downloads);
        setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    new ShareBundleDialog(bundlePath, view.getContext()).show();
                    break;
                case 1:
                    new SaveBundletoDownloads(bundlePath, true, view.getContext()).execute();
                    break;
            }
            return false;
        });
        show();
    }

}