/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package org.telegram.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.entypo_typeface_library.Entypo;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.ionicons_typeface_library.Ionicons;
import com.mikepenz.octicons_typeface_library.Octicons;
import com.mikepenz.typeicons_typeface_library.Typeicons;
import com.nightonke.boommenu.Animation.EaseEnum;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextInsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;

import net.hockeyapp.android.FeedbackManager;

import org.gramity.GramityConstants;
import org.gramity.GramityUtilities;
import org.gramity.RevelationActivity;
import org.gramity.database.AndroidDatabaseManager;
import org.gramity.database.Favourite;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.query.SearchQuery;
import org.telegram.messenger.query.StickersQuery;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.MenuDrawable;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Adapters.DialogsAdapter;
import org.telegram.ui.Adapters.DialogsSearchAdapter;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.DialogsEmptyCell;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.DrawerActionCell;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.HashtagSearchCell;
import org.telegram.ui.Cells.HintDialogCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.FragmentContextView;
import org.telegram.ui.Components.JoinGroupAlert;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.StickersAlert;

import java.util.ArrayList;

public class DialogsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;
    private DialogsAdapter dialogsAdapter;
    private DialogsSearchAdapter dialogsSearchAdapter;
    private EmptyTextProgressView searchEmptyView;
    private RadialProgressView progressView;
    private ActionBarMenuItem passcodeItem;
    private ActionBarMenuItem cloudItem; // TGY
    private ActionBarMenuItem proxyItem; // TGY
    //    private ImageView floatingButton; //TGY
    private RecyclerView sideMenu;
    private FragmentContextView fragmentContextView;
    private FragmentContextView fragmentLocationContextView;
    private ChatActivityEnterView commentView;

    private AlertDialog permissionDialog;

    private int prevPosition;
    private int prevTop;
    private boolean scrollUpdated;
    //    private boolean floatingHidden; //TGY
    private final AccelerateDecelerateInterpolator floatingInterpolator = new AccelerateDecelerateInterpolator();

    private boolean checkPermission = true;

    private String selectAlertString;
    private String selectAlertStringGroup;
    private String addToGroupAlertString;
    private int dialogsType;

    public static boolean dialogsLoaded;
    private boolean searching;
    private boolean searchWas;
    private boolean onlySelect;
    private long selectedDialog;
    private String searchString;
    private long openedDialogId;
    private boolean cantSendToChannels;

    private DialogsActivityDelegate delegate;

    // tgy
    private Context contextTgy;
    private BoomMenuButton boomButton;
    private boolean boomHidden;

    private FrameLayout tabsView;
    private LinearLayout tabsLayout;
    private int tabsHeight;
    private ImageView allTab;
    private ImageView usersTab;
    private ImageView groupsTab;
    private ImageView superGroupsTab;
    private ImageView channelsTab;
    private ImageView botsTab;
    private ImageView favsTab;
    private TextView allCounter;
    private TextView usersCounter;
    private TextView groupsCounter;
    private TextView sGroupsCounter;
    private TextView botsCounter;
    private TextView channelsCounter;
    private TextView favsCounter;
//    private boolean countSize;

    private boolean hideTabs;
    private int selectedTab;
    private DialogsAdapter dialogsBackupAdapter;
    private boolean tabsHidden;
    private boolean disableAnimation;
    //

    public interface DialogsActivityDelegate {
        void didSelectDialogs(DialogsActivity fragment, ArrayList<Long> dids, CharSequence message, boolean param);
    }

    public DialogsActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        if (getArguments() != null) {
            onlySelect = arguments.getBoolean("onlySelect", false);
            cantSendToChannels = arguments.getBoolean("cantSendToChannels", false);
            dialogsType = arguments.getInt("dialogsType", 0);
            selectAlertString = arguments.getString("selectAlertString");
            selectAlertStringGroup = arguments.getString("selectAlertStringGroup");
            addToGroupAlertString = arguments.getString("addToGroupAlertString");
        }

        if (searchString == null) {
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.contactsDidLoaded);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.openedChatChanged);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.notificationsSettingsUpdated);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByAck);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByServer);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageSendError);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.didLoadedReplyMessages);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.refreshTabs);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.proxySettingsChanged);

        }

        if (!dialogsLoaded) {
            MessagesController.getInstance().loadDialogs(0, 100, true);
            MessagesController.getInstance().loadHintDialogs();
            ContactsController.getInstance().checkInviteText();
            MessagesController.getInstance().loadPinnedDialogs(0, null);
            StickersQuery.loadRecents(StickersQuery.TYPE_FAVE, false, true, false);
            StickersQuery.checkFeaturedStickers();
            dialogsLoaded = true;
        }
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (searchString == null) {
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.contactsDidLoaded);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.openedChatChanged);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.notificationsSettingsUpdated);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageReceivedByAck);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageReceivedByServer);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageSendError);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didLoadedReplyMessages);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.refreshTabs);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.proxySettingsChanged);
        }
        if (commentView != null) {
            commentView.onDestroy();
        }
        delegate = null;
    }

    private void markAsReadDialog(final boolean all) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        TLRPC.Chat currentChat = MessagesController.getInstance().getChat((int) -selectedDialog);
        TLRPC.User user = MessagesController.getInstance().getUser((int) selectedDialog);
        String title = currentChat != null ? currentChat.title : user != null ? UserObject.getUserName(user) : LocaleController.getString("AppNameTgy", R.string.AppNameTgy);
        builder.setTitle(all ? getHeaderAllTitles() : title);
        builder.setMessage((all ? LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead) : LocaleController.getString("MarkAsRead", R.string.MarkAsRead)) + '\n' + LocaleController.getString("AreYouSure", R.string.AreYouSure));
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (all) {
                    ArrayList<TLRPC.TL_dialog> dialogs = getDialogsArray();
                    if (dialogs != null && !dialogs.isEmpty()) {
                        for (int a = 0; a < dialogs.size(); a++) {
                            TLRPC.TL_dialog dialg = getDialogsArray().get(a);
                            if (dialg.unread_count > 0) {
                                MessagesController.getInstance().markDialogAsRead(dialg.id, dialg.top_message, Math.max(0, dialg.top_message), dialg.last_message_date, true, false);
                            }
                        }
                    }
                } else {
                    TLRPC.TL_dialog dialg = MessagesController.getInstance().dialogs_dict.get(selectedDialog);
                    if (dialg.unread_count > 0) {
                        MessagesController.getInstance().markDialogAsRead(dialg.id, dialg.top_message, Math.max(0, dialg.top_message), dialg.last_message_date, true, false); //dialg.top_message instead of dialg.last_read
                    }
                }
            }
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        showDialog(builder.create());
    }

    @Override
    public View createView(final Context context) {
        contextTgy = context;
        final SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);

        searching = false;
        searchWas = false;

        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                Theme.createChatResources(context, false);
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        if (!onlySelect && searchString == null) {
            passcodeItem = menu.addItem(1, R.drawable.lock_close);
            updatePasscodeButton();
        }

        MessagesController.getInstance().reRunUpdateTimerProc(); //TGY - specter

