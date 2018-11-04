/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package org.telegram.ui.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;

import org.gramity.GramityConstants;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.DrawerActionCell;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

public class DrawerLayoutAdapter extends RecyclerListView.SelectionAdapter {

    private Context mContext;
    private ArrayList<Item> items = new ArrayList<>(21);

    public DrawerLayoutAdapter(Context context) {
        mContext = context;
        Theme.createDialogsResources(context);
        resetItems();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void notifyDataSetChanged() {
        resetItems();
        super.notifyDataSetChanged();
    }

    @Override
    public boolean isEnabled(RecyclerView.ViewHolder holder) {
        return holder.getItemViewType() == 3;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 0:
                view = new DrawerProfileCell(mContext);
                break;
            case 1:
            default:
                view = new EmptyCell(mContext, AndroidUtilities.dp(8));
                break;
            case 2:
                view = new DividerCell(mContext);
                break;
            case 3:
                view = new DrawerActionCell(mContext);
                break;
        }
        view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new RecyclerListView.Holder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 0:
                ((DrawerProfileCell) holder.itemView).setUser(MessagesController.getInstance().getUser(UserConfig.getClientUserId()));
                holder.itemView.setBackgroundColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
                break;
            case 3:
                items.get(position).bind((DrawerActionCell) holder.itemView);
                break;
        }
    }

    @Override
    public int getItemViewType(int i) {
        if (i == 0) {
            return 0;
        } else if (i == 1) {
            return 1;
        } else if (i == 3 || i == 7 || i == 17) { //dividers TODO: change it if update add new row!
            return 2;
        } else if (i == 18 || i == 19) { // official channel and comment us TODO: change it if update add new row!
            if (ApplicationLoader.applicationContext.getPackageName().equals(GramityConstants.TGPPKG)) {
                return 1;
            } else {
                return 3;
            }
        }
        return 3;
    }

    private void resetItems() {
        // TGY
        int itemIconSize = 24;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
        boolean isMonoColored = preferences.getBoolean(GramityConstants.PREF_MONOCOLORED_ICONS, false);
        Drawable drawbleTgyColorful = mContext.getResources().getDrawable(R.drawable.menu_telegram);
        Drawable drawbleTgyMono = mContext.getResources().getDrawable(R.drawable.notification);
        if (isMonoColored) {
            drawbleTgyMono.setColorFilter(Theme.getColor(Theme.key_chats_menuItemIcon), PorterDuff.Mode.SRC_ATOP);
        }
        //
        items.clear();
        if (!UserConfig.isClientActivated()) {
            return;
        }
        items.add(null); // profile 0
        items.add(null); // padding 1
        items.add(new Item(2, LocaleController.getString("ChangeUserAccount", R.string.ChangeUserAccount), new IconicsDrawable(mContext, FontAwesome.Icon.faw_user_circle2).sizeDp(itemIconSize).color(isMonoColored ? Theme.getColor(Theme.key_chats_menuItemIcon) : 0xffd1df47)));
        items.add(null); // DIVIDER 3
        items.add(new Item(4, LocaleController.getString("NewGroup", R.string.NewGroup), new IconicsDrawable(mContext, FontAwesome.Icon.faw_users).sizeDp(itemIconSize).color(isMonoColored ? Theme.getColor(Theme.key_chats_menuItemIcon) : 0xff0c85e6)));
        items.add(new Item(5, LocaleController.getString("NewSecretChat", R.string.NewSecretChat), new IconicsDrawable(mContext, FontAwesome.Icon.faw_lock).sizeDp(itemIconSize).color(isMonoColored ? Theme.getColor(Theme.key_chats_menuItemIcon) : 0xffffa01f)));
        items.add(new Item(6, LocaleController.getString("NewChannel", R.string.NewChannel), new IconicsDrawable(mContext, FontAwesome.Icon.faw_bullhorn).sizeDp(itemIconSize).color(isMonoColored ? Theme.getColor(Theme.key_chats_menuItemIcon) : 0xff832194)));
        items.add(null); // DIVIDER 7
        items.add(new Item(8, LocaleController.getString("DrawerOnlineContacts", R.string.DrawerOnlineContacts), new IconicsDrawable(mContext, CommunityMaterial.Icon.cmd_account_box).sizeDp(itemIconSize).color(isMonoColored ? Theme.getColor(Theme.key_chats_menuItemIcon) : 0xff8efa00)));
        items.add(new Item(9, LocaleController.getString("DrawerMutualContacts", R.string.DrawerMutualContacts), new IconicsDrawable(mContext, CommunityMaterial.Icon.cmd_account_switch).sizeDp(itemIconSize).color(isMonoColored ? Theme.getColor(Theme.key_chats_menuItemIcon) : 0xffff9300)));
        items.add(new Item(10, LocaleController.getString("DrawerIDRevealer", R.string.DrawerIDRevealer), new IconicsDrawable(mContext, FontAwesome.Icon.faw_crosshairs).sizeDp(itemIconSize).color(isMonoColored ? Theme.getColor(Theme.key_chats_menuItemIcon) : 0xff673ab7)));
        items.add(new Item(11, LocaleController.getString("Contacts", R.string.Contacts), new IconicsDrawable(mContext, FontAwesome.Icon.faw_user).sizeDp(itemIconSize).color(isMonoColored ? Theme.getColor(Theme.key_chats_menuItemIcon) : 0xffe0165b)));
        items.add(new Item(12, LocaleController.getString("SavedMessages", R.string.SavedMessages), new IconicsDrawable(mContext, CommunityMaterial.Icon.cmd_bookmark).sizeDp(itemIconSize).color(isMonoColored ? Theme.getColor(Theme.key_chats_menuItemIcon) : 0xfffffc79)));
        items.add(new Item(13, LocaleController.getString("Calls", R.string.Calls), new IconicsDrawable(mContext, FontAwesome.Icon.faw_phone).sizeDp(itemIconSize).color(isMonoColored ? Theme.getColor(Theme.key_chats_menuItemIcon) : 0xff08ff49)));
        items.add(new Item(14, LocaleController.getString("InviteFriends", R.string.InviteFriends), isMonoColored ? drawbleTgyMono : drawbleTgyColorful));
        items.add(new Item(15, LocaleController.getString("Settings", R.string.Settings), new IconicsDrawable(mContext, FontAwesome.Icon.faw_cog).sizeDp(itemIconSize).color(isMonoColored ? Theme.getColor(Theme.key_chats_menuItemIcon) : 0xff845c4e)));
        items.add(new Item(16, LocaleController.getString("AdvancedSettings", R.string.AdvancedSettings), new IconicsDrawable(mContext, FontAwesome.Icon.faw_wrench).sizeDp(itemIconSize).color(isMonoColored ? Theme.getColor(Theme.key_chats_menuItemIcon) : 0xff3f51b5)));
        items.add(null); // DIVIDER 17
        items.add(new Item(18, LocaleController.getString("DrawerOfficialChannel", R.string.DrawerOfficialChannel), new IconicsDrawable(mContext, MaterialDesignIconic.Icon.gmi_tv_list).sizeDp(itemIconSize).color(isMonoColored ? Theme.getColor(Theme.key_chats_menuItemIcon) : 0xffff6433)));
        items.add(new Item(19, LocaleController.getString("DrawerComment", R.string.DrawerComment), new IconicsDrawable(mContext, CommunityMaterial.Icon.cmd_comment_check).sizeDp(itemIconSize).color(isMonoColored ? Theme.getColor(Theme.key_chats_menuItemIcon) : 0xff00a797)));
        items.add(new Item(20, LocaleController.getString("TelegramFaq", R.string.TelegramFaq), new IconicsDrawable(mContext, FontAwesome.Icon.faw_question_circle).sizeDp(itemIconSize).color(isMonoColored ? Theme.getColor(Theme.key_chats_menuItemIcon) : 0xff607d8b)));
    }

    public int getId(int position) {
        if (position < 0 || position >= items.size()) {
            return -1;
        }
        Item item = items.get(position);
        return item != null ? item.id : -1;
    }

    private class Item {
        public Drawable icon;
        public String text;
        public int id;

        public Item(int id, String text, Drawable icon) {
            this.icon = icon;
            this.id = id;
            this.text = text;
        }

        public void bind(DrawerActionCell actionCell) {
            actionCell.setTextAndIcon(text, icon);
        }
    }
}
