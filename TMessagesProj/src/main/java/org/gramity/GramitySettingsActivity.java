/*
 * This is the source code of Telegramity for Android
 *
 * Copyright Shayan Amani, 2016.
 */

package org.gramity;

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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
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
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.CacheControlActivity;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextColorCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.PasscodeActivity;
import org.telegram.ui.ThemeActivity;

import java.util.Locale;

import co.ronash.pushe.Pushe;
import co.ronash.pushe.service.PusheActivityService;

public class GramitySettingsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private RecyclerListView listView;
    private ListAdapter listAdapter;
    private LinearLayoutManager layoutManager;
    private TextView nameTextView;
    private View extraHeightView;
    private View shadowView;

    private int extraHeight;

    private int overscrollRow;
    private int emptyRow;

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
    private int answeringMachineRow;
    private int answeringMachineMessageRow;
    private int chatShowDirectShareBtn;
    private int chatDirectShareToMenu;
    private int chatDirectShareFavsFirst;
    private int cacheRow;
    private int messagesSectionBottomRow;

    private int customizationsSectionRow;
    private int themeRow;
    private int monoColoredIconsRow;
    private int textFontRow;
    private int textSizeRow;
    private int dateTimeAgoRow;
    private int dateSolarCalendarRow;
    private int exactMemberNumberRow;
    private int chatStatusBubbleRow;
    private int tabsBackgroundColorRow;
    private int customizationsSectionBottomRow;

    private int premiumSecuritySectionRow;
    private int passcodeRow;
    private int noNumberRow;
    private int premiumSecuritySectionBottomRow;

    private int mediaSectionRow;
    private int profileSharedOptionsRow;
    private int keepOriginalFilenameRow;
    private int mediaDetailRow;
    private int mediaSectionBottomRow;

    private int versionDescriptionRow;

    private int rowCount;
    private boolean needRestart;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.refreshTabs);

        rowCount = 0;
        overscrollRow = rowCount++;
        emptyRow = rowCount++;

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
        answeringMachineRow = rowCount++;
        answeringMachineMessageRow = rowCount++;
        chatDirectShareToMenu = rowCount++;
        chatShowDirectShareBtn = rowCount++;
        chatDirectShareFavsFirst = rowCount++;
        cacheRow = rowCount++;
        messagesSectionBottomRow = rowCount++;

        customizationsSectionRow = rowCount++;
        themeRow = rowCount++;
        monoColoredIconsRow = rowCount++;
        textFontRow = rowCount++;
        textSizeRow = rowCount++;
        dateTimeAgoRow = rowCount++;
        dateSolarCalendarRow = rowCount++;
        exactMemberNumberRow = rowCount++;
        chatStatusBubbleRow = rowCount++;
        tabsBackgroundColorRow = rowCount++;
        customizationsSectionBottomRow = rowCount++;

        premiumSecuritySectionRow = rowCount++;
        passcodeRow = rowCount++;
        noNumberRow = rowCount++;
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
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.refreshTabs);
        if (needRestart) {
            GramityUtilities.restartTelegramity();
        }
    }

    @Override
    public View createView(final Context context) {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
        final int instanceOfTabsBackgroundColor = preferences.getInt(GramityConstants.PREF_TABS_BACKGROUND_COLOR, GramityUtilities.colorTH());
        needRestart = false;
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_avatar_actionBarSelectorBlue), false);
        actionBar.setItemsColor(Theme.getColor(Theme.key_avatar_actionBarIconBlue), false);
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
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(true);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        });
        listView.setGlowColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(final View view, final int position) {
                final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
                if (position == dialogsHideTabsCheckRow) {
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
                    numberPicker.setValue(preferences.getInt(GramityConstants.PREF_TABS_HEIGHT, AndroidUtilities.isTablet() ? 46 : 44));
                    builder.setView(numberPicker);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt(GramityConstants.PREF_TABS_HEIGHT, numberPicker.getValue());
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
                    boolean disable = preferences.getBoolean(GramityConstants.PREF_INFINITE_TABS_SWIPE, true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(GramityConstants.PREF_INFINITE_TABS_SWIPE, !disable);
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
                    preferences.edit().putBoolean("tabsCountersCountNotMuted", !disable).apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!disable);
                    }
                } else if (position == answeringMachineRow) {
                    boolean answering = preferences.getBoolean(GramityConstants.PREF_ANSWERING_MACHINE, false);
                    preferences.edit().putBoolean(GramityConstants.PREF_ANSWERING_MACHINE, !answering).apply();
                    if (!answering) {
                        Toast.makeText(context, LocaleController.getString("AnsweringMachineActivated", R.string.AnsweringMachineActivated), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, LocaleController.getString("AnsweringMachineInactivated", R.string.AnsweringMachineInactivated), Toast.LENGTH_SHORT).show();
                    }
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!answering);
                    }
                } else if (position == answeringMachineMessageRow) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setCustomTitle(GramityUtilities.alertTitleMaker(context, LocaleController.getString("AnsweringMachineMessage", R.string.AnsweringMachineMessage)));
                    String message = preferences.getString(GramityConstants.PREF_ANSWERING_MACHINE_MESSAGE, LocaleController.getString("AnsweringMachineDefaultMessage", R.string.AnsweringMachineDefaultMessage));
                    final EditTextBoldCursor input = new EditTextBoldCursor(context);

                    input.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                    input.setTypeface(AndroidUtilities.getTypeface(null));
                    input.setText(message == null || message.isEmpty() || message.equals(" ") ? LocaleController.getString("AnsweringMachineDefaultMessage", R.string.AnsweringMachineDefaultMessage) : message);
                    input.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
                    input.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    input.setHint(LocaleController.getString("AnsweringMachineMessage", R.string.AnsweringMachineMessage));
                    input.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
                    input.setPadding(AndroidUtilities.dp(10), AndroidUtilities.dp(10), AndroidUtilities.dp(10), AndroidUtilities.dp(6));
                    input.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
                    InputFilter[] inputFilters = new InputFilter[1];
                    inputFilters[0] = new InputFilter.LengthFilter(255);
                    input.setFilters(inputFilters);
                    input.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    input.setCursorSize(AndroidUtilities.dp(20));
                    input.setCursorWidth(1.5f);