//        new PorterDuffColorFilter(lastCloudColor = Theme.getColor(Theme.key_chats_menuCloud), PorterDuff.Mode.MULTIPLY));

        //TGY
        final SharedPreferences pref = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();

        //TGY
        proxyItem = menu.addItem(3, new IconicsDrawable(context, FontAwesome.Icon.faw_keycdn).sizeDp(24).color(Color.WHITE));
        proxyItem.setAlpha(pref.getBoolean("proxy_enabled", false) ? 1.0f : 0.5f);
        if (GramityUtilities.isAllowedPackage()) {
            cloudItem = menu.addItem(7,new IconicsDrawable(context, FontAwesome.Icon.faw_user_secret).sizeDp(24).color(Color.WHITE));
            cloudItem.setAlpha(advancedPrefs.getBoolean(GramityConstants.PREF_SPECTER_MODE, false) ? 1.0f : 0.5f);
        } else {
            cloudItem = menu.addItem(7, R.drawable.bookmark_large);
        }
        final ActionBarMenuItem item = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
            @Override
            public void onSearchExpand() {
                //plus
                refreshTabAndListViews(true);
                //
                searching = true;
                if (listView != null) {
                    if (searchString != null) {
                        listView.setEmptyView(searchEmptyView);
                        progressView.setVisibility(View.GONE);
                    }
                    if (!onlySelect) {
//                        floatingButton.setVisibility(View.GONE);
                        boomButton.setVisibility(View.GONE); //TGY
                    }
                }
                updatePasscodeButton();
            }

            @Override
            public boolean canCollapseSearch() {
                if (searchString != null) {
                    finishFragment();
                    return false;
                }
                return true;
            }

            @Override
            public void onSearchCollapse() {
                //plus
                refreshTabAndListViews(false);
                //
                searching = false;
                searchWas = false;
                if (listView != null) {
                    if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
                        listView.setEmptyView(progressView);
                    } else {
                        progressView.setVisibility(View.GONE);
                        listView.setEmptyView(null);
                    }
                    searchEmptyView.setVisibility(View.GONE);
                    if (!onlySelect) {
                        /*floatingButton.setVisibility(View.VISIBLE);
                        floatingHidden = true;
                        floatingButton.setTranslationY(AndroidUtilities.dp(100) + tabsHeight); //BottGrav
                        hideFloatingButton(false);*/
                        //TGY
                        boomButton.setVisibility(View.VISIBLE);
                        boomHidden = true;
                        boomButton.setTranslationY(AndroidUtilities.dp(100) + tabsHeight); //BottGrav
                        hideBoomButton(false);
                        //
                    }
                    if (listView.getAdapter() != dialogsAdapter) {
                        listView.setAdapter(dialogsAdapter);
                        dialogsAdapter.notifyDataSetChanged();
                    }
                }
                if (dialogsSearchAdapter != null) {
                    dialogsSearchAdapter.searchDialogs(null);
                }
                updatePasscodeButton();
            }

            @Override
            public void onTextChanged(EditText editText) {
                String text = editText.getText().toString();
                if (text.length() != 0 || dialogsSearchAdapter != null && dialogsSearchAdapter.hasRecentRearch()) {
                    searchWas = true;
                    if (dialogsSearchAdapter != null && listView.getAdapter() != dialogsSearchAdapter) {
                        listView.setAdapter(dialogsSearchAdapter);
                        dialogsSearchAdapter.notifyDataSetChanged();
                    }
                    if (searchEmptyView != null && listView.getEmptyView() != searchEmptyView) {
                        progressView.setVisibility(View.GONE);
                        searchEmptyView.showTextView();
                        listView.setEmptyView(searchEmptyView);
                    }
                }
                if (dialogsSearchAdapter != null) {
                    dialogsSearchAdapter.searchDialogs(text);
                }
            }
        });
        item.getSearchField().setHint(LocaleController.getString("Search", R.string.Search));
        if (onlySelect) {
            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            if (dialogsType == 3 && selectAlertString == null) {
                actionBar.setTitle(LocaleController.getString("ForwardTo", R.string.ForwardTo));
            } else {
                actionBar.setTitle(LocaleController.getString("SelectChat", R.string.SelectChat));
            }
        } else {
            if (searchString != null) {
                actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            } else {
                actionBar.setBackButtonDrawable(new MenuDrawable());
            }
            if (BuildVars.DEBUG_VERSION) {
                actionBar.setTitle(LocaleController.getString("AppNameBeta", R.string.AppNameBetaTgy));
            } else {
                actionBar.setTitle(LocaleController.getString("AppNameTgy", R.string.AppNameTgy));
            }
        }
        actionBar.setAllowOverlayTitle(true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    if (onlySelect) {
                        finishFragment();
                    } else if (parentLayout != null) {
                        // plus
                        //if (!hideTabs) {
                        //    parentLayout.getDrawerLayoutContainer().setAllowOpenDrawer(true, false);
                        //}
                        //
                        parentLayout.getDrawerLayoutContainer().openDrawer(false);
                    }
                } else if (id == 1) {
                    UserConfig.appLocked = !UserConfig.appLocked;
                    UserConfig.saveConfig(false);
                    updatePasscodeButton();
                } else if (id == 7) { // TGY
                    if (GramityUtilities.isAllowedPackage()) {
                        boolean specter = advancedPrefs.getBoolean(GramityConstants.PREF_SPECTER_MODE, false);
                        advancedPrefs.edit().putBoolean(GramityConstants.PREF_SPECTER_MODE, !specter).apply();
                        MessagesController.getInstance().reRunUpdateTimerProc();
                        if (!specter) {
                            cloudItem.setAlpha(1.0f);
                            Toast.makeText(context, LocaleController.getString("SpecterModeActivated", R.string.SpecterModeActivated), Toast.LENGTH_SHORT).show();

                        } else {
                            cloudItem.setAlpha(0.5f);
                            Toast.makeText(context, LocaleController.getString("SpecterModeDeactivated", R.string.SpecterModeDeactivated), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Bundle args = new Bundle();
                        args.putInt("user_id", UserConfig.getClientUserId());
                        presentFragment(new ChatActivity(args));
                    }
                } else if (id == 3) { // TGY
                    if (!pref.getBoolean("proxy_enabled", false)) {
                        String proxyIP = GramityUtilities.getRandomCustomProxy();

                        editor.putBoolean("proxy_enabled", true);
                        editor.putString("proxy_ip", proxyIP);
//                    int p = Utilities.parseInt("1080");
                        editor.putInt("proxy_port", GramityConstants.SCK_P);
                        // if there's no u/p
                        editor.remove("proxy_pass");
                        editor.remove("proxy_user");
                    /*if (TextUtils.isEmpty(password)) {
                        editor.remove("proxy_pass");
                    } else {
                        editor.putString("proxy_pass", password);
                    }
                    if (TextUtils.isEmpty(user)) {
                        editor.remove("proxy_user");
                    } else {
                        editor.putString("proxy_user", user);
                    }*/
                        editor.commit();
                        ConnectionsManager.native_setProxySettings(proxyIP, GramityConstants.SCK_P, "", "");
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.proxySettingsChanged);

                        proxyItem.setAlpha(1.0f);
                        Toast.makeText(context, LocaleController.getString("SocksActivated", R.string.SocksActivated), Toast.LENGTH_SHORT).show();

                    } else {
                        editor.putBoolean("proxy_enabled", false);
                        editor.commit();
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.proxySettingsChanged);

                        proxyItem.setAlpha(0.5f);
                        Toast.makeText(context, LocaleController.getString("SocksDeactivated", R.string.SocksDeactivated), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        actionBar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (org.telegram.messenger.BuildConfig.DEBUG || org.telegram.messenger.BuildConfig.BUILD_TYPE.equals("debug")) {
                    CharSequence[] itemTitles = {
                            "Chan",
                            "Table"
                    };
                    Drawable[] itemDrawables = {
                            new IconicsDrawable(context, FontAwesome.Icon.faw_server).sizeDp(24).color(0xff00CC66),
                            new IconicsDrawable(context, FontAwesome.Icon.faw_database).sizeDp(24).color(0xff8F2D56),
                    };
                    BottomSheet.Builder dBuilder = new BottomSheet.Builder(context);
                    dBuilder.setItems(itemTitles, itemDrawables, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    GramityUtilities.snkrGApnr("TeessT");
                                    break;
                                case 1:
                                    context.startActivity(new Intent(context, AndroidDatabaseManager.class));
                                    break;
                            }
                        }
                    });
                    showDialog(dBuilder.create());
                    return true;
                }
                return false;
            }
        });

        if (sideMenu != null) {
            sideMenu.setBackgroundColor(Theme.getColor(Theme.key_chats_menuBackground));
            sideMenu.setGlowColor(Theme.getColor(Theme.key_chats_menuBackground));
            sideMenu.getAdapter().notifyDataSetChanged();
        }

        SizeNotifierFrameLayout contentView = new SizeNotifierFrameLayout(context) { //converted from FrameLayout to SizeNotifierFrameLayout in ver 4.6

            int inputFieldHeight = 0;

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);

                setMeasuredDimension(widthSize, heightSize);
                heightSize -= getPaddingTop();

                measureChildWithMargins(actionBar, widthMeasureSpec, 0, heightMeasureSpec, 0);

                int keyboardSize = getKeyboardHeight();
                int childCount = getChildCount();

                if (commentView != null) {
                    measureChildWithMargins(commentView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                    Object tag = commentView.getTag();
                    if (tag != null && tag.equals(2)) {
                        if (keyboardSize <= AndroidUtilities.dp(20) && !AndroidUtilities.isInMultiwindow) {
                            heightSize -= commentView.getEmojiPadding();
                        }
                        inputFieldHeight = commentView.getMeasuredHeight();
                    } else {
                        inputFieldHeight = 0;
                    }
                }

                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    if (child == null || child.getVisibility() == GONE || child == commentView || child == actionBar) {
                        continue;
                    }
                    if (child == listView || child == progressView || child == searchEmptyView) {
                        int contentWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
                        int contentHeightSpec = MeasureSpec.makeMeasureSpec(Math.max(AndroidUtilities.dp(10), heightSize - inputFieldHeight + AndroidUtilities.dp(2)), MeasureSpec.EXACTLY);
                        child.measure(contentWidthSpec, contentHeightSpec);
                    } else if (commentView != null && commentView.isPopupView(child)) {
                        if (AndroidUtilities.isInMultiwindow) {
                            if (AndroidUtilities.isTablet()) {
                                child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(Math.min(AndroidUtilities.dp(320), heightSize - inputFieldHeight - AndroidUtilities.statusBarHeight + getPaddingTop()), MeasureSpec.EXACTLY));
                            } else {
                                child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize - inputFieldHeight - AndroidUtilities.statusBarHeight + getPaddingTop(), MeasureSpec.EXACTLY));
                            }
                        } else {
                            child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(child.getLayoutParams().height, MeasureSpec.EXACTLY));
                        }
                    } else {
                        measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                    }
                }
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                final int count = getChildCount();

                int paddingBottom;
                Object tag = commentView != null ? commentView.getTag() : null;
                if (tag != null && tag.equals(2)) {
                    paddingBottom = getKeyboardHeight() <= AndroidUtilities.dp(20) && !AndroidUtilities.isInMultiwindow ? commentView.getEmojiPadding() : 0;
                } else {
                    paddingBottom = 0;
                }
                setBottomClip(paddingBottom);

                for (int i = 0; i < count; i++) {
                    final View child = getChildAt(i);
                    if (child.getVisibility() == GONE) {
                        continue;
                    }
                    final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                    final int width = child.getMeasuredWidth();
                    final int height = child.getMeasuredHeight();

                    int childLeft;
                    int childTop;

                    int gravity = lp.gravity;
                    if (gravity == -1) {
                        gravity = Gravity.TOP | Gravity.LEFT;
                    }

                    final int absoluteGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
                    final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                    switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                        case Gravity.CENTER_HORIZONTAL:
                            childLeft = (r - l - width) / 2 + lp.leftMargin - lp.rightMargin;
                            break;
                        case Gravity.RIGHT:
                            childLeft = r - width - lp.rightMargin;
                            break;
                        case Gravity.LEFT:
                        default:
                            childLeft = lp.leftMargin;
                    }

                    switch (verticalGravity) {
                        case Gravity.TOP:
                            childTop = lp.topMargin + getPaddingTop();
                            break;
                        case Gravity.CENTER_VERTICAL:
                            childTop = ((b - paddingBottom) - t - height) / 2 + lp.topMargin - lp.bottomMargin;
                            break;
                        case Gravity.BOTTOM:
                            childTop = ((b - paddingBottom) - t) - height - lp.bottomMargin;
                            break;
                        default:
                            childTop = lp.topMargin;
                    }

                    if (commentView != null && commentView.isPopupView(child)) {
                        if (AndroidUtilities.isInMultiwindow) {
                            childTop = commentView.getTop() - child.getMeasuredHeight() + AndroidUtilities.dp(1);
                        } else {
                            childTop = commentView.getBottom();
                        }
                    }
                    child.layout(childLeft, childTop, childLeft + width, childTop + height);
                }

                notifyHeightChanged();
            }
        };
        fragmentView = contentView;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(true);
        listView.setItemAnimator(null);
        listView.setInstantClick(true);
        listView.setLayoutAnimation(null);
        listView.setTag(4);
        layoutManager = new LinearLayoutManager(context) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(layoutManager);
        listView.setVerticalScrollbarPosition(LocaleController.isRTL ? RecyclerListView.SCROLLBAR_POSITION_LEFT : RecyclerListView.SCROLLBAR_POSITION_RIGHT);

        DialogsOnTouch onTouchListener = new DialogsOnTouch(context);
        listView.setOnTouchListener(onTouchListener);

        contentView.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (listView == null || listView.getAdapter() == null || getParentActivity() == null) {
                    return;
                }
                long dialog_id = 0;
                int message_id = 0;
                RecyclerView.Adapter adapter = listView.getAdapter();
                if (adapter == dialogsAdapter) {
                    TLObject object = dialogsAdapter.getItem(position);
                    if (object instanceof TLRPC.TL_dialog) {
                        dialog_id = ((TLRPC.TL_dialog) object).id;
                    } else if (object instanceof TLRPC.TL_recentMeUrlChat) {
                        dialog_id = -((TLRPC.TL_recentMeUrlChat) object).chat_id;
                    } else if (object instanceof TLRPC.TL_recentMeUrlUser) {
                        dialog_id = ((TLRPC.TL_recentMeUrlUser) object).user_id;
                    } else if (object instanceof TLRPC.TL_recentMeUrlChatInvite) {
                        TLRPC.TL_recentMeUrlChatInvite chatInvite = (TLRPC.TL_recentMeUrlChatInvite) object;
                        TLRPC.ChatInvite invite = chatInvite.chat_invite;
                        if (invite.chat == null && (!invite.channel || invite.megagroup) || invite.chat != null && (!ChatObject.isChannel(invite.chat) || invite.chat.megagroup)) {
                            String hash = chatInvite.url;
                            int index = hash.indexOf('/');
                            if (index > 0) {
                                hash = hash.substring(index + 1);
                            }
                            showDialog(new JoinGroupAlert(getParentActivity(), invite, hash, DialogsActivity.this));
                            return;
                        } else {
                            if (invite.chat != null) {
                                dialog_id = -invite.chat.id;
                            } else {
                                return;
                            }
                        }
                    } else if (object instanceof TLRPC.TL_recentMeUrlStickerSet) {
                        TLRPC.StickerSet stickerSet = ((TLRPC.TL_recentMeUrlStickerSet) object).set.set;
                        TLRPC.TL_inputStickerSetID set = new TLRPC.TL_inputStickerSetID();
                        set.id = stickerSet.id;
                        set.access_hash = stickerSet.access_hash;
                        showDialog(new StickersAlert(getParentActivity(), DialogsActivity.this, set, null, null));
                        return;
                    } else if (object instanceof TLRPC.TL_recentMeUrlUnknown) {
                        return;
                    } else {
                        return;
                    }
                } else if (adapter == dialogsSearchAdapter) {
                    Object obj = dialogsSearchAdapter.getItem(position);
                    if (obj instanceof TLRPC.User) {
                        dialog_id = ((TLRPC.User) obj).id;
                        if (dialogsSearchAdapter.isGlobalSearch(position)) {
                            ArrayList<TLRPC.User> users = new ArrayList<>();
                            users.add((TLRPC.User) obj);
                            MessagesController.getInstance().putUsers(users, false);
                            MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
                        }
                        if (!onlySelect) {
                            dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.User) obj);
                        }
                    } else if (obj instanceof TLRPC.Chat) {
                        if (dialogsSearchAdapter.isGlobalSearch(position)) {
                            ArrayList<TLRPC.Chat> chats = new ArrayList<>();
                            chats.add((TLRPC.Chat) obj);
                            MessagesController.getInstance().putChats(chats, false);
                            MessagesStorage.getInstance().putUsersAndChats(null, chats, false, true);
                        }
                        if (((TLRPC.Chat) obj).id > 0) {
                            dialog_id = -((TLRPC.Chat) obj).id;
                        } else {
                            dialog_id = AndroidUtilities.makeBroadcastId(((TLRPC.Chat) obj).id);
                        }
                        if (!onlySelect) {
                            dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.Chat) obj);
                        }
                    } else if (obj instanceof TLRPC.EncryptedChat) {
                        dialog_id = ((long) ((TLRPC.EncryptedChat) obj).id) << 32;
                        if (!onlySelect) {
                            dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.EncryptedChat) obj);
                        }
                    } else if (obj instanceof MessageObject) {
                        MessageObject messageObject = (MessageObject) obj;
                        dialog_id = messageObject.getDialogId();
                        message_id = messageObject.getId();
                        dialogsSearchAdapter.addHashtagsFromMessage(dialogsSearchAdapter.getLastSearchString());
                    } else if (obj instanceof String) {
                        actionBar.openSearchField((String) obj);
                    }
                }

                if (dialog_id == 0) {
                    return;
                }

                if (onlySelect) {
                    if (dialogsAdapter.hasSelectedDialogs()) {
                        dialogsAdapter.addOrRemoveSelectedDialog(dialog_id, view);
                        updateSelectedCount();
                    } else {
                        didSelectResult(dialog_id, true, false);
                    }
                } else {
                    Bundle args = new Bundle();
                    int lower_part = (int) dialog_id;
                    int high_id = (int) (dialog_id >> 32);
                    if (lower_part != 0) {
                        if (high_id == 1) {
                            args.putInt("chat_id", lower_part);
                        } else {
                            if (lower_part > 0) {
                                args.putInt("user_id", lower_part);
                            } else if (lower_part < 0) {
                                if (message_id != 0) {
                                    TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_part);
                                    if (chat != null && chat.migrated_to != null) {
                                        args.putInt("migrated_to", lower_part);
                                        lower_part = -chat.migrated_to.channel_id;
                                    }
                                }
                                args.putInt("chat_id", -lower_part);
                            }
                        }
                    } else {
                        args.putInt("enc_id", high_id);
                    }
                    if (message_id != 0) {
                        args.putInt("message_id", message_id);
                    } else {
                        if (actionBar != null) {
                            actionBar.closeSearchField();
                        }
                    }
                    if (AndroidUtilities.isTablet()) {
                        if (openedDialogId == dialog_id && adapter != dialogsSearchAdapter) {
                            return;
                        }
                        if (dialogsAdapter != null) {
                            dialogsAdapter.setOpenedDialogId(openedDialogId = dialog_id);
                            updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
                        }
                    }
                    if (searchString != null) {
                        if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                            presentFragment(new ChatActivity(args));
                        }
                    } else {
                        if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {
                            presentFragment(new ChatActivity(args));
                        }
                    }
                }
            }
        });
        listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener() {
            @Override
            public boolean onItemClick(View view, int position) {
                if (getParentActivity() == null) {
                    return false;
                }
                RecyclerView.Adapter adapter = listView.getAdapter();
                if (adapter == dialogsSearchAdapter) {
                    Object item = dialogsSearchAdapter.getItem(position);
                    if (item instanceof String || dialogsSearchAdapter.isRecentSearchDisplayed()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("AppNameTgy", R.string.AppNameTgy));
                        builder.setMessage(LocaleController.getString("ClearSearch", R.string.ClearSearch));
                        builder.setPositiveButton(LocaleController.getString("ClearButton", R.string.ClearButton).toUpperCase(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (dialogsSearchAdapter.isRecentSearchDisplayed()) {
                                    dialogsSearchAdapter.clearRecentSearch();
                                } else {
                                    dialogsSearchAdapter.clearRecentHashtags();
                                }
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showDialog(builder.create());
                        return true;
                    }
                    return false;
                }
                TLRPC.TL_dialog dialog;
                ArrayList<TLRPC.TL_dialog> dialogs = getDialogsArray();
                if (position < 0 || position >= dialogs.size()) {
                    return false;
                }
                dialog = dialogs.get(position);
                if (onlySelect) {
                    if (dialogsType != 3 || selectAlertString != null) {
                        return false;
                    }
                    dialogsAdapter.addOrRemoveSelectedDialog(dialog.id, view);
                    updateSelectedCount();
                } else {
                    selectedDialog = dialog.id;
                    final boolean pinned = dialog.pinned;

                    BottomSheet.Builder builder = new BottomSheet.Builder(getParentActivity());
                    int lower_id = (int) selectedDialog;
                    int high_id = (int) (selectedDialog >> 32);

                    boolean isMuted = MessagesController.getInstance().isDialogMuted(selectedDialog);
                    CharSequence csMute = isMuted ? LocaleController.getString("UnmuteNotifications", R.string.UnmuteNotifications) : LocaleController.getString("MuteNotifications", R.string.MuteNotifications);
                    final boolean isFav = Favourite.isFavourite(dialog.id);
                    CharSequence csFavorite = isFav ? LocaleController.getString("DeleteFromFavorites", R.string.DeleteFromFavorites) : LocaleController.getString("AddToFavorites", R.string.AddToFavorites);
                    CharSequence csMarkAsRead = LocaleController.getString("MarkAsRead", R.string.MarkAsRead);
                    CharSequence csShortcut = LocaleController.getString("AddShortcut", R.string.AddShortcut);

                    boolean isMonoColored = advancedPrefs.getBoolean(GramityConstants.PREF_MONOCOLORED_ICONS, false);
                    int iconSize = 24;

                    Drawable drawableUnpin = new IconicsDrawable(context, CommunityMaterial.Icon.cmd_pin_off).sizeDp(iconSize).color(isMonoColored ? Theme.getColor(Theme.key_dialogIcon) : 0xff006BA6); // SAPPHIRE BLUE
                    Drawable drawablePin = new IconicsDrawable(context, CommunityMaterial.Icon.cmd_pin).sizeDp(iconSize).color(isMonoColored ? Theme.getColor(Theme.key_dialogIcon) : 0xff0496FF); // AZURE
                    Drawable drawableClear = new IconicsDrawable(context, CommunityMaterial.Icon.cmd_broom).sizeDp(iconSize).color(isMonoColored ? Theme.getColor(Theme.key_dialogIcon) : 0xff00CC66); // GO GREEN
                    Drawable drawableLeave = new IconicsDrawable(context, CommunityMaterial.Icon.cmd_export).sizeDp(iconSize).color(isMonoColored ? Theme.getColor(Theme.key_dialogIcon) : 0xff8F2D56); // DARK RASPBERRY
                    Drawable drawableDelete = new IconicsDrawable(context, CommunityMaterial.Icon.cmd_delete_forever).sizeDp(iconSize).color(isMonoColored ? Theme.getColor(Theme.key_dialogIcon) : 0xff8F2D56); // DARK RASPBERRY
                    Drawable drawableUnmute = new IconicsDrawable(context, CommunityMaterial.Icon.cmd_bell_ring).sizeDp(iconSize).color(isMonoColored ? Theme.getColor(Theme.key_dialogIcon) : 0xffFF5B42); // ORANGE SODA
                    Drawable drawableMute = new IconicsDrawable(context, CommunityMaterial.Icon.cmd_bell_off).sizeDp(iconSize).color(isMonoColored ? Theme.getColor(Theme.key_dialogIcon) : 0xffFE4A49); // TART ORANGE
                    Drawable drawableUnFav = new IconicsDrawable(context, CommunityMaterial.Icon.cmd_star_off).sizeDp(iconSize).color(isMonoColored ? Theme.getColor(Theme.key_dialogIcon) : 0xffFAF33E); // MAXIMUM YELLOW
                    Drawable drawableFav = new IconicsDrawable(context, CommunityMaterial.Icon.cmd_star_circle).sizeDp(iconSize).color(isMonoColored ? Theme.getColor(Theme.key_dialogIcon) : 0xffF0C808); // YELLOW (MUNSELL)
                    Drawable drawableMarkAsRead = new IconicsDrawable(context, CommunityMaterial.Icon.cmd_eye).sizeDp(iconSize).color(isMonoColored ? Theme.getColor(Theme.key_dialogIcon) : 0xff0F2C52); // SPACE CADET
//                Drawable drawableMarkAsUnRead = new IconicsDrawable(context, CommunityMaterial.Icon.cmd_eye_off).sizeDp(iconSize).color(isMonoColored ? Theme.getColor(Theme.key_dialogIcon) : 0xff006BA6);
                    Drawable drawableShortcut = new IconicsDrawable(context, CommunityMaterial.Icon.cmd_book_plus).sizeDp(iconSize).color(isMonoColored ? Theme.getColor(Theme.key_dialogIcon) : 0xff009688); // DARK CYAN

                    if (DialogObject.isChannel(dialog)) {
                        final TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_id);
                        CharSequence items[];
                        Drawable icons[] = new Drawable[]{
                                dialog.pinned ? drawableUnpin : drawablePin,
                                drawableClear,
                                drawableLeave,
                                isMuted ? drawableUnmute : drawableMute,
                                isFav ? drawableUnFav : drawableFav,
                                drawableMarkAsRead,
                                drawableShortcut
                        };
                        if (chat != null && chat.megagroup) {
                            items = new CharSequence[]{
                                    dialog.pinned || MessagesController.getInstance().canPinDialog(false) ? (dialog.pinned ? LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop) : LocaleController.getString("PinToTop", R.string.PinToTop)) : null,
                                    TextUtils.isEmpty(chat.username) ? LocaleController.getString("ClearHistory", R.string.ClearHistory) : LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache),
                                    LocaleController.getString("LeaveMegaMenu", R.string.LeaveMegaMenu),
                                    csFavorite,
                                    csMarkAsRead,
                                    csShortcut
                            };
                        } else if (chat != null && GramityConstants.OFFICIAL_CHAN.equalsIgnoreCase(chat.username)) {
                            items = new CharSequence[]{
                                    dialog.pinned || MessagesController.getInstance().canPinDialog(false) ? (dialog.pinned ? LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop) : LocaleController.getString("PinToTop", R.string.PinToTop)) : null,
                                    LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache),
                                    LocaleController.getString("ChanLeave", R.string.ChanLeave),
                                    csMute,
                                    csFavorite,
                                    csMarkAsRead,
                                    csShortcut
                            };
                        } else {
                            items = new CharSequence[]{
                                    dialog.pinned || MessagesController.getInstance().canPinDialog(false) ? (dialog.pinned ? LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop) : LocaleController.getString("PinToTop", R.string.PinToTop)) : null,
                                    LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache),
                                    LocaleController.getString("LeaveChannelMenu", R.string.LeaveChannelMenu),
                                    csMute,
                                    csFavorite,
                                    csMarkAsRead,
                                    csShortcut
                            };
                        }
                        builder.setItems(items, icons, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, final int which) {
                                // TGY
                                if (which == 3) {
                                    boolean muted = MessagesController.getInstance().isDialogMuted(selectedDialog);
                                    if (!muted) {
                                        showDialog(AlertsCreator.createMuteAlert(getParentActivity(), selectedDialog));
                                    } else {
                                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.putInt("notify2_" + selectedDialog, 0);
                                        MessagesStorage.getInstance().setDialogFlags(selectedDialog, 0);
                                        editor.commit();
                                        TLRPC.TL_dialog dialg = MessagesController.getInstance().dialogs_dict.get(selectedDialog);
                                        if (dialg != null) {
                                            dialg.notify_settings = new TLRPC.TL_peerNotifySettings();
                                        }
                                        NotificationsController.updateServerNotificationsSettings(selectedDialog);
                                    }
                                } else if (which == 4) {
                                    TLRPC.TL_dialog dialg = MessagesController.getInstance().dialogs_dict.get(selectedDialog);
                                    if (isFav) {
                                        Favourite.deleteFavourite(selectedDialog);
                                        MessagesController.getInstance().dialogsFavs.remove(dialg);
                                    } else {
                                        Favourite.addFavourite(selectedDialog);
                                        MessagesController.getInstance().dialogsFavs.add(dialg);
                                    }
                                    if (dialogsType == 13) { //Fav Dialog
                                        dialogsAdapter.notifyDataSetChanged();
                                        if (!hideTabs) {
                                            updateTabs();
                                        }
                                    }
                                    unreadCount(MessagesController.getInstance().dialogsFavs, favsCounter);
                                } else if (which == 5) {
                                    markAsReadDialog(false);
                                } else if (which == 6) {
                                    AndroidUtilities.installShortcut(selectedDialog);
                                }
                                //
                                else {
                                    if (which == 0) {
                                        if (MessagesController.getInstance().pinDialog(selectedDialog, !pinned, null, 0) && !pinned) {
                                            listView.smoothScrollToPosition(0);
                                        }
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
//                                    builder.setTitle(LocaleController.getString("AppNameTgy", R.string.AppNameTgy));
                                        builder.setTitle(chat != null ? chat.title : LocaleController.getString("AppNameTgy", R.string.AppNameTgy));
                                        if (which == 1) {
                                            if (chat != null && chat.megagroup) {
                                                if (TextUtils.isEmpty(chat.username)) {
                                                    builder.setMessage(LocaleController.getString("AreYouSureClearHistory", R.string.AreYouSureClearHistory));
                                                } else {
                                                    builder.setMessage(LocaleController.getString("AreYouSureClearHistoryGroup", R.string.AreYouSureClearHistoryGroup));
                                                }
                                            } else {
                                                builder.setMessage(LocaleController.getString("AreYouSureClearHistoryChannel", R.string.AreYouSureClearHistoryChannel));
                                            }
                                            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    if (chat != null && chat.megagroup && TextUtils.isEmpty(chat.username)) {
                                                        MessagesController.getInstance().deleteDialog(selectedDialog, 1);
                                                    } else {
                                                        MessagesController.getInstance().deleteDialog(selectedDialog, 2);
                                                    }
                                                }
                                            });
                                        } else {
                                            if (chat != null && chat.megagroup) {
                                                builder.setMessage(LocaleController.getString("MegaLeaveAlert", R.string.MegaLeaveAlert));
                                            } else {
                                                if (GramityConstants.OFFICIAL_CHAN.equalsIgnoreCase(chat.username)) { //TGY
                                                    builder.setMessage(LocaleController.getString("ChanLeaveAlert", R.string.ChanLeaveAlert));
                                                } else {
                                                    builder.setMessage(LocaleController.getString("ChannelLeaveAlert", R.string.ChannelLeaveAlert));
                                                }
                                            }
                                            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    MessagesController.getInstance().deleteUserFromChat((int) -selectedDialog, UserConfig.getCurrentUser(), null);
                                                    if (AndroidUtilities.isTablet()) {
                                                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, selectedDialog);
                                                    }
                                                }
                                            });
                                        }
                                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                                        showDialog(builder.create());
                                    }
                                }
                            }
                        });
                        showDialog(builder.create());
                    } else {
                        final boolean isChat = lower_id < 0 && high_id != 1;
                        TLRPC.User user = null;
                        if (!isChat && lower_id > 0 && high_id != 1) {
                            user = MessagesController.getInstance().getUser(lower_id);
                        }
                        final boolean isBot = user != null && user.bot;

                        builder.setItems(new CharSequence[]{
                                dialog.pinned || MessagesController.getInstance().canPinDialog(lower_id == 0) ? (dialog.pinned ? LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop) : LocaleController.getString("PinToTop", R.string.PinToTop)) : null,
                                LocaleController.getString("ClearHistory", R.string.ClearHistory),
                                isChat ? LocaleController.getString("DeleteChat", R.string.DeleteChat) : isBot ? LocaleController.getString("DeleteAndStop", R.string.DeleteAndStop) : LocaleController.getString("Delete", R.string.Delete),
                                csMute,
                                csFavorite,
                                csMarkAsRead,
                                csShortcut
                        }, new Drawable[]{
                                dialog.pinned ? drawableUnpin : drawablePin,
                                drawableClear,
                                isChat ? drawableLeave : drawableDelete,
                                isMuted ? drawableUnmute : drawableMute,
                                isFav ? drawableUnFav : drawableFav,
                                drawableMarkAsRead,
                                drawableShortcut
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, final int which) {
                                // TGY
                                if (which == 3) {
                                    boolean muted = MessagesController.getInstance().isDialogMuted(selectedDialog);
                                    if (!muted) {
                                        showDialog(AlertsCreator.createMuteAlert(getParentActivity(), selectedDialog));
                                    } else {
                                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.putInt("notify2_" + selectedDialog, 0);
                                        MessagesStorage.getInstance().setDialogFlags(selectedDialog, 0);
                                        editor.commit();
                                        TLRPC.TL_dialog dialg = MessagesController.getInstance().dialogs_dict.get(selectedDialog);
                                        if (dialg != null) {
                                            dialg.notify_settings = new TLRPC.TL_peerNotifySettings();
                                        }
                                        NotificationsController.updateServerNotificationsSettings(selectedDialog);
                                    }
                                } else if (which == 4) {
                                    TLRPC.TL_dialog dialg = MessagesController.getInstance().dialogs_dict.get(selectedDialog);
                                    if (isFav) {
                                        Favourite.deleteFavourite(selectedDialog);
                                        MessagesController.getInstance().dialogsFavs.remove(dialg);
                                    } else {
                                        Favourite.addFavourite(selectedDialog);
                                        MessagesController.getInstance().dialogsFavs.add(dialg);
                                    }
                                    if (dialogsType == 10) { //Fav Dialog
                                        dialogsAdapter.notifyDataSetChanged();
                                        if (!hideTabs) {
                                            updateTabs();
                                        }
                                    }
                                    unreadCount(MessagesController.getInstance().dialogsFavs, favsCounter);
                                } else if (which == 5) {
                                    markAsReadDialog(false);
                                } else if (which == 6) {
                                    AndroidUtilities.installShortcut(selectedDialog);
                                }
                                //
                                else {
                                    if (which == 0) {
                                        if (MessagesController.getInstance().pinDialog(selectedDialog, !pinned, null, 0) && !pinned) {
                                            listView.smoothScrollToPosition(0);
                                        }
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                                        //TGY
                                        //builder.setTitle(LocaleController.getString("AppNameTgy", R.string.AppNameTgy));
                                        TLRPC.Chat currentChat = MessagesController.getInstance().getChat((int) -selectedDialog);
                                        TLRPC.User user = MessagesController.getInstance().getUser((int) selectedDialog);
                                        String title = currentChat != null ? currentChat.title : user != null ? UserObject.getUserName(user) : LocaleController.getString("AppNameTgy", R.string.AppNameTgy);
                                        builder.setTitle(title);
                                        //
                                        if (which == 1) {
                                            builder.setMessage(LocaleController.getString("AreYouSureClearHistory", R.string.AreYouSureClearHistory));
                                        } else {
                                            if (isChat) {
                                                builder.setMessage(LocaleController.getString("AreYouSureDeleteAndExit", R.string.AreYouSureDeleteAndExit));
                                            } else {
                                                builder.setMessage(LocaleController.getString("AreYouSureDeleteThisChat", R.string.AreYouSureDeleteThisChat));
                                            }
                                        }
                                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (which != 1) {
                                                    if (isChat) {
                                                        TLRPC.Chat currentChat = MessagesController.getInstance().getChat((int) -selectedDialog);
                                                        if (currentChat != null && ChatObject.isNotInChat(currentChat)) {
                                                            MessagesController.getInstance().deleteDialog(selectedDialog, 0);
                                                        } else {
                                                            MessagesController.getInstance().deleteUserFromChat((int) -selectedDialog, MessagesController.getInstance().getUser(UserConfig.getClientUserId()), null);
                                                        }
                                                    } else {
                                                        MessagesController.getInstance().deleteDialog(selectedDialog, 0);
                                                    }
                                                    if (isBot) {
                                                        MessagesController.getInstance().blockUser((int) selectedDialog);
                                                    }
                                                    if (AndroidUtilities.isTablet()) {
                                                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, selectedDialog);
                                                    }
                                                } else {
                                                    MessagesController.getInstance().deleteDialog(selectedDialog, 1);
                                                }
                                            }
                                        });
                                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                                        showDialog(builder.create());
                                    }
                                }
                            }
                        });

                        showDialog(builder.create());
                    }
                }
                return true;
            }
        });

        searchEmptyView = new EmptyTextProgressView(context);
        searchEmptyView.setVisibility(View.GONE);
        searchEmptyView.setShowAtCenter(true);
        searchEmptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        contentView.addView(searchEmptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        searchEmptyView.setOnTouchListener(onTouchListener);

        progressView = new RadialProgressView(context);
        progressView.setVisibility(View.GONE);
        contentView.addView(progressView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        //TGY
        boomButton = new BoomMenuButton(context);
        boomButton.setVisibility(onlySelect ? View.GONE : View.VISIBLE);
        boomButton.setButtonEnum(ButtonEnum.TextInsideCircle);
        boomButton.setPiecePlaceEnum(PiecePlaceEnum.DOT_8_2);
        boomButton.setButtonPlaceEnum(ButtonPlaceEnum.SC_8_2);
        boomButton.setNormalColor(Theme.getColor(Theme.key_chats_actionBackground));
        boomButton.setDraggable(true);
        boomButton.setShowMoveEaseEnum(EaseEnum.EaseOutBack);
        boomButton.setShowRotateEaseEnum(EaseEnum.EaseOutBack);
        boomButton.setShowScaleEaseEnum(EaseEnum.EaseOutBack);
        boomButton.setHideMoveEaseEnum(EaseEnum.EaseInBack);
        boomButton.setHideRotateEaseEnum(EaseEnum.EaseInBack);
        boomButton.setHideScaleEaseEnum(EaseEnum.EaseInBack);
//        boomButton.setAutoHide(true);

        int p = 25;
        int bmSize = 32;
        for (int i = 0; i < boomButton.getPiecePlaceEnum().pieceNumber(); i++) {
            TextInsideCircleButton.Builder builder = new TextInsideCircleButton.Builder()
                    .imagePadding(new Rect(p, p, p, p))
                    .normalImageDrawable(new IconicsDrawable(context, GramityConstants.BOOM_ICON[i]).color(Color.WHITE).sizeDp(bmSize))
                    .normalText(GramityConstants.BOOM_TEXT[i])
                    .typeface(AndroidUtilities.getTypeface(null))
                    .listener(new OnBMClickListener() {
                        @Override
                        public void onBoomButtonClick(int index) {
                            switch (index) {
                                case 0:
                                    presentFragment(new RevelationActivity());
                                    break;
                                case 1:
                                    boolean answering = advancedPrefs.getBoolean(GramityConstants.PREF_ANSWERING_MACHINE, false);
                                    advancedPrefs.edit().putBoolean(GramityConstants.PREF_ANSWERING_MACHINE, !answering).apply();
                                    if (!answering) {
                                        Toast.makeText(context, LocaleController.getString("AnsweringMachineActivated", R.string.AnsweringMachineActivated), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, LocaleController.getString("AnsweringMachineInactivated", R.string.AnsweringMachineInactivated), Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case 2:
                                    presentFragment(new ThemeActivity());
                                    GramityUtilities.snkrGApnr(GramityConstants.GRAMITY_THEMES_ID, DialogsActivity.this, false, false);
                                    break;
                                case 3:
                                    Bundle arg1 = new Bundle();
                                    arg1.putInt("user_id", GramityConstants.SPAM_BOT_ID);
                                    presentFragment(new ChatActivity(arg1));
                                    break;
                                case 4:
                                    presentFragment(new CacheControlActivity());
                                    break;
                                case 5:
                                    Bundle arg2 = new Bundle();
                                    arg2.putBoolean("destroyAfterSelect", true);
                                    presentFragment(new ContactsActivity(arg2));
                                    break;
                                case 6:
//                                    FeedbackManager.setActivityForScreenshot(new LaunchActivity());
                                    FeedbackManager.showFeedbackActivity(context);
                                    Toast.makeText(context, LocaleController.getString("RateFeedbackToast", R.string.RateFeedbackToast), Toast.LENGTH_SHORT).show();
                                    break;
                                case 7:
                                    presentFragment(new ProxySettingsActivity());
                                    break;
                            }
                        }
                    });
            boomButton.addBuilder(builder);
        }

        tabsHeight = advancedPrefs.getInt(GramityConstants.PREF_TABS_HEIGHT, 44);
        contentView.addView(boomButton, LayoutHelper.createFrame(Build.VERSION.SDK_INT >= 21 ? 56 + (tabsHeight / 2) : 60 + (tabsHeight / 2), Build.VERSION.SDK_INT >= 21 ? 56 + tabsHeight : 60 + tabsHeight, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 10 : 0, 0, LocaleController.isRTL ? 0 : 10, 14 + (tabsHeight / 2))); //BottGrav
        //TGY tabs frame layout initialization
        tabsView = new FrameLayout(context);
        createTabs(context);
        contentView.addView(tabsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, tabsHeight, Gravity.BOTTOM, 0, 0, 0, 0));
        //
        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && searching && searchWas) {
                    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = Math.abs(layoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
                int totalItemCount = recyclerView.getAdapter().getItemCount();

                if (searching && searchWas) {
                    if (visibleItemCount > 0 && layoutManager.findLastVisibleItemPosition() == totalItemCount - 1 && !dialogsSearchAdapter.isMessagesSearchEndReached()) {
                        dialogsSearchAdapter.loadMoreSearchMessages();
                    }
                    return;
                }
                if (visibleItemCount > 0) {
                    if (layoutManager.findLastVisibleItemPosition() >= getDialogsArray().size() - 10) {
                        boolean fromCache = !MessagesController.getInstance().dialogsEndReached;
                        if (fromCache || !MessagesController.getInstance().serverDialogsEndReached) {
                            MessagesController.getInstance().loadDialogs(-1, 100, fromCache);
                        }
                    }
                }

//                if (floatingButton.getVisibility() != View.GONE) {
                if (boomButton.getVisibility() != View.GONE) { //TGY
                    final View topChild = recyclerView.getChildAt(0);
                    int firstViewTop = 0;
                    if (topChild != null) {
                        firstViewTop = topChild.getTop();
                    }
                    boolean goingDown;
                    boolean changed = true;
                    if (prevPosition == firstVisibleItem) {
                        final int topDelta = prevTop - firstViewTop;
                        goingDown = firstViewTop < prevTop;
                        changed = Math.abs(topDelta) > 1;
                    } else {
                        goingDown = firstVisibleItem > prevPosition;
                    }
                    if (changed && scrollUpdated) {
                        if (!hideTabs && !disableAnimation || hideTabs) { //TGU
//                            hideFloatingButton(goingDown);
                            hideBoomButton(goingDown);
                        }
                    }
                    prevPosition = firstVisibleItem;
                    prevTop = firstViewTop;
                    scrollUpdated = true;
                }

                if (!hideTabs) {
                    if (dy > 1) {
                        //Down (HIDE)
                        if (recyclerView.getChildAt(0).getTop() < 0) {
                            if (!disableAnimation) {
                                hideTabsAnimated(true);
                            } else {
//                                hideFloatingButton(true);
                                hideBoomButton(true); //TGY
                            }
                        }

                    }
                    if (dy < -1) {
                        //Up (SHOW)
                        if (!disableAnimation) {
                            hideTabsAnimated(false);
                            if (firstVisibleItem == 0) {
                                listView.setPadding(0, 0, 0, AndroidUtilities.dp(tabsHeight)); //BottGrav
                            }
                        } else {
//                            hideFloatingButton(false);
                            hideBoomButton(false); //TGY
                        }
                    }
                }
            }
        });

        if (searchString == null) {
            dialogsAdapter = new DialogsAdapter(context, dialogsType, onlySelect);
            if (AndroidUtilities.isTablet() && openedDialogId != 0) {
                dialogsAdapter.setOpenedDialogId(openedDialogId);
            }
            listView.setAdapter(dialogsAdapter);
            dialogsBackupAdapter = dialogsAdapter;
        }
        int type = 0;
        if (searchString != null) {
            type = 2;
        } else if (!onlySelect) {
            type = 1;
        }
        dialogsSearchAdapter = new DialogsSearchAdapter(context, type, dialogsType);
        dialogsSearchAdapter.setDelegate(new DialogsSearchAdapter.DialogsSearchAdapterDelegate() {
            @Override
            public void searchStateChanged(boolean search) {
                if (searching && searchWas && searchEmptyView != null) {
                    if (search) {
                        searchEmptyView.showProgress();
                    } else {
                        searchEmptyView.showTextView();
                    }
                }
            }

            @Override
            public void didPressedOnSubDialog(long did) {
                if (onlySelect) {
                    if (dialogsAdapter.hasSelectedDialogs()) {
                        dialogsAdapter.addOrRemoveSelectedDialog(did, null);
                        updateSelectedCount();
                        actionBar.closeSearchField();
                    } else {
                        didSelectResult(did, true, false);
                    }
                } else {
                    int lower_id = (int) did;
                    Bundle args = new Bundle();
                    if (lower_id > 0) {
                        args.putInt("user_id", lower_id);
                    } else {
                        args.putInt("chat_id", -lower_id);
                    }
                    if (actionBar != null) {
                        actionBar.closeSearchField();
                    }
                    if (AndroidUtilities.isTablet()) {
                        if (dialogsAdapter != null) {
                            dialogsAdapter.setOpenedDialogId(openedDialogId = did);
                            updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
                        }
                    }
                    if (searchString != null) {
                        if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                            presentFragment(new ChatActivity(args));
                        }
                    } else {
                        if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {
                            presentFragment(new ChatActivity(args));
                        }
                    }
                }
            }

            @Override
            public void needRemoveHint(final int did) {
                if (getParentActivity() == null) {
                    return;
                }
                TLRPC.User user = MessagesController.getInstance().getUser(did);
                if (user == null) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("AppNameTgy", R.string.AppNameTgy));
                builder.setMessage(LocaleController.formatString("ChatHintsDelete", R.string.ChatHintsDelete, ContactsController.formatName(user.first_name, user.last_name)));
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SearchQuery.removePeer(did);
                    }
                });
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                showDialog(builder.create());
            }
        });

        if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
            searchEmptyView.setVisibility(View.GONE);
            listView.setEmptyView(progressView);
        } else {
            searchEmptyView.setVisibility(View.GONE);
            progressView.setVisibility(View.GONE);
            listView.setEmptyView(null);
        }
        if (searchString != null) {
            actionBar.openSearchField(searchString);
        }

        //if (!onlySelect && dialogsType == 0) {
        if (!onlySelect && (dialogsType == 0 || dialogsType > 7)) { //TGY
            contentView.addView(fragmentLocationContextView = new FragmentContextView(context, this, true), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 39, Gravity.TOP | Gravity.LEFT, 0, -36, 0, 0));
            contentView.addView(fragmentContextView = new FragmentContextView(context, this, false), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 39, Gravity.TOP | Gravity.LEFT, 0, -36, 0, 0));
            fragmentContextView.setAdditionalContextView(fragmentLocationContextView);
            fragmentLocationContextView.setAdditionalContextView(fragmentContextView);
        } else if (dialogsType == 3 && selectAlertString == null) {
            if (commentView != null) {
                commentView.onDestroy();
            }
            commentView = new ChatActivityEnterView(getParentActivity(), contentView, null, false);
            commentView.setAllowStickersAndGifs(false, false);
            commentView.setForceShowSendButton(true, false);
            commentView.setVisibility(View.GONE);
            contentView.addView(commentView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM));
            commentView.setDelegate(new ChatActivityEnterView.ChatActivityEnterViewDelegate() {
                @Override
                public void onMessageSend(CharSequence message) {
                    if (delegate == null) {
                        return;
                    }
                    ArrayList<Long> selectedDialogs = dialogsAdapter.getSelectedDialogs();
                    if (selectedDialogs.isEmpty()) {
                        return;
                    }
                    delegate.didSelectDialogs(DialogsActivity.this, selectedDialogs, message, false);
                }

                @Override
                public void onSwitchRecordMode(boolean video) {

                }

                @Override
                public void onPreAudioVideoRecord() {

                }

                @Override
                public void onTextChanged(final CharSequence text, boolean bigChange) {

                }

                @Override
                public void needSendTyping() {

                }

                @Override
                public void onAttachButtonHidden() {

                }

                @Override
                public void onAttachButtonShow() {

                }

                @Override
                public void onMessageEditEnd(boolean loading) {

                }

                @Override
                public void onWindowSizeChanged(int size) {

                }

                @Override
                public void onStickersTab(boolean opened) {

                }

                @Override
                public void didPressedAttachButton() {

                }

                @Override
                public void needStartRecordVideo(int state) {

                }

                @Override
                public void needChangeVideoPreviewState(int state, float seekProgress) {

                }

                @Override
                public void needStartRecordAudio(int state) {

                }

                @Override
                public void needShowMediaBanHint() {

                }
            });
        }

        refreshTabs(); //TGY

        return fragmentView;
    }

    private class DialogsOnTouch implements View.OnTouchListener {

        private DisplayMetrics displayMetrics;
        //private static final String logTag = "SwipeDetector";
        private static final int MIN_DISTANCE_HIGH = 40;
        private static final int MIN_DISTANCE_HIGH_Y = 60;
        private float downX, downY, upX, upY;
        private float vDPI;
        private float touchPositionDP;

        Context mContext;

        DialogsOnTouch(Context context) {
            this.mContext = context;
            displayMetrics = context.getResources().getDisplayMetrics();
            vDPI = displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT;
            //Log.e("DialogsActivity","DialogsOnTouch vDPI " + vDPI);
        }

        public boolean onTouch(View view, MotionEvent event) {

            touchPositionDP = Math.round(event.getX() / vDPI);
            //Log.e("DialogsActivity","onTouch touchPositionDP " + touchPositionDP + " hideTabs " + hideTabs);
            if (hideTabs) {
                return false;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    downX = Math.round(event.getX() / vDPI);
                    downY = Math.round(event.getY() / vDPI);
                    //Log.e("DialogsActivity", "view " + view.toString());
                    if (touchPositionDP > 50) {
                        parentLayout.getDrawerLayoutContainer().setAllowOpenDrawer(false, false);
                        //Log.e("DialogsActivity", "DOWN setAllowOpenDrawer FALSE");
                    }
                    //Log.e("DialogsActivity", "DOWN downX " + downX);
                    return view instanceof LinearLayout; // for emptyView
                }
                case MotionEvent.ACTION_UP: {
                    upX = Math.round(event.getX() / vDPI);
                    upY = Math.round(event.getY() / vDPI);
                    float deltaX = downX - upX;
                    float deltaY = downY - upY;
                    //Log.e(logTag, "MOVE X " + deltaX);
                    //Log.e(logTag, "MOVE Y " + deltaY);
                    //Log.e("DialogsActivity", "UP downX " + downX);
                    //Log.e("DialogsActivity", "UP upX " + upX);
                    //Log.e("DialogsActivity", "UP deltaX " + deltaX);
                    // horizontal swipe detection
                    if (Math.abs(deltaX) > MIN_DISTANCE_HIGH && Math.abs(deltaY) < MIN_DISTANCE_HIGH_Y) {
                        //if (Math.abs(deltaX) > MIN_DISTANCE_HIGH) {
                        refreshDialogType(deltaX < 0 ? 0 : 1);//0: Left - Right 1: Right - Left
                        downX = Math.round(event.getX() / vDPI);
                        refreshAdapter(mContext);
                        //dialogsAdapter.notifyDataSetChanged();
                        refreshTabAndListViews(false);
                        //return true;
                    }
                    //Log.e("DialogsActivity", "UP2 downX " + downX);
                    if (touchPositionDP > 50) {
                        parentLayout.getDrawerLayoutContainer().setAllowOpenDrawer(true, false);
                    }
                    //downX = downY = upX = upY = 0;
                    return false;
                }
            }
            return false;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (dialogsAdapter != null) {
            dialogsAdapter.notifyDataSetChanged();
        }
        if (commentView != null) {
            commentView.onResume();
        }
        if (dialogsSearchAdapter != null) {
            dialogsSearchAdapter.notifyDataSetChanged();
        }
        if (checkPermission && !onlySelect && Build.VERSION.SDK_INT >= 23) {
            Activity activity = getParentActivity();
            if (activity != null) {
                checkPermission = false;
                if (activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle(LocaleController.getString("AppNameTgy", R.string.AppNameTgy));
                        builder.setMessage(LocaleController.getString("PermissionContacts", R.string.PermissionContacts));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                        showDialog(permissionDialog = builder.create());
                    } else if (activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle(LocaleController.getString("AppNameTgy", R.string.AppNameTgy));
                        builder.setMessage(LocaleController.getString("PermissionStorage", R.string.PermissionStorage));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                        showDialog(permissionDialog = builder.create());
                    } else {
                        askForPermissons();
                    }
                }
            }
        }

        //TGY
        final SharedPreferences pref = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        if (contextTgy != null) {
            createTabs(contextTgy);
            refreshAdapter(contextTgy);
//            updateTheme();
            unreadCount();
        }
        proxyItem.setAlpha(pref.getBoolean("proxy_enabled", false) ? 1.0f : 0.5f);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (commentView != null) {
            commentView.onResume();
        }
    }

    private void updateSelectedCount() {
        if (commentView == null) {
            return;
        }
        if (!dialogsAdapter.hasSelectedDialogs()) {
            if (dialogsType == 3 && selectAlertString == null) {
                actionBar.setTitle(LocaleController.getString("ForwardTo", R.string.ForwardTo));
            } else {
                actionBar.setTitle(LocaleController.getString("SelectChat", R.string.SelectChat));
            }
            if (commentView.getTag() != null) {
                commentView.hidePopup(false);
                commentView.closeKeyboard();
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(ObjectAnimator.ofFloat(commentView, "translationY", 0, commentView.getMeasuredHeight()));
                animatorSet.setDuration(180);
                animatorSet.setInterpolator(new DecelerateInterpolator());
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        commentView.setVisibility(View.GONE);
                    }
                });
                animatorSet.start();
                commentView.setTag(null);
                listView.requestLayout();
            }
        } else {
            if (commentView.getTag() == null) {
                commentView.setFieldText("");
                commentView.setVisibility(View.VISIBLE);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(ObjectAnimator.ofFloat(commentView, "translationY", commentView.getMeasuredHeight(), 0));
                animatorSet.setDuration(180);
                animatorSet.setInterpolator(new DecelerateInterpolator());
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        commentView.setTag(2);
                    }
                });
                animatorSet.start();
                commentView.setTag(1);
            }
            actionBar.setTitle(LocaleController.formatPluralString("Recipient", dialogsAdapter.getSelectedDialogs().size()));
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void askForPermissons() {
        Activity activity = getParentActivity();
        if (activity == null) {
            return;
        }
        ArrayList<String> permissons = new ArrayList<>();
        if (activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissons.add(Manifest.permission.READ_CONTACTS);
            permissons.add(Manifest.permission.WRITE_CONTACTS);
            permissons.add(Manifest.permission.GET_ACCOUNTS);
        }
        if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissons.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissons.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        String[] items = permissons.toArray(new String[permissons.size()]);
        try {
            activity.requestPermissions(items, 1);
        } catch (Exception ignore) {
        }
    }

    @Override
    protected void onDialogDismiss(Dialog dialog) {
        super.onDialogDismiss(dialog);
        if (permissionDialog != null && dialog == permissionDialog && getParentActivity() != null) {
            askForPermissons();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        /*if (!onlySelect && floatingButton != null) {
            floatingButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    floatingButton.setTranslationY(floatingHidden ? (AndroidUtilities.dp(100) + tabsHeight) : 0); //BottGrav
                    floatingButton.setClickable(!floatingHidden);
                    if (floatingButton != null) {
                        floatingButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }*/
        //TGY
        if (!onlySelect && boomButton != null) {
            boomButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    boomButton.setTranslationY(boomHidden ? (AndroidUtilities.dp(100) + tabsHeight) : 0); //BottGrav
                    boomButton.setClickable(!boomHidden);
                    if (boomButton != null) {
                        if (Build.VERSION.SDK_INT < 16) {
                            boomButton.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            boomButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                }
            });
        }
        //
    }

    @Override
    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int a = 0; a < permissions.length; a++) {
                if (grantResults.length <= a || grantResults[a] != PackageManager.PERMISSION_GRANTED) {
                    continue;
                }
                switch (permissions[a]) {
                    case Manifest.permission.READ_CONTACTS:
                        ContactsController.getInstance().forceImportContacts();
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        ImageLoader.getInstance().checkMediaPaths();
                        break;
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void didReceivedNotification(int id, Object... args) {
        final SharedPreferences pref = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE); // TGY

        if (id == NotificationCenter.dialogsNeedReload) {
            if (dialogsAdapter != null) {
                if (dialogsAdapter.isDataSetChanged()) {
                    dialogsAdapter.notifyDataSetChanged();
                } else {
                    updateVisibleRows(MessagesController.UPDATE_MASK_NEW_MESSAGE);
                }
            }
            if (dialogsSearchAdapter != null) {
                dialogsSearchAdapter.notifyDataSetChanged();
            }
            if (listView != null) {
                try {
                    if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
                        searchEmptyView.setVisibility(View.GONE);
                        listView.setEmptyView(progressView);
                    } else {
                        progressView.setVisibility(View.GONE);
                        if (searching && searchWas) {
                            listView.setEmptyView(searchEmptyView);
                        } else {
                            searchEmptyView.setVisibility(View.GONE);
                            listView.setEmptyView(null);
                        }
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        } else if (id == NotificationCenter.emojiDidLoaded) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.updateInterfaces) {
            updateVisibleRows((Integer) args[0]);
        } else if (id == NotificationCenter.appDidLogout) {
            dialogsLoaded = false;
        } else if (id == NotificationCenter.encryptedChatUpdated) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.contactsDidLoaded) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.openedChatChanged) {
            if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                boolean close = (Boolean) args[1];
                long dialog_id = (Long) args[0];
                if (close) {
                    if (dialog_id == openedDialogId) {
                        openedDialogId = 0;
                    }
                } else {
                    openedDialogId = dialog_id;
                }
                if (dialogsAdapter != null) {
                    dialogsAdapter.setOpenedDialogId(openedDialogId);
                }
                updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
            }
        } else if (id == NotificationCenter.notificationsSettingsUpdated) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.messageReceivedByAck || id == NotificationCenter.messageReceivedByServer || id == NotificationCenter.messageSendError) {
            updateVisibleRows(MessagesController.UPDATE_MASK_SEND_STATE);
        } else if (id == NotificationCenter.didSetPasscode) {
            updatePasscodeButton();
        } else if (id == NotificationCenter.refreshTabs) {
            updateTabs();
            hideShowTabs((int) args[0]);
        } else if (id == NotificationCenter.needReloadRecentDialogsSearch) {
            if (dialogsSearchAdapter != null) {
                dialogsSearchAdapter.loadRecentSearch();
            }
        } else if (id == NotificationCenter.didLoadedReplyMessages) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.proxySettingsChanged) { // TGY
            proxyItem.setAlpha(pref.getBoolean("proxy_enabled", false) ? 1.0f : 0.5f);
        }
    }

    private ArrayList<TLRPC.TL_dialog> getDialogsArray() {
        if (dialogsType == 0) {
            return MessagesController.getInstance().dialogs;
        } else if (dialogsType == 1) {
            return MessagesController.getInstance().dialogsServerOnly;
        } else if (dialogsType == 2) {
            return MessagesController.getInstance().dialogsGroupsOnly;
        } else if (dialogsType == 3) {
            return MessagesController.getInstance().dialogsForward;
        }
        //TGY tabs
        else if (dialogsType == 8) {
            return MessagesController.getInstance().dialogsUsers;
        } else if (dialogsType == 9) {
            return MessagesController.getInstance().dialogsGroups;
        } else if (dialogsType == 10) {
            return MessagesController.getInstance().dialogsChannels;
        } else if (dialogsType == 11) {
            return MessagesController.getInstance().dialogsBots;
        } else if (dialogsType == 12) {
            return MessagesController.getInstance().dialogsMegaGroups;
        } else if (dialogsType == 13) {
            return MessagesController.getInstance().dialogsFavs;
        } else if (dialogsType == 14) {
            return MessagesController.getInstance().dialogsGroupsAll;
            //TGY added
        } else if (dialogsType == 7) {
            return MessagesController.getInstance().dialogsBtns;
        }
        //


        return null;
    }

    public void setSideMenu(RecyclerView recyclerView) {
        sideMenu = recyclerView;
        sideMenu.setBackgroundColor(Theme.getColor(Theme.key_chats_menuBackground));
        sideMenu.setGlowColor(Theme.getColor(Theme.key_chats_menuBackground));
    }

    private void updatePasscodeButton() {
        if (passcodeItem == null) {
            return;
        }
        if (UserConfig.passcodeHash.length() != 0 && !searching) {
            passcodeItem.setVisibility(View.VISIBLE);
            if (UserConfig.appLocked) {
                passcodeItem.setIcon(R.drawable.lock_close);
            } else {
                passcodeItem.setIcon(R.drawable.lock_open);
            }
        } else {
            passcodeItem.setVisibility(View.GONE);
        }
    }

    /*private void hideFloatingButton(boolean hide) {
        if (floatingHidden == hide) {
            return;
        }
        floatingHidden = hide;
        ObjectAnimator animator = ObjectAnimator.ofFloat(floatingButton, "translationY", floatingHidden ? AndroidUtilities.dp(100) + tabsHeight : 0).setDuration(250);
        animator.setInterpolator(floatingInterpolator);
        floatingButton.setClickable(!hide);
        animator.start();
    }*/

    //TGY
    private void hideBoomButton(boolean hide) {
        if (boomHidden == hide) {
            return;
        }
        boomHidden = hide;
        ObjectAnimator animator = ObjectAnimator.ofFloat(boomButton, "translationY", boomHidden ? AndroidUtilities.dp(100) + tabsHeight : 0).setDuration(250);
        animator.setInterpolator(floatingInterpolator);
        boomButton.setClickable(!hide);
        animator.start();
    }
    //

    private void updateVisibleRows(int mask) {
        if (listView == null) {
            return;
        }
        int count = listView.getChildCount();
        for (int a = 0; a < count; a++) {
            View child = listView.getChildAt(a);
            if (child instanceof DialogCell) {
                if (listView.getAdapter() != dialogsSearchAdapter) {
                    DialogCell cell = (DialogCell) child;
                    if ((mask & MessagesController.UPDATE_MASK_NEW_MESSAGE) != 0) {
                        cell.checkCurrentDialogIndex();
                        if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                            cell.setDialogSelected(cell.getDialogId() == openedDialogId);
                        }
                    } else if ((mask & MessagesController.UPDATE_MASK_SELECT_DIALOG) != 0) {
                        if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                            cell.setDialogSelected(cell.getDialogId() == openedDialogId);
                        }
                    } else {
                        cell.update(mask);
                    }
                }
            } else if (child instanceof UserCell) {
                ((UserCell) child).update(mask);
            } else if (child instanceof ProfileSearchCell) {
                ((ProfileSearchCell) child).update(mask);
            }
        }
        unreadCount();
    }

    private void unreadCount() {
        unreadCount(MessagesController.getInstance().dialogs, allCounter);
        unreadCount(MessagesController.getInstance().dialogsUsers, usersCounter);
        unreadCount(MessagesController.getInstance().dialogsBots, botsCounter);
        unreadCount(MessagesController.getInstance().dialogsChannels, channelsCounter);
        unreadCount(MessagesController.getInstance().dialogsFavs, favsCounter);
        unreadCountGroups();
    }

    private void unreadCountGroups() {
        SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
        boolean hideSGroups = advancedPrefs.getBoolean("hideSGroups", false);
        if (hideSGroups) {
            unreadCount(MessagesController.getInstance().dialogsGroupsAll, groupsCounter);
        } else {
            unreadCount(MessagesController.getInstance().dialogsGroups, groupsCounter);
            unreadCount(MessagesController.getInstance().dialogsMegaGroups, sGroupsCounter);
        }
    }

    private void unreadCount(ArrayList<TLRPC.TL_dialog> dialogs, TextView tv) {
        SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
        boolean hTabs = advancedPrefs.getBoolean("hideTabs", false);
        if (hTabs) return;
        boolean hideCounters = advancedPrefs.getBoolean("hideTabsCounters", false);
        if (hideCounters) {
            tv.setVisibility(View.GONE);

            return;
        }
        boolean allMuted = true;
        boolean countDialogs = advancedPrefs.getBoolean("tabsCountersCountChats", false);
        boolean countNotMuted = advancedPrefs.getBoolean("tabsCountersCountNotMuted", false);
        int unreadCount = 0;

        if (dialogs != null && !dialogs.isEmpty()) {
            for (int a = 0; a < dialogs.size(); a++) {
                TLRPC.TL_dialog dialg = dialogs.get(a);
                boolean isMuted = MessagesController.getInstance().isDialogMuted(dialg.id);
                if (!isMuted || !countNotMuted) {
                    int i = dialg.unread_count;
                    if (i > 0) {
                        if (countDialogs) {
                            if (i > 0) unreadCount = unreadCount + 1;
                        } else {
                            unreadCount = unreadCount + i;
                        }
                        if (i > 0 && !isMuted) allMuted = false;
                    }
                }
            }
        }

        if (unreadCount == 0) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);

            String countStr;
            if (LocaleController.isRTL) {
                countStr = GramityUtilities.getPersianNumbering(String.valueOf(unreadCount));
            } else {
                countStr = String.valueOf(unreadCount);
            }
            tv.setText(countStr);

            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
            tv.setPadding(AndroidUtilities.dp(4), 0, AndroidUtilities.dp(4), 0);
            int cColor = 0xffffffff; //chatsHeaderTabCounterColor
            if (allMuted) {
                tv.getBackground().setColorFilter(0xffb9b9b9, PorterDuff.Mode.SRC_IN); //chatsHeaderTabCounterSilentBGColor
                tv.setTextColor(cColor);
            } else {
                tv.getBackground().setColorFilter(0xffd32f2f, PorterDuff.Mode.SRC_IN); //chatsHeaderTabCounterBGColor
                tv.setTextColor(cColor);
            }
        }
    }

    public void setDelegate(DialogsActivityDelegate dialogsActivityDelegate) {
        delegate = dialogsActivityDelegate;
    }

    public void setSearchString(String string) {
        searchString = string;
    }

    public boolean isMainDialogList() {
        return delegate == null && searchString == null;
    }

    private void didSelectResult(final long dialog_id, boolean useAlert, final boolean param) {
        if (addToGroupAlertString == null) {
            if ((int) dialog_id < 0) {
                TLRPC.Chat chat = MessagesController.getInstance().getChat(-(int) dialog_id);
                if (ChatObject.isChannel(chat) && !chat.megagroup && (cantSendToChannels || !ChatObject.isCanWriteToChannel(-(int) dialog_id))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("AppNameTgy", R.string.AppNameTgy));
                    builder.setMessage(LocaleController.getString("ChannelCantSendMessage", R.string.ChannelCantSendMessage));
                    builder.setNegativeButton(LocaleController.getString("OK", R.string.OK), null);
                    showDialog(builder.create());
                    return;
                }
            }
        }
        if (useAlert && (selectAlertString != null && selectAlertStringGroup != null || addToGroupAlertString != null)) {
            if (getParentActivity() == null) {
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppNameTgy", R.string.AppNameTgy));
            int lower_part = (int) dialog_id;
            int high_id = (int) (dialog_id >> 32);
            if (lower_part != 0) {
                if (high_id == 1) {
                    TLRPC.Chat chat = MessagesController.getInstance().getChat(lower_part);
                    if (chat == null) {
                        return;
                    }
                    builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, chat.title));
                } else {
                    if (lower_part == UserConfig.getClientUserId()) {
                        builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, LocaleController.getString("SavedMessages", R.string.SavedMessages)));
                    } else if (lower_part > 0) {
                        TLRPC.User user = MessagesController.getInstance().getUser(lower_part);
                        if (user == null) {
                            return;
                        }
                        builder.setMessage(LocaleController.formatStringSimple(selectAlertString, UserObject.getUserName(user)));
                    } else if (lower_part < 0) {
                        TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_part);
                        if (chat == null) {
                            return;
                        }
                        if (addToGroupAlertString != null) {
                            builder.setMessage(LocaleController.formatStringSimple(addToGroupAlertString, chat.title));
                        } else {
                            builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, chat.title));
                        }
                    }
                }
            } else {
                TLRPC.EncryptedChat chat = MessagesController.getInstance().getEncryptedChat(high_id);
                TLRPC.User user = MessagesController.getInstance().getUser(chat.user_id);
                if (user == null) {
                    return;
                }
                builder.setMessage(LocaleController.formatStringSimple(selectAlertString, UserObject.getUserName(user)));
            }

            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    didSelectResult(dialog_id, false, false);
                }
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            showDialog(builder.create());
        } else {
            if (delegate != null) {
                ArrayList<Long> dids = new ArrayList<>();
                dids.add(dialog_id);
                delegate.didSelectDialogs(DialogsActivity.this, dids, null, param);
                delegate = null;
            } else {
                finishFragment();
            }
        }
    }

    // TGY begins here!
    private String getHeaderAllTitles() {
        switch (dialogsType) {
            case 8:
                return LocaleController.getString("Users", R.string.Users);
            case 9:
            case 14:
                return LocaleController.getString("Groups", R.string.Groups);
            case 10:
                return LocaleController.getString("Channels", R.string.Channels);
            case 11:
                return LocaleController.getString("Bots", R.string.Bots);
            case 12:
                return LocaleController.getString("SuperGroups", R.string.SuperGroups);
            case 13:
                return LocaleController.getString("Favorites", R.string.Favorites);
            default:
                return LocaleController.getString("AppNameTgy", R.string.AppNameTgy);
        }
    }

    private void createTabs(final Context context) {
        SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = advancedPrefs.edit();

        boolean hideUsers = advancedPrefs.getBoolean("hideUsers", false);
        boolean hideGroups = advancedPrefs.getBoolean("hideGroups", false);
        boolean hideSGroups = advancedPrefs.getBoolean("hideSGroups", false);
        boolean hideChannels = advancedPrefs.getBoolean("hideChannels", false);
        boolean hideBots = advancedPrefs.getBoolean("hideBots", false);
        boolean hideFavs = advancedPrefs.getBoolean("hideFavs", false);

        hideTabs = advancedPrefs.getBoolean("hideTabs", false);
        disableAnimation = advancedPrefs.getBoolean("disableTabsAnimation", false);

        if (hideUsers && hideGroups && hideSGroups && hideChannels && hideBots && hideFavs) {
            if (!hideTabs) {
                hideTabs = true;
                editor.putBoolean("hideTabs", true).apply();
            }
        }

        tabsHeight = advancedPrefs.getInt(GramityConstants.PREF_TABS_HEIGHT, 44);

        refreshTabAndListViews(false);

        int t = advancedPrefs.getInt("defTab", -1);
        selectedTab = t != -1 ? t : advancedPrefs.getInt("selTab", 0);

        if (!hideTabs && dialogsType != selectedTab) {
            dialogsType = selectedTab == 9 && hideSGroups ? 14 : selectedTab;
            dialogsAdapter = new DialogsAdapter(context, dialogsType, onlySelect); //added onlySelect in ver 4.6
            listView.setAdapter(dialogsAdapter);
            dialogsAdapter.notifyDataSetChanged();
        }

        dialogsBackupAdapter = new DialogsAdapter(context, 0, onlySelect);

        tabsLayout = new LinearLayout(context);
        tabsLayout.setOrientation(LinearLayout.HORIZONTAL);
        final int bgColor = advancedPrefs.getInt(GramityConstants.PREF_TABS_BACKGROUND_COLOR, GramityUtilities.colorTH());
        tabsLayout.setBackgroundColor(bgColor);
        tabsLayout.setGravity(Gravity.CENTER);

        int iconSize = 36;

        //1
        allTab = new ImageView(context);
        //allTab.setScaleType(ImageView.ScaleType.CENTER);
        allTab.setImageDrawable(new IconicsDrawable(context, GoogleMaterial.Icon.gmd_dashboard).sizePx(iconSize));

        //tabsLayout.addView(allTab, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));

        allCounter = new TextView(context);
        allCounter.setTag("ALL");
        allCounter.setTypeface(AndroidUtilities.getTypeface(null));
        addTabView(context, allTab, allCounter, true);

        //2
        usersTab = new ImageView(context);
        usersTab.setImageDrawable(new IconicsDrawable(context, GoogleMaterial.Icon.gmd_account_circle).sizePx(iconSize));
        /*usersTab.setScaleType(ImageView.ScaleType.CENTER);
        if(!hideUsers) {
            tabsLayout.addView(usersTab, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
        }*/
        usersCounter = new TextView(context);
        usersCounter.setTag("USERS");
        usersCounter.setTypeface(AndroidUtilities.getTypeface(null));
        addTabView(context, usersTab, usersCounter, !hideUsers);
        //3
        groupsTab = new ImageView(context);
        groupsTab.setImageDrawable(new IconicsDrawable(context, Ionicons.Icon.ion_android_contacts).sizePx(iconSize));
        /*groupsTab.setScaleType(ImageView.ScaleType.CENTER);
        if(!hideGroups) {
            tabsLayout.addView(groupsTab, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
        }*/
        groupsCounter = new TextView(context);
        groupsCounter.setTag("GROUPS");
        groupsCounter.setTypeface(AndroidUtilities.getTypeface(null));
        addTabView(context, groupsTab, groupsCounter, !hideGroups);
        //4
        superGroupsTab = new ImageView(context);
        superGroupsTab.setImageDrawable(new IconicsDrawable(context, Typeicons.Icon.typ_group).sizePx(iconSize));
        /*superGroupsTab.setScaleType(ImageView.ScaleType.CENTER);
        if(!hideSGroups){
            tabsLayout.addView(superGroupsTab, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
        }*/
        sGroupsCounter = new TextView(context);
        sGroupsCounter.setTag("SGROUP");
        sGroupsCounter.setTypeface(AndroidUtilities.getTypeface(null));
        addTabView(context, superGroupsTab, sGroupsCounter, !hideSGroups);
        //5
        channelsTab = new ImageView(context);
        channelsTab.setImageDrawable(new IconicsDrawable(context, Entypo.Icon.ent_megaphone).sizePx(iconSize));
        /*channelsTab.setScaleType(ImageView.ScaleType.CENTER);
        if(!hideChannels){
            tabsLayout.addView(channelsTab, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
        }*/
        channelsCounter = new TextView(context);
        channelsCounter.setTag("CHANNELS");
        channelsCounter.setTypeface(AndroidUtilities.getTypeface(null));
        addTabView(context, channelsTab, channelsCounter, !hideChannels);
        //6
        botsTab = new ImageView(context);
        botsTab.setImageDrawable(new IconicsDrawable(context, Octicons.Icon.oct_hubot).sizePx(iconSize));
        /*botsTab.setScaleType(ImageView.ScaleType.CENTER);
        if(!hideBots){
            tabsLayout.addView(botsTab, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
        }*/
        botsCounter = new TextView(context);
        botsCounter.setTag("BOTS");
        botsCounter.setTypeface(AndroidUtilities.getTypeface(null));
        addTabView(context, botsTab, botsCounter, !hideBots);
        //7
        favsTab = new ImageView(context);
        favsTab.setImageDrawable(new IconicsDrawable(context, GoogleMaterial.Icon.gmd_stars).sizePx(iconSize));
        /*favsTab.setScaleType(ImageView.ScaleType.CENTER);
        if(!hideFavs){
            tabsLayout.addView(favsTab, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
        }*/
        favsCounter = new TextView(context);
        favsCounter.setTag("FAVS");
        favsCounter.setTypeface(AndroidUtilities.getTypeface(null));
        addTabView(context, favsTab, favsCounter, !hideFavs);

        tabsView.addView(tabsLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        allTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogsType != 0) {
                    dialogsType = 0;
                    refreshAdapter(context);
                }
            }
        });

        allTab.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("All", R.string.All));
                CharSequence items[];
                SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
                final int tabVal = 0;
                final int def = advancedPrefs.getInt("defTab", -1);
                final int sort = advancedPrefs.getInt("sortAll", 0);

                CharSequence cs2 = def == tabVal ? LocaleController.getString("ResetDefaultTab", R.string.ResetDefaultTab) : LocaleController.getString("SetAsDefaultTab", R.string.SetAsDefaultTab);
                CharSequence cs1 = sort == 0 ? LocaleController.getString("SortByUnreadCount", R.string.SortByUnreadCount) : LocaleController.getString("SortByLastMessage", R.string.SortByLastMessage);
                CharSequence cs0 = LocaleController.getString("HideShowTabs", R.string.HideShowTabs);
                items = new CharSequence[]{cs0, cs1, cs2, LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead)};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = advancedPrefs.edit();
                        if (which == 0) {
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getParentActivity());
                            createTabsDialog(context, builder);
                            builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), null);
                            showDialog(builder.create());
                        } else if (which == 1) {
                            editor.putInt("sortAll", sort == 0 ? 1 : 0).apply();
                            if (dialogsAdapter.getItemCount() > 1) {
                                dialogsAdapter.notifyDataSetChanged();
                            }
                        } else if (which == 2) {
                            editor.putInt("defTab", def == tabVal ? -1 : tabVal).apply();
                        } else if (which == 3) {
                            markAsReadDialog(true);
                        }
                    }
                });
                showDialog(builder.create());
                return true;
            }
        });

        usersTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogsType != 8) {
                    dialogsType = 8;
                    refreshAdapter(context);
                }
            }
        });

        usersTab.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("Users", R.string.Users));
                CharSequence items[];
                SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
                final int tabVal = 3;
                final int sort = advancedPrefs.getInt("sortUsers", 0);
                final int def = advancedPrefs.getInt("defTab", -1);
                CharSequence cs = def == tabVal ? LocaleController.getString("ResetDefaultTab", R.string.ResetDefaultTab) : LocaleController.getString("SetAsDefaultTab", R.string.SetAsDefaultTab);
                items = new CharSequence[]{sort == 0 ? LocaleController.getString("SortByStatus", R.string.SortByStatus) : LocaleController.getString("SortByLastMessage", R.string.SortByLastMessage), cs, LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead)};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = advancedPrefs.edit();
                        if (which == 1) {
                            editor.putInt("defTab", def == tabVal ? -1 : tabVal).apply();
                        } else if (which == 0) {
                            editor.putInt("sortUsers", sort == 0 ? 1 : 0).apply();
                            if (dialogsAdapter.getItemCount() > 1) {
                                dialogsAdapter.notifyDataSetChanged();
                            }
                        } else if (which == 2) {
                            markAsReadDialog(true);
                        }
                    }
                });
                showDialog(builder.create());
                return true;
            }
        });

        groupsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
                final boolean hideSGroups = advancedPrefs.getBoolean("hideSGroups", false);
                int i = hideSGroups ? 14 : 9;
                if (dialogsType != i) {
                    dialogsType = i;
                    refreshAdapter(context);
                }
            }
        });

        groupsTab.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("Groups", R.string.Groups));
                CharSequence items[];
                SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
                final boolean hideSGroups = advancedPrefs.getBoolean("hideSGroups", false);
                final int tabVal = 4;
                final int sort = advancedPrefs.getInt("sortGroups", 0);
                final int def = advancedPrefs.getInt("defTab", -1);

                CharSequence cs2 = def == tabVal ? LocaleController.getString("ResetDefaultTab", R.string.ResetDefaultTab) : LocaleController.getString("SetAsDefaultTab", R.string.SetAsDefaultTab);
                CharSequence cs1 = sort == 0 ? LocaleController.getString("SortByUnreadCount", R.string.SortByUnreadCount) : LocaleController.getString("SortByLastMessage", R.string.SortByLastMessage);
                CharSequence cs0 = hideSGroups ? LocaleController.getString("ShowSuperGroupsTab", R.string.ShowSuperGroupsTab) : LocaleController.getString("HideSuperGroupsTab", R.string.HideSuperGroupsTab);
                items = new CharSequence[]{cs0, cs1, cs2, LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead)};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {

                        SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = advancedPrefs.edit();
                        if (which == 0) {
                            RelativeLayout rl = (RelativeLayout) superGroupsTab.getParent();
                            editor.putBoolean("hideSGroups", !hideSGroups).apply();
                            if (!hideSGroups) {
                                tabsLayout.removeView(rl);
                                if (dialogsType == 12) {
                                    dialogsType = 14;
                                    refreshAdapter(context);
                                }
                            } else {
                                boolean hideUsers = advancedPrefs.getBoolean("hideUsers", false);
                                tabsLayout.addView(rl, hideUsers ? 2 : 3, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
                            }
                            unreadCountGroups();
                        } else if (which == 1) {
                            editor.putInt("sortGroups", sort == 0 ? 1 : 0).apply();
                            if (dialogsAdapter.getItemCount() > 1) {
                                dialogsAdapter.notifyDataSetChanged();
                            }
                        } else if (which == 2) {
                            editor.putInt("defTab", def == tabVal ? -1 : tabVal).apply();
                        } else if (which == 3) {
                            markAsReadDialog(true);
                        }
                    }
                });
                showDialog(builder.create());
                return true;
            }
        });

        superGroupsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogsType != 12) {
                    dialogsType = 12;
                    refreshAdapter(context);
                }
            }
        });

        superGroupsTab.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("SuperGroups", R.string.SuperGroups));
                CharSequence items[];
                SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
                final int tabVal = 7;
                final int def = advancedPrefs.getInt("defTab", -1);
                final int sort = advancedPrefs.getInt("sortSGroups", 0);
                final boolean hideSGroups = advancedPrefs.getBoolean("hideSGroups", false);
                CharSequence cs2 = def == tabVal ? LocaleController.getString("ResetDefaultTab", R.string.ResetDefaultTab) : LocaleController.getString("SetAsDefaultTab", R.string.SetAsDefaultTab);
                CharSequence cs1 = sort == 0 ? LocaleController.getString("SortByUnreadCount", R.string.SortByUnreadCount) : LocaleController.getString("SortByLastMessage", R.string.SortByLastMessage);
                CharSequence cs0 = hideSGroups ? LocaleController.getString("ShowSuperGroupsTab", R.string.ShowSuperGroupsTab) : LocaleController.getString("HideSuperGroupsTab", R.string.HideSuperGroupsTab);
                items = new CharSequence[]{cs0, cs1, cs2, LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead)};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = advancedPrefs.edit();

                        if (which == 0) {
                            RelativeLayout rl = (RelativeLayout) superGroupsTab.getParent();
                            editor.putBoolean("hideSGroups", !hideSGroups).apply();
                            if (!hideSGroups) {
                                tabsLayout.removeView(rl);
                                if (dialogsType == 12) {
                                    dialogsType = 0;
                                    refreshAdapter(context);
                                }
                            } else {
                                tabsLayout.addView(rl, 3, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
                            }
                            unreadCountGroups();
                        } else if (which == 1) {
                            editor.putInt("sortSGroups", sort == 0 ? 1 : 0).apply();
                            if (dialogsAdapter.getItemCount() > 1) {
                                dialogsAdapter.notifyDataSetChanged();
                            }
                        } else if (which == 2) {
                            editor.putInt("defTab", def == tabVal ? -1 : tabVal).apply();
                        } else if (which == 3) {
                            markAsReadDialog(true);
                        }
                    }
                });
                showDialog(builder.create());
                return true;
            }
        });

        channelsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogsType != 10) {
                    dialogsType = 10;
                    refreshAdapter(context);
                }
            }
        });

        channelsTab.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("Channels", R.string.Channels));
                CharSequence items[];
                SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
                final int tabVal = 5;
                final int sort = advancedPrefs.getInt("sortChannels", 0);
                final int def = advancedPrefs.getInt("defTab", -1);
                CharSequence cs = def == tabVal ? LocaleController.getString("ResetDefaultTab", R.string.ResetDefaultTab) : LocaleController.getString("SetAsDefaultTab", R.string.SetAsDefaultTab);
                CharSequence cs1 = sort == 0 ? LocaleController.getString("SortByUnreadCount", R.string.SortByUnreadCount) : LocaleController.getString("SortByLastMessage", R.string.SortByLastMessage);
                items = new CharSequence[]{cs1, cs, LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead)};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = advancedPrefs.edit();
                        if (which == 1) {
                            editor.putInt("defTab", def == tabVal ? -1 : tabVal).apply();
                        } else if (which == 0) {
                            editor.putInt("sortChannels", sort == 0 ? 1 : 0).apply();
                            if (dialogsAdapter.getItemCount() > 1) {
                                dialogsAdapter.notifyDataSetChanged();
                            }
                        } else if (which == 2) {
                            markAsReadDialog(true);
                        }

                    }
                });
                showDialog(builder.create());
                return true;
            }
        });

        botsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogsType != 11) {
                    dialogsType = 11;
                    refreshAdapter(context);
                }
            }
        });

        botsTab.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("Bots", R.string.Bots));
                CharSequence items[];
                SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
                final int tabVal = 6;
                final int sort = advancedPrefs.getInt("sortBots", 0);
                final int def = advancedPrefs.getInt("defTab", -1);
                CharSequence cs = def == tabVal ? LocaleController.getString("ResetDefaultTab", R.string.ResetDefaultTab) : LocaleController.getString("SetAsDefaultTab", R.string.SetAsDefaultTab);
                CharSequence cs1 = sort == 0 ? LocaleController.getString("SortByUnreadCount", R.string.SortByUnreadCount) : LocaleController.getString("SortByLastMessage", R.string.SortByLastMessage);
                items = new CharSequence[]{cs1, cs, LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead)};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = advancedPrefs.edit();
                        if (which == 1) {
                            editor.putInt("defTab", def == tabVal ? -1 : tabVal).apply();
                        } else if (which == 0) {
                            editor.putInt("sortBots", sort == 0 ? 1 : 0).apply();
                            if (dialogsAdapter.getItemCount() > 1) {
                                dialogsAdapter.notifyDataSetChanged();
                            }
                        } else if (which == 2) {
                            markAsReadDialog(true);
                        }
                    }
                });
                showDialog(builder.create());
                return true;
            }
        });

        favsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogsType != 13) {
                    dialogsType = 13;
                    refreshAdapter(context);
                }
            }
        });

        favsTab.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("Favorites", R.string.Favorites));
                CharSequence items[];
                SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
                final int tabVal = 8;
                final int sort = advancedPrefs.getInt("sortFavs", 0);
                final int def = advancedPrefs.getInt("defTab", -1);
                CharSequence cs = def == tabVal ? LocaleController.getString("ResetDefaultTab", R.string.ResetDefaultTab) : LocaleController.getString("SetAsDefaultTab", R.string.SetAsDefaultTab);
                CharSequence cs1 = sort == 0 ? LocaleController.getString("SortByUnreadCount", R.string.SortByUnreadCount) : LocaleController.getString("SortByLastMessage", R.string.SortByLastMessage);
                items = new CharSequence[]{cs1, cs, LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead)};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = advancedPrefs.edit();
                        if (which == 1) {
                            editor.putInt("defTab", def == tabVal ? -1 : tabVal).apply();
                        } else if (which == 0) {
                            editor.putInt("sortFavs", sort == 0 ? 1 : 0).apply();
                            if (dialogsAdapter.getItemCount() > 1) {
                                dialogsAdapter.notifyDataSetChanged();
                            }
                        } else if (which == 2) {
                            markAsReadDialog(true);
                        }

                    }
                });
                showDialog(builder.create());
                return true;
            }
        });
    }

    private void addTabView(Context context, ImageView iv, TextView tv, boolean show) {

        iv.setScaleType(ImageView.ScaleType.CENTER);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
        tv.setGravity(Gravity.CENTER);

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(AndroidUtilities.dp(32));

        tv.setBackgroundDrawable(shape);
        tv.setPadding(AndroidUtilities.dp(4), 0, AndroidUtilities.dp(4), 0);
        RelativeLayout layout = new RelativeLayout(context);
        layout.addView(iv, LayoutHelper.createRelative(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        layout.addView(tv, LayoutHelper.createRelative(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 0, 3, 6, RelativeLayout.ALIGN_PARENT_RIGHT));
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        tv.setLayoutParams(params);
        if (show) {
            tabsLayout.addView(layout, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
        }
    }

    private android.app.AlertDialog.Builder createTabsDialog(final Context context, android.app.AlertDialog.Builder builder) {
        builder.setTitle(LocaleController.getString("HideShowTabs", R.string.HideShowTabs));

        SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
        boolean hideUsers = advancedPrefs.getBoolean("hideUsers", false);
        boolean hideGroups = advancedPrefs.getBoolean("hideGroups", false);
        boolean hideSGroups = advancedPrefs.getBoolean("hideSGroups", false);
        boolean hideChannels = advancedPrefs.getBoolean("hideChannels", false);
        boolean hideBots = advancedPrefs.getBoolean("hideBots", false);
        boolean hideFavs = advancedPrefs.getBoolean("hideFavs", false);

        builder.setMultiChoiceItems(
                new CharSequence[]{LocaleController.getString("Users", R.string.Users), LocaleController.getString("Groups", R.string.Groups), LocaleController.getString("SuperGroups", R.string.SuperGroups), LocaleController.getString("Channels", R.string.Channels), LocaleController.getString("Bots", R.string.Bots), LocaleController.getString("Favorites", R.string.Favorites)},
                new boolean[]{!hideUsers, !hideGroups, !hideSGroups, !hideChannels, !hideBots, !hideFavs},
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = advancedPrefs.edit();

                        boolean hide = advancedPrefs.getBoolean("hideTabs", false);

                        boolean hideUsers = advancedPrefs.getBoolean("hideUsers", false);
                        boolean hideGroups = advancedPrefs.getBoolean("hideGroups", false);
                        boolean hideSGroups = advancedPrefs.getBoolean("hideSGroups", false);
                        boolean hideChannels = advancedPrefs.getBoolean("hideChannels", false);
                        boolean hideBots = advancedPrefs.getBoolean("hideBots", false);
                        boolean hideFavs = advancedPrefs.getBoolean("hideFavs", false);

                        if (which == 0) {
                            RelativeLayout rl = (RelativeLayout) usersTab.getParent();
                            editor.putBoolean("hideUsers", !hideUsers).apply();
                            if (!hideUsers) {
                                tabsLayout.removeView(rl);
                                if (dialogsType == 8) {
                                    dialogsType = 0;
                                    refreshAdapter(context);
                                }
                                hideUsers = true;
                            } else {
                                tabsLayout.addView(rl, 1, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
                            }
                        } else if (which == 1) {
                            RelativeLayout rl = (RelativeLayout) groupsTab.getParent();
                            editor.putBoolean("hideGroups", !hideGroups).apply();
                            if (!hideGroups) {
                                tabsLayout.removeView(rl);
                                if (dialogsType == 9) {
                                    dialogsType = 0;
                                    refreshAdapter(context);
                                }
                                hideGroups = true;
                            } else {
                                tabsLayout.addView(rl, hideUsers ? 1 : 2, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
                            }
                        } else if (which == 2) {
                            RelativeLayout rl = (RelativeLayout) superGroupsTab.getParent();
                            editor.putBoolean("hideSGroups", !hideSGroups).apply();
                            if (!hideSGroups) {
                                tabsLayout.removeView(rl);
                                if (dialogsType == 12) {
                                    dialogsType = 9;
                                    refreshAdapter(context);
                                }
                                hideSGroups = true;
                            } else {
                                int pos = 3;
                                if (hideUsers) pos = pos - 1;
                                if (hideGroups) pos = pos - 1;
                                tabsLayout.addView(rl, pos, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
                            }
                        } else if (which == 3) {
                            RelativeLayout rl = (RelativeLayout) channelsTab.getParent();
                            editor.putBoolean("hideChannels", !hideChannels).apply();
                            if (!hideChannels) {
                                tabsLayout.removeView(rl);
                                if (dialogsType == 10) {
                                    dialogsType = 0;
                                    refreshAdapter(context);
                                }
                                hideChannels = true;
                            } else {
                                int place = tabsLayout.getChildCount();
                                if (!hideFavs) --place;
                                if (!hideBots) --place;
                                tabsLayout.addView(rl, place, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
                            }
                        } else if (which == 4) {
                            RelativeLayout rl = (RelativeLayout) botsTab.getParent();
                            editor.putBoolean("hideBots", !hideBots).apply();
                            if (!hideBots) {
                                tabsLayout.removeView(rl);
                                if (dialogsType == 11) {
                                    dialogsType = 0;
                                    refreshAdapter(context);
                                }
                                hideBots = true;
                            } else {
                                int place = tabsLayout.getChildCount();
                                if (!hideFavs) --place;
                                tabsLayout.addView(rl, place, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f, Gravity.TOP, 0, 0, 0, 0));
                            }
                        } else if (which == 5) {
                            RelativeLayout rl = (RelativeLayout) favsTab.getParent();
                            editor.putBoolean("hideFavs", !hideFavs).apply();
                            if (!hideFavs) {
                                tabsLayout.removeView(rl);
                                if (dialogsType == 13) {
                                    dialogsType = 0;
                                    refreshAdapter(context);
                                }
                                hideFavs = true;
                            } else {
                                tabsLayout.addView(rl, tabsLayout.getChildCount(), LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
                            }
                        }
                        if (hideUsers && hideGroups && hideSGroups && hideChannels && hideBots && hideFavs) {
                            hideTabs = true;
                            editor.putBoolean("hideTabs", true).apply();
                            refreshTabAndListViews(true);
                        }
                        if (isChecked && hide) {
                            hideTabs = false;
                            editor.putBoolean("hideTabs", false).apply();
                            refreshTabAndListViews(false);
                        }
                    }
                });
        return builder;
    }

    private void refreshAdapter(Context context) {
        refreshAdapterAndTabs(new DialogsAdapter(context, dialogsType, onlySelect)); //added onlySelect in ver 4.6
    }

    private void refreshAdapterAndTabs(DialogsAdapter adapter) {
        dialogsAdapter = adapter;
        listView.setAdapter(dialogsAdapter);
        dialogsAdapter.notifyDataSetChanged();
        if (!onlySelect) {
            selectedTab = dialogsType == 14 ? 9 : dialogsType;
            SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = advancedPrefs.edit();
            editor.putInt("selTab", selectedTab).apply();
        }
        refreshTabs();
    }

    private void refreshTabs() {
        //resetTabs();
        int defColor = 0xffffffff; //chatsHeaderIconsColor
        int iconColor = defColor; //chatsHeaderTabIconColor

        int iColor = 0x4dffffff;

        allTab.setBackgroundResource(0);
        usersTab.setBackgroundResource(0);
        groupsTab.setBackgroundResource(0);
        superGroupsTab.setBackgroundResource(0);
        channelsTab.setBackgroundResource(0);
        botsTab.setBackgroundResource(0);
        favsTab.setBackgroundResource(0);

        allTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        usersTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        groupsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        superGroupsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        channelsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        botsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        favsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);

        Drawable selected = getParentActivity().getResources().getDrawable(R.drawable.tab_selected);
        selected.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);

        switch (dialogsType == 14 ? 9 : dialogsType) {
            case 8:
                usersTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                usersTab.setBackgroundDrawable(selected);
                break;
            case 9:
                groupsTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                groupsTab.setBackgroundDrawable(selected);
                break;
            case 10:
                channelsTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                channelsTab.setBackgroundDrawable(selected);
                break;
            case 11:
                botsTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                botsTab.setBackgroundDrawable(selected);
                break;
            case 12:
                superGroupsTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                superGroupsTab.setBackgroundDrawable(selected);
                break;
            case 13:
                favsTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                favsTab.setBackgroundDrawable(selected);
                break;
            default:
                allTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                allTab.setBackgroundDrawable(selected);
        }

        String t = getHeaderAllTitles();
//        actionBar.setTitle(t); //tgy header for all tabs
        actionBar.setTitle(LocaleController.getString("AppNameTgy", R.string.AppNameTgy));

        if (getDialogsArray() != null && getDialogsArray().isEmpty()) {
            progressView.setVisibility(View.GONE); // edited TGY 4.6

            //TGY in ver 4.6
            searchEmptyView.setPadding(10, 10, 10, 10);
            searchEmptyView.setText(dialogsType < 4 ? LocaleController.getString("NoChats", R.string.NoChats) : dialogsType == 13 ? LocaleController.getString("NoFavoritesHelp", R.string.NoFavoritesHelp) : t);
            searchEmptyView.setTextColor(0xff212121); //chatsNameColor
            searchEmptyView.setVisibility(View.VISIBLE);
            listView.setEmptyView(searchEmptyView);
            //


            /*if (emptyView.getChildCount() > 0) {
                TextView tv = (TextView) emptyView.getChildAt(0);
                if (tv != null) {
                    PaintDrawable paintDrawable = new PaintDrawable();
                    paintDrawable.setCornerRadius(8);
                    paintDrawable.setColorFilter(Theme.getColor(Theme.key_chats_actionBackground), PorterDuff.Mode.SRC_ATOP);
                    paintDrawable.setPadding(10, 10, 10, 10);
                    tv.setText(dialogsType < 4 ? LocaleController.getString("NoChats", R.string.NoChats) : dialogsType == 13 ? LocaleController.getString("NoFavoritesHelp", R.string.NoFavoritesHelp) : t);
                    tv.setTextColor(0xff212121); //chatsNameColor
                    tv.setBackgroundDrawable(paintDrawable);
                    tv.setTypeface(AndroidUtilities.getTypeface(null));
                }
                if (emptyView.getChildAt(1) != null)
                    emptyView.getChildAt(1).setVisibility(View.GONE);
            }

            emptyView.setVisibility(View.VISIBLE);
//            emptyView.setBackgroundColor(0xffffffff); //chatsRowColor
            listView.setEmptyView(emptyView);*/
        } else {
            progressView.setVisibility(View.GONE); // edited TGY 4.6
        }
    }

    private void hideShowTabs(int i) {
        RelativeLayout rl = null;
        int pos = 0;
        boolean b = false;
        SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
        boolean hideUsers = advancedPrefs.getBoolean("hideUsers", false);
        boolean hideGroups = advancedPrefs.getBoolean("hideGroups", false);
        boolean hideSGroups = advancedPrefs.getBoolean("hideSGroups", false);
        boolean hideBots = advancedPrefs.getBoolean("hideBots", false);
        boolean hideFavs = advancedPrefs.getBoolean("hideFavs", false);
        switch (i) {
            case 0: // Users
                rl = (RelativeLayout) usersTab.getParent();
                pos = 1;
                b = hideUsers;
                break;
            case 1: //Groups
                rl = (RelativeLayout) groupsTab.getParent();
                pos = hideUsers ? 1 : 2;
                b = hideGroups;
                break;
            case 2: //Supergroups
                rl = (RelativeLayout) superGroupsTab.getParent();
                pos = 3;
                if (hideGroups) pos = pos - 1;
                if (hideUsers) pos = pos - 1;
                b = hideSGroups;
                break;
            case 3: //Channels
                rl = (RelativeLayout) channelsTab.getParent();
                pos = tabsLayout.getChildCount();
                if (!hideBots) pos = pos - 1;
                if (!hideFavs) pos = pos - 1;
                b = advancedPrefs.getBoolean("hideChannels", false);
                break;
            case 4: //Bots
                rl = (RelativeLayout) botsTab.getParent();
                pos = tabsLayout.getChildCount();
                if (!hideFavs) pos = pos - 1;
                b = hideBots;
                break;
            case 5: //Favorites
                rl = (RelativeLayout) favsTab.getParent();
                pos = tabsLayout.getChildCount();
                b = hideFavs;
                break;
            default:
                updateTabs();
        }

        if (rl != null) {
            if (!b) {
                tabsLayout.addView(rl, pos, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
            } else {
                tabsLayout.removeView(rl);
            }
        }

    }

    private void updateTabs() {
        SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
        hideTabs = advancedPrefs.getBoolean("hideTabs", false);
        disableAnimation = advancedPrefs.getBoolean("disableTabsAnimation", false);

        tabsHeight = advancedPrefs.getInt(GramityConstants.PREF_TABS_HEIGHT, 44);

        refreshTabAndListViews(false);

        if (hideTabs && dialogsType > 7) {
            dialogsType = 0;
            refreshAdapterAndTabs(dialogsBackupAdapter);
        }
        //hideTabsAnimated(false);
    }

    private void refreshTabAndListViews(boolean forceHide) {
        if (hideTabs || forceHide) {
            tabsView.setVisibility(View.GONE);
            listView.setPadding(0, 0, 0, 0);
        } else {
            tabsView.setVisibility(View.VISIBLE);
            int h = AndroidUtilities.dp(tabsHeight);
            ViewGroup.LayoutParams params = tabsView.getLayoutParams();
            if (params != null) {
                params.height = h;
                tabsView.setLayoutParams(params);
            }
            listView.setPadding(0, 0, 0, h); //BottGrav
            hideTabsAnimated(false);
        }
        listView.scrollToPosition(0);
    }

    private void hideTabsAnimated(final boolean hide) {
        if (tabsHidden == hide) {
            return;
        }
        tabsHidden = hide;
        if (hide) listView.setPadding(0, 0, 0, 0);
        ObjectAnimator animator = ObjectAnimator.ofFloat(tabsView, "translationY", hide ? AndroidUtilities.dp(100) : 0).setDuration(300);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!tabsHidden)
                    listView.setPadding(0, 0, 0, AndroidUtilities.dp(tabsHeight)); //BottGrav
            }
        });
        animator.start();
    }

    private void refreshDialogType(int d) {
        if (hideTabs) return;
        SharedPreferences advancedPrefs = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
        boolean hideUsers = advancedPrefs.getBoolean("hideUsers", false);
        boolean hideGroups = advancedPrefs.getBoolean("hideGroups", false);
        boolean hideSGroups = advancedPrefs.getBoolean("hideSGroups", false);
        boolean hideChannels = advancedPrefs.getBoolean("hideChannels", false);
        boolean hideBots = advancedPrefs.getBoolean("hideBots", false);
        boolean hideFavs = advancedPrefs.getBoolean("hideFavs", false);
        boolean loop = advancedPrefs.getBoolean(GramityConstants.PREF_INFINITE_TABS_SWIPE, true);
        if (d == 1) {
            switch (dialogsType) {
                case 8: // Users
                    if (hideGroups) {
                        dialogsType = !hideSGroups ? 12 : !hideChannels ? 10 : !hideBots ? 11 : !hideFavs ? 13 : loop ? 0 : dialogsType;
                    } else {
                        dialogsType = hideSGroups ? 14 : 9;
                    }
                    break;
                case 9: //Groups
                    dialogsType = !hideSGroups ? 12 : !hideChannels ? 10 : !hideBots ? 11 : !hideFavs ? 13 : loop ? 0 : dialogsType;
                    break;
                case 14: //Groups
                case 12: //Supergroups
                    dialogsType = !hideChannels ? 10 : !hideBots ? 11 : !hideFavs ? 13 : loop ? 0 : dialogsType;
                    break;
                case 10: //Channels
                    dialogsType = !hideBots ? 11 : !hideFavs ? 13 : loop ? 0 : dialogsType;
                    break;
                case 11: //Bots
                    dialogsType = !hideFavs ? 13 : loop ? 0 : dialogsType;
                    break;
                case 13: //Favorites
                    if (loop) {
                        dialogsType = 0;
                    }
                    break;
                default: //All
                    dialogsType = !hideUsers ? 8 : !hideGroups && hideSGroups ? 14 : !hideGroups ? 12 : !hideChannels ? 10 : !hideBots ? 11 : !hideFavs ? 13 : loop ? 0 : dialogsType;
            }
        } else {
            switch (dialogsType) {
                case 8: // Users
                    dialogsType = 0;
                    break;
                case 9: //Groups
                case 14: //Groups
                    dialogsType = !hideUsers ? 8 : 0;
                    break;
                case 12: //Supergroups
                    dialogsType = !hideGroups ? 9 : !hideUsers ? 8 : 0;
                    break;
                case 10: //Channels
                    dialogsType = !hideSGroups ? 12 : !hideGroups ? 14 : !hideUsers ? 8 : 0;
                    break;
                case 11: //Bots
                    dialogsType = !hideChannels ? 10 : !hideSGroups ? 12 : !hideGroups ? 14 : !hideUsers ? 8 : 0;
                    break;
                case 13: //Favorites
                    dialogsType = !hideBots ? 11 : !hideChannels ? 10 : !hideSGroups ? 12 : !hideGroups ? 14 : !hideUsers ? 8 : 0;
                    break;
                default: //All
                    if (loop) {
                        dialogsType = !hideFavs ? 13 : !hideBots ? 11 : !hideChannels ? 10 : !hideSGroups ? 12 : !hideGroups ? 14 : !hideUsers ? 8 : 0;
                    }
            }
        }

    }
    // TGY ends here!

    @Override
    public ThemeDescription[] getThemeDescriptions() {
        ThemeDescription.ThemeDescriptionDelegate сellDelegate = new ThemeDescription.ThemeDescriptionDelegate() {
            @Override
            public void didSetColor(int color) {
                int count = listView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View child = listView.getChildAt(a);
                    if (child instanceof ProfileSearchCell) {
                        ((ProfileSearchCell) child).update(0);
                    } else if (child instanceof DialogCell) {
                        ((DialogCell) child).update(0);
                    }
                }
                RecyclerListView recyclerListView = dialogsSearchAdapter.getInnerListView();
                if (recyclerListView != null) {
                    count = recyclerListView.getChildCount();
                    for (int a = 0; a < count; a++) {
                        View child = recyclerListView.getChildAt(a);
                        if (child instanceof HintDialogCell) {
                            ((HintDialogCell) child).update();
                        }
                    }
                }
            }
        };
        return new ThemeDescription[]{
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite),

                new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, Theme.key_actionBarDefaultSearch),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, Theme.key_actionBarDefaultSearchPlaceholder),

                new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),

                new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider),

                new ThemeDescription(searchEmptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder),
                new ThemeDescription(searchEmptyView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_progressCircle),

                new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{DialogsEmptyCell.class}, new String[]{"emptyTextView1"}, null, null, null, Theme.key_emptyListPlaceholder),
                new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{DialogsEmptyCell.class}, new String[]{"emptyTextView2"}, null, null, null, Theme.key_emptyListPlaceholder),

