/*
 * This is the source code of Telegramity for Android
 *
 * Copyright Shayan Amani, 2016.
 */

package com.ioton;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import org.telegram.messenger.AnimationCompat.AnimatorSetProxy;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextColorCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.AvatarUpdater;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;
import org.telegram.ui.PasscodeActivity;
import org.telegram.ui.PhotoViewer;

import java.util.ArrayList;
import java.util.Locale;

public class TelegramitySettingsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, PhotoViewer.PhotoViewerProvider {

    private ListView listView;
    private ListAdapter listAdapter;
    private BackupImageView avatarImage;
    private TextView nameTextView;
    private TextView onlineTextView;
    private ImageView writeButton;
    private AnimatorSetProxy writeButtonAnimation;
    private AvatarUpdater avatarUpdater = new AvatarUpdater();
    private View extraHeightView;
    private View shadowView;

    private int extraHeight;
    private boolean needRestart;

    private int overscrollRow;
    private int emptyRow;

    private int slySectionRow;
    private int specterModeRow;
    private int hiddenTypingRow;
    private int noNumberRow;
    private int slyDetailRow;
    private int slySectionBottomRow;

    private int dialogsSectionRow;
    private int dialogsHideTabsCheckRow;
    private int dialogsTabsRow;
    private int dialogsTabsHeightRow;
    private int dialogsDisableTabsAnimationCheckRow;
    private int dialogsInfiniteTabsSwipe;
    private int dialogsHideTabsCounters;
    private int dialogsTabsCountersCountChats;
    private int dialogsTabsCountersCountNotMuted;
    private int dialogsSectionBottomRow;

    private int messagesSectionRow;
    private int chatShowDirectShareBtn;
    private int chatDirectShareToMenu;
    private int chatDirectShareFavsFirst;
    private int chatShowDateToastRow;
    private int messagesSectionBottomRow;

    private int customizationsSectionRow;
    private int textFontRow;
    private int textSizeRow;
    private int hideStatusIndicatorCheckRow;
    private int actionBarBackgroundColorRow;
    private int tabsBackgroundColorRow;
    private int drawerHeaderColorRow;
    private int profileBackgroundColorRow;
    private int resetDefaultRow;
//    private int customizationsDetailRow;
    private int customizationsSectionBottomRow;

    private int premiumSecuritySectionRow;
    private int passcodeRow;
//    private int premiumSecurityDetailRow;
    private int premiumSecuritySectionBottomRow;

    private int mediaSectionRow;
    private int profileSharedOptionsRow;
    private int keepOriginalFilenameRow;
    private int mediaDetailRow;
    private int mediaSectionBottomRow;

    private int textDescriptionRow;
    private int rowCount;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        avatarUpdater.parentFragment = this;
        avatarUpdater.delegate = new AvatarUpdater.AvatarUpdaterDelegate() {
            @Override
            public void didUploadedPhoto(TLRPC.InputFile file, TLRPC.PhotoSize small, TLRPC.PhotoSize big) {
                TLRPC.TL_photos_uploadProfilePhoto req = new TLRPC.TL_photos_uploadProfilePhoto();
                req.caption = "";
                req.crop = new TLRPC.TL_inputPhotoCropAuto();
                req.file = file;
                req.geo_point = new TLRPC.TL_inputGeoPointEmpty();
                ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                    @Override
                    public void run(TLObject response, TLRPC.TL_error error) {
                        if (error == null) {
                            TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
                            if (user == null) {
                                user = UserConfig.getCurrentUser();
                                if (user == null) {
                                    return;
                                }
                                MessagesController.getInstance().putUser(user, false);
                            } else {
                                UserConfig.setCurrentUser(user);
                            }
                            TLRPC.TL_photos_photo photo = (TLRPC.TL_photos_photo) response;
                            ArrayList<TLRPC.PhotoSize> sizes = photo.photo.sizes;
                            TLRPC.PhotoSize smallSize = FileLoader.getClosestPhotoSizeWithSize(sizes, 100);
                            TLRPC.PhotoSize bigSize = FileLoader.getClosestPhotoSizeWithSize(sizes, 1000);
                            user.photo = new TLRPC.TL_userProfilePhoto();
                            user.photo.photo_id = photo.photo.id;
                            if (smallSize != null) {
                                user.photo.photo_small = smallSize.location;
                            }
                            if (bigSize != null) {
                                user.photo.photo_big = bigSize.location;
                            } else if (smallSize != null) {
                                user.photo.photo_small = smallSize.location;
                            }
                            MessagesStorage.getInstance().clearUserPhotos(user.id);
                            ArrayList<TLRPC.User> users = new ArrayList<>();
                            users.add(user);
                            MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_ALL);
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.mainUserInfoChanged);
                                    UserConfig.saveConfig(true);
                                }
                            });
                        }
                    }
                });
            }
        };
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.refreshTabs);

        rowCount = 0;
        overscrollRow = rowCount++;
        emptyRow = rowCount++;

        slySectionRow = rowCount++;
        specterModeRow = rowCount++;
        hiddenTypingRow = rowCount++;
        noNumberRow = rowCount++;
        slyDetailRow = rowCount++;
        slySectionBottomRow = rowCount++;

        dialogsSectionRow = rowCount++;
        dialogsHideTabsCheckRow = rowCount++;
        dialogsTabsRow = rowCount++;
        dialogsTabsHeightRow = rowCount++;
        dialogsDisableTabsAnimationCheckRow = rowCount++;
        dialogsInfiniteTabsSwipe = rowCount++;
        dialogsHideTabsCounters = rowCount++;
        dialogsTabsCountersCountNotMuted = rowCount++;
        dialogsTabsCountersCountChats = rowCount++;
        dialogsSectionBottomRow = rowCount++;

        messagesSectionRow = rowCount++;
        chatDirectShareToMenu = rowCount++;
        chatShowDirectShareBtn = rowCount++;
        chatDirectShareFavsFirst = rowCount++;
        chatShowDateToastRow = rowCount++;
        messagesSectionBottomRow = rowCount++;

        customizationsSectionRow = rowCount++;
        textFontRow = rowCount++;
        textSizeRow = rowCount++;
        hideStatusIndicatorCheckRow = rowCount++;
        actionBarBackgroundColorRow = rowCount++;
        tabsBackgroundColorRow = rowCount++;
        drawerHeaderColorRow = rowCount++;
        profileBackgroundColorRow = rowCount++;
        resetDefaultRow = rowCount++;
//        customizationsDetailRow = rowCount++;
        customizationsSectionBottomRow = rowCount++;

        premiumSecuritySectionRow = rowCount++;
        passcodeRow = rowCount++;
