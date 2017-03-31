/*
 * This is the source code of Telegramity for Android
 *
 * Copyright Shayan Amani, 2016.
 */

package com.ioton;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;
import com.onesignal.OneSignal;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextColorCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.PasscodeActivity;

import java.util.Locale;

public class TelegramitySettingsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private RecyclerListView listView;
    private ListAdapter listAdapter;
    private LinearLayoutManager layoutManager;
    private TextView nameTextView;
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

    private int versionDescriptionRow;
    private int rowCount;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
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

        versionDescriptionRow = rowCount++;

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

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(true);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setGlowColor(instanceOfProfileBackgroundColor);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(final View view, final int position) {
                final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);
                if (position == specterModeRow) {
                    boolean specter = preferences.getBoolean("specterMode", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("specterMode", !specter);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!specter);
                    }
                    MessagesController.getInstance().reRunUpdateTimerProc();
                } else if (position == hiddenTypingRow) {
                    boolean typing = preferences.getBoolean("hiddenTyping", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hiddenTyping", !typing);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!typing);
                    }
                } else if (position == noNumberRow) {
                    boolean number = preferences.getBoolean("noNumber", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("noNumber", !number);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!number);
                    }
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.mainUserInfoChanged);
                } else if (position == dialogsHideTabsCheckRow) {
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
                        //editor.putBoolea
                        // n("hideGroups", false).apply();
                        if (listView != null) {
                            listView.invalidateViews();
                        }
                    }
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 10);
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!hide);
                    }
                } else if (position == dialogsTabsRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("HideShowTabs", R.string.HideShowTabs));
                    builder.setCancelable(false);
                    final SharedPreferences.Editor editor = preferences.edit();
                    final boolean hideUsers = preferences.getBoolean("hideUsers", false);
                    final boolean hideGroups = preferences.getBoolean("hideGroups", false);
                    final boolean hideSGroups = preferences.getBoolean("hideSGroups", false);
                    final boolean hideChannels = preferences.getBoolean("hideChannels", false);
                    final boolean hideBots = preferences.getBoolean("hideBots", false);
                    final boolean hideFavs = preferences.getBoolean("hideFavs", false);
                    builder.setMultiChoiceItems(
                            new CharSequence[]{LocaleController.getString("Users", R.string.Users), LocaleController.getString("Groups", R.string.Groups), LocaleController.getString("SuperGroups", R.string.SuperGroups), LocaleController.getString("Channels", R.string.Channels), LocaleController.getString("Bots", R.string.Bots), LocaleController.getString("Favorites", R.string.Favorites)},
                            new boolean[]{!hideUsers, !hideGroups, !hideSGroups, !hideChannels, !hideBots, !hideFavs},
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {

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

                                    preferences.getBoolean("hideUsers", false);
                                    preferences.getBoolean("hideGroups", false);
                                    preferences.getBoolean("hideSGroups", false);
                                    preferences.getBoolean("hideChannels", false);
                                    preferences.getBoolean("hideBots", false);
                                    preferences.getBoolean("hideFavs", false);
                                    if (hideUsers && hideGroups && hideSGroups && hideChannels && hideBots && hideFavs) {
                                        editor.putBoolean("hideTabs", true);
                                        editor.apply();
                                        toast(context, LocaleController.getString("TabsWillHide", R.string.TabsWillHide));
                                        if (listAdapter != null) {
                                            listAdapter.notifyItemChanged(position - 1);
                                        }
                                    }
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, which);
                                }
                            });
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 13);
                            if (listAdapter != null) {
                                listAdapter.notifyItemChanged(position);
                            }
                            if (hideUsers && hideGroups && hideSGroups && hideChannels && hideBots && hideFavs) {
                                editor.putBoolean("hideTabs", true);
                                editor.apply();
                                toast(context, LocaleController.getString("TabsWillHide", R.string.TabsWillHide));
                                if (listAdapter != null) {
                                    listAdapter.notifyItemChanged(position - 1);
                                }
                            }
                        }
                    });
                    showDialog(builder.create());
                } else if (position == dialogsTabsHeightRow) {
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
                            if (listAdapter != null) {
                                listAdapter.notifyItemChanged(position);
                            }
                        }
                    });
                    showDialog(builder.create());
                } else if (position == dialogsDisableTabsAnimationCheckRow) {
                    boolean disable = preferences.getBoolean("disableTabsAnimation", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("disableTabsAnimation", !disable);
                    editor.apply();
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 11);
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!disable);
                    }
                } else if (position == dialogsInfiniteTabsSwipe) {
                    boolean disable = preferences.getBoolean("infiniteTabsSwipe", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("infiniteTabsSwipe", !disable);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!disable);
                    }
                } else if (position == dialogsHideTabsCounters) {
                    boolean disable = preferences.getBoolean("hideTabsCounters", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hideTabsCounters", !disable);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!disable);
                    }
                } else if (position == dialogsTabsCountersCountChats) {
                    boolean disable = preferences.getBoolean("tabsCountersCountChats", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("tabsCountersCountChats", !disable);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!disable);
                    }
                } else if (position == dialogsTabsCountersCountNotMuted) {
                    boolean disable = preferences.getBoolean("tabsCountersCountNotMuted", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("tabsCountersCountNotMuted", !disable);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!disable);
                    }
                } else if (position == chatShowDirectShareBtn) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    createDialog(builder, chatShowDirectShareBtn);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (listAdapter != null) {
                                listAdapter.notifyItemChanged(position);
                            }
                        }
                    });
                    showDialog(builder.create());
                } else if (position == chatDirectShareToMenu) {
                    boolean send = preferences.getBoolean("directShareToMenu", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("directShareToMenu", !send);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!send);
                    }
                } else if (position == chatDirectShareFavsFirst) {
                    boolean send = preferences.getBoolean("directShareFavsFirst", true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("directShareFavsFirst", !send);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!send);
                    }
                } else if (position == chatShowDateToastRow) {
                    boolean show = preferences.getBoolean("showDateToast", true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("showDateToast", !show);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!show);
                    }
                } else if (position == textFontRow) {
                    presentFragment(new FontSelectActivity());
                } else if (position == textSizeRow) {
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
                            if (listAdapter != null) {
                                listAdapter.notifyItemChanged(position);
                            }
                        }
                    });
                    builder.setNeutralButton(LocaleController.getString("Default", R.string.Default), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            editor.putInt("font_size", AndroidUtilities.isTablet() ? 18 : 16);
                            MessagesController.getInstance().fontSize = AndroidUtilities.isTablet() ? 18 : 16;
                            editor.commit();
                            if (listAdapter != null) {
                                listAdapter.notifyItemChanged(position);
                            }
                        }
                    });
                    showDialog(builder.create());
                } else if (position == hideStatusIndicatorCheckRow) {
                    boolean disable = preferences.getBoolean("chatsHideStatusIndicator", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("chatsHideStatusIndicator", !disable);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!disable);
                    }
                } else if (position == actionBarBackgroundColorRow || position == tabsBackgroundColorRow || position == profileBackgroundColorRow || position == drawerHeaderColorRow) {
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

                    if (position == actionBarBackgroundColorRow) {
                        colorPicker.setOldCenterColor(instanceOfActionBarBackgroundColor);
//                        cPDB.initialColor(initialActionBarBackgroundColor);
                    } else if (position == tabsBackgroundColorRow) {
                        colorPicker.setOldCenterColor(instanceOfTabsBackgroundColor);
//                        cPDB.initialColor(instanceOfTabsBackgroundColor);
                    } else if (position == profileBackgroundColorRow) {
                        colorPicker.setOldCenterColor(instanceOfProfileBackgroundColor);
//                        cPDB.initialColor(instanceOfProfileBackgroundColor);
                    } else if (position == drawerHeaderColorRow) {
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
                                    if (position == actionBarBackgroundColorRow) {
                                        editor.putInt("actionBarBackgroundColor", colorPicker.getColor());
                                        textCell.setTextAndColor(LocaleController.getString("itemActionBarBackgroundColor", R.string.itemActionBarBackgroundColor), colorPicker.getColor(), true);
                                        needRestart = true;
                                        toast(context, LocaleController.getString("ThemeChangeToastMessage", R.string.ThemeChangeToastMessage));
                                    } else if (position == tabsBackgroundColorRow) {
                                        editor.putInt("tabsBackgroundColor", colorPicker.getColor());
                                        textCell.setTextAndColor(LocaleController.getString("itemTabsBackgroundColor", R.string.itemTabsBackgroundColor), colorPicker.getColor(), true);
                                        needRestart = true;
                                        toast(context, LocaleController.getString("ThemeChangeToastMessage", R.string.ThemeChangeToastMessage));
                                    } else if (position == drawerHeaderColorRow) {
                                        editor.putInt("drawerHeaderColor", colorPicker.getColor());
                                        textCell.setTextAndColor(LocaleController.getString("itemDrawerHeaderColor", R.string.itemDrawerHeaderColor), colorPicker.getColor(), true);
                                        needRestart = true;
                                        toast(context, LocaleController.getString("ThemeChangeToastMessage", R.string.ThemeChangeToastMessage));
                                    } else if (position == profileBackgroundColorRow) {
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
                                    if (position == actionBarBackgroundColorRow) {
                                        editor.putInt("actionBarBackgroundColor", selectedColor);
                                        textCell.setTextAndColor(LocaleController.getString("itemActionBarBackgroundColor", R.string.itemActionBarBackgroundColor), selectedColor, true);
                                        toast(context, LocaleController.getString("ThemeChangeToastMessage", R.string.ThemeChangeToastMessage));
                                    } else if (position == tabsBackgroundColorRow) {
                                        editor.putInt("tabsBackgroundColorRow", selectedColor);
                                        textCell.setTextAndColor(LocaleController.getString("itemTabsBackgroundColor", R.string.itemTabsBackgroundColor), selectedColor, true);
                                        toast(context, LocaleController.getString("ThemeChangeToastMessage", R.string.ThemeChangeToastMessage));
                                    } else if (position == drawerHeaderColorRow) {
                                        editor.putInt("drawerHeaderColor", selectedColor);
                                        textCell.setTextAndColor(LocaleController.getString("itemDrawerHeaderColor", R.string.itemDrawerHeaderColor), selectedColor, true);
                                        toast(context, LocaleController.getString("ThemeChangeToastMessage", R.string.ThemeChangeToastMessage));
                                    } else if (position == profileBackgroundColorRow) {
                                        editor.putInt("profileBackgroundColor", selectedColor);
                                        textCell.setTextAndColor(LocaleController.getString("itemProfileBackgroundColor", R.string.itemProfileBackgroundColor), selectedColor, true);
                                    }
                                    editor.apply();
//                                    listAdapter.notifyItemChanged(position);
                                }
                            })
                            .setNegativeButton(LocaleController.getString("ThemeDialogCancelButton", R.string.ThemeDialogCancelButton), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .build()
                            .show();*/
                } else if (position == resetDefaultRow) {
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
                } else if (position == passcodeRow) {
                    if (UserConfig.passcodeHash.length() > 0) {
                        presentFragment(new PasscodeActivity(2));
                    } else {
                        presentFragment(new PasscodeActivity(0));
                    }
                } else if (position == profileSharedOptionsRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    createSharedOptions(builder);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 13);
                            if (listAdapter != null) {
                                listAdapter.notifyItemChanged(position);
                            }
                        }
                    });
                    showDialog(builder.create());
                } else if (position == keepOriginalFilenameRow) {
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

        listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener() {
            @Override
            public boolean onItemClick(View view, int position) {
                if (position == versionDescriptionRow) {
                    OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
                        @Override
                        public void idsAvailable(final String userId, final String registrationId) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                            builder.setTitle("INFO");
                            builder.setItems(new CharSequence[]{
                                    "Player ID: " + userId + "\n\n" + "Reg ID: " + registrationId
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {
                                        try {
                                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                            android.content.ClipData clip = android.content.ClipData.newPlainText("label", "Player ID: " + userId + "\n\n" + "Reg ID: " + registrationId);
                                            clipboard.setPrimaryClip(clip);
                                            toast(context, LocaleController.formatString("Copied", R.string.Copied, "INFO"));
                                        } catch (Exception e) {
                                            FileLog.e("tmessages", e);
                                        }
                                    }
                                }
                            });
                            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                            showDialog(builder.create());
                        }
                    });
                    return true;
                }
                return false;
            }
        });

        frameLayout.addView(actionBar);

        extraHeightView = new View(context);
        extraHeightView.setPivotY(0);
        extraHeightView.setBackgroundColor(instanceOfProfileBackgroundColor);
        frameLayout.addView(extraHeightView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 88));

        shadowView = new View(context);
        shadowView.setBackgroundResource(R.drawable.header_shadow);
        frameLayout.addView(shadowView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 3));

        nameTextView = new TextView(context);
        nameTextView.setTextColor(0xffffffff);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        nameTextView.setMaxLines(2);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        nameTextView.setGravity(Gravity.LEFT);
        nameTextView.setTypeface(AndroidUtilities.getTypeface());
        nameTextView.setPivotX(0);
        nameTextView.setPivotY(0);
        frameLayout.addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 48, 0, 48, 0));

        needLayout();

        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (layoutManager.getItemCount() == 0) {
                    return;
                }
                int height = 0;
                View child = recyclerView.getChildAt(0);
                if (child != null) {
                    if (layoutManager.findFirstVisibleItemPosition() == 0) {
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

    private AlertDialog.Builder createTabsDialog(AlertDialog.Builder builder, final int position) {

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
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void saveSelfArgs(Bundle args) {
    }

    @Override
    public void restoreSelfArgs(Bundle args) {
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        nameTextView.setText(LocaleController.getString("TelegramitySettings", R.string.TelegramitySettings));
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
                extraHeightView.setTranslationY(newTop);
            }
        }

        float diff = extraHeight / (float) AndroidUtilities.dp(88);
        extraHeightView.setScaleY(diff);
        shadowView.setTranslationY(newTop + extraHeight);

        float avatarY = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() / 2.0f * (1.0f + diff) - 21 * AndroidUtilities.density + 27 * AndroidUtilities.density * diff;
        nameTextView.setTranslationX(-21 * AndroidUtilities.density * diff);
        nameTextView.setTranslationY((float) Math.floor(avatarY) - (float) Math.ceil(AndroidUtilities.density) + (float) Math.floor(7 * AndroidUtilities.density * diff));
        nameTextView.setScaleX(1.0f + 0.12f * diff);
        nameTextView.setScaleY(1.0f + 0.12f * diff);
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

    private void toast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {

    }

    private class ListAdapter extends RecyclerView.Adapter {

        private Context mContext;

        private class Holder extends RecyclerView.ViewHolder {

            public Holder(View itemView) {
                super(itemView);
            }
        }

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            boolean checkBackground = true;
            switch (holder.getItemViewType()) {
                case 0: {
                    if (position == overscrollRow) {
                        ((EmptyCell) holder.itemView).setHeight(AndroidUtilities.dp(88));
                    } else {
                        ((EmptyCell) holder.itemView).setHeight(AndroidUtilities.dp(16));
                    }
                    checkBackground = false;
                    break;
                }
                case 2: {
                    TextColorCell textColorCell = (TextColorCell) holder.itemView;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);
                    int initialActionBarBackgroundColor = preferences.getInt("actionBarBackgroundColor", TelegramityUtilities.colorABBG());
                    int initialTabsBackgroundColor = preferences.getInt("tabsBackgroundColor", TelegramityUtilities.colorTH());
                    int initialDrawerHeaderColor = preferences.getInt("drawerHeaderColor", TelegramityUtilities.colorDH());
                    final int initialProfileBackgroundColor = preferences.getInt("profileBackgroundColor", TelegramityUtilities.colorPBG());
                    if (position == actionBarBackgroundColorRow) {
                        textColorCell.setTextAndColor(LocaleController.getString("itemActionBarBackgroundColor", R.string.itemActionBarBackgroundColor), initialActionBarBackgroundColor, true);
                    } else if (position == tabsBackgroundColorRow) {
                        textColorCell.setTextAndColor(LocaleController.getString("itemTabsBackgroundColor", R.string.itemTabsBackgroundColor), initialTabsBackgroundColor, true);
                    } else if (position == drawerHeaderColorRow) {
                        textColorCell.setTextAndColor(LocaleController.getString("itemDrawerHeaderColor", R.string.itemDrawerHeaderColor), initialDrawerHeaderColor, true);
                    } else if (position == profileBackgroundColorRow) {
                        textColorCell.setTextAndColor(LocaleController.getString("itemProfileBackgroundColor", R.string.itemProfileBackgroundColor), initialProfileBackgroundColor, true);
                    }
                    break;
                }
                case 3: {
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == slyDetailRow) {
                        textInfoPrivacyCell.setText(LocaleController.getString("SlyHelp", R.string.SlyHelp));
                        textInfoPrivacyCell.setBackgroundResource(R.drawable.greydivider_bottom);
                    /*} else if (position == customizationsDetailRow) {
                         textInfoPrivacyCell.setText(LocaleController.getString("CustomizationsHelp", R.string.CustomizationsHelp));
                         textInfoPrivacyCell.setBackgroundResource(R.drawable.greydivider_bottom);
                    } else if (position == premiumSecurityDetailRow) {
                         textInfoPrivacyCell.setText(LocaleController.getString("AdvancedSecurityHelp", R.string.AdvancedSecurityHelp));
                         textInfoPrivacyCell.setBackgroundResource(R.drawable.greydivider_bottom);*/
                    } else if (position == mediaDetailRow) {
                        textInfoPrivacyCell.setText(LocaleController.getString("KeepOriginalFilenameHelp", R.string.KeepOriginalFilenameHelp));
                        textInfoPrivacyCell.setBackgroundResource(R.drawable.greydivider_bottom);
                    }
                    break;
                }
                case 4: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == slySectionRow) {
                        headerCell.setText(LocaleController.getString("SlyHeader", R.string.SlyHeader));
                    } else if (position == dialogsSectionRow) {
                        headerCell.setText(LocaleController.getString("DialogsSettings", R.string.TabsHeader));
                    } else if (position == messagesSectionRow) {
                        headerCell.setText(LocaleController.getString("MessagesSettings", R.string.MessagesSettings));
                    } else if (position == customizationsSectionRow) {
                        headerCell.setText(LocaleController.getString("CustomizationsHeader", R.string.CustomizationsHeader));
                    } else if (position == premiumSecuritySectionRow) {
                        headerCell.setText(LocaleController.getString("PremiumSecurityHeader", R.string.AdvancedSecurityHeader));
                    } else if (position == mediaSectionRow) {
                        headerCell.setText(LocaleController.getString("FilesAndMediaHeader", R.string.FilesAndMediaHeader));
                    }
                    break;
                }
                case 6: {
                    TextSettingsCell textSettingsCell = (TextSettingsCell) holder.itemView;
                    if (position == dialogsTabsHeightRow) {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Context.MODE_PRIVATE);
                        int size = preferences.getInt("tabsHeight", AndroidUtilities.isTablet() ? 46 : 44);
                        textSettingsCell.setTextAndValue(LocaleController.getString("TabsHeight", R.string.TabsHeight), String.format("%d", size), true);
                    } else if (position == textFontRow) {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Context.MODE_PRIVATE);
                        String[] fontNameArray = ApplicationLoader.applicationContext.getResources().getStringArray(R.array.FontNameArr);
                        String fontName = preferences.getString("customFontName", fontNameArray[2]);
                        textSettingsCell.setTextAndValue(LocaleController.getString("itemFontName", R.string.itemFontName), fontName, true);
                    } else if (position == textSizeRow) {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                        int size = preferences.getInt("font_size", AndroidUtilities.isTablet() ? 18 : 16);
                        textSettingsCell.setTextAndValue(LocaleController.getString("TextSize", R.string.TextSize), String.format("%d", size), true);
                    } else if (position == resetDefaultRow) {
                        textSettingsCell.setText(LocaleController.getString("CustomizationsResetDefault", R.string.CustomizationsResetDefault), true);
                    } else if (position == passcodeRow) {
                        textSettingsCell.setText(LocaleController.getString("Passcode", R.string.Passcode), true);
                    }
                    break;
                }
                case 7: {
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);
                    if (position == specterModeRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("itemSpecterMode", R.string.itemSpecterMode), preferences.getBoolean("specterMode", false), true);
                    } else if (position == hiddenTypingRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("itemHiddenTyping", R.string.itemHiddenTyping), preferences.getBoolean("hiddenTyping", false), true);
                    } else if (position == noNumberRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("itemNoNumber", R.string.itemNoNumber), preferences.getBoolean("noNumber", false), true);
                    } else if (position == dialogsHideTabsCheckRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("HideTabs", R.string.HideTabs), preferences.getBoolean("hideTabs", false), true);
                    } else if (position == dialogsDisableTabsAnimationCheckRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("DisableTabsAnimation", R.string.DisableTabsAnimation), preferences.getBoolean("disableTabsAnimation", false), true);
                    } else if (position == dialogsInfiniteTabsSwipe) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("InfiniteSwipe", R.string.InfiniteSwipe), preferences.getBoolean("infiniteTabsSwipe", false), true);
                    } else if (position == dialogsHideTabsCounters) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("HideTabsCounters", R.string.HideTabsCounters), preferences.getBoolean("hideTabsCounters", false), true);
                    } else if (position == dialogsTabsCountersCountChats) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("HeaderTabCounterCountChats", R.string.HeaderTabCounterCountChats), preferences.getBoolean("tabsCountersCountChats", false), true);
                    } else if (position == dialogsTabsCountersCountNotMuted) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("HeaderTabCounterCountNotMuted", R.string.HeaderTabCounterCountNotMuted), preferences.getBoolean("tabsCountersCountNotMuted", false), false);
                    } else if (position == chatDirectShareToMenu) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("DirectShareToMenu", R.string.DirectShareToMenu), preferences.getBoolean("directShareToMenu", true), true);
                    } else if (position == chatDirectShareFavsFirst) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("DirectShareShowFavsFirst", R.string.DirectShareShowFavsFirst), preferences.getBoolean("directShareFavsFirst", true), true);
                    } else if (position == chatShowDateToastRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ShowDateToast", R.string.ShowDateToast), preferences.getBoolean("showDateToast", true), false);
                    } else if (position == hideStatusIndicatorCheckRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("HideStatusIndicator", R.string.HideStatusIndicator), preferences.getBoolean("chatsHideStatusIndicator", false), true);
                    } else if (position == keepOriginalFilenameRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("KeepOriginalFilename", R.string.KeepOriginalFilename), preferences.getBoolean("keepOriginalFilename", false), false);
                    }
                    break;
                }
                case 8: {
                    TextDetailSettingsCell textDetailSettingsCell = (TextDetailSettingsCell) holder.itemView;
                    if (position == dialogsTabsRow) {
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
                        textDetailSettingsCell.setTextAndValue(value, text, true);
                    } else if (position == chatShowDirectShareBtn) {
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
                        textDetailSettingsCell.setTextAndValue(value, text, true);
                    } else if (position == profileSharedOptionsRow) {

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
                        textDetailSettingsCell.setTextAndValue(value, text, true);
                    }
                    break;
                }
                default:
                    checkBackground = false;
                    break;
            }
            if (checkBackground) {
                if (position == specterModeRow || position == hiddenTypingRow || position == noNumberRow || position == dialogsHideTabsCheckRow || position == dialogsTabsRow ||
                        position == dialogsTabsHeightRow || position == dialogsDisableTabsAnimationCheckRow || position == dialogsInfiniteTabsSwipe || position == dialogsHideTabsCounters || position == dialogsTabsCountersCountChats ||
                        position == dialogsTabsCountersCountNotMuted || position == chatShowDirectShareBtn || position == chatDirectShareToMenu || position == chatDirectShareFavsFirst || position == chatShowDateToastRow || position == textFontRow ||
                        position == textSizeRow || position == hideStatusIndicatorCheckRow || position == actionBarBackgroundColorRow || position == tabsBackgroundColorRow || position == drawerHeaderColorRow || position == profileBackgroundColorRow || position == resetDefaultRow ||
                        position == passcodeRow || position == profileSharedOptionsRow || position == keepOriginalFilenameRow) {
                    if (holder.itemView.getBackground() == null) {
                        holder.itemView.setBackgroundResource(R.drawable.list_selector);
                    }
                } else {
                    if (holder.itemView.getBackground() != null) {
                        holder.itemView.setBackgroundDrawable(null);
                    }
                }
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case 0:
                    view = new EmptyCell(mContext);
                    break;
                case 1:
                    view = new ShadowSectionCell(mContext);
                    break;
                case 2:
                    view = new TextColorCell(mContext) {
                        @Override
                        public boolean onTouchEvent(MotionEvent event) {
                            if (Build.VERSION.SDK_INT >= 21 && getBackground() != null) {
                                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                                    getBackground().setHotspot(event.getX(), event.getY());
                                }
                            }
                            return super.onTouchEvent(event);
                        }
                    };
                    break;
                case 3:
                    view = new TextInfoPrivacyCell(mContext) {
                        @Override
                        public boolean onTouchEvent(MotionEvent event) {
                            if (Build.VERSION.SDK_INT >= 21 && getBackground() != null) {
                                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                                    getBackground().setHotspot(event.getX(), event.getY());
                                }
                            }
                            return super.onTouchEvent(event);
                        }
                    };
                    break;
                case 4:
                    view = new HeaderCell(mContext);
                    break;
                case 5:
                    view = new TextInfoCell(mContext) {
                        @Override
                        public boolean onTouchEvent(MotionEvent event) {
                            if (Build.VERSION.SDK_INT >= 21 && getBackground() != null) {
                                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                                    getBackground().setHotspot(event.getX(), event.getY());
                                }
                            }
                            return super.onTouchEvent(event);
                        }
                    };
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
                    break;
                case 6:
                    view = new TextSettingsCell(mContext) {
                        @Override
                        public boolean onTouchEvent(MotionEvent event) {
                            if (Build.VERSION.SDK_INT >= 21 && getBackground() != null) {
                                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                                    getBackground().setHotspot(event.getX(), event.getY());
                                }
                            }
                            return super.onTouchEvent(event);
                        }
                    };
                    break;
                case 7:
                    view = new TextCheckCell(mContext) {
                        @Override
                        public boolean onTouchEvent(MotionEvent event) {
                            if (Build.VERSION.SDK_INT >= 21 && getBackground() != null) {
                                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                                    getBackground().setHotspot(event.getX(), event.getY());
                                }
                            }
                            return super.onTouchEvent(event);
                        }
                    };
                    break;
                case 8:
                    view = new TextDetailSettingsCell(mContext) {
                        @Override
                        public boolean onTouchEvent(MotionEvent event) {
                            if (Build.VERSION.SDK_INT >= 21 && getBackground() != null) {
                                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                                    getBackground().setHotspot(event.getX(), event.getY());
                                }
                            }
                            return super.onTouchEvent(event);
                        }
                    };
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == emptyRow || position == overscrollRow) {
                return 0;
            }
            if (position == slySectionBottomRow || position == dialogsSectionBottomRow || position == messagesSectionBottomRow || position == customizationsSectionBottomRow || position == premiumSecuritySectionBottomRow || position == mediaSectionBottomRow) {
                return 1;
            } else if (position == actionBarBackgroundColorRow || position == tabsBackgroundColorRow || position == drawerHeaderColorRow || position == profileBackgroundColorRow) {
                return 2;
            } else if (position == slyDetailRow || /*position == customizationsDetailRow || position == premiumSecurityDetailRow ||*/ position == mediaDetailRow) {
                return 3;
            } else if (position == slySectionRow || position == dialogsSectionRow || position == messagesSectionRow || position == customizationsSectionRow || position == premiumSecuritySectionRow || position == mediaSectionRow) {
                return 4;
            } else if (position == versionDescriptionRow) {
                return 5;
            } else if (position == dialogsTabsHeightRow || position == textFontRow || position == textSizeRow || position == resetDefaultRow || position == passcodeRow) {
                return 6;
            } else if (position == dialogsTabsRow || position == chatShowDirectShareBtn || position == profileSharedOptionsRow) {
                return 8;
            } else {
                return 7;
            }
        }
    }
}