//                new ThemeDescription(floatingButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_chats_actionIcon),
                new ThemeDescription(boomButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_chats_actionBackground), //TGY
//                new ThemeDescription(floatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_chats_actionPressedBackground),

                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.avatar_photoDrawable, Theme.avatar_broadcastDrawable, Theme.avatar_savedDrawable}, null, Theme.key_avatar_text),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundRed),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundOrange),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundViolet),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundGreen),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundCyan),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundBlue),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundPink),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundSaved),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countPaint, null, null, Theme.key_chats_unreadCounter),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countGrayPaint, null, null, Theme.key_chats_unreadCounterMuted),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countTextPaint, null, null, Theme.key_chats_unreadCounterText),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, Theme.dialogs_namePaint, null, null, Theme.key_chats_name),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, Theme.dialogs_nameEncryptedPaint, null, null, Theme.key_chats_secretName),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_lockDrawable}, null, Theme.key_chats_secretIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_groupDrawable, Theme.dialogs_broadcastDrawable, Theme.dialogs_botDrawable}, null, Theme.key_chats_nameIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_pinnedDrawable}, null, Theme.key_chats_pinnedIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_messagePaint, null, null, Theme.key_chats_message),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_chats_nameMessage),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_chats_draft),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_chats_attachMessage),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_messagePrintingPaint, null, null, Theme.key_chats_actionMessage),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_timePaint, null, null, Theme.key_chats_date),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_pinnedPaint, null, null, Theme.key_chats_pinnedOverlay),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_tabletSeletedPaint, null, null, Theme.key_chats_tabletSelectedOverlay),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_checkDrawable, Theme.dialogs_halfCheckDrawable}, null, Theme.key_chats_sentCheck),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_clockDrawable}, null, Theme.key_chats_sentClock),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_errorPaint, null, null, Theme.key_chats_sentError),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_errorDrawable}, null, Theme.key_chats_sentErrorIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_verifiedCheckDrawable}, null, Theme.key_chats_verifiedCheck),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_verifiedDrawable}, null, Theme.key_chats_verifiedBackground),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_muteDrawable}, null, Theme.key_chats_muteIcon),

                new ThemeDescription(sideMenu, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_chats_menuBackground),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuName),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuPhone),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuPhoneCats),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuCloudBackgroundCats),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, new String[]{"cloudDrawable"}, null, null, null, Theme.key_chats_menuCloud),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chat_serviceBackground),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuTopShadow),

                new ThemeDescription(sideMenu, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{DrawerActionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemIcon),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerActionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemText),

                new ThemeDescription(sideMenu, 0, new Class[]{DividerCell.class}, Theme.dividerPaint, null, null, Theme.key_divider),

                new ThemeDescription(listView, 0, new Class[]{LoadingCell.class}, new String[]{"progressBar"}, null, null, null, Theme.key_progressCircle),

                new ThemeDescription(listView, 0, new Class[]{ProfileSearchCell.class}, Theme.dialogs_offlinePaint, null, null, Theme.key_windowBackgroundWhiteGrayText3),
                new ThemeDescription(listView, 0, new Class[]{ProfileSearchCell.class}, Theme.dialogs_onlinePaint, null, null, Theme.key_windowBackgroundWhiteBlueText3),

                new ThemeDescription(listView, 0, new Class[]{GraySectionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),
                new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{GraySectionCell.class}, null, null, null, Theme.key_graySection),

                new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{HashtagSearchCell.class}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),

                new ThemeDescription(progressView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_progressCircle),

                new ThemeDescription(dialogsSearchAdapter.getInnerListView(), 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countPaint, null, null, Theme.key_chats_unreadCounter),
                new ThemeDescription(dialogsSearchAdapter.getInnerListView(), 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countGrayPaint, null, null, Theme.key_chats_unreadCounterMuted),
                new ThemeDescription(dialogsSearchAdapter.getInnerListView(), 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countTextPaint, null, null, Theme.key_chats_unreadCounterText),
                new ThemeDescription(dialogsSearchAdapter.getInnerListView(), 0, new Class[]{HintDialogCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),

                new ThemeDescription(fragmentContextView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_inappPlayerBackground),
                new ThemeDescription(fragmentContextView, 0, new Class[]{FragmentContextView.class}, new String[]{"playButton"}, null, null, null, Theme.key_inappPlayerPlayPause),
                new ThemeDescription(fragmentContextView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_inappPlayerTitle),
                new ThemeDescription(fragmentContextView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_inappPlayerPerformer),
                new ThemeDescription(fragmentContextView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{FragmentContextView.class}, new String[]{"closeButton"}, null, null, null, Theme.key_inappPlayerClose),

                new ThemeDescription(fragmentContextView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_returnToCallBackground),
                new ThemeDescription(fragmentContextView, 0, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_returnToCallText),

                new ThemeDescription(fragmentLocationContextView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_inappPlayerBackground),
                new ThemeDescription(fragmentLocationContextView, 0, new Class[]{FragmentContextView.class}, new String[]{"playButton"}, null, null, null, Theme.key_inappPlayerPlayPause),
                new ThemeDescription(fragmentLocationContextView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_inappPlayerTitle),
                new ThemeDescription(fragmentLocationContextView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_inappPlayerPerformer),
                new ThemeDescription(fragmentLocationContextView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{FragmentContextView.class}, new String[]{"closeButton"}, null, null, null, Theme.key_inappPlayerClose),

                new ThemeDescription(fragmentLocationContextView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_returnToCallBackground),
                new ThemeDescription(fragmentLocationContextView, 0, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_returnToCallText),

                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBackgroundGray),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlack),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextLink),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogLinkSelection),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue2),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue3),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue4),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextRed),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray2),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray3),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray4),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogIcon),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextHint),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogInputField),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogInputFieldActivated),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareCheck),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareUnchecked),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareDisabled),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRadioBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRadioBackgroundChecked),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogProgressCircle),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogButton),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogButtonSelector),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogScrollGlow),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRoundCheckBox),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRoundCheckBoxCheck),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBadgeBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBadgeText),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogLineProgress),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogLineProgressBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogGrayLine),

                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBar),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBarSelector),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBarTitle),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBarTop),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBarSubtitle),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBarItems),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_background),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_time),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_progressBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_progress),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_placeholder),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_placeholderBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_button),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_buttonActive),
        };
    }
}