//                    AndroidUtilities.clearCursorDrawable(input);

                    builder.setView(input);
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            preferences.edit().putString(GramityConstants.PREF_ANSWERING_MACHINE_MESSAGE, input.getText().toString()).apply();
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
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
                    boolean send = preferences.getBoolean(GramityConstants.PREF_DIRECT_SHARE_TO_MENU, true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(GramityConstants.PREF_DIRECT_SHARE_TO_MENU, !send);
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
                } else if (position == cacheRow) {
                    presentFragment(new CacheControlActivity());
                } else if (position == themeRow) {
                    presentFragment(new ThemeActivity());
                    GramityUtilities.snkrGApnr(GramityConstants.GRAMITY_THEMES_ID, GramitySettingsActivity.this, false, false);
                } else if (position == monoColoredIconsRow) {
                    boolean isMonoColored = preferences.getBoolean(GramityConstants.PREF_MONOCOLORED_ICONS, false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(GramityConstants.PREF_MONOCOLORED_ICONS, !isMonoColored);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!isMonoColored);
                    }
                    needRestart = true;
                    toast(context, LocaleController.getString("ThemeChangeToastMessage", R.string.ThemeChangeToastMessage));
                } else if (position == textFontRow) {
                    presentFragment(new FontSelectActivity());
                } else if (position == textSizeRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    final SharedPreferences.Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE).edit();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setCustomTitle(GramityUtilities.alertTitleMaker(context, LocaleController.getString("TextSize", R.string.TextSize)));
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
                } else if (position == dateTimeAgoRow) {
                    boolean timeAgo = preferences.getBoolean(GramityConstants.PREF_DATE_TIME_AGO, true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(GramityConstants.PREF_DATE_TIME_AGO, !timeAgo);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!timeAgo);
                    }
                } else if (position == dateSolarCalendarRow) {
                    boolean solarCalendar = preferences.getBoolean(GramityConstants.PREF_DATE_SOLAR_CALENDAR, true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(GramityConstants.PREF_DATE_SOLAR_CALENDAR, !solarCalendar);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!solarCalendar);
                    }
                } else if (position == exactMemberNumberRow) {
                    boolean exactNumber = preferences.getBoolean(GramityConstants.PREF_EXACT_MEMBER_NUMBER, true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(GramityConstants.PREF_EXACT_MEMBER_NUMBER, !exactNumber);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!exactNumber);
                    }
                } else if (position == chatStatusBubbleRow) {
                    boolean statusIndicator = preferences.getBoolean(GramityConstants.PREF_CHATS_STATUS_BUBBLE, true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(GramityConstants.PREF_CHATS_STATUS_BUBBLE, !statusIndicator);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!statusIndicator);
                    }
                } else if (position == tabsBackgroundColorRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    final ColorPickerDialogBuilder cPDB = ColorPickerDialogBuilder.with(context, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
                    if (position == tabsBackgroundColorRow) {
                        cPDB.initialColor(instanceOfTabsBackgroundColor);
                    }
                    cPDB
                            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                            .setTitle(LocaleController.getString("ChooseColor", R.string.ChooseColor))
                            .lightnessSliderOnly()
                            .density(12)
                            .setPositiveButton(LocaleController.getString("OK", R.string.OK), new ColorPickerClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                    SharedPreferences.Editor editor = preferences.edit();
                                    TextColorCell textCell = (TextColorCell) view;
                                    editor.putInt(GramityConstants.PREF_TABS_BACKGROUND_COLOR, selectedColor);
                                    editor.apply();
                                    textCell.setTextAndColor(LocaleController.getString("itemTabsBackgroundColor", R.string.itemTabsBackgroundColor), selectedColor, true);
                                }
                            })
                            .setNegativeButton(LocaleController.getString("Default", R.string.Default), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences.Editor editor = preferences.edit();
                                    TextColorCell textCell = (TextColorCell) view;
                                    editor.putInt(GramityConstants.PREF_TABS_BACKGROUND_COLOR, GramityUtilities.colorTH());
                                    textCell.setTextAndColor(LocaleController.getString("itemTabsBackgroundColor", R.string.itemTabsBackgroundColor), GramityUtilities.colorTH(), true);
                                    editor.apply();
                                }
                            })
                            .build()
                            .show();
                } else if (position == passcodeRow) {
                    if (UserConfig.passcodeHash.length() > 0) {
                        presentFragment(new PasscodeActivity(2));
                    } else {
                        presentFragment(new PasscodeActivity(0));
                    }
                } else if (position == noNumberRow) {
                    boolean number = preferences.getBoolean(GramityConstants.PREF_PRIVACY_NO_NUMBER, true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(GramityConstants.PREF_PRIVACY_NO_NUMBER, !number);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!number);
                    }
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.mainUserInfoChanged);
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
                    boolean keep = preferences.getBoolean("keepOriginalFilename", true);
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
                            final CharSequence cs = "OS_Player ID: " + userId + "\n\nOS_Reg ID: " + registrationId + "\n\nOS_SDK_type: " + OneSignal.sdkType + "\n\nOS_ver:" + OneSignal.VERSION + "\n\n-------\n\nP_pid: " + Pushe.getPusheId(ApplicationLoader.applicationContext) + "\n\nP_isInit = " + Pushe.isPusheInitialized(ApplicationLoader.applicationContext);

                            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                            builder.setTitle("INFO");
                            builder.setItems(new CharSequence[]{cs}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {
                                        try {
                                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                            android.content.ClipData clip = android.content.ClipData.newPlainText("label", cs);
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
        extraHeightView.setBackgroundColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
        frameLayout.addView(extraHeightView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 88));

        shadowView = new View(context);
        shadowView.setBackgroundResource(R.drawable.header_shadow);
        frameLayout.addView(shadowView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 3));

        nameTextView = new TextView(context);
        nameTextView.setTextColor(Theme.getColor(Theme.key_profile_title));
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        nameTextView.setGravity(Gravity.LEFT);
        nameTextView.setTypeface(AndroidUtilities.getTypeface(null));
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

    private AlertDialog.Builder createDialog(AlertDialog.Builder builder, int i) {
        if (i == chatShowDirectShareBtn) {
            builder.setTitle(LocaleController.getString("ShowDirectShareButton", R.string.ShowDirectShareButton));

            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
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
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
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

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
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
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
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
        nameTextView.setText(LocaleController.getString("AdvancedSettings", R.string.AdvancedSettings));
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

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0: {
                    if (position == overscrollRow) {
                        ((EmptyCell) holder.itemView).setHeight(AndroidUtilities.dp(88));
                    } else {
                        ((EmptyCell) holder.itemView).setHeight(AndroidUtilities.dp(16));
                    }
                    break;
                }
                case 2: {
                    TextColorCell textColorCell = (TextColorCell) holder.itemView;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
                    int initialTabsBackgroundColor = preferences.getInt(GramityConstants.PREF_TABS_BACKGROUND_COLOR, GramityUtilities.colorTH());
                    if (position == tabsBackgroundColorRow) {
                        textColorCell.setTextAndColor(LocaleController.getString("itemTabsBackgroundColor", R.string.itemTabsBackgroundColor), initialTabsBackgroundColor, true);
                    }
                    break;
                }
                case 3: {
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == mediaDetailRow) {
                        textInfoPrivacyCell.setText(LocaleController.getString("KeepOriginalFilenameHelp", R.string.KeepOriginalFilenameHelp));
                        textInfoPrivacyCell.setBackgroundResource(R.drawable.greydivider_bottom);
                    }
                    break;
                }
                case 4: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == dialogsSectionRow) {
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
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Context.MODE_PRIVATE);
                        int size = preferences.getInt(GramityConstants.PREF_TABS_HEIGHT, AndroidUtilities.isTablet() ? 46 : 44);
                        textSettingsCell.setTextAndValue(LocaleController.getString("TabsHeight", R.string.TabsHeight), String.format("%d", size), true);
                    } else if (position == answeringMachineMessageRow) {
                        textSettingsCell.setText(LocaleController.getString("AnsweringMachineMessage", R.string.AnsweringMachineMessage), true);
                    } else if (position == cacheRow) {
                        textSettingsCell.setText(LocaleController.getString("CacheSettings", R.string.CacheSettings), true);
                    } else if (position == themeRow) {
                        textSettingsCell.setTextAndValue(LocaleController.getString("Theme", R.string.Theme), Theme.getCurrentThemeName(), true);
                    } else if (position == textFontRow) {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Context.MODE_PRIVATE);
                        String[] fontNameArray = ApplicationLoader.applicationContext.getResources().getStringArray(R.array.FontNameArr);
                        String fontName = preferences.getString(GramityConstants.PREF_CUSTOM_FONT_NAME, fontNameArray[1]);
                        textSettingsCell.setTextAndValue(LocaleController.getString("itemFontName", R.string.itemFontName), fontName, true);
                    } else if (position == textSizeRow) {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                        int size = preferences.getInt("font_size", AndroidUtilities.isTablet() ? 18 : 16);
                        textSettingsCell.setTextAndValue(LocaleController.getString("TextSize", R.string.TextSize), String.format("%d", size), true);
                    } else if (position == passcodeRow) {
                        textSettingsCell.setText(LocaleController.getString("Passcode", R.string.Passcode), true);
                    }
                    break;
                }
                case 7: {
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
                    if (position == noNumberRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("itemNoNumber", R.string.itemNoNumber), LocaleController.getString("itemNoNumberDescription", R.string.itemNoNumberDescription), preferences.getBoolean(GramityConstants.PREF_PRIVACY_NO_NUMBER, true), true, true);
                    } else if (position == dialogsHideTabsCheckRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("HideTabs", R.string.HideTabs), preferences.getBoolean("hideTabs", false), true);
                    } else if (position == dialogsDisableTabsAnimationCheckRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("DisableTabsAnimation", R.string.DisableTabsAnimation), preferences.getBoolean("disableTabsAnimation", false), true);
                    } else if (position == dialogsInfiniteTabsSwipe) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("InfiniteSwipe", R.string.InfiniteSwipe), preferences.getBoolean(GramityConstants.PREF_INFINITE_TABS_SWIPE, true), true);
                    } else if (position == dialogsHideTabsCounters) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("HideTabsCounters", R.string.HideTabsCounters), preferences.getBoolean("hideTabsCounters", false), true);
                    } else if (position == dialogsTabsCountersCountChats) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("HeaderTabCounterCountChats", R.string.HeaderTabCounterCountChats), preferences.getBoolean("tabsCountersCountChats", false), true);
                    } else if (position == dialogsTabsCountersCountNotMuted) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("HeaderTabCounterCountNotMuted", R.string.HeaderTabCounterCountNotMuted), preferences.getBoolean("tabsCountersCountNotMuted", false), true);
                    } else if (position == answeringMachineRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("AnsweringMachine", R.string.AnsweringMachine), LocaleController.getString("AnsweringMachineDescription", R.string.AnsweringMachineDescription), preferences.getBoolean(GramityConstants.PREF_ANSWERING_MACHINE, false), true, true);
                    } else if (position == chatDirectShareToMenu) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("DirectShareInPopupMenu", R.string.DirectShareInPopupMenu), LocaleController.getString("DirectShareInPopupMenuDescription", R.string.DirectShareInPopupMenuDescription), preferences.getBoolean(GramityConstants.PREF_DIRECT_SHARE_TO_MENU, true), true, true);
                    } else if (position == chatDirectShareFavsFirst) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("DirectShareShowFavsFirst", R.string.DirectShareShowFavsFirst), LocaleController.getString("DirectShareShowFavsFirstDescription", R.string.DirectShareShowFavsFirstDescription), preferences.getBoolean("directShareFavsFirst", true), true, true);
                    } else if (position == monoColoredIconsRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("CustomizationsMonoColoredIcons", R.string.CustomizationsMonoColoredIcons), preferences.getBoolean(GramityConstants.PREF_MONOCOLORED_ICONS, false), true);
                    } else if (position == dateTimeAgoRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("CustomizationsDateTimeAgo", R.string.CustomizationsDateTimeAgo), LocaleController.getString("CustomizationsDateTimeAgoDescription", R.string.CustomizationsDateTimeAgoDescription), preferences.getBoolean(GramityConstants.PREF_DATE_TIME_AGO, true), true, true);
                    } else if (position == dateSolarCalendarRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("CustomizationsDateSolarCalendar", R.string.CustomizationsDateSolarCalendar), LocaleController.getString("CustomizationsDateSolarCalendarDescription", R.string.CustomizationsDateSolarCalendarDescription), preferences.getBoolean(GramityConstants.PREF_DATE_SOLAR_CALENDAR, true), true, true);
                    } else if (position == exactMemberNumberRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("CustomizationsExactMemberNumber", R.string.CustomizationsExactMemberNumber), preferences.getBoolean(GramityConstants.PREF_EXACT_MEMBER_NUMBER, true), true);
                    } else if (position == chatStatusBubbleRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ChatStatusBubble", R.string.ChatStatusBubble), preferences.getBoolean(GramityConstants.PREF_CHATS_STATUS_BUBBLE, true), true);
                    } else if (position == keepOriginalFilenameRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("KeepOriginalFilename", R.string.KeepOriginalFilename), preferences.getBoolean("keepOriginalFilename", true), false);
                    }
                    break;
                }
                case 8: {
                    TextDetailSettingsCell textDetailSettingsCell = (TextDetailSettingsCell) holder.itemView;
                    if (position == dialogsTabsRow) {
                        String value;
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);

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
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
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
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);

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
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();
            return position == noNumberRow || position == dialogsHideTabsCheckRow ||
                    position == dialogsTabsRow || position == dialogsTabsHeightRow || position == dialogsDisableTabsAnimationCheckRow ||
                    position == dialogsInfiniteTabsSwipe || position == dialogsHideTabsCounters || position == dialogsTabsCountersCountChats ||
                    position == dialogsTabsCountersCountNotMuted || position == answeringMachineRow || position == answeringMachineMessageRow || position == chatShowDirectShareBtn || position == chatDirectShareToMenu ||
                    position == chatDirectShareFavsFirst || position == cacheRow || position == themeRow || position == monoColoredIconsRow ||
                    position == dateTimeAgoRow || position == dateSolarCalendarRow || position == exactMemberNumberRow || position == textFontRow ||
                    position == textSizeRow || position == chatStatusBubbleRow || position == tabsBackgroundColorRow ||
                    position == passcodeRow || position == profileSharedOptionsRow || position == keepOriginalFilenameRow || position == versionDescriptionRow;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case 0:
                    view = new EmptyCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 1:
                    view = new ShadowSectionCell(mContext);
                    break;
                case 2:
                    view = new TextColorCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new TextInfoPrivacyCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 5:
                    view = new TextInfoCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    try {
                        PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                        int code = pInfo.versionCode / 10;
                        ((TextInfoCell) view).setText(LocaleController.formatString("TelegramVersion", R.string.TelegramVersion, String.format(Locale.US, "v%s (%d)", pInfo.versionName, code)));
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                    break;
                case 6:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 7:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 8:
                    view = new TextDetailSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == emptyRow || position == overscrollRow) {
                return 0;
            }
            if (position == dialogsSectionBottomRow || position == messagesSectionBottomRow || position == customizationsSectionBottomRow || position == premiumSecuritySectionBottomRow || position == mediaSectionBottomRow) {
                return 1;
            } else if (position == tabsBackgroundColorRow) {
                return 2;
            } else if (position == mediaDetailRow) {
                return 3;
            } else if (position == dialogsSectionRow || position == messagesSectionRow || position == customizationsSectionRow || position == premiumSecuritySectionRow || position == mediaSectionRow) {
                return 4;
            } else if (position == versionDescriptionRow) {
                return 5;
            } else if (position == dialogsTabsHeightRow || position == answeringMachineMessageRow || position == cacheRow || position == themeRow || position == textFontRow || position == textSizeRow || position == passcodeRow) {
                return 6;
            } else if (position == dialogsTabsRow || position == chatShowDirectShareBtn || position == profileSharedOptionsRow) {
                return 8;
            } else {
                return 7;
            }
        }
    }

    @Override
    public ThemeDescription[] getThemeDescriptions() {
        return new ThemeDescription[]{
                new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{EmptyCell.class, TextSettingsCell.class, TextCheckCell.class, HeaderCell.class, TextInfoCell.class, TextDetailSettingsCell.class}, null, null, null, Theme.key_windowBackgroundWhite),
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray),

                new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue),
                new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue),
                new ThemeDescription(extraHeightView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_avatar_actionBarIconBlue),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_avatar_actionBarSelectorBlue),
                new ThemeDescription(nameTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_profile_title),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, null, null, null, null, Theme.key_actionBarDefaultSubmenuBackground),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, null, null, null, null, Theme.key_actionBarDefaultSubmenuItem),

                new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),

                new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider),

                new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow),

                new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText),

                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchThumb),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchThumbChecked),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked),

                new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader),

                new ThemeDescription(listView, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),

                new ThemeDescription(listView, 0, new Class[]{TextInfoCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText5),
        };
    }
}