//        premiumSecurityDetailRow = rowCount++;
        premiumSecuritySectionBottomRow = rowCount++;

        mediaSectionRow = rowCount++;
        profileSharedOptionsRow = rowCount++;
        keepOriginalFilenameRow = rowCount++;
        mediaDetailRow = rowCount++;
        mediaSectionBottomRow = rowCount++;

        textDescriptionRow = rowCount++;

        MessagesController.getInstance().loadFullUser(UserConfig.getCurrentUser(), classGuid, true);

        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.refreshTabs);

        if (needRestart) {
            TelegramityUtilities.restartTelegramity();
        }
    }

    @Override
    public View createView(final Context context) {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);
        final int instanceOfActionBarBackgroundColor = preferences.getInt("actionBarBackgroundColor", TelegramityUtilities.colorABBG());
        final int instanceOfTabsBackgroundColor = preferences.getInt("tabsBackgroundColor", TelegramityUtilities.colorTH());
        final int instanceOfProfileBackgroundColor = preferences.getInt("profileBackgroundColor", TelegramityUtilities.colorPBG());
        final int instanceOfDrawerHeaderColor = preferences.getInt("drawerHeaderColor", TelegramityUtilities.colorDH());
        needRestart = false;
        actionBar.setBackgroundColor(instanceOfProfileBackgroundColor);
