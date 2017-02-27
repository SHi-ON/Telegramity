/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2016.
 */

package org.telegram.ui.Adapters;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.DrawerActionCell;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Cells.EmptyCell;

public class DrawerLayoutAdapter extends BaseAdapter {

    private Context mContext;

    public DrawerLayoutAdapter(Context context) {
        mContext = context;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int i) {
        if (i == 6 || i == 13 || i == 14) {
            try {
                PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                if (pInfo.versionCode % 10 == 5) {
                    return false;
                } else {
                    return true;
                }
            } catch (PackageManager.NameNotFoundException e) {
                FileLog.e("tmessages", e);
            }
        }
        return !(i == 0 || i == 1 || i == 5 || i == 12);
    }

    @Override
    public int getCount() {
        return UserConfig.isClientActivated() ? 16 : 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        int type = getItemViewType(i);
        if (type == 0) {
            if (view == null) {
                view = new DrawerProfileCell(mContext);
            }
            ((DrawerProfileCell) view).setUser(MessagesController.getInstance().getUser(UserConfig.getClientUserId()));
        } else if (type == 1) {
            if (view == null) {
                view = new EmptyCell(mContext, AndroidUtilities.dp(8));
            }
            EmptyCell emptyCell = (EmptyCell) view;
            if (i == 6 || i == 13 || i == 14) {
                emptyCell.setHeight(AndroidUtilities.dp(1));
            }
        } else if (type == 2) {
            if (view == null) {
                view = new DividerCell(mContext);
            }
        } else if (type == 3) {
            if (view == null) {
                view = new DrawerActionCell(mContext);
            }
            DrawerActionCell actionCell = (DrawerActionCell) view;
            if (i == 2) {
                actionCell.setTextAndIcon(LocaleController.getString("NewGroup", R.string.NewGroup), new IconicsDrawable(mContext, FontAwesome.Icon.faw_users).sizePx(48).color(0xff0c85e6));
            } else if (i == 3) {
                actionCell.setTextAndIcon(LocaleController.getString("NewSecretChat", R.string.NewSecretChat), new IconicsDrawable(mContext, FontAwesome.Icon.faw_user_secret).sizePx(48).color(0xff25d025));
            } else if (i == 4) {
                actionCell.setTextAndIcon(LocaleController.getString("NewChannel", R.string.NewChannel), new IconicsDrawable(mContext, FontAwesome.Icon.faw_bullhorn).sizePx(48).color(0xff832194));
            } else if (i == 6) {
                actionCell.setTextAndIcon(LocaleController.getString("DrawerPremium", R.string.DrawerPremium), new IconicsDrawable(mContext, FontAwesome.Icon.faw_star).sizePx(48).color(0xffffa01f));
            } else if (i == 7) {
                actionCell.setTextAndIcon(LocaleController.getString("DrawerIDRevealer", R.string.DrawerIDRevealer), new IconicsDrawable(mContext, FontAwesome.Icon.faw_crosshairs).sizePx(48).color(0xff673ab7));
            } else if (i == 8) {
                actionCell.setTextAndIcon(LocaleController.getString("Contacts", R.string.Contacts), new IconicsDrawable(mContext, FontAwesome.Icon.faw_user).sizePx(48).color(0xffe0165b));
            } else if (i == 9) {
                actionCell.setTextAndIcon(LocaleController.getString("InviteFriends", R.string.InviteFriends), mContext.getResources().getDrawable(R.drawable.menu_telegram));
            } else if (i == 10) {
                actionCell.setTextAndIcon(LocaleController.getString("Settings", R.string.Settings), new IconicsDrawable(mContext, FontAwesome.Icon.faw_cog).sizePx(48).color(0xff845c4e));
            } else if (i == 11) {
                actionCell.setTextAndIcon(LocaleController.getString("DrawerAdvancedSettings", R.string.TelegramitySettings), new IconicsDrawable(mContext, FontAwesome.Icon.faw_wrench).sizePx(48).color(0xff3f51b5));
            } else if (i == 13) {
                actionCell.setTextAndIcon(LocaleController.getString("DrawerOfficialChannel", R.string.DrawerOfficialChannel), new IconicsDrawable(mContext, MaterialDesignIconic.Icon.gmi_tv_list).sizePx(48).color(0xffff6433));
            } else if (i == 14) {
                actionCell.setTextAndIcon(LocaleController.getString("DrawerComment", R.string.DrawerComment), new IconicsDrawable(mContext, CommunityMaterial.Icon.cmd_comment_check).sizePx(48).color(0xff00a797));
            } else if (i == 15) {
                actionCell.setTextAndIcon(LocaleController.getString("TelegramFaq", R.string.TelegramFaq), new IconicsDrawable(mContext, FontAwesome.Icon.faw_question_circle).sizePx(48).color(0xff607d8b));
            }
        }

        return view;
    }

    @Override
    public int getItemViewType(int i) {
        if (i == 0) {
            return 0;
        } else if (i == 1) {
            return 1;
        } else if (i == 5 || i == 12) {
            return 2;
        } else if (i == 6 || i == 13 || i == 14) {
            try {
                PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                if (pInfo.versionCode % 10 == 5) {
                    return 1;
                } else {
                    return 3;
                }
            } catch (PackageManager.NameNotFoundException e) {
                FileLog.e("tmessages", e);
            }
        }
        return 3;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public boolean isEmpty() {
        return !UserConfig.isClientActivated();
    }
}