//        actionBar.setItemsBackgroundColor(AvatarDrawable.getButtonColorForId(5));
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAddToContainer(false);
        extraHeight = 88;
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context) {
            @Override
            protected boolean drawChild(@NonNull Canvas canvas, @NonNull View child, long drawingTime) {
                if (child == listView) {
                    boolean result = super.drawChild(canvas, child, drawingTime);
                    if (parentLayout != null) {
                        int actionBarHeight = 0;
                        int childCount = getChildCount();
                        for (int a = 0; a < childCount; a++) {
                            View view = getChildAt(a);
                            if (view == child) {
                                continue;
                            }
                            if (view instanceof ActionBar && view.getVisibility() == VISIBLE) {
                                if (((ActionBar) view).getCastShadows()) {
                                    actionBarHeight = view.getMeasuredHeight();
                                }
                                break;
                            }
                        }
                        parentLayout.drawHeaderShadow(canvas, actionBarHeight);
                    }
                    return result;
                } else {
                    return super.drawChild(canvas, child, drawingTime);
                }
            }
        };
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new ListView(context);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setVerticalScrollBarEnabled(true);
        AndroidUtilities.setListViewEdgeEffectColor(listView, instanceOfProfileBackgroundColor);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, final int i, long l) {
                final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);
                if (i == specterModeRow) {
                    boolean specter = preferences.getBoolean("specterMode", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("specterMode", !specter);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!specter);
                        if (!specter) {
                            onlineTextView.setText(LocaleController.getString("SpecterOnline", R.string.SpecterOnline));
                        } else {
                            onlineTextView.setText(LocaleController.getString("Online", R.string.Online));
                        }
                    }
                    MessagesController.getInstance().reRunUpdateTimerProc();
                } else if (i == hiddenTypingRow) {
                    boolean typing = preferences.getBoolean("hiddenTyping", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hiddenTyping", !typing);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!typing);
                    }
                } else if (i == noNumberRow) {
                    boolean number = preferences.getBoolean("noNumber", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("noNumber", !number);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!number);
                    }
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.mainUserInfoChanged);
                } else if (i == dialogsHideTabsCheckRow) {
                    boolean hide = preferences.getBoolean("hideTabs", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hideTabs", !hide);
                    editor.apply();

                    boolean hideUsers = preferences.getBoolean("hideUsers", false);
                    boolean hideGroups = preferences.getBoolean("hideGroups", false);
                    boolean hideSGroups = preferences.getBoolean("hideSGroups", false);
                    boolean hideChannels = preferences.getBoolean("hideChannels", false);
                    boolean hideBots = preferences.getBoolean("hideBots", false);
                    boolean hideFavs = preferences.getBoolean("hideFavs", false);
                    if (hideUsers && hideGroups && hideSGroups && hideChannels && hideBots && hideFavs) {
                        //editor.putBoolean("hideUsers", false).apply();
                        //editor.putBoolean("hideGroups", false).apply();
                        if (listView != null) {
                            listView.invalidateViews();
                        }
                    }
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 10);
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!hide);
                    }
                } else if (i == dialogsTabsRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    createTabsDialog(builder);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 13);
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    showDialog(builder.create());
                } else if (i == dialogsTabsHeightRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("TabsHeight", R.string.TabsHeight));
                    final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                    numberPicker.setMinValue(30);
                    numberPicker.setMaxValue(48);
                    numberPicker.setValue(preferences.getInt("tabsHeight", AndroidUtilities.isTablet() ? 46 : 44));
                    builder.setView(numberPicker);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("tabsHeight", numberPicker.getValue());
                            editor.apply();
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 12);
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    showDialog(builder.create());
                } else if (i == dialogsDisableTabsAnimationCheckRow) {
                    boolean disable = preferences.getBoolean("disableTabsAnimation", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("disableTabsAnimation", !disable);
                    editor.apply();
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 11);
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!disable);
                    }
                } else if (i == dialogsInfiniteTabsSwipe) {
                    boolean disable = preferences.getBoolean("infiniteTabsSwipe", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("infiniteTabsSwipe", !disable);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!disable);
                    }
                } else if (i == dialogsHideTabsCounters) {
                    boolean disable = preferences.getBoolean("hideTabsCounters", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hideTabsCounters", !disable);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!disable);
                    }
                } else if (i == dialogsTabsCountersCountChats) {
                    boolean disable = preferences.getBoolean("tabsCountersCountChats", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("tabsCountersCountChats", !disable);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!disable);
                    }
                } else if (i == dialogsTabsCountersCountNotMuted) {
                    boolean disable = preferences.getBoolean("tabsCountersCountNotMuted", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("tabsCountersCountNotMuted", !disable);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!disable);
                    }
                } else if (i == chatShowDirectShareBtn) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    createDialog(builder, chatShowDirectShareBtn);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    showDialog(builder.create());
                } else if (i == chatDirectShareToMenu) {
                    boolean send = preferences.getBoolean("directShareToMenu", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("directShareToMenu", !send);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!send);
                    }
                } else if (i == chatDirectShareFavsFirst) {
                    boolean send = preferences.getBoolean("directShareFavsFirst", true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("directShareFavsFirst", !send);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!send);
                    }
                } else if (i == chatShowDateToastRow) {
                    boolean show = preferences.getBoolean("showDateToast", true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("showDateToast", !show);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!show);
                    }
                } else if (i == textFontRow) {
                    presentFragment(new FontSelectActivity());
                } else if (i == textSizeRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    final SharedPreferences.Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE).edit();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    TextView titleTextView = new TextView(context);
                    titleTextView.setText(LocaleController.getString("TextSize", R.string.TextSize));
                    titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    titleTextView.setTextColor(instanceOfActionBarBackgroundColor);
                    titleTextView.setTypeface(AndroidUtilities.getTypeface());
                    titleTextView.setPadding(24, 18, 24, 0);
                    builder.setCustomTitle(titleTextView);
                    final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                    numberPicker.setMinValue(12);
                    numberPicker.setMaxValue(30);
                    numberPicker.setValue(MessagesController.getInstance().fontSize);
                    builder.setView(numberPicker);
                    builder.setPositiveButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editor.putInt("font_size", numberPicker.getValue());
                            MessagesController.getInstance().fontSize = numberPicker.getValue();
                            editor.commit();
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    builder.setNeutralButton(LocaleController.getString("Default", R.string.Default), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            editor.putInt("font_size", AndroidUtilities.isTablet() ? 18 : 16);
                            MessagesController.getInstance().fontSize = AndroidUtilities.isTablet() ? 18 : 16;
                            editor.commit();
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    showDialog(builder.create());
                } else if (i == hideStatusIndicatorCheckRow) {
                    boolean disable = preferences.getBoolean("chatsHideStatusIndicator", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("chatsHideStatusIndicator", !disable);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!disable);
                    }
                } else if (i == actionBarBackgroundColorRow || i == tabsBackgroundColorRow || i == profileBackgroundColorRow || i == drawerHeaderColorRow) {
                    if (getParentActivity() == null) {
                        return;
                    }

                    LinearLayout linearLayout = new LinearLayout(getParentActivity());
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    final ColorPicker colorPicker = new ColorPicker(getParentActivity());
                    SVBar svBar = new SVBar(getParentActivity());
                    /*OpacityBar opacityBar = new OpacityBar(getParentActivity());
                    SaturationBar saturationBar = new SaturationBar(getParentActivity());
                    ValueBar valueBar = new ValueBar(getParentActivity());*/
                    linearLayout.addView(colorPicker, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
                    linearLayout.addView(svBar, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
/*                    linearLayout.addView(opacityBar, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
                    linearLayout.addView(saturationBar, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
                    linearLayout.addView(valueBar, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));*/
                    colorPicker.addSVBar(svBar);
/*                    colorPicker.addOpacityBar(opacityBar);
                    colorPicker.addSaturationBar(saturationBar);
                    colorPicker.addValueBar(valueBar);*/
//                    ColorPickerDialogBuilder cPDB = ColorPickerDialogBuilder.with(context);

                    if (i == actionBarBackgroundColorRow) {
                        colorPicker.setOldCenterColor(instanceOfActionBarBackgroundColor);
//                        cPDB.initialColor(initialActionBarBackgroundColor);
                    } else if (i == tabsBackgroundColorRow) {
                        colorPicker.setOldCenterColor(instanceOfTabsBackgroundColor);
//                        cPDB.initialColor(instanceOfTabsBackgroundColor);
                    } else if (i == profileBackgroundColorRow) {
                        colorPicker.setOldCenterColor(instanceOfProfileBackgroundColor);
//                        cPDB.initialColor(instanceOfProfileBackgroundColor);
                    } else if (i == drawerHeaderColorRow) {
                        colorPicker.setOldCenterColor(instanceOfDrawerHeaderColor);
//                        cPDB.initialColor(instanceOfDrawerHeaderColor);
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    TextView titleTextView = new TextView(context);
                    titleTextView.setText(LocaleController.getString("ChooseColor", R.string.ChooseColor));
                    titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    titleTextView.setTextColor(instanceOfActionBarBackgroundColor);
                    titleTextView.setTypeface(AndroidUtilities.getTypeface());
                    titleTextView.setPadding(24, 18, 24, 0);
                    builder.setCustomTitle(titleTextView)
                            .setView(linearLayout)
                            .setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    SharedPreferences.Editor editor = preferences.edit();
                                    TextColorCell textCell = (TextColorCell) view;
                                    if (i == actionBarBackgroundColorRow) {
                                        editor.putInt("actionBarBackgroundColor", colorPicker.getColor());
                                        textCell.setTextAndColor(LocaleController.getString("itemActionBarBackgroundColor", R.string.itemActionBarBackgroundColor), colorPicker.getColor(), true);
                                        needRestart = true;
                                        toast(context, LocaleController.getString("ThemeChangeToastMessage", R.string.ThemeChangeToastMessage));
                                    } else if (i == tabsBackgroundColorRow) {
                                        editor.putInt("tabsBackgroundColor", colorPicker.getColor());
                                        textCell.setTextAndColor(LocaleController.getString("itemTabsBackgroundColor", R.string.itemTabsBackgroundColor), colorPicker.getColor(), true);
                                        needRestart = true;
                                        toast(context, LocaleController.getString("ThemeChangeToastMessage", R.string.ThemeChangeToastMessage));
                                    } else if (i == drawerHeaderColorRow) {
                                        editor.putInt("drawerHeaderColor", colorPicker.getColor());
                                        textCell.setTextAndColor(LocaleController.getString("itemDrawerHeaderColor", R.string.itemDrawerHeaderColor), colorPicker.getColor(), true);
                                        needRestart = true;
                                        toast(context, LocaleController.getString("ThemeChangeToastMessage", R.string.ThemeChangeToastMessage));
                                    } else if (i == profileBackgroundColorRow) {
                                        editor.putInt("profileBackgroundColor", colorPicker.getColor());
                                        textCell.setTextAndColor(LocaleController.getString("itemProfileBackgroundColor", R.string.itemProfileBackgroundColor), colorPicker.getColor(), true);
                                    }
                                    editor.apply();
                                }
                            })
                            .setNeutralButton(LocaleController.getString("Cancel", R.string.Cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    showDialog(builder.create());

                    /*cPDB
                            .setTitle(LocaleController.getString("ChooseColor", R.string.ChooseColor))
                            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                            .lightnessSliderOnly()
                            .density(14)
                            .setPositiveButton(LocaleController.getString("ThemeDialogSetButton", R.string.ThemeDialogSetButton), new ColorPickerClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                    SharedPreferences.Editor editor = preferences.edit();
                                    TextColorCell textCell = (TextColorCell) view;
                                    if (i == actionBarBackgroundColorRow) {
                                        editor.putInt("actionBarBackgroundColor", selectedColor);
                                        textCell.setTextAndColor(LocaleController.getString("itemActionBarBackgroundColor", R.string.itemActionBarBackgroundColor), selectedColor, true);
                                        toast(context, LocaleController.getString("ThemeChangeToastMessage", R.string.ThemeChangeToastMessage));
                                    } else if (i == tabsBackgroundColorRow) {
                                        editor.putInt("tabsBackgroundColorRow", selectedColor);
                                        textCell.setTextAndColor(LocaleController.getString("itemTabsBackgroundColor", R.string.itemTabsBackgroundColor), selectedColor, true);
                                        toast(context, LocaleController.getString("ThemeChangeToastMessage", R.string.ThemeChangeToastMessage));
                                    } else if (i == drawerHeaderColorRow) {
                                        editor.putInt("drawerHeaderColor", selectedColor);
                                        textCell.setTextAndColor(LocaleController.getString("itemDrawerHeaderColor", R.string.itemDrawerHeaderColor), selectedColor, true);
                                        toast(context, LocaleController.getString("ThemeChangeToastMessage", R.string.ThemeChangeToastMessage));
                                    } else if (i == profileBackgroundColorRow) {
                                        editor.putInt("profileBackgroundColor", selectedColor);
                                        textCell.setTextAndColor(LocaleController.getString("itemProfileBackgroundColor", R.string.itemProfileBackgroundColor), selectedColor, true);
                                    }
                                    editor.apply();
//                                    listView.invalidateViews();
                                }
                            })
                            .setNegativeButton(LocaleController.getString("ThemeDialogCancelButton", R.string.ThemeDialogCancelButton), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .build()
                            .show();*/
                } else if (i == resetDefaultRow) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(LocaleController.getString("resetDefaultMessage", R.string.resetDefaultMessage));
                    TextView titleTextView = new TextView(context);
                    titleTextView.setText(LocaleController.getString("AppName", R.string.AppName));
                    titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    titleTextView.setTextColor(instanceOfActionBarBackgroundColor);
                    titleTextView.setTypeface(AndroidUtilities.getTypeface());
                    titleTextView.setPadding(24, 18, 24, 0);
                    builder.setCustomTitle(titleTextView);
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("actionBarBackgroundColor", TelegramityUtilities.colorABBG());
                            editor.putInt("tabsBackgroundColor", TelegramityUtilities.colorTH());
                            editor.putInt("drawerHeaderColor", TelegramityUtilities.colorDH());
                            editor.putInt("profileBackgroundColor", TelegramityUtilities.colorPBG());
                            editor.apply();
                            needRestart = true;
                            toast(context, LocaleController.getString("ThemeChangeToastMessage", R.string.ThemeChangeToastMessage));
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    showDialog(builder.create());
                } else if (i == passcodeRow) {
                    if (UserConfig.passcodeHash.length() > 0) {
                        presentFragment(new PasscodeActivity(2));
                    } else {
                        presentFragment(new PasscodeActivity(0));
                    }
                } else if (i == profileSharedOptionsRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    createSharedOptions(builder);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 13);
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    showDialog(builder.create());
                } else if (i == keepOriginalFilenameRow) {
                    boolean keep = preferences.getBoolean("keepOriginalFilename", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("keepOriginalFilename", !keep);
                    editor.apply();
                    ApplicationLoader.KEEP_ORIGINAL_FILENAME = !keep;
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!keep);
                    }
                }

            }
        });

        frameLayout.addView(actionBar);

        extraHeightView = new View(context);
        ViewProxy.setPivotY(extraHeightView, 0);
        extraHeightView.setBackgroundColor(instanceOfProfileBackgroundColor);
        frameLayout.addView(extraHeightView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 88));

        shadowView = new View(context);
        shadowView.setBackgroundResource(R.drawable.header_shadow);
        frameLayout.addView(shadowView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 3));

        avatarImage = new BackupImageView(context);
        avatarImage.setRoundRadius(AndroidUtilities.dp(21));
        ViewProxy.setPivotX(avatarImage, 0);
        ViewProxy.setPivotY(avatarImage, 0);
        frameLayout.addView(avatarImage, LayoutHelper.createFrame(42, 42, Gravity.TOP | Gravity.LEFT, 64, 0, 0, 0));
        avatarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
                if (user != null && user.photo != null && user.photo.photo_big != null) {
                    PhotoViewer.getInstance().setParentActivity(getParentActivity());
                    PhotoViewer.getInstance().openPhoto(user.photo.photo_big, TelegramitySettingsActivity.this);
                }
            }
        });

        nameTextView = new TextView(context);
        nameTextView.setTextColor(0xffffffff);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        nameTextView.setGravity(Gravity.LEFT);
        nameTextView.setTypeface(AndroidUtilities.getTypeface());
        ViewProxy.setPivotX(nameTextView, 0);
        ViewProxy.setPivotY(nameTextView, 0);
        frameLayout.addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118, 0, 48, 0));

        onlineTextView = new TextView(context);
        onlineTextView.setTextColor(AvatarDrawable.getProfileTextColorForId(5));
        onlineTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        onlineTextView.setTypeface(AndroidUtilities.getTypeface());
        onlineTextView.setLines(1);
        onlineTextView.setMaxLines(1);
        onlineTextView.setSingleLine(true);
        onlineTextView.setEllipsize(TextUtils.TruncateAt.END);
        onlineTextView.setGravity(Gravity.LEFT);
        frameLayout.addView(onlineTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118, 0, 48, 0));

        writeButton = new ImageView(context);
        writeButton.setBackgroundResource(R.drawable.floating_user_states);
        writeButton.setImageDrawable(new IconicsDrawable(context, FontAwesome.Icon.faw_camera).sizePx(40).color(0xff8e6455)); //Brown +2
        writeButton.setScaleType(ImageView.ScaleType.CENTER);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(writeButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(writeButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            writeButton.setStateListAnimator(animator);
            writeButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        frameLayout.addView(writeButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT | Gravity.TOP, 0, 0, 16, 0));
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getParentActivity() == null) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());

                CharSequence[] items;

                TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
                if (user == null) {
                    user = UserConfig.getCurrentUser();
                }
                if (user == null) {
                    return;
                }
                boolean fullMenu = false;
                if (user.photo != null && user.photo.photo_big != null && !(user.photo instanceof TLRPC.TL_userProfilePhotoEmpty)) {
                    items = new CharSequence[]{LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley), LocaleController.getString("DeletePhoto", R.string.DeletePhoto)};
                    fullMenu = true;
                } else {
                    items = new CharSequence[]{LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley)};
                }

                final boolean full = fullMenu;
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            avatarUpdater.openCamera();
                        } else if (i == 1) {
                            avatarUpdater.openGallery();
                        } else if (i == 2) {
                            MessagesController.getInstance().deleteUserPhoto(null);
                        }
                    }
                });
                showDialog(builder.create());
            }
        });              // Camera icon on the top - End

        needLayout();

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount == 0) {
                    return;
                }
                int height = 0;
                View child = view.getChildAt(0);
                if (child != null) {
                    if (firstVisibleItem == 0) {
                        height = AndroidUtilities.dp(88) + (child.getTop() < 0 ? child.getTop() : 0);
                    }
                    if (extraHeight != height) {
                        extraHeight = height;
                        needLayout();
                    }
                }
            }
        });

        return fragmentView;
    }

    private AlertDialog.Builder createTabsDialog(AlertDialog.Builder builder) {
        builder.setTitle(LocaleController.getString("HideShowTabs", R.string.HideShowTabs));

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);
        boolean hideUsers = preferences.getBoolean("hideUsers", false);
        boolean hideGroups = preferences.getBoolean("hideGroups", false);
        boolean hideSGroups = preferences.getBoolean("hideSGroups", false);
        boolean hideChannels = preferences.getBoolean("hideChannels", false);
        boolean hideBots = preferences.getBoolean("hideBots", false);
        boolean hideFavs = preferences.getBoolean("hideFavs", false);

        builder.setMultiChoiceItems(
                new CharSequence[]{LocaleController.getString("Users", R.string.Users), LocaleController.getString("Groups", R.string.Groups), LocaleController.getString("SuperGroups", R.string.SuperGroups), LocaleController.getString("Channels", R.string.Channels), LocaleController.getString("Bots", R.string.Bots), LocaleController.getString("Favorites", R.string.Favorites)},
                new boolean[]{!hideUsers, !hideGroups, !hideSGroups, !hideChannels, !hideBots, !hideFavs},
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();

                        if (which == 0) {
                            editor.putBoolean("hideUsers", !isChecked);
                        } else if (which == 1) {
                            editor.putBoolean("hideGroups", !isChecked);
                        } else if (which == 2) {
                            editor.putBoolean("hideSGroups", !isChecked);
                        } else if (which == 3) {
                            editor.putBoolean("hideChannels", !isChecked);
                        } else if (which == 4) {
                            editor.putBoolean("hideBots", !isChecked);
                        } else if (which == 5) {
                            editor.putBoolean("hideFavs", !isChecked);
                        }
                        editor.apply();

                        boolean hideUsers = preferences.getBoolean("hideUsers", false);
                        boolean hideGroups = preferences.getBoolean("hideGroups", false);
                        boolean hideSGroups = preferences.getBoolean("hideSGroups", false);
                        boolean hideChannels = preferences.getBoolean("hideChannels", false);
                        boolean hideBots = preferences.getBoolean("hideBots", false);
                        boolean hideFavs = preferences.getBoolean("hideFavs", false);
                        if (hideUsers && hideGroups && hideSGroups && hideChannels && hideBots && hideFavs) {
                            editor.putBoolean("hideTabs", true);
                            editor.apply();
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, which);
                    }
                });
        return builder;
    }

    private AlertDialog.Builder createDialog(AlertDialog.Builder builder, int i) {
        if (i == chatShowDirectShareBtn) {
            builder.setTitle(LocaleController.getString("ShowDirectShareButton", R.string.ShowDirectShareButton));

            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);
            //SharedPreferences mainPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
            boolean showDSBtnUsers = preferences.getBoolean("showDSBtnUsers", true);
            boolean showDSBtnGroups = preferences.getBoolean("showDSBtnGroups", true);
            boolean showDSBtnSGroups = preferences.getBoolean("showDSBtnSGroups", true);
            boolean showDSBtnChannels = preferences.getBoolean("showDSBtnChannels", true);
            boolean showDSBtnBots = preferences.getBoolean("showDSBtnBots", true);

            builder.setMultiChoiceItems(
                    new CharSequence[]{LocaleController.getString("Users", R.string.Users), LocaleController.getString("Groups", R.string.Groups), LocaleController.getString("SuperGroups", R.string.SuperGroups), LocaleController.getString("Channels", R.string.Channels), LocaleController.getString("Bots", R.string.Bots)},
                    new boolean[]{showDSBtnUsers, showDSBtnGroups, showDSBtnSGroups, showDSBtnChannels, showDSBtnBots},
                    new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            //Log.e("createDialog","which " + which + " isChecked " + isChecked);
                            if (which == 0) {
                                editor.putBoolean("showDSBtnUsers", isChecked);
                            } else if (which == 1) {
                                editor.putBoolean("showDSBtnGroups", isChecked);
                            } else if (which == 2) {
                                editor.putBoolean("showDSBtnSGroups", isChecked);
                            } else if (which == 3) {
                                editor.putBoolean("showDSBtnChannels", isChecked);
                            } else if (which == 4) {
                                editor.putBoolean("showDSBtnBots", isChecked);
                            }
                            editor.apply();
                        }
                    });
        }
        return builder;
    }

    private AlertDialog.Builder createSharedOptions(AlertDialog.Builder builder) {
        builder.setTitle(LocaleController.getString("SharedMedia", R.string.SharedMedia));

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);
        boolean hideMedia = preferences.getBoolean("hideSharedMedia", false);
        boolean hideFiles = preferences.getBoolean("hideSharedFiles", false);
        boolean hideMusic = preferences.getBoolean("hideSharedMusic", false);
        boolean hideLinks = preferences.getBoolean("hideSharedLinks", false);
        CharSequence[] cs = BuildVars.DEBUG_VERSION ? new CharSequence[]{LocaleController.getString("SharedMediaTitle", R.string.SharedMediaTitle), LocaleController.getString("DocumentsTitle", R.string.DocumentsTitle), LocaleController.getString("AudioTitle", R.string.AudioTitle), LocaleController.getString("LinksTitle", R.string.LinksTitle)} :
                new CharSequence[]{LocaleController.getString("SharedMediaTitle", R.string.SharedMediaTitle), LocaleController.getString("DocumentsTitle", R.string.DocumentsTitle), LocaleController.getString("AudioTitle", R.string.AudioTitle)};
        boolean[] b = BuildVars.DEBUG_VERSION ? new boolean[]{!hideMedia, !hideFiles, !hideMusic, !hideLinks} :
                new boolean[]{!hideMedia, !hideFiles, !hideMusic};
        builder.setMultiChoiceItems(cs, b,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();

                        if (which == 0) {
                            editor.putBoolean("hideSharedMedia", !isChecked);
                        } else if (which == 1) {
                            editor.putBoolean("hideSharedFiles", !isChecked);
                        } else if (which == 2) {
                            editor.putBoolean("hideSharedMusic", !isChecked);
                        } else if (which == 3) {
                            editor.putBoolean("hideSharedLinks", !isChecked);
                        }
                        editor.apply();
                    }
                });
        return builder;
    }

    @Override
    protected void onDialogDismiss(Dialog dialog) {
        MediaController.getInstance().checkAutodownloadSettings();
    }

    @Override
    public void updatePhotoAtIndex(int index) {

    }

    @Override
    public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
        if (fileLocation == null) {
            return null;
        }
        TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
        if (user != null && user.photo != null && user.photo.photo_big != null) {
            TLRPC.FileLocation photoBig = user.photo.photo_big;
            if (photoBig.local_id == fileLocation.local_id && photoBig.volume_id == fileLocation.volume_id && photoBig.dc_id == fileLocation.dc_id) {
                int coords[] = new int[2];
                avatarImage.getLocationInWindow(coords);
                PhotoViewer.PlaceProviderObject object = new PhotoViewer.PlaceProviderObject();
                object.viewX = coords[0];
                object.viewY = coords[1] - AndroidUtilities.statusBarHeight;
                object.parentView = avatarImage;
                object.imageReceiver = avatarImage.getImageReceiver();
                object.user_id = UserConfig.getClientUserId();
                object.thumb = object.imageReceiver.getBitmap();
                object.size = -1;
                object.radius = avatarImage.getImageReceiver().getRoundRadius();
                object.scale = ViewProxy.getScaleX(avatarImage);
                return object;
            }
        }
        return null;
    }

    @Override
    public Bitmap getThumbForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
        return null;
    }

    @Override
    public void willSwitchFromPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
    }

    @Override
    public void willHidePhotoViewer() {
        avatarImage.getImageReceiver().setVisible(true, true);
    }

    @Override
    public boolean isPhotoChecked(int index) {
        return false;
    }

    @Override
    public void setPhotoChecked(int index) {
    }

    @Override
    public boolean cancelButtonPressed() {
        return true;
    }

    @Override
    public void sendButtonPressed(int index) {
    }

    @Override
    public int getSelectedCount() {
        return 0;
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        avatarUpdater.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void saveSelfArgs(Bundle args) {
        if (avatarUpdater != null && avatarUpdater.currentPicturePath != null) {
            args.putString("path", avatarUpdater.currentPicturePath);
        }
    }

    @Override
    public void restoreSelfArgs(Bundle args) {
        if (avatarUpdater != null) {
            avatarUpdater.currentPicturePath = args.getString("path");
        }
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.updateInterfaces) {
            int mask = (Integer) args[0];
            if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_NAME) != 0) {
                updateUserData();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        updateUserData();
        fixLayout();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    private void needLayout() {
        FrameLayout.LayoutParams layoutParams;
        int newTop = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight();
        if (listView != null) {
            layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
            if (layoutParams.topMargin != newTop) {
                layoutParams.topMargin = newTop;
                listView.setLayoutParams(layoutParams);
                ViewProxy.setTranslationY(extraHeightView, newTop);
            }
        }

        if (avatarImage != null) {
            float diff = extraHeight / (float) AndroidUtilities.dp(88);
            ViewProxy.setScaleY(extraHeightView, diff);
            ViewProxy.setTranslationY(shadowView, newTop + extraHeight);

            if (Build.VERSION.SDK_INT < 11) {
                layoutParams = (FrameLayout.LayoutParams) writeButton.getLayoutParams();
                layoutParams.topMargin = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() + extraHeight - AndroidUtilities.dp(29.5f);
                writeButton.setLayoutParams(layoutParams);
            } else {
                ViewProxy.setTranslationY(writeButton, (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() + extraHeight - AndroidUtilities.dp(29.5f));
            }

            final boolean setVisible = diff > 0.2f;
            boolean currentVisible = writeButton.getTag() == null;
            if (setVisible != currentVisible) {
                if (setVisible) {
                    writeButton.setTag(null);
                    writeButton.setVisibility(View.VISIBLE);
                } else {
                    writeButton.setTag(0);
                }
                if (writeButtonAnimation != null) {
                    AnimatorSetProxy old = writeButtonAnimation;
                    writeButtonAnimation = null;
                    old.cancel();
                }
                writeButtonAnimation = new AnimatorSetProxy();
                if (setVisible) {
                    writeButtonAnimation.setInterpolator(new DecelerateInterpolator());
                    writeButtonAnimation.playTogether(
                            ObjectAnimatorProxy.ofFloat(writeButton, "scaleX", 1.0f),
                            ObjectAnimatorProxy.ofFloat(writeButton, "scaleY", 1.0f),
                            ObjectAnimatorProxy.ofFloat(writeButton, "alpha", 1.0f)
                    );
                } else {
                    writeButtonAnimation.setInterpolator(new AccelerateInterpolator());
                    writeButtonAnimation.playTogether(
                            ObjectAnimatorProxy.ofFloat(writeButton, "scaleX", 0.2f),
                            ObjectAnimatorProxy.ofFloat(writeButton, "scaleY", 0.2f),
                            ObjectAnimatorProxy.ofFloat(writeButton, "alpha", 0.0f)
                    );
                }
                writeButtonAnimation.setDuration(150);
                writeButtonAnimation.addListener(new AnimatorListenerAdapterProxy() {
                    @Override
                    public void onAnimationEnd(Object animation) {
                        if (writeButtonAnimation != null && writeButtonAnimation.equals(animation)) {
                            writeButton.clearAnimation();
                            writeButton.setVisibility(setVisible ? View.VISIBLE : View.GONE);
                            writeButtonAnimation = null;
                        }
                    }
                });
                writeButtonAnimation.start();
            }

            ViewProxy.setScaleX(avatarImage, (42 + 18 * diff) / 42.0f);
            ViewProxy.setScaleY(avatarImage, (42 + 18 * diff) / 42.0f);
            float avatarY = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() / 2.0f * (1.0f + diff) - 21 * AndroidUtilities.density + 27 * AndroidUtilities.density * diff;
            ViewProxy.setTranslationX(avatarImage, -AndroidUtilities.dp(47) * diff);
            ViewProxy.setTranslationY(avatarImage, (float) Math.ceil(avatarY));
            ViewProxy.setTranslationX(nameTextView, -21 * AndroidUtilities.density * diff);
            ViewProxy.setTranslationY(nameTextView, (float) Math.floor(avatarY) - (float) Math.ceil(AndroidUtilities.density) + (float) Math.floor(7 * AndroidUtilities.density * diff));
            ViewProxy.setTranslationX(onlineTextView, -21 * AndroidUtilities.density * diff);
            ViewProxy.setTranslationY(onlineTextView, (float) Math.floor(avatarY) + AndroidUtilities.dp(22) + (float) Math.floor(11 * AndroidUtilities.density) * diff);
            ViewProxy.setScaleX(nameTextView, 1.0f + 0.12f * diff);
            ViewProxy.setScaleY(nameTextView, 1.0f + 0.12f * diff);
        }
    }

    private void fixLayout() {
        if (fragmentView == null) {
            return;
        }
        fragmentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (fragmentView != null) {
                    needLayout();
                    fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return true;
            }
        });
    }

    private void updateUserData() {
        TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
        TLRPC.FileLocation photo = null;
        TLRPC.FileLocation photoBig = null;
        if (user.photo != null) {
            photo = user.photo.photo_small;
            photoBig = user.photo.photo_big;
        }
        AvatarDrawable avatarDrawable = new AvatarDrawable(user, true);

        avatarDrawable.setColor(Theme.ACTION_BAR_MAIN_AVATAR_COLOR);
        if (avatarImage != null) {
            avatarImage.setImage(photo, "50_50", avatarDrawable);
            avatarImage.getImageReceiver().setVisible(!PhotoViewer.getInstance().isShowingImage(photoBig), false);

            nameTextView.setText(UserObject.getUserName(user));
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);
            boolean specter = preferences.getBoolean("specterMode", false);
            if (specter) {
                onlineTextView.setText(LocaleController.getString("SpecterOnline", R.string.SpecterOnline));
            } else {
                onlineTextView.setText(LocaleController.getString("Online", R.string.Online));
            }
            avatarImage.getImageReceiver().setVisible(!PhotoViewer.getInstance().isShowingImage(photoBig), false);
        }
    }

    private void toast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    private class ListAdapter extends BaseFragmentAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int i) {
            return i == specterModeRow || i == hiddenTypingRow || i == noNumberRow || i == dialogsHideTabsCheckRow || i == dialogsTabsRow ||
                    i == dialogsTabsHeightRow || i == dialogsDisableTabsAnimationCheckRow || i == dialogsInfiniteTabsSwipe || i == dialogsHideTabsCounters || i == dialogsTabsCountersCountChats ||
                    i == dialogsTabsCountersCountNotMuted || i == chatShowDirectShareBtn || i == chatDirectShareToMenu || i == chatDirectShareFavsFirst || i == chatShowDateToastRow || i == textFontRow ||
                    i == textSizeRow || i == hideStatusIndicatorCheckRow || i == actionBarBackgroundColorRow || i == tabsBackgroundColorRow || i == drawerHeaderColorRow || i == profileBackgroundColorRow || i == resetDefaultRow ||
                    i == passcodeRow || i == profileSharedOptionsRow || i == keepOriginalFilenameRow;
        }

        @Override
        public int getCount() {
            return rowCount;
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
            return false;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            int type = getItemViewType(i);
            if (type == 0) {
                if (view == null) {
                    view = new EmptyCell(mContext);
                }
                if (i == overscrollRow) {
                    ((EmptyCell) view).setHeight(AndroidUtilities.dp(88));
                } else {
                    ((EmptyCell) view).setHeight(AndroidUtilities.dp(16));
                }
            } else if (type == 1) {             // Ending section header cell (gray shadow)
                if (view == null) {
                    view = new ShadowSectionCell(mContext);
                }
            } else if (type == 2) {             // Color and text item (LED Color)
                if (view == null) {
                    view = new TextColorCell(mContext);
                }
                TextColorCell textCell = (TextColorCell) view;

                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);
                int initialActionBarBackgroundColor = preferences.getInt("actionBarBackgroundColor", TelegramityUtilities.colorABBG());
                int initialTabsBackgroundColor = preferences.getInt("tabsBackgroundColor", TelegramityUtilities.colorTH());
                int initialDrawerHeaderColor = preferences.getInt("drawerHeaderColor", TelegramityUtilities.colorDH());
                final int initialProfileBackgroundColor = preferences.getInt("profileBackgroundColor", TelegramityUtilities.colorPBG());

                if (i == actionBarBackgroundColorRow) {
                    textCell.setTextAndColor(LocaleController.getString("itemActionBarBackgroundColor", R.string.itemActionBarBackgroundColor), initialActionBarBackgroundColor, true);
                } else if (i == tabsBackgroundColorRow) {
                    textCell.setTextAndColor(LocaleController.getString("itemTabsBackgroundColor", R.string.itemTabsBackgroundColor), initialTabsBackgroundColor, true);
                } else if (i == drawerHeaderColorRow) {
                    textCell.setTextAndColor(LocaleController.getString("itemDrawerHeaderColor", R.string.itemDrawerHeaderColor), initialDrawerHeaderColor, true);
                } else if (i == profileBackgroundColorRow) {
                    textCell.setTextAndColor(LocaleController.getString("itemProfileBackgroundColor", R.string.itemProfileBackgroundColor), initialProfileBackgroundColor, true);
                }
            } else if (type == 3) {
                if (view == null) {
                    view = new TextInfoPrivacyCell(mContext);
                }
                if (i == slyDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("SlyHelp", R.string.SlyHelp));
                    view.setBackgroundResource(R.drawable.greydivider_bottom);
                /*} else if (i == customizationsDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("CustomizationsHelp", R.string.CustomizationsHelp));
                    view.setBackgroundResource(R.drawable.greydivider_bottom);
                } else if (i == premiumSecurityDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("AdvancedSecurityHelp", R.string.AdvancedSecurityHelp));
                    view.setBackgroundResource(R.drawable.greydivider_bottom);*/
                } else if (i == mediaDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("KeepOriginalFilenameHelp", R.string.KeepOriginalFilenameHelp));
                    view.setBackgroundResource(R.drawable.greydivider_bottom);
                }
            } else if (type == 4) {             // Header type cell, categorie's header
                if (view == null) {
                    view = new HeaderCell(mContext);
                }
                if (i == slySectionRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("SlyHeader", R.string.SlyHeader));
                } else if (i == dialogsSectionRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("DialogsSettings", R.string.TabsHeader));
                } else if (i == messagesSectionRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("MessagesSettings", R.string.MessagesSettings));
                } else if (i == customizationsSectionRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("CustomizationsHeader", R.string.CustomizationsHeader));
                } else if (i == premiumSecuritySectionRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("PremiumSecurityHeader", R.string.AdvancedSecurityHeader));
                } else if (i == mediaSectionRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("FilesAndMediaHeader", R.string.FilesAndMediaHeader));
                }
            } else if (type == 5) {             // Text information cell (e.g. ending version gray colored text)
                if (view == null) {
                    view = new TextInfoCell(mContext);
                    try {
                        PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                        int code = pInfo.versionCode / 10;
                        String abi = "";
                        switch (pInfo.versionCode % 10) {
                            case 0:
                                abi = "arm";
                                break;
                            case 1:
                                abi = "arm-v7a";
                                break;
                            case 2:
                                abi = "x86";
                                break;
                        }
                        ((TextInfoCell) view).setText(String.format(Locale.US, "Telegramity for Android v%s (%d) %s", pInfo.versionName, code, abi));
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                }
            } else if (type == 6) {
                if (view == null) {
                    view = new TextSettingsCell(mContext);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                if (i == dialogsTabsHeightRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Context.MODE_PRIVATE);
                    int size = preferences.getInt("tabsHeight", AndroidUtilities.isTablet() ? 46 : 44);
                    textCell.setTextAndValue(LocaleController.getString("TabsHeight", R.string.TabsHeight), String.format("%d", size), true);
                } else if (i == textFontRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Context.MODE_PRIVATE);
                    String[] fontNameArray = ApplicationLoader.applicationContext.getResources().getStringArray(R.array.FontNameArr);
                    String fontName = preferences.getString("customFontName", fontNameArray[2]);
                    textCell.setTextAndValue(LocaleController.getString("itemFontName", R.string.itemFontName), fontName, true);
                } else if (i == textSizeRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    int size = preferences.getInt("font_size", AndroidUtilities.isTablet() ? 18 : 16);
                    textCell.setTextAndValue(LocaleController.getString("TextSize", R.string.TextSize), String.format("%d", size), true);
                } else if (i == resetDefaultRow) {
                    textCell.setText(LocaleController.getString("CustomizationsResetDefault", R.string.CustomizationsResetDefault), true);
                } else if (i == passcodeRow) {
                    textCell.setText(LocaleController.getString("Passcode", R.string.Passcode), true);
                }
            } else if (type == 7) {
                if (view == null) {
                    view = new TextCheckCell(mContext);
                }
                TextCheckCell textCheckCell = (TextCheckCell) view;

                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);
                if (i == specterModeRow) {
                    textCheckCell.setTextAndCheck(LocaleController.getString("itemSpecterMode", R.string.itemSpecterMode), preferences.getBoolean("specterMode", false), true);
                } else if (i == hiddenTypingRow) {
                    textCheckCell.setTextAndCheck(LocaleController.getString("itemHiddenTyping", R.string.itemHiddenTyping), preferences.getBoolean("hiddenTyping", false), true);
                } else if (i == noNumberRow) {
                    textCheckCell.setTextAndCheck(LocaleController.getString("itemNoNumber", R.string.itemNoNumber), preferences.getBoolean("noNumber", false), true);
                } else if (i == dialogsHideTabsCheckRow) {
                    textCheckCell.setTextAndCheck(LocaleController.getString("HideTabs", R.string.HideTabs), preferences.getBoolean("hideTabs", false), true);
                } else if (i == dialogsDisableTabsAnimationCheckRow) {
                    textCheckCell.setTextAndCheck(LocaleController.getString("DisableTabsAnimation", R.string.DisableTabsAnimation), preferences.getBoolean("disableTabsAnimation", false), true);
                } else if (i == dialogsInfiniteTabsSwipe) {
                    textCheckCell.setTextAndCheck(LocaleController.getString("InfiniteSwipe", R.string.InfiniteSwipe), preferences.getBoolean("infiniteTabsSwipe", false), true);
                } else if (i == dialogsHideTabsCounters) {
                    textCheckCell.setTextAndCheck(LocaleController.getString("HideTabsCounters", R.string.HideTabsCounters), preferences.getBoolean("hideTabsCounters", false), true);
                } else if (i == dialogsTabsCountersCountChats) {
                    textCheckCell.setTextAndCheck(LocaleController.getString("HeaderTabCounterCountChats", R.string.HeaderTabCounterCountChats), preferences.getBoolean("tabsCountersCountChats", false), true);
                } else if (i == dialogsTabsCountersCountNotMuted) {
                    textCheckCell.setTextAndCheck(LocaleController.getString("HeaderTabCounterCountNotMuted", R.string.HeaderTabCounterCountNotMuted), preferences.getBoolean("tabsCountersCountNotMuted", false), false);
                } else if (i == chatDirectShareToMenu) {
                    textCheckCell.setTextAndCheck(LocaleController.getString("DirectShareToMenu", R.string.DirectShareToMenu), preferences.getBoolean("directShareToMenu", true), true);
                } else if (i == chatDirectShareFavsFirst) {
                    textCheckCell.setTextAndCheck(LocaleController.getString("DirectShareShowFavsFirst", R.string.DirectShareShowFavsFirst), preferences.getBoolean("directShareFavsFirst", true), true);
                } else if (i == chatShowDateToastRow) {
                    textCheckCell.setTextAndCheck(LocaleController.getString("ShowDateToast", R.string.ShowDateToast), preferences.getBoolean("showDateToast", true), false);
                } else if (i == hideStatusIndicatorCheckRow) {
                    textCheckCell.setTextAndCheck(LocaleController.getString("HideStatusIndicator", R.string.HideStatusIndicator), preferences.getBoolean("chatsHideStatusIndicator", false), true);
                } else if (i == keepOriginalFilenameRow) {
                    textCheckCell.setTextAndCheck(LocaleController.getString("KeepOriginalFilename", R.string.KeepOriginalFilename), preferences.getBoolean("keepOriginalFilename", false), false);
                }
            } else if (type == 8) {
                if (view == null) {
                    view = new TextDetailSettingsCell(mContext);
                }
                TextDetailSettingsCell textCell = (TextDetailSettingsCell) view;

                if (i == dialogsTabsRow) {
                    String value;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);

                    boolean hideUsers = preferences.getBoolean("hideUsers", false);
                    boolean hideGroups = preferences.getBoolean("hideGroups", false);
                    boolean hideSGroups = preferences.getBoolean("hideSGroups", false);
                    boolean hideChannels = preferences.getBoolean("hideChannels", false);
                    boolean hideBots = preferences.getBoolean("hideBots", false);
                    boolean hideFavs = preferences.getBoolean("hideFavs", false);

                    value = LocaleController.getString("HideShowTabs", R.string.HideShowTabs);

                    String text = "";
                    if (!hideUsers) {
                        text += LocaleController.getString("Users", R.string.Users);
                    }
                    if (!hideGroups) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Groups", R.string.Groups);
                    }
                    if (!hideSGroups) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("SuperGroups", R.string.SuperGroups);
                    }
                    if (!hideChannels) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Channels", R.string.Channels);
                    }
                    if (!hideBots) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Bots", R.string.Bots);
                    }
                    if (!hideFavs) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Favorites", R.string.Favorites);
                    }
                    if (text.length() == 0) {
                        text = "";
                    }
                    textCell.setTextAndValue(value, text, true);
                } else if (i == chatShowDirectShareBtn) {
                    String value;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);
                    boolean showDSBtnUsers = preferences.getBoolean("showDSBtnUsers", true);
                    boolean showDSBtnGroups = preferences.getBoolean("showDSBtnGroups", true);
                    boolean showDSBtnSGroups = preferences.getBoolean("showDSBtnSGroups", true);
                    boolean showDSBtnChannels = preferences.getBoolean("showDSBtnChannels", true);
                    boolean showDSBtnBots = preferences.getBoolean("showDSBtnBots", true);

                    value = LocaleController.getString("ShowDirectShareButton", R.string.ShowDirectShareButton);

                    String text = "";
                    if (showDSBtnUsers) {
                        text += LocaleController.getString("Users", R.string.Users);
                    }
                    if (showDSBtnGroups) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Groups", R.string.Groups);
                    }
                    if (showDSBtnSGroups) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("SuperGroups", R.string.SuperGroups);
                    }
                    if (showDSBtnChannels) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Channels", R.string.Channels);
                    }
                    if (showDSBtnBots) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Bots", R.string.Bots);
                    }

                    if (text.length() == 0) {
                        text = LocaleController.getString("Channels", R.string.UsernameEmpty);
                    }
                    textCell.setTextAndValue(value, text, true);
                } else if (i == profileSharedOptionsRow) {

                    String value;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);

                    boolean hideMedia = preferences.getBoolean("hideSharedMedia", false);
                    boolean hideFiles = preferences.getBoolean("hideSharedFiles", false);
                    boolean hideMusic = preferences.getBoolean("hideSharedMusic", false);
                    boolean hideLinks = preferences.getBoolean("hideSharedLinks", false);

                    value = LocaleController.getString("SharedMedia", R.string.SharedMedia);

                    String text = "";
                    if (!hideMedia) {
                        text += LocaleController.getString("Users", R.string.SharedMediaTitle);
                    }
                    if (!hideFiles) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("DocumentsTitle", R.string.DocumentsTitle);
                    }
                    if (!hideMusic) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("AudioTitle", R.string.AudioTitle);
                    }
                    if (!hideLinks && BuildVars.DEBUG_VERSION) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("LinksTitle", R.string.LinksTitle);
                    }

                    if (text.length() == 0) {
                        text = "";
                    }
                    textCell.setTextAndValue(value, text, true);
                }
            }
            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if (i == emptyRow || i == overscrollRow) {
                return 0;
            }
            if (i == slySectionBottomRow || i == dialogsSectionBottomRow || i == messagesSectionBottomRow || i == customizationsSectionBottomRow || i == premiumSecuritySectionBottomRow || i == mediaSectionBottomRow) {
                return 1;
            } else if (i == actionBarBackgroundColorRow || i == tabsBackgroundColorRow || i == drawerHeaderColorRow || i == profileBackgroundColorRow) {
                return 2;
            } else if (i == slyDetailRow || /*i == customizationsDetailRow || i == premiumSecurityDetailRow ||*/ i == mediaDetailRow) {
                return 3;
            } else if (i == slySectionRow || i == dialogsSectionRow || i == messagesSectionRow || i == customizationsSectionRow || i == premiumSecuritySectionRow || i == mediaSectionRow) {
                return 4;
            } else if (i == textDescriptionRow) {
                return 5;
            } else if ( i == dialogsTabsHeightRow || i == textFontRow || i == textSizeRow || i == resetDefaultRow || i == passcodeRow) {
                return 6;
            } else if (i == dialogsTabsRow || i == chatShowDirectShareBtn || i == profileSharedOptionsRow) {
                return 8;
            } else {
                return 7;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 9;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

}
