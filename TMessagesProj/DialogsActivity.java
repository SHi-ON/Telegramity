package org.telegram.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager.TaskDescription;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Outline;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatPhoto;
import org.telegram.tgnet.TLRPC.Dialog;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputChannel;
import org.telegram.tgnet.TLRPC.TL_dialogChannel;
import org.telegram.tgnet.TLRPC.TL_peerNotifySettings;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuItem.ActionBarMenuItemSearchListener;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet.Builder;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.ActionBar.MenuDrawable;
import org.telegram.ui.Adapters.DialogsAdapter;
import org.telegram.ui.Adapters.DialogsSearchAdapter;
import org.telegram.ui.Adapters.DialogsSearchAdapter.MessagesActivitySearchAdapterDelegate;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.Favourite;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PlayerView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.OnItemLongClickListener;
import org.telegram.ui.Components.ResourceLoader;

public class DialogsActivity
  extends BaseFragment
  implements NotificationCenterDelegate, PhotoViewer.PhotoViewerProvider
{
  private static boolean dialogsLoaded;
  private String addToGroupAlertString;
  private TextView allCounter;
  private ImageView allTab;
  private BackupImageView avatarImage;
  private ImageView botsTab;
  private ImageView channelsTab;
  private int chat_id = 0;
  private boolean checkPermission = true;
  private MessagesActivityDelegate delegate;
  private DialogsAdapter dialogsAdapter;
  private DialogsAdapter dialogsBackupAdapter;
  private DialogsSearchAdapter dialogsSearchAdapter;
  private int dialogsType;
  private boolean disableAnimation;
  private DisplayMetrics displayMetrics;
  private LinearLayout emptyView;
  private ImageView favsTab;
  private ImageView floatingButton;
  private boolean floatingHidden;
  private final AccelerateDecelerateInterpolator floatingInterpolator = new AccelerateDecelerateInterpolator();
  private ImageView groupsTab;
  private boolean hideTabs;
  private LinearLayoutManager layoutManager;
  private RecyclerListView listView;
  private DialogsOnTouch onTouchListener = null;
  private boolean onlySelect;
  private long openedDialogId;
  private ActionBarMenuItem passcodeItem;
  private AlertDialog permissionDialog;
  private int prevPosition;
  private int prevTop;
  private ProgressBar progressView;
  private boolean scrollUpdated;
  private EmptyTextProgressView searchEmptyView;
  private String searchString;
  private boolean searchWas;
  private boolean searching;
  private String selectAlertString;
  private String selectAlertStringGroup;
  private long selectedDialog;
  private int selectedTab;
  private ImageView superGroupsTab;
  private int tabsHeight;
  private boolean tabsHidden;
  private LinearLayout tabsLayout;
  private FrameLayout tabsView;
  private Button toastBtn;
  private float touchPositionDP;
  private int user_id = 0;
  private ImageView usersTab;
  
  public DialogsActivity(Bundle paramBundle)
  {
    super(paramBundle);
  }
  
  @TargetApi(23)
  private void askForPermissons()
  {
    Activity localActivity = getParentActivity();
    if (localActivity == null) {
      return;
    }
    ArrayList localArrayList = new ArrayList();
    if (localActivity.checkSelfPermission("android.permission.READ_CONTACTS") != 0)
    {
      localArrayList.add("android.permission.READ_CONTACTS");
      localArrayList.add("android.permission.WRITE_CONTACTS");
      localArrayList.add("android.permission.GET_ACCOUNTS");
    }
    if (localActivity.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0)
    {
      localArrayList.add("android.permission.READ_EXTERNAL_STORAGE");
      localArrayList.add("android.permission.WRITE_EXTERNAL_STORAGE");
    }
    localActivity.requestPermissions((String[])localArrayList.toArray(new String[localArrayList.size()]), 1);
  }
  
  private void createTabs(final Context paramContext)
  {
    SharedPreferences localSharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0);
    Editor localEditor = localSharedPreferences.edit();
    boolean bool1 = localSharedPreferences.getBoolean("hideUsers", false);
    boolean bool2 = localSharedPreferences.getBoolean("hideGroups", false);
    boolean bool3 = localSharedPreferences.getBoolean("hideSGroups", false);
    boolean bool4 = localSharedPreferences.getBoolean("hideChannels", false);
    boolean bool5 = localSharedPreferences.getBoolean("hideBots", false);
    boolean bool6 = localSharedPreferences.getBoolean("hideFavs", false);
    this.hideTabs = localSharedPreferences.getBoolean("hideTabs", false);
    this.disableAnimation = localSharedPreferences.getBoolean("disableTabsAnimation", false);
    if ((bool1) && (bool2) && (bool3) && (bool4) && (bool5) && (bool6) && (!this.hideTabs))
    {
      this.hideTabs = true;
      localEditor.putBoolean("hideTabs", true).apply();
    }
    this.tabsHeight = localSharedPreferences.getInt("tabsHeight", 40);
    refreshTabAndListViews(false);
    int i = localSharedPreferences.getInt("defTab", -1);
    if (i != -1)
    {
      this.selectedTab = i;
      if ((!this.hideTabs) && (this.dialogsType != this.selectedTab)) {
        if ((this.selectedTab != 4) || (!bool3)) {
          break label969;
        }
      }
    }
    label969:
    for (i = 9;; i = this.selectedTab)
    {
      this.dialogsType = i;
      this.dialogsAdapter = new DialogsAdapter(paramContext, this.dialogsType);
      this.listView.setAdapter(this.dialogsAdapter);
      this.dialogsAdapter.notifyDataSetChanged();
      this.dialogsBackupAdapter = new DialogsAdapter(paramContext, 0);
      this.tabsLayout = new LinearLayout(paramContext);
      this.tabsLayout.setOrientation(0);
      this.tabsLayout.setGravity(17);
      this.allTab = new ImageView(paramContext);
      this.allTab.setScaleType(ScaleType.CENTER);
      this.allTab.setImageResource(2130838025);
      this.tabsLayout.addView(this.allTab, LayoutHelper.createLinear(0, -1, 1.0F));
      this.usersTab = new ImageView(paramContext);
      this.usersTab.setScaleType(ScaleType.CENTER);
      this.usersTab.setImageResource(2130838036);
      if (!bool1) {
        this.tabsLayout.addView(this.usersTab, LayoutHelper.createLinear(0, -1, 1.0F));
      }
      this.groupsTab = new ImageView(paramContext);
      this.groupsTab.setScaleType(ScaleType.CENTER);
      this.groupsTab.setImageResource(2130838032);
      if (!bool2) {
        this.tabsLayout.addView(this.groupsTab, LayoutHelper.createLinear(0, -1, 1.0F));
      }
      this.superGroupsTab = new ImageView(paramContext);
      this.superGroupsTab.setScaleType(ScaleType.CENTER);
      this.superGroupsTab.setImageResource(2130838034);
      if (!bool3) {
        this.tabsLayout.addView(this.superGroupsTab, LayoutHelper.createLinear(0, -1, 1.0F));
      }
      this.channelsTab = new ImageView(paramContext);
      this.channelsTab.setScaleType(ScaleType.CENTER);
      this.channelsTab.setImageResource(2130838030);
      if (!bool4) {
        this.tabsLayout.addView(this.channelsTab, LayoutHelper.createLinear(0, -1, 1.0F));
      }
      this.botsTab = new ImageView(paramContext);
      this.botsTab.setScaleType(ScaleType.CENTER);
      this.botsTab.setImageResource(2130838029);
      if (!bool5) {
        this.tabsLayout.addView(this.botsTab, LayoutHelper.createLinear(0, -1, 1.0F));
      }
      this.favsTab = new ImageView(paramContext);
      this.favsTab.setScaleType(ScaleType.CENTER);
      this.favsTab.setImageResource(2130838031);
      if (!bool6) {
        this.tabsLayout.addView(this.favsTab, LayoutHelper.createLinear(0, -1, 1.0F));
      }
      this.tabsView.addView(this.tabsLayout, LayoutHelper.createFrame(-1, -1.0F));
      this.allTab.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (DialogsActivity.this.dialogsType != 0)
          {
            DialogsActivity.access$3102(DialogsActivity.this, 0);
            DialogsActivity.this.refreshAdapter(paramContext);
          }
        }
      });
      this.allTab.setOnLongClickListener(new OnLongClickListener()
      {
        public boolean onLongClick(View paramAnonymousView)
        {
          AlertDialog.Builder localBuilder = new AlertDialog.Builder(DialogsActivity.this.getParentActivity());
          localBuilder.setTitle(LocaleController.getString("All", 2131166338));
          paramAnonymousView = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0);
          final int i = paramAnonymousView.getInt("defTab", -1);
          final int j = paramAnonymousView.getInt("sortAll", 0);
          if (i == 0)
          {
            paramAnonymousView = LocaleController.getString("ResetDefaultTab", 2131166377);
            if (j != 0) {
              break label171;
            }
          }
          label171:
          for (String str1 = LocaleController.getString("SortByUnreadCount", 2131166398);; str1 = LocaleController.getString("SortByLastMessage", 2131166396))
          {
            String str2 = LocaleController.getString("HideShowTabs", 2131166363);
            String str3 = LocaleController.getString("MarkAllAsRead", 2131166406);
            DialogInterface.OnClickListener local1 = new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                int j = 0;
                int i = 0;
                paramAnonymous2DialogInterface = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0).edit();
                if (paramAnonymous2Int == 0)
                {
                  paramAnonymous2DialogInterface = new AlertDialog.Builder(DialogsActivity.this.getParentActivity());
                  DialogsActivity.this.createTabsDialog(DialogsActivity.16.this.val$context, paramAnonymous2DialogInterface);
                  paramAnonymous2DialogInterface.setNegativeButton(LocaleController.getString("Done", 2131165529), null);
                  DialogsActivity.this.showDialog(paramAnonymous2DialogInterface.create());
                }
                do
                {
                  do
                  {
                    return;
                    if (paramAnonymous2Int != 1) {
                      break;
                    }
                    paramAnonymous2Int = i;
                    if (j == 0) {
                      paramAnonymous2Int = 1;
                    }
                    paramAnonymous2DialogInterface.putInt("sortAll", paramAnonymous2Int).apply();
                  } while (DialogsActivity.this.dialogsAdapter.getItemCount() <= 1);
                  DialogsActivity.this.dialogsAdapter.notifyDataSetChanged();
                  return;
                  if (paramAnonymous2Int == 2)
                  {
                    paramAnonymous2Int = j;
                    if (i == 0) {
                      paramAnonymous2Int = -1;
                    }
                    paramAnonymous2DialogInterface.putInt("defTab", paramAnonymous2Int).apply();
                    return;
                  }
                } while (paramAnonymous2Int != 3);
                DialogsActivity.this.markAsReadDialog(true);
              }
            };
            localBuilder.setItems(new CharSequence[] { str2, str1, paramAnonymousView, str3 }, local1);
            DialogsActivity.this.showDialog(localBuilder.create());
            return true;
            paramAnonymousView = LocaleController.getString("SetAsDefaultTab", 2131166386);
            break;
          }
        }
      });
      this.usersTab.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (DialogsActivity.this.dialogsType != 3)
          {
            DialogsActivity.access$3102(DialogsActivity.this, 3);
            DialogsActivity.this.refreshAdapter(paramContext);
          }
        }
      });
      this.usersTab.setOnLongClickListener(new OnLongClickListener()
      {
        public boolean onLongClick(View paramAnonymousView)
        {
          AlertDialog.Builder localBuilder = new AlertDialog.Builder(DialogsActivity.this.getParentActivity());
          localBuilder.setTitle(LocaleController.getString("Users", 2131166404));
          paramAnonymousView = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0);
          final int i = paramAnonymousView.getInt("sortUsers", 0);
          final int j = paramAnonymousView.getInt("defTab", -1);
          if (j == 3)
          {
            paramAnonymousView = LocaleController.getString("ResetDefaultTab", 2131166377);
            if (i != 0) {
              break label158;
            }
          }
          label158:
          for (String str1 = LocaleController.getString("SortByStatus", 2131166397);; str1 = LocaleController.getString("SortByLastMessage", 2131166396))
          {
            String str2 = LocaleController.getString("MarkAllAsRead", 2131166406);
            DialogInterface.OnClickListener local1 = new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                int i = 3;
                paramAnonymous2DialogInterface = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0).edit();
                if (paramAnonymous2Int == 1)
                {
                  paramAnonymous2Int = i;
                  if (j == 3) {
                    paramAnonymous2Int = -1;
                  }
                  paramAnonymous2DialogInterface.putInt("defTab", paramAnonymous2Int).apply();
                }
                do
                {
                  return;
                  if (paramAnonymous2Int == 0)
                  {
                    if (i == 0) {}
                    for (paramAnonymous2Int = 1;; paramAnonymous2Int = 0)
                    {
                      paramAnonymous2DialogInterface.putInt("sortUsers", paramAnonymous2Int).apply();
                      if (DialogsActivity.this.dialogsAdapter.getItemCount() <= 1) {
                        break;
                      }
                      DialogsActivity.this.dialogsAdapter.notifyDataSetChanged();
                      return;
                    }
                  }
                } while (paramAnonymous2Int != 2);
                DialogsActivity.this.markAsReadDialog(true);
              }
            };
            localBuilder.setItems(new CharSequence[] { str1, paramAnonymousView, str2 }, local1);
            DialogsActivity.this.showDialog(localBuilder.create());
            return true;
            paramAnonymousView = LocaleController.getString("SetAsDefaultTab", 2131166386);
            break;
          }
        }
      });
      this.groupsTab.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0).getBoolean("hideSGroups", false)) {}
          for (int i = 9;; i = 4)
          {
            if (DialogsActivity.this.dialogsType != i)
            {
              DialogsActivity.access$3102(DialogsActivity.this, i);
              DialogsActivity.this.refreshAdapter(paramContext);
            }
            return;
          }
        }
      });
      this.groupsTab.setOnLongClickListener(new OnLongClickListener()
      {
        public boolean onLongClick(View paramAnonymousView)
        {
          AlertDialog.Builder localBuilder = new AlertDialog.Builder(DialogsActivity.this.getParentActivity());
          localBuilder.setTitle(LocaleController.getString("Groups", 2131166362));
          paramAnonymousView = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0);
          final boolean bool = paramAnonymousView.getBoolean("hideSGroups", false);
          final int i = paramAnonymousView.getInt("sortGroups", 0);
          final int j = paramAnonymousView.getInt("defTab", -1);
          String str1;
          if (j == 4)
          {
            paramAnonymousView = LocaleController.getString("ResetDefaultTab", 2131166377);
            if (i != 0) {
              break label190;
            }
            str1 = LocaleController.getString("SortByUnreadCount", 2131166398);
            label96:
            if (!bool) {
              break label202;
            }
          }
          label190:
          label202:
          for (String str2 = LocaleController.getString("ShowSuperGroupsTab", 2131166394);; str2 = LocaleController.getString("HideSuperGroupsTab", 2131166365))
          {
            String str3 = LocaleController.getString("MarkAllAsRead", 2131166406);
            DialogInterface.OnClickListener local1 = new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                boolean bool = true;
                int i = 0;
                paramAnonymous2DialogInterface = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0);
                Object localObject = paramAnonymous2DialogInterface.edit();
                if (paramAnonymous2Int == 0) {
                  if (!bool)
                  {
                    ((Editor)localObject).putBoolean("hideSGroups", bool).apply();
                    if (bool) {
                      break label132;
                    }
                    DialogsActivity.this.tabsLayout.removeView(DialogsActivity.this.superGroupsTab);
                    if (DialogsActivity.this.dialogsType == 7)
                    {
                      DialogsActivity.access$3102(DialogsActivity.this, 9);
                      DialogsActivity.this.refreshAdapter(DialogsActivity.20.this.val$context);
                    }
                  }
                }
                label132:
                label254:
                do
                {
                  do
                  {
                    return;
                    bool = false;
                    break;
                    bool = paramAnonymous2DialogInterface.getBoolean("hideUsers", false);
                    paramAnonymous2DialogInterface = DialogsActivity.this.tabsLayout;
                    localObject = DialogsActivity.this.superGroupsTab;
                    if (bool) {}
                    for (paramAnonymous2Int = 2;; paramAnonymous2Int = 3)
                    {
                      paramAnonymous2DialogInterface.addView((View)localObject, paramAnonymous2Int, LayoutHelper.createLinear(0, -1, 1.0F));
                      return;
                    }
                    if (paramAnonymous2Int != 1) {
                      break label254;
                    }
                    paramAnonymous2Int = i;
                    if (i == 0) {
                      paramAnonymous2Int = 1;
                    }
                    ((Editor)localObject).putInt("sortGroups", paramAnonymous2Int).apply();
                  } while (DialogsActivity.this.dialogsAdapter.getItemCount() <= 1);
                  DialogsActivity.this.dialogsAdapter.notifyDataSetChanged();
                  return;
                  if (paramAnonymous2Int == 2)
                  {
                    if (j == 4) {}
                    for (paramAnonymous2Int = -1;; paramAnonymous2Int = 4)
                    {
                      ((Editor)localObject).putInt("defTab", paramAnonymous2Int).apply();
                      return;
                    }
                  }
                } while (paramAnonymous2Int != 3);
                DialogsActivity.this.markAsReadDialog(true);
              }
            };
            localBuilder.setItems(new CharSequence[] { str2, str1, paramAnonymousView, str3 }, local1);
            DialogsActivity.this.showDialog(localBuilder.create());
            return true;
            paramAnonymousView = LocaleController.getString("SetAsDefaultTab", 2131166386);
            break;
            str1 = LocaleController.getString("SortByLastMessage", 2131166396);
            break label96;
          }
        }
      });
      this.superGroupsTab.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (DialogsActivity.this.dialogsType != 7)
          {
            DialogsActivity.access$3102(DialogsActivity.this, 7);
            DialogsActivity.this.refreshAdapter(paramContext);
          }
        }
      });
      this.superGroupsTab.setOnLongClickListener(new OnLongClickListener()
      {
        public boolean onLongClick(View paramAnonymousView)
        {
          AlertDialog.Builder localBuilder = new AlertDialog.Builder(DialogsActivity.this.getParentActivity());
          localBuilder.setTitle(LocaleController.getString("SuperGroups", 2131166399));
          paramAnonymousView = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0);
          final int i = paramAnonymousView.getInt("defTab", -1);
          final int j = paramAnonymousView.getInt("sortSGroups", 0);
          final boolean bool = paramAnonymousView.getBoolean("hideSGroups", false);
          String str1;
          if (i == 7)
          {
            paramAnonymousView = LocaleController.getString("ResetDefaultTab", 2131166377);
            if (j != 0) {
              break label191;
            }
            str1 = LocaleController.getString("SortByUnreadCount", 2131166398);
            label97:
            if (!bool) {
              break label203;
            }
          }
          label191:
          label203:
          for (String str2 = LocaleController.getString("ShowSuperGroupsTab", 2131166394);; str2 = LocaleController.getString("HideSuperGroupsTab", 2131166365))
          {
            String str3 = LocaleController.getString("MarkAllAsRead", 2131166406);
            DialogInterface.OnClickListener local1 = new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                boolean bool = true;
                int i = 0;
                paramAnonymous2DialogInterface = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0).edit();
                if (paramAnonymous2Int == 0) {
                  if (!bool)
                  {
                    paramAnonymous2DialogInterface.putBoolean("hideSGroups", bool).apply();
                    if (bool) {
                      break label127;
                    }
                    DialogsActivity.this.tabsLayout.removeView(DialogsActivity.this.superGroupsTab);
                    if (DialogsActivity.this.dialogsType == 7)
                    {
                      DialogsActivity.access$3102(DialogsActivity.this, 0);
                      DialogsActivity.this.refreshAdapter(DialogsActivity.22.this.val$context);
                    }
                  }
                }
                label127:
                label219:
                do
                {
                  do
                  {
                    return;
                    bool = false;
                    break;
                    DialogsActivity.this.tabsLayout.addView(DialogsActivity.this.superGroupsTab, 3, LayoutHelper.createLinear(0, -1, 1.0F));
                    return;
                    if (paramAnonymous2Int != 1) {
                      break label219;
                    }
                    paramAnonymous2Int = i;
                    if (j == 0) {
                      paramAnonymous2Int = 1;
                    }
                    paramAnonymous2DialogInterface.putInt("sortSGroups", paramAnonymous2Int).apply();
                  } while (DialogsActivity.this.dialogsAdapter.getItemCount() <= 1);
                  DialogsActivity.this.dialogsAdapter.notifyDataSetChanged();
                  return;
                  if (paramAnonymous2Int == 2)
                  {
                    if (i == 7) {}
                    for (paramAnonymous2Int = -1;; paramAnonymous2Int = 7)
                    {
                      paramAnonymous2DialogInterface.putInt("defTab", paramAnonymous2Int).apply();
                      return;
                    }
                  }
                } while (paramAnonymous2Int != 3);
                DialogsActivity.this.markAsReadDialog(true);
              }
            };
            localBuilder.setItems(new CharSequence[] { str2, str1, paramAnonymousView, str3 }, local1);
            DialogsActivity.this.showDialog(localBuilder.create());
            return true;
            paramAnonymousView = LocaleController.getString("SetAsDefaultTab", 2131166386);
            break;
            str1 = LocaleController.getString("SortByLastMessage", 2131166396);
            break label97;
          }
        }
      });
      this.channelsTab.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (DialogsActivity.this.dialogsType != 5)
          {
            DialogsActivity.access$3102(DialogsActivity.this, 5);
            DialogsActivity.this.refreshAdapter(paramContext);
          }
        }
      });
      this.channelsTab.setOnLongClickListener(new OnLongClickListener()
      {
        public boolean onLongClick(View paramAnonymousView)
        {
          AlertDialog.Builder localBuilder = new AlertDialog.Builder(DialogsActivity.this.getParentActivity());
          localBuilder.setTitle(LocaleController.getString("Channels", 2131166344));
          paramAnonymousView = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0);
          final int i = paramAnonymousView.getInt("sortChannels", 0);
          final int j = paramAnonymousView.getInt("defTab", -1);
          if (j == 5)
          {
            paramAnonymousView = LocaleController.getString("ResetDefaultTab", 2131166377);
            if (i != 0) {
              break label158;
            }
          }
          label158:
          for (String str1 = LocaleController.getString("SortByUnreadCount", 2131166398);; str1 = LocaleController.getString("SortByLastMessage", 2131166396))
          {
            String str2 = LocaleController.getString("MarkAllAsRead", 2131166406);
            DialogInterface.OnClickListener local1 = new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                int i = 5;
                paramAnonymous2DialogInterface = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0).edit();
                if (paramAnonymous2Int == 1)
                {
                  paramAnonymous2Int = i;
                  if (j == 5) {
                    paramAnonymous2Int = -1;
                  }
                  paramAnonymous2DialogInterface.putInt("defTab", paramAnonymous2Int).apply();
                }
                do
                {
                  return;
                  if (paramAnonymous2Int == 0)
                  {
                    if (i == 0) {}
                    for (paramAnonymous2Int = 1;; paramAnonymous2Int = 0)
                    {
                      paramAnonymous2DialogInterface.putInt("sortChannels", paramAnonymous2Int).apply();
                      if (DialogsActivity.this.dialogsAdapter.getItemCount() <= 1) {
                        break;
                      }
                      DialogsActivity.this.dialogsAdapter.notifyDataSetChanged();
                      return;
                    }
                  }
                } while (paramAnonymous2Int != 2);
                DialogsActivity.this.markAsReadDialog(true);
              }
            };
            localBuilder.setItems(new CharSequence[] { str1, paramAnonymousView, str2 }, local1);
            DialogsActivity.this.showDialog(localBuilder.create());
            return true;
            paramAnonymousView = LocaleController.getString("SetAsDefaultTab", 2131166386);
            break;
          }
        }
      });
      this.botsTab.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (DialogsActivity.this.dialogsType != 6)
          {
            DialogsActivity.access$3102(DialogsActivity.this, 6);
            DialogsActivity.this.refreshAdapter(paramContext);
          }
        }
      });
      this.botsTab.setOnLongClickListener(new OnLongClickListener()
      {
        public boolean onLongClick(View paramAnonymousView)
        {
          AlertDialog.Builder localBuilder = new AlertDialog.Builder(DialogsActivity.this.getParentActivity());
          localBuilder.setTitle(LocaleController.getString("Bots", 2131166342));
          paramAnonymousView = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0);
          final int i = paramAnonymousView.getInt("sortBots", 0);
          final int j = paramAnonymousView.getInt("defTab", -1);
          if (j == 6)
          {
            paramAnonymousView = LocaleController.getString("ResetDefaultTab", 2131166377);
            if (i != 0) {
              break label159;
            }
          }
          label159:
          for (String str1 = LocaleController.getString("SortByUnreadCount", 2131166398);; str1 = LocaleController.getString("SortByLastMessage", 2131166396))
          {
            String str2 = LocaleController.getString("MarkAllAsRead", 2131166406);
            DialogInterface.OnClickListener local1 = new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                int i = 6;
                paramAnonymous2DialogInterface = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0).edit();
                if (paramAnonymous2Int == 1)
                {
                  paramAnonymous2Int = i;
                  if (j == 6) {
                    paramAnonymous2Int = -1;
                  }
                  paramAnonymous2DialogInterface.putInt("defTab", paramAnonymous2Int).apply();
                }
                do
                {
                  return;
                  if (paramAnonymous2Int == 0)
                  {
                    if (i == 0) {}
                    for (paramAnonymous2Int = 1;; paramAnonymous2Int = 0)
                    {
                      paramAnonymous2DialogInterface.putInt("sortBots", paramAnonymous2Int).apply();
                      if (DialogsActivity.this.dialogsAdapter.getItemCount() <= 1) {
                        break;
                      }
                      DialogsActivity.this.dialogsAdapter.notifyDataSetChanged();
                      return;
                    }
                  }
                } while (paramAnonymous2Int != 2);
                DialogsActivity.this.markAsReadDialog(true);
              }
            };
            localBuilder.setItems(new CharSequence[] { str1, paramAnonymousView, str2 }, local1);
            DialogsActivity.this.showDialog(localBuilder.create());
            return true;
            paramAnonymousView = LocaleController.getString("SetAsDefaultTab", 2131166386);
            break;
          }
        }
      });
      this.favsTab.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (DialogsActivity.this.dialogsType != 8)
          {
            DialogsActivity.access$3102(DialogsActivity.this, 8);
            DialogsActivity.this.refreshAdapter(paramContext);
          }
        }
      });
      this.favsTab.setOnLongClickListener(new OnLongClickListener()
      {
        public boolean onLongClick(View paramAnonymousView)
        {
          AlertDialog.Builder localBuilder = new AlertDialog.Builder(DialogsActivity.this.getParentActivity());
          localBuilder.setTitle(LocaleController.getString("Favorites", 2131166360));
          paramAnonymousView = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0);
          final int i = paramAnonymousView.getInt("sortFavs", 0);
          final int j = paramAnonymousView.getInt("defTab", -1);
          if (j == 8)
          {
            paramAnonymousView = LocaleController.getString("ResetDefaultTab", 2131166377);
            if (i != 0) {
              break label159;
            }
          }
          label159:
          for (String str1 = LocaleController.getString("SortByUnreadCount", 2131166398);; str1 = LocaleController.getString("SortByLastMessage", 2131166396))
          {
            String str2 = LocaleController.getString("MarkAllAsRead", 2131166406);
            DialogInterface.OnClickListener local1 = new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                int i = 8;
                paramAnonymous2DialogInterface = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0).edit();
                if (paramAnonymous2Int == 1)
                {
                  paramAnonymous2Int = i;
                  if (j == 8) {
                    paramAnonymous2Int = -1;
                  }
                  paramAnonymous2DialogInterface.putInt("defTab", paramAnonymous2Int).apply();
                }
                do
                {
                  return;
                  if (paramAnonymous2Int == 0)
                  {
                    if (i == 0) {}
                    for (paramAnonymous2Int = 1;; paramAnonymous2Int = 0)
                    {
                      paramAnonymous2DialogInterface.putInt("sortFavs", paramAnonymous2Int).apply();
                      if (DialogsActivity.this.dialogsAdapter.getItemCount() <= 1) {
                        break;
                      }
                      DialogsActivity.this.dialogsAdapter.notifyDataSetChanged();
                      return;
                    }
                  }
                } while (paramAnonymous2Int != 2);
                DialogsActivity.this.markAsReadDialog(true);
              }
            };
            localBuilder.setItems(new CharSequence[] { str1, paramAnonymousView, str2 }, local1);
            DialogsActivity.this.showDialog(localBuilder.create());
            return true;
            paramAnonymousView = LocaleController.getString("SetAsDefaultTab", 2131166386);
            break;
          }
        }
      });
      return;
      i = localSharedPreferences.getInt("selTab", 0);
      break;
    }
  }
  
  private AlertDialog.Builder createTabsDialog(final Context paramContext, AlertDialog.Builder paramBuilder)
  {
    paramBuilder.setTitle(LocaleController.getString("HideShowTabs", 2131166363));
    Object localObject = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0);
    boolean bool1 = ((SharedPreferences)localObject).getBoolean("hideUsers", false);
    boolean bool2 = ((SharedPreferences)localObject).getBoolean("hideGroups", false);
    boolean bool3 = ((SharedPreferences)localObject).getBoolean("hideSGroups", false);
    boolean bool4 = ((SharedPreferences)localObject).getBoolean("hideChannels", false);
    boolean bool5 = ((SharedPreferences)localObject).getBoolean("hideBots", false);
    boolean bool6 = ((SharedPreferences)localObject).getBoolean("hideFavs", false);
    localObject = LocaleController.getString("Users", 2131166404);
    String str1 = LocaleController.getString("Groups", 2131166362);
    String str2 = LocaleController.getString("SuperGroups", 2131166399);
    String str3 = LocaleController.getString("Channels", 2131166344);
    String str4 = LocaleController.getString("Bots", 2131166342);
    String str5 = LocaleController.getString("Favorites", 2131166360);
    if (!bool1)
    {
      bool1 = true;
      if (bool2) {
        break label306;
      }
      bool2 = true;
      label183:
      if (bool3) {
        break label312;
      }
      bool3 = true;
      label191:
      if (bool4) {
        break label318;
      }
      bool4 = true;
      label199:
      if (bool5) {
        break label324;
      }
      bool5 = true;
      label207:
      if (bool6) {
        break label330;
      }
    }
    label306:
    label312:
    label318:
    label324:
    label330:
    for (bool6 = true;; bool6 = false)
    {
      paramContext = new OnMultiChoiceClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt, boolean paramAnonymousBoolean)
        {
          Object localObject = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0);
          paramAnonymousDialogInterface = ((SharedPreferences)localObject).edit();
          boolean bool12 = ((SharedPreferences)localObject).getBoolean("hideTabs", false);
          boolean bool6 = ((SharedPreferences)localObject).getBoolean("hideUsers", false);
          boolean bool7 = ((SharedPreferences)localObject).getBoolean("hideGroups", false);
          boolean bool8 = ((SharedPreferences)localObject).getBoolean("hideSGroups", false);
          boolean bool9 = ((SharedPreferences)localObject).getBoolean("hideChannels", false);
          boolean bool10 = ((SharedPreferences)localObject).getBoolean("hideBots", false);
          boolean bool11 = ((SharedPreferences)localObject).getBoolean("hideFavs", false);
          boolean bool1;
          int j;
          boolean bool5;
          boolean bool4;
          boolean bool3;
          boolean bool2;
          if (paramAnonymousInt == 0) {
            if (!bool6)
            {
              bool1 = true;
              paramAnonymousDialogInterface.putBoolean("hideUsers", bool1).apply();
              if (bool6) {
                break label314;
              }
              DialogsActivity.this.tabsLayout.removeView(DialogsActivity.this.usersTab);
              if (DialogsActivity.this.dialogsType == 3)
              {
                DialogsActivity.access$3102(DialogsActivity.this, 0);
                DialogsActivity.this.refreshAdapter(paramContext);
              }
              j = 1;
              bool5 = bool8;
              bool4 = bool7;
              bool3 = bool11;
              bool2 = bool9;
              bool1 = bool10;
            }
          }
          for (;;)
          {
            if ((j != 0) && (bool4) && (bool5) && (bool2) && (bool1) && (bool3))
            {
              DialogsActivity.access$1702(DialogsActivity.this, true);
              paramAnonymousDialogInterface.putBoolean("hideTabs", true).apply();
              DialogsActivity.this.refreshTabAndListViews(true);
            }
            if ((paramAnonymousBoolean) && (bool12))
            {
              DialogsActivity.access$1702(DialogsActivity.this, false);
              paramAnonymousDialogInterface.putBoolean("hideTabs", false).apply();
              DialogsActivity.this.refreshTabAndListViews(false);
            }
            return;
            bool1 = false;
            break;
            label314:
            DialogsActivity.this.tabsLayout.addView(DialogsActivity.this.usersTab, 1, LayoutHelper.createLinear(0, -1, 1.0F));
            bool1 = bool10;
            bool2 = bool9;
            bool3 = bool11;
            bool4 = bool7;
            bool5 = bool8;
            j = bool6;
            continue;
            if (paramAnonymousInt == 1)
            {
              if (!bool7) {}
              for (bool1 = true;; bool1 = false)
              {
                paramAnonymousDialogInterface.putBoolean("hideGroups", bool1).apply();
                if (bool7) {
                  break label478;
                }
                DialogsActivity.this.tabsLayout.removeView(DialogsActivity.this.groupsTab);
                if (DialogsActivity.this.dialogsType == 4)
                {
                  DialogsActivity.access$3102(DialogsActivity.this, 0);
                  DialogsActivity.this.refreshAdapter(paramContext);
                }
                bool4 = true;
                bool1 = bool10;
                bool2 = bool9;
                bool3 = bool11;
                bool5 = bool8;
                j = bool6;
                break;
              }
              label478:
              localObject = DialogsActivity.this.tabsLayout;
              ImageView localImageView = DialogsActivity.this.groupsTab;
              if (bool6) {}
              for (paramAnonymousInt = 1;; paramAnonymousInt = 2)
              {
                ((LinearLayout)localObject).addView(localImageView, paramAnonymousInt, LayoutHelper.createLinear(0, -1, 1.0F));
                bool1 = bool10;
                bool2 = bool9;
                bool3 = bool11;
                bool4 = bool7;
                bool5 = bool8;
                j = bool6;
                break;
              }
            }
            label663:
            int i;
            if (paramAnonymousInt == 2)
            {
              if (!bool8) {}
              for (bool1 = true;; bool1 = false)
              {
                paramAnonymousDialogInterface.putBoolean("hideSGroups", bool1).apply();
                if (bool8) {
                  break label663;
                }
                DialogsActivity.this.tabsLayout.removeView(DialogsActivity.this.superGroupsTab);
                if (DialogsActivity.this.dialogsType == 7)
                {
                  DialogsActivity.access$3102(DialogsActivity.this, 4);
                  DialogsActivity.this.refreshAdapter(paramContext);
                }
                bool5 = true;
                bool1 = bool10;
                bool2 = bool9;
                bool3 = bool11;
                bool4 = bool7;
                j = bool6;
                break;
              }
              paramAnonymousInt = 3;
              if (bool6) {
                paramAnonymousInt = 3 - 1;
              }
              i = paramAnonymousInt;
              if (bool7) {
                i = paramAnonymousInt - 1;
              }
              DialogsActivity.this.tabsLayout.addView(DialogsActivity.this.superGroupsTab, i, LayoutHelper.createLinear(0, -1, 1.0F));
              bool1 = bool10;
              bool2 = bool9;
              bool3 = bool11;
              bool4 = bool7;
              bool5 = bool8;
              j = bool6;
            }
            else if (paramAnonymousInt == 3)
            {
              if (!bool9) {}
              for (bool1 = true;; bool1 = false)
              {
                paramAnonymousDialogInterface.putBoolean("hideChannels", bool1).apply();
                if (bool9) {
                  break label852;
                }
                DialogsActivity.this.tabsLayout.removeView(DialogsActivity.this.channelsTab);
                if (DialogsActivity.this.dialogsType == 5)
                {
                  DialogsActivity.access$3102(DialogsActivity.this, 0);
                  DialogsActivity.this.refreshAdapter(paramContext);
                }
                bool2 = true;
                bool1 = bool10;
                bool3 = bool11;
                bool4 = bool7;
                bool5 = bool8;
                j = bool6;
                break;
              }
              label852:
              i = DialogsActivity.this.tabsLayout.getChildCount();
              paramAnonymousInt = i;
              if (!bool11) {
                paramAnonymousInt = i - 1;
              }
              i = paramAnonymousInt;
              if (!bool10) {
                i = paramAnonymousInt - 1;
              }
              DialogsActivity.this.tabsLayout.addView(DialogsActivity.this.channelsTab, i, LayoutHelper.createLinear(0, -1, 1.0F));
              bool1 = bool10;
              bool2 = bool9;
              bool3 = bool11;
              bool4 = bool7;
              bool5 = bool8;
              j = bool6;
            }
            else if (paramAnonymousInt == 4)
            {
              if (!bool10) {}
              for (bool1 = true;; bool1 = false)
              {
                paramAnonymousDialogInterface.putBoolean("hideBots", bool1).apply();
                if (bool10) {
                  break label1056;
                }
                DialogsActivity.this.tabsLayout.removeView(DialogsActivity.this.botsTab);
                if (DialogsActivity.this.dialogsType == 6)
                {
                  DialogsActivity.access$3102(DialogsActivity.this, 0);
                  DialogsActivity.this.refreshAdapter(paramContext);
                }
                bool1 = true;
                bool2 = bool9;
                bool3 = bool11;
                bool4 = bool7;
                bool5 = bool8;
                j = bool6;
                break;
              }
              label1056:
              i = DialogsActivity.this.tabsLayout.getChildCount();
              paramAnonymousInt = i;
              if (!bool11) {
                paramAnonymousInt = i - 1;
              }
              DialogsActivity.this.tabsLayout.addView(DialogsActivity.this.botsTab, paramAnonymousInt, LayoutHelper.createLinear(0, -1, 1.0F, 48, 0, 0, 0, 0));
              bool1 = bool10;
              bool2 = bool9;
              bool3 = bool11;
              bool4 = bool7;
              bool5 = bool8;
              j = bool6;
            }
            else
            {
              bool1 = bool10;
              bool2 = bool9;
              bool3 = bool11;
              bool4 = bool7;
              bool5 = bool8;
              j = bool6;
              if (paramAnonymousInt == 5)
              {
                if (!bool11) {}
                for (bool1 = true;; bool1 = false)
                {
                  paramAnonymousDialogInterface.putBoolean("hideFavs", bool1).apply();
                  if (bool11) {
                    break label1276;
                  }
                  DialogsActivity.this.tabsLayout.removeView(DialogsActivity.this.favsTab);
                  if (DialogsActivity.this.dialogsType == 8)
                  {
                    DialogsActivity.access$3102(DialogsActivity.this, 0);
                    DialogsActivity.this.refreshAdapter(paramContext);
                  }
                  bool3 = true;
                  bool1 = bool10;
                  bool2 = bool9;
                  bool4 = bool7;
                  bool5 = bool8;
                  j = bool6;
                  break;
                }
                label1276:
                DialogsActivity.this.tabsLayout.addView(DialogsActivity.this.favsTab, DialogsActivity.this.tabsLayout.getChildCount(), LayoutHelper.createLinear(0, -1, 1.0F));
                bool1 = bool10;
                bool2 = bool9;
                bool3 = bool11;
                bool4 = bool7;
                bool5 = bool8;
                j = bool6;
              }
            }
          }
        }
      };
      paramBuilder.setMultiChoiceItems(new CharSequence[] { localObject, str1, str2, str3, str4, str5 }, new boolean[] { bool1, bool2, bool3, bool4, bool5, bool6 }, paramContext);
      return paramBuilder;
      bool1 = false;
      break;
      bool2 = false;
      break label183;
      bool3 = false;
      break label191;
      bool4 = false;
      break label199;
      bool5 = false;
      break label207;
    }
  }
  
  private void didSelectResult(final long paramLong, boolean paramBoolean1, boolean paramBoolean2)
  {
    AlertDialog.Builder localBuilder;
    if ((this.addToGroupAlertString == null) && ((int)paramLong < 0) && (ChatObject.isChannel(-(int)paramLong)) && (!ChatObject.isCanWriteToChannel(-(int)paramLong)))
    {
      localBuilder = new AlertDialog.Builder(getParentActivity());
      localBuilder.setTitle(LocaleController.getString("AppName", 2131165280));
      localBuilder.setMessage(LocaleController.getString("ChannelCantSendMessage", 2131165385));
      localBuilder.setNegativeButton(LocaleController.getString("OK", 2131165940), null);
      showDialog(localBuilder.create());
    }
    int i;
    int j;
    Object localObject;
    do
    {
      do
      {
        return;
        if ((!paramBoolean1) || (((this.selectAlertString == null) || (this.selectAlertStringGroup == null)) && (this.addToGroupAlertString == null))) {
          break;
        }
      } while (getParentActivity() == null);
      localBuilder = new AlertDialog.Builder(getParentActivity());
      localBuilder.setTitle(LocaleController.getString("AppName", 2131165280));
      i = (int)paramLong;
      j = (int)(paramLong >> 32);
      if (i == 0) {
        break label414;
      }
      if (j != 1) {
        break;
      }
      localObject = MessagesController.getInstance().getChat(Integer.valueOf(i));
    } while (localObject == null);
    localBuilder.setMessage(LocaleController.formatStringSimple(this.selectAlertStringGroup, new Object[] { ((TLRPC.Chat)localObject).title }));
    for (;;)
    {
      localBuilder.setPositiveButton(LocaleController.getString("OK", 2131165940), new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
        {
          DialogsActivity.this.didSelectResult(paramLong, false, false);
        }
      });
      localBuilder.setNegativeButton(LocaleController.getString("Cancel", 2131165360), null);
      showDialog(localBuilder.create());
      return;
      if (i > 0)
      {
        localObject = MessagesController.getInstance().getUser(Integer.valueOf(i));
        if (localObject == null) {
          break;
        }
        localBuilder.setMessage(LocaleController.formatStringSimple(this.selectAlertString, new Object[] { UserObject.getUserName((TLRPC.User)localObject) }));
        continue;
      }
      if (i < 0)
      {
        localObject = MessagesController.getInstance().getChat(Integer.valueOf(-i));
        if (localObject == null) {
          break;
        }
        if (this.addToGroupAlertString != null)
        {
          localBuilder.setMessage(LocaleController.formatStringSimple(this.addToGroupAlertString, new Object[] { ((TLRPC.Chat)localObject).title }));
        }
        else
        {
          localBuilder.setMessage(LocaleController.formatStringSimple(this.selectAlertStringGroup, new Object[] { ((TLRPC.Chat)localObject).title }));
          continue;
          label414:
          localObject = MessagesController.getInstance().getEncryptedChat(Integer.valueOf(j));
          localObject = MessagesController.getInstance().getUser(Integer.valueOf(((TLRPC.EncryptedChat)localObject).user_id));
          if (localObject == null) {
            break;
          }
          localBuilder.setMessage(LocaleController.formatStringSimple(this.selectAlertString, new Object[] { UserObject.getUserName((TLRPC.User)localObject) }));
        }
      }
    }
    if (this.delegate != null)
    {
      this.delegate.didSelectDialog(this, paramLong, paramBoolean2);
      this.delegate = null;
      return;
    }
    finishFragment();
  }
  
  private ArrayList<TLRPC.Dialog> getDialogsArray()
  {
    if (this.dialogsType == 0) {
      return MessagesController.getInstance().dialogs;
    }
    if (this.dialogsType == 1) {
      return MessagesController.getInstance().dialogsServerOnly;
    }
    if (this.dialogsType == 2) {
      return MessagesController.getInstance().dialogsGroupsOnly;
    }
    if (this.dialogsType == 3) {
      return MessagesController.getInstance().dialogsUsers;
    }
    if (this.dialogsType == 4) {
      return MessagesController.getInstance().dialogsGroups;
    }
    if (this.dialogsType == 5) {
      return MessagesController.getInstance().dialogsChannels;
    }
    if (this.dialogsType == 6) {
      return MessagesController.getInstance().dialogsBots;
    }
    if (this.dialogsType == 7) {
      return MessagesController.getInstance().dialogsMegaGroups;
    }
    if (this.dialogsType == 8) {
      return MessagesController.getInstance().dialogsFavs;
    }
    if (this.dialogsType == 9) {
      return MessagesController.getInstance().dialogsGroupsAll;
    }
    return null;
  }
  
  private String getHeaderAllTitles()
  {
    switch (this.dialogsType)
    {
    default: 
      return getHeaderTitle();
    case 3: 
      return LocaleController.getString("Users", 2131166404);
    case 4: 
    case 9: 
      return LocaleController.getString("Groups", 2131166362);
    case 5: 
      return LocaleController.getString("Channels", 2131166344);
    case 6: 
      return LocaleController.getString("Bots", 2131166342);
    case 7: 
      return LocaleController.getString("SuperGroups", 2131166399);
    }
    return LocaleController.getString("Favorites", 2131166360);
  }
  
  private String getHeaderTitle()
  {
    int i = ApplicationLoader.applicationContext.getSharedPreferences("theme", 0).getInt("chatsHeaderTitle", 0);
    String str2 = LocaleController.getString("AppName", 2131165280);
    TLRPC.User localUser = UserConfig.getCurrentUser();
    String str1;
    if (i == 1) {
      str1 = LocaleController.getString("ShortAppName", 2131166410);
    }
    do
    {
      do
      {
        do
        {
          do
          {
            do
            {
              do
              {
                return str1;
                if (i != 2) {
                  break;
                }
                str1 = str2;
              } while (localUser == null);
              if (localUser.first_name != null) {
                break;
              }
              str1 = str2;
            } while (localUser.last_name == null);
            return ContactsController.formatName(localUser.first_name, localUser.last_name);
            if (i != 3) {
              break;
            }
            str1 = str2;
          } while (localUser == null);
          str1 = str2;
        } while (localUser.username == null);
        str1 = str2;
      } while (localUser.username.length() == 0);
      return "@" + localUser.username;
      str1 = str2;
    } while (i != 4);
    return "";
  }
  
  private void hideFloatingButton(boolean paramBoolean)
  {
    if (this.floatingHidden == paramBoolean) {
      return;
    }
    this.floatingHidden = paramBoolean;
    Object localObject = this.floatingButton;
    float f;
    ImageView localImageView;
    if (this.floatingHidden)
    {
      f = AndroidUtilities.dp(100.0F);
      localObject = ObjectAnimatorProxy.ofFloatProxy(localObject, "translationY", new float[] { f }).setDuration(300L);
      ((ObjectAnimatorProxy)localObject).setInterpolator(this.floatingInterpolator);
      localImageView = this.floatingButton;
      if (paramBoolean) {
        break label92;
      }
    }
    label92:
    for (paramBoolean = true;; paramBoolean = false)
    {
      localImageView.setClickable(paramBoolean);
      ((ObjectAnimatorProxy)localObject).start();
      return;
      f = 0.0F;
      break;
    }
  }
  
  private void hideShowTabs()
  {
    Object localObject = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0);
    boolean bool1 = ((SharedPreferences)localObject).getBoolean("hideUsers", false);
    boolean bool2 = ((SharedPreferences)localObject).getBoolean("hideGroups", false);
    boolean bool3 = ((SharedPreferences)localObject).getBoolean("hideSGroups", false);
    boolean bool4 = ((SharedPreferences)localObject).getBoolean("hideChannels", false);
    boolean bool5 = ((SharedPreferences)localObject).getBoolean("hideBots", false);
    boolean bool6 = ((SharedPreferences)localObject).getBoolean("hideFavs", false);
    if (!bool1) {}
    for (;;)
    {
      int i;
      try
      {
        if (this.usersTab.getParent() == null) {
          this.tabsLayout.addView(this.usersTab, 1, LayoutHelper.createLinear(0, -1, 1.0F));
        }
        if (bool2) {
          break label409;
        }
        ImageView localImageView;
        if (this.groupsTab.getParent() == null)
        {
          localObject = this.tabsLayout;
          localImageView = this.groupsTab;
          if (!bool1) {
            break label404;
          }
          i = 1;
          ((LinearLayout)localObject).addView(localImageView, i, LayoutHelper.createLinear(0, -1, 1.0F));
        }
        if (bool3) {
          break label433;
        }
        if (this.superGroupsTab.getParent() == null)
        {
          localObject = this.tabsLayout;
          localImageView = this.superGroupsTab;
          if (!bool2) {
            break label532;
          }
          if (!bool1) {
            break label527;
          }
          i = 1;
          ((LinearLayout)localObject).addView(localImageView, i, LayoutHelper.createLinear(0, -1, 1.0F));
        }
        if (bool4) {
          break label457;
        }
        if (this.channelsTab.getParent() == null)
        {
          localObject = this.tabsLayout;
          localImageView = this.channelsTab;
          if (!bool3) {
            break label547;
          }
          if (!bool2) {
            break label542;
          }
          if (!bool1) {
            break label537;
          }
          i = 1;
          ((LinearLayout)localObject).addView(localImageView, i, LayoutHelper.createLinear(0, -1, 1.0F));
        }
        if (bool5) {
          break label481;
        }
        int j = this.tabsLayout.getChildCount();
        i = j;
        if (!bool6) {
          i = j - 1;
        }
        if (this.botsTab.getParent() == null) {
          this.tabsLayout.addView(this.botsTab, i, LayoutHelper.createLinear(0, -1, 1.0F));
        }
        if (bool6) {
          break label505;
        }
        if (this.favsTab.getParent() != null) {
          break label526;
        }
        this.tabsLayout.addView(this.favsTab, this.tabsLayout.getChildCount(), LayoutHelper.createLinear(0, -1, 1.0F));
        return;
      }
      catch (Exception localException)
      {
        FileLog.e("tmessages", localException);
        return;
      }
      if (this.usersTab.getParent() != null)
      {
        this.tabsLayout.removeView(this.usersTab);
        continue;
        label404:
        i = 2;
        continue;
        label409:
        if (this.groupsTab.getParent() != null)
        {
          this.tabsLayout.removeView(this.groupsTab);
          continue;
          label433:
          if (this.superGroupsTab.getParent() != null)
          {
            this.tabsLayout.removeView(this.superGroupsTab);
            continue;
            label457:
            if (this.channelsTab.getParent() != null)
            {
              this.tabsLayout.removeView(this.channelsTab);
              continue;
              label481:
              if (this.botsTab.getParent() != null)
              {
                this.tabsLayout.removeView(this.botsTab);
                continue;
                label505:
                if (this.favsTab.getParent() != null) {
                  this.tabsLayout.removeView(this.favsTab);
                }
                label526:
                return;
                label527:
                i = 2;
                continue;
                label532:
                i = 3;
                continue;
                label537:
                i = 2;
                continue;
                label542:
                i = 3;
                continue;
                label547:
                i = 4;
              }
            }
          }
        }
      }
    }
  }
  
  private void hideTabsAnimated(boolean paramBoolean)
  {
    if (this.tabsHidden == paramBoolean) {
      return;
    }
    this.tabsHidden = paramBoolean;
    if (paramBoolean) {
      this.listView.setPadding(0, 0, 0, 0);
    }
    Object localObject = this.tabsView;
    if (paramBoolean) {}
    for (float f = -AndroidUtilities.dp(this.tabsHeight);; f = 0.0F)
    {
      localObject = ObjectAnimatorProxy.ofFloatProxy(localObject, "translationY", new float[] { f }).setDuration(300L);
      ((ObjectAnimatorProxy)localObject).addListener(new AnimatorListenerAdapterProxy()
      {
        public void onAnimationEnd(Object paramAnonymousObject)
        {
          if (!DialogsActivity.this.tabsHidden) {
            DialogsActivity.this.listView.setPadding(0, AndroidUtilities.dp(DialogsActivity.this.tabsHeight), 0, 0);
          }
        }
      });
      ((ObjectAnimatorProxy)localObject).start();
      return;
    }
  }
  
  private void markAsReadDialog(final boolean paramBoolean)
  {
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(getParentActivity());
    localBuilder.setMessage(LocaleController.getString("AreYouSure", 2131165286));
    if (paramBoolean) {}
    for (String str = LocaleController.getString("MarkAllAsRead", 2131166406);; str = LocaleController.getString("MarkAsRead", 2131166407))
    {
      localBuilder.setTitle(str);
      localBuilder.setPositiveButton(LocaleController.getString("OK", 2131165940), new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
        {
          if (paramBoolean)
          {
            paramAnonymousDialogInterface = DialogsActivity.this.getDialogsArray();
            if ((paramAnonymousDialogInterface != null) && (!paramAnonymousDialogInterface.isEmpty()))
            {
              paramAnonymousInt = 0;
              while (paramAnonymousInt < paramAnonymousDialogInterface.size())
              {
                TLRPC.Dialog localDialog = (TLRPC.Dialog)DialogsActivity.this.getDialogsArray().get(paramAnonymousInt);
                if (localDialog.unread_count > 0) {
                  MessagesController.getInstance().markDialogAsRead(localDialog.id, localDialog.last_read, Math.max(0, localDialog.top_message), localDialog.last_message_date, true, false);
                }
                paramAnonymousInt += 1;
              }
            }
          }
          else
          {
            paramAnonymousDialogInterface = (TLRPC.Dialog)MessagesController.getInstance().dialogs_dict.get(Long.valueOf(DialogsActivity.this.selectedDialog));
            if (paramAnonymousDialogInterface.unread_count > 0) {
              MessagesController.getInstance().markDialogAsRead(paramAnonymousDialogInterface.id, paramAnonymousDialogInterface.last_read, Math.max(0, paramAnonymousDialogInterface.top_message), paramAnonymousDialogInterface.last_message_date, true, false);
            }
          }
        }
      });
      localBuilder.setNegativeButton(LocaleController.getString("Cancel", 2131165360), null);
      showDialog(localBuilder.create());
      return;
    }
  }
  
  private void paintHeader(boolean paramBoolean)
  {
    SharedPreferences localSharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("theme", 0);
    this.actionBar.setTitleColor(localSharedPreferences.getInt("chatsHeaderTitleColor", -1));
    int i = localSharedPreferences.getInt("themeColor", -16738680);
    int j = localSharedPreferences.getInt("chatsHeaderColor", i);
    if (!paramBoolean) {
      this.actionBar.setBackgroundColor(j);
    }
    if (paramBoolean) {
      this.tabsView.setBackgroundColor(j);
    }
    int k = localSharedPreferences.getInt("chatsHeaderGradient", 0);
    Object localObject;
    if (k > 0) {
      switch (k)
      {
      default: 
        localObject = Orientation.TOP_BOTTOM;
      }
    }
    for (;;)
    {
      localObject = new GradientDrawable((Orientation)localObject, new int[] { j, localSharedPreferences.getInt("chatsHeaderGradientColor", i) });
      if (!paramBoolean) {
        this.actionBar.setBackgroundDrawable((Drawable)localObject);
      }
      if (paramBoolean) {
        this.tabsView.setBackgroundDrawable((Drawable)localObject);
      }
      return;
      localObject = Orientation.LEFT_RIGHT;
      continue;
      localObject = Orientation.TL_BR;
      continue;
      localObject = Orientation.BL_TR;
    }
  }
  
  private void refreshAdapter(Context paramContext)
  {
    refreshAdapterAndTabs(new DialogsAdapter(paramContext, this.dialogsType));
  }
  
  private void refreshAdapterAndTabs(DialogsAdapter paramDialogsAdapter)
  {
    this.dialogsAdapter = paramDialogsAdapter;
    this.listView.setAdapter(this.dialogsAdapter);
    this.dialogsAdapter.notifyDataSetChanged();
    if (!this.onlySelect) {
      if (this.dialogsType != 9) {
        break label83;
      }
    }
    label83:
    for (int i = 4;; i = this.dialogsType)
    {
      this.selectedTab = i;
      ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0).edit().putInt("selTab", this.selectedTab).apply();
      refreshTabs();
      return;
    }
  }
  
  private void refreshDialogType(int paramInt)
  {
    if (this.hideTabs) {}
    boolean bool1;
    boolean bool2;
    boolean bool3;
    boolean bool4;
    boolean bool5;
    boolean bool6;
    boolean bool7;
    do
    {
      return;
      SharedPreferences localSharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0);
      bool1 = localSharedPreferences.getBoolean("hideUsers", false);
      bool2 = localSharedPreferences.getBoolean("hideGroups", false);
      bool3 = localSharedPreferences.getBoolean("hideSGroups", false);
      bool4 = localSharedPreferences.getBoolean("hideChannels", false);
      bool5 = localSharedPreferences.getBoolean("hideBots", false);
      bool6 = localSharedPreferences.getBoolean("hideFavs", false);
      bool7 = localSharedPreferences.getBoolean("infiniteTabsSwipe", false);
      if (paramInt == 1)
      {
        switch (this.dialogsType)
        {
        default: 
          if (!bool1) {
            paramInt = 3;
          }
          break;
        }
        for (;;)
        {
          this.dialogsType = paramInt;
          return;
          if (bool2)
          {
            if (!bool3) {
              paramInt = 7;
            }
            for (;;)
            {
              this.dialogsType = paramInt;
              return;
              if (!bool4) {
                paramInt = 5;
              } else if (!bool5) {
                paramInt = 6;
              } else if (!bool6) {
                paramInt = 8;
              } else if (bool7) {
                paramInt = 0;
              } else {
                paramInt = this.dialogsType;
              }
            }
          }
          if (bool3) {}
          for (paramInt = 9;; paramInt = 4)
          {
            this.dialogsType = paramInt;
            return;
          }
          if (!bool3) {
            paramInt = 7;
          }
          for (;;)
          {
            this.dialogsType = paramInt;
            return;
            if (!bool4) {
              paramInt = 5;
            } else if (!bool5) {
              paramInt = 6;
            } else if (!bool6) {
              paramInt = 8;
            } else if (bool7) {
              paramInt = 0;
            } else {
              paramInt = this.dialogsType;
            }
          }
          if (!bool4) {
            paramInt = 5;
          }
          for (;;)
          {
            this.dialogsType = paramInt;
            return;
            if (!bool5) {
              paramInt = 6;
            } else if (!bool6) {
              paramInt = 8;
            } else if (bool7) {
              paramInt = 0;
            } else {
              paramInt = this.dialogsType;
            }
          }
          if (!bool5) {
            paramInt = 6;
          }
          for (;;)
          {
            this.dialogsType = paramInt;
            return;
            if (!bool6) {
              paramInt = 8;
            } else if (bool7) {
              paramInt = 0;
            } else {
              paramInt = this.dialogsType;
            }
          }
          if (!bool6) {
            paramInt = 8;
          }
          for (;;)
          {
            this.dialogsType = paramInt;
            return;
            if (bool7) {
              paramInt = 0;
            } else {
              paramInt = this.dialogsType;
            }
          }
          if (!bool7) {
            break;
          }
          this.dialogsType = 0;
          return;
          if ((!bool2) && (bool3)) {
            paramInt = 9;
          } else if (!bool2) {
            paramInt = 7;
          } else if (!bool4) {
            paramInt = 5;
          } else if (!bool5) {
            paramInt = 6;
          } else if (!bool6) {
            paramInt = 8;
          } else if (bool7) {
            paramInt = 0;
          } else {
            paramInt = this.dialogsType;
          }
        }
      }
      switch (this.dialogsType)
      {
      }
    } while (!bool7);
    if (!bool6) {
      paramInt = 8;
    }
    for (;;)
    {
      this.dialogsType = paramInt;
      return;
      this.dialogsType = 0;
      return;
      if (!bool1) {}
      for (paramInt = 3;; paramInt = 0)
      {
        this.dialogsType = paramInt;
        return;
      }
      if (!bool2) {
        paramInt = 4;
      }
      for (;;)
      {
        this.dialogsType = paramInt;
        return;
        if (!bool1) {
          paramInt = 3;
        } else {
          paramInt = 0;
        }
      }
      if (!bool3) {
        paramInt = 7;
      }
      for (;;)
      {
        this.dialogsType = paramInt;
        return;
        if (!bool2) {
          paramInt = 9;
        } else if (!bool1) {
          paramInt = 3;
        } else {
          paramInt = 0;
        }
      }
      if (!bool4) {
        paramInt = 5;
      }
      for (;;)
      {
        this.dialogsType = paramInt;
        return;
        if (!bool3) {
          paramInt = 7;
        } else if (!bool2) {
          paramInt = 9;
        } else if (!bool1) {
          paramInt = 3;
        } else {
          paramInt = 0;
        }
      }
      if (!bool5) {
        paramInt = 6;
      }
      for (;;)
      {
        this.dialogsType = paramInt;
        return;
        if (!bool4) {
          paramInt = 5;
        } else if (!bool3) {
          paramInt = 7;
        } else if (!bool2) {
          paramInt = 9;
        } else if (!bool1) {
          paramInt = 3;
        } else {
          paramInt = 0;
        }
      }
      if (!bool5) {
        paramInt = 6;
      } else if (!bool4) {
        paramInt = 5;
      } else if (!bool3) {
        paramInt = 7;
      } else if (!bool2) {
        paramInt = 9;
      } else if (!bool1) {
        paramInt = 3;
      } else {
        paramInt = 0;
      }
    }
  }
  
  private void refreshTabAndListViews(boolean paramBoolean)
  {
    if ((this.hideTabs) || (paramBoolean))
    {
      this.tabsView.setVisibility(8);
      this.listView.setPadding(0, 0, 0, 0);
    }
    for (;;)
    {
      this.listView.scrollToPosition(0);
      return;
      this.tabsView.setVisibility(0);
      int i = AndroidUtilities.dp(this.tabsHeight);
      ViewGroup.LayoutParams localLayoutParams = this.tabsView.getLayoutParams();
      if (localLayoutParams != null)
      {
        localLayoutParams.height = i;
        this.tabsView.setLayoutParams(localLayoutParams);
      }
      this.listView.setPadding(0, i, 0, 0);
      hideTabsAnimated(false);
    }
  }
  
  private void refreshTabs()
  {
    SharedPreferences localSharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("theme", 0);
    int j = localSharedPreferences.getInt("chatsHeaderIconsColor", -1);
    int i = AndroidUtilities.getIntAlphaColor("chatsHeaderIconsColor", -1, 0.3F);
    this.allTab.setBackgroundResource(0);
    this.usersTab.setBackgroundResource(0);
    this.groupsTab.setBackgroundResource(0);
    this.superGroupsTab.setBackgroundResource(0);
    this.channelsTab.setBackgroundResource(0);
    this.botsTab.setBackgroundResource(0);
    this.favsTab.setBackgroundResource(0);
    this.allTab.setColorFilter(i, PorterDuff.Mode.SRC_IN);
    this.usersTab.setColorFilter(i, PorterDuff.Mode.SRC_IN);
    this.groupsTab.setColorFilter(i, PorterDuff.Mode.SRC_IN);
    this.superGroupsTab.setColorFilter(i, PorterDuff.Mode.SRC_IN);
    this.channelsTab.setColorFilter(i, PorterDuff.Mode.SRC_IN);
    this.botsTab.setColorFilter(i, PorterDuff.Mode.SRC_IN);
    this.favsTab.setColorFilter(i, PorterDuff.Mode.SRC_IN);
    Object localObject = getParentActivity().getResources().getDrawable(2130838033);
    ((Drawable)localObject).setColorFilter(j, PorterDuff.Mode.SRC_IN);
    label259:
    TextView localTextView;
    if (this.dialogsType == 9)
    {
      i = 4;
      switch (i)
      {
      default: 
        this.allTab.setColorFilter(j, PorterDuff.Mode.SRC_IN);
        this.allTab.setBackgroundDrawable((Drawable)localObject);
        localObject = getHeaderAllTitles();
        this.actionBar.setTitle((CharSequence)localObject);
        paintHeader(true);
        if ((getDialogsArray() != null) && (getDialogsArray().isEmpty()))
        {
          this.searchEmptyView.setVisibility(8);
          this.progressView.setVisibility(8);
          if (this.emptyView.getChildCount() > 0)
          {
            localTextView = (TextView)this.emptyView.getChildAt(0);
            if (localTextView != null)
            {
              if (this.dialogsType >= 3) {
                break label584;
              }
              localObject = LocaleController.getString("NoChats", 2131165865);
            }
          }
        }
        break;
      }
    }
    for (;;)
    {
      localTextView.setText((CharSequence)localObject);
      localTextView.setTextColor(localSharedPreferences.getInt("chatsNameColor", -14606047));
      if (this.emptyView.getChildAt(1) != null) {
        this.emptyView.getChildAt(1).setVisibility(8);
      }
      this.emptyView.setVisibility(0);
      this.emptyView.setBackgroundColor(localSharedPreferences.getInt("chatsRowColor", -1));
      this.listView.setEmptyView(this.emptyView);
      return;
      i = this.dialogsType;
      break;
      this.usersTab.setColorFilter(j, PorterDuff.Mode.SRC_IN);
      this.usersTab.setBackgroundDrawable((Drawable)localObject);
      break label259;
      this.groupsTab.setColorFilter(j, PorterDuff.Mode.SRC_IN);
      this.groupsTab.setBackgroundDrawable((Drawable)localObject);
      break label259;
      this.channelsTab.setColorFilter(j, PorterDuff.Mode.SRC_IN);
      this.channelsTab.setBackgroundDrawable((Drawable)localObject);
      break label259;
      this.botsTab.setColorFilter(j, PorterDuff.Mode.SRC_IN);
      this.botsTab.setBackgroundDrawable((Drawable)localObject);
      break label259;
      this.superGroupsTab.setColorFilter(j, PorterDuff.Mode.SRC_IN);
      this.superGroupsTab.setBackgroundDrawable((Drawable)localObject);
      break label259;
      this.favsTab.setColorFilter(j, PorterDuff.Mode.SRC_IN);
      this.favsTab.setBackgroundDrawable((Drawable)localObject);
      break label259;
      label584:
      if (this.dialogsType == 8) {
        localObject = LocaleController.getString("NoFavoritesHelp", 2131166371);
      }
    }
  }
  
  private void updateListBG()
  {
    SharedPreferences localSharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("theme", 0);
    int i = localSharedPreferences.getInt("chatsRowColor", -1);
    int j = localSharedPreferences.getInt("chatsRowGradient", 0);
    if ((j > 0) && (1 != 0))
    {
      Object localObject;
      switch (j)
      {
      default: 
        localObject = Orientation.TOP_BOTTOM;
      }
      for (;;)
      {
        localObject = new GradientDrawable((Orientation)localObject, new int[] { i, localSharedPreferences.getInt("chatsRowGradientColor", -1) });
        this.listView.setBackgroundDrawable((Drawable)localObject);
        return;
        localObject = Orientation.LEFT_RIGHT;
        continue;
        localObject = Orientation.TL_BR;
        continue;
        localObject = Orientation.BL_TR;
      }
    }
    this.listView.setBackgroundColor(i);
  }
  
  private void updatePasscodeButton()
  {
    if (this.passcodeItem == null) {
      return;
    }
    if ((UserConfig.passcodeHash.length() != 0) && (!this.searching))
    {
      this.passcodeItem.setVisibility(0);
      int i = ApplicationLoader.applicationContext.getSharedPreferences("theme", 0).getInt("chatsHeaderIconsColor", -1);
      if (UserConfig.appLocked)
      {
        localDrawable = getParentActivity().getResources().getDrawable(2130837801);
        if (localDrawable != null) {
          localDrawable.setColorFilter(i, PorterDuff.Mode.MULTIPLY);
        }
        this.passcodeItem.setIcon(localDrawable);
        return;
      }
      Drawable localDrawable = getParentActivity().getResources().getDrawable(2130837802);
      if (localDrawable != null) {
        localDrawable.setColorFilter(i, PorterDuff.Mode.MULTIPLY);
      }
      this.passcodeItem.setIcon(localDrawable);
      return;
    }
    this.passcodeItem.setVisibility(8);
  }
  
  private void updateTabs()
  {
    SharedPreferences localSharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0);
    this.hideTabs = localSharedPreferences.getBoolean("hideTabs", false);
    this.disableAnimation = localSharedPreferences.getBoolean("disableTabsAnimation", false);
    this.tabsHeight = localSharedPreferences.getInt("tabsHeight", 40);
    refreshTabAndListViews(false);
    if ((this.hideTabs) && (this.dialogsType > 2))
    {
      this.dialogsType = 0;
      refreshAdapterAndTabs(this.dialogsBackupAdapter);
    }
  }
  
  private void updateTheme()
  {
    paintHeader(false);
    Object localObject1 = ApplicationLoader.applicationContext.getSharedPreferences("theme", 0);
    int i = ((SharedPreferences)localObject1).getInt("themeColor", -16738680);
    int j = ((SharedPreferences)localObject1).getInt("chatsHeaderIconsColor", -1);
    for (;;)
    {
      try
      {
        int k = ((SharedPreferences)localObject1).getInt("chatsHeaderColor", i);
        if (Build.VERSION.SDK_INT >= 21)
        {
          localObject2 = BitmapFactory.decodeResource(getParentActivity().getResources(), 2130837742);
          ActivityManager.TaskDescription localTaskDescription = new ActivityManager.TaskDescription(getHeaderTitle(), (Bitmap)localObject2, k);
          getParentActivity().setTaskDescription(localTaskDescription);
          ((Bitmap)localObject2).recycle();
        }
        Object localObject2 = getParentActivity().getResources().getDrawable(2130837694);
        if (localObject2 != null) {
          ((Drawable)localObject2).setColorFilter(((SharedPreferences)localObject1).getInt("chatsFloatingBGColor", i), PorterDuff.Mode.MULTIPLY);
        }
        this.floatingButton.setBackgroundDrawable((Drawable)localObject2);
        localObject2 = getParentActivity().getResources().getDrawable(2130837690);
        if (localObject2 != null) {
          ((Drawable)localObject2).setColorFilter(((SharedPreferences)localObject1).getInt("chatsFloatingPencilColor", -1), PorterDuff.Mode.MULTIPLY);
        }
        this.floatingButton.setImageDrawable((Drawable)localObject2);
      }
      catch (NullPointerException localNullPointerException)
      {
        FileLog.e("tmessages", localNullPointerException);
        continue;
      }
      try
      {
        localObject1 = getParentActivity().getResources().getDrawable(2130837718);
        if (localObject1 != null) {
          ((Drawable)localObject1).setColorFilter(j, PorterDuff.Mode.MULTIPLY);
        }
        localObject1 = getParentActivity().getResources().getDrawable(2130837801);
        if (localObject1 != null) {
          ((Drawable)localObject1).setColorFilter(j, PorterDuff.Mode.MULTIPLY);
        }
        localObject1 = getParentActivity().getResources().getDrawable(2130837802);
        if (localObject1 != null) {
          ((Drawable)localObject1).setColorFilter(j, PorterDuff.Mode.MULTIPLY);
        }
        localObject1 = getParentActivity().getResources().getDrawable(2130837725);
        if (localObject1 != null) {
          ((Drawable)localObject1).setColorFilter(j, PorterDuff.Mode.MULTIPLY);
        }
      }
      catch (OutOfMemoryError localOutOfMemoryError)
      {
        FileLog.e("tmessages", localOutOfMemoryError);
      }
    }
    refreshTabs();
    paintHeader(true);
  }
  
  private void updateVisibleRows(int paramInt)
  {
    if (this.listView == null) {
      return;
    }
    int j = this.listView.getChildCount();
    int i = 0;
    if (i < j)
    {
      Object localObject = this.listView.getChildAt(i);
      boolean bool;
      if ((localObject instanceof DialogCell)) {
        if (this.listView.getAdapter() != this.dialogsSearchAdapter)
        {
          localObject = (DialogCell)localObject;
          if ((paramInt & 0x800) == 0) {
            break label124;
          }
          ((DialogCell)localObject).checkCurrentDialogIndex();
          if ((this.dialogsType == 0) && (AndroidUtilities.isTablet()))
          {
            if (((DialogCell)localObject).getDialogId() != this.openedDialogId) {
              break label118;
            }
            bool = true;
            label104:
            ((DialogCell)localObject).setDialogSelected(bool);
          }
        }
      }
      for (;;)
      {
        i += 1;
        break;
        label118:
        bool = false;
        break label104;
        label124:
        if ((paramInt & 0x200) != 0)
        {
          if ((this.dialogsType == 0) && (AndroidUtilities.isTablet()))
          {
            if (((DialogCell)localObject).getDialogId() == this.openedDialogId) {}
            for (bool = true;; bool = false)
            {
              ((DialogCell)localObject).setDialogSelected(bool);
              break;
            }
          }
        }
        else
        {
          ((DialogCell)localObject).update(paramInt);
          continue;
          if ((localObject instanceof UserCell)) {
            ((UserCell)localObject).update(paramInt);
          } else if ((localObject instanceof ProfileSearchCell)) {
            ((ProfileSearchCell)localObject).update(paramInt);
          }
        }
      }
    }
    updateListBG();
  }
  
  public boolean cancelButtonPressed()
  {
    return true;
  }
  
  public View createView(Context paramContext)
  {
    this.searching = false;
    this.searchWas = false;
    ResourceLoader.loadRecources(paramContext);
    SharedPreferences localSharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("theme", 0);
    int i = localSharedPreferences.getInt("chatsHeaderIconsColor", -1);
    int j = localSharedPreferences.getInt("chatsHeaderTitleColor", -1);
    this.avatarImage = new BackupImageView(paramContext);
    this.avatarImage.setRoundRadius(AndroidUtilities.dp(30.0F));
    Object localObject1 = this.actionBar.createMenu();
    if ((!this.onlySelect) && (this.searchString == null))
    {
      localObject2 = getParentActivity().getResources().getDrawable(2130837801);
      ((Drawable)localObject2).setColorFilter(i, PorterDuff.Mode.MULTIPLY);
      this.passcodeItem = ((ActionBarMenu)localObject1).addItem(1, (Drawable)localObject2);
      updatePasscodeButton();
    }
    localObject1 = ((ActionBarMenu)localObject1).addItem(0, getParentActivity().getResources().getDrawable(2130837718)).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItemSearchListener()
    {
      public boolean canCollapseSearch()
      {
        if (DialogsActivity.this.searchString != null)
        {
          DialogsActivity.this.finishFragment();
          return false;
        }
        return true;
      }
      
      public void onSearchCollapse()
      {
        DialogsActivity.this.refreshTabAndListViews(false);
        DialogsActivity.access$102(DialogsActivity.this, false);
        DialogsActivity.access$1002(DialogsActivity.this, false);
        if (DialogsActivity.this.listView != null)
        {
          DialogsActivity.this.searchEmptyView.setVisibility(8);
          if ((!MessagesController.getInstance().loadingDialogs) || (!MessagesController.getInstance().dialogs.isEmpty())) {
            break label228;
          }
          DialogsActivity.this.emptyView.setVisibility(8);
          DialogsActivity.this.listView.setEmptyView(DialogsActivity.this.progressView);
        }
        for (;;)
        {
          if (!DialogsActivity.this.onlySelect)
          {
            DialogsActivity.this.floatingButton.setVisibility(0);
            DialogsActivity.access$1102(DialogsActivity.this, true);
            ViewProxy.setTranslationY(DialogsActivity.this.floatingButton, AndroidUtilities.dp(100.0F));
            DialogsActivity.this.hideFloatingButton(false);
          }
          if (DialogsActivity.this.listView.getAdapter() != DialogsActivity.this.dialogsAdapter)
          {
            DialogsActivity.this.listView.setAdapter(DialogsActivity.this.dialogsAdapter);
            DialogsActivity.this.dialogsAdapter.notifyDataSetChanged();
          }
          if (DialogsActivity.this.dialogsSearchAdapter != null) {
            DialogsActivity.this.dialogsSearchAdapter.searchDialogs(null);
          }
          DialogsActivity.this.updatePasscodeButton();
          return;
          label228:
          DialogsActivity.this.progressView.setVisibility(8);
          DialogsActivity.this.listView.setEmptyView(DialogsActivity.this.emptyView);
        }
      }
      
      public void onSearchExpand()
      {
        DialogsActivity.this.refreshTabAndListViews(true);
        DialogsActivity.access$102(DialogsActivity.this, true);
        if (DialogsActivity.this.listView != null)
        {
          if (DialogsActivity.this.searchString != null)
          {
            DialogsActivity.this.listView.setEmptyView(DialogsActivity.this.searchEmptyView);
            DialogsActivity.this.progressView.setVisibility(8);
            DialogsActivity.this.emptyView.setVisibility(8);
          }
          if (!DialogsActivity.this.onlySelect) {
            DialogsActivity.this.floatingButton.setVisibility(8);
          }
        }
        DialogsActivity.this.updatePasscodeButton();
      }
      
      public void onTextChanged(EditText paramAnonymousEditText)
      {
        paramAnonymousEditText = paramAnonymousEditText.getText().toString();
        if ((paramAnonymousEditText.length() != 0) || ((DialogsActivity.this.dialogsSearchAdapter != null) && (DialogsActivity.this.dialogsSearchAdapter.hasRecentRearch())))
        {
          DialogsActivity.access$1002(DialogsActivity.this, true);
          if ((DialogsActivity.this.dialogsSearchAdapter != null) && (DialogsActivity.this.listView.getAdapter() != DialogsActivity.this.dialogsSearchAdapter))
          {
            DialogsActivity.this.listView.setAdapter(DialogsActivity.this.dialogsSearchAdapter);
            DialogsActivity.this.dialogsSearchAdapter.notifyDataSetChanged();
          }
          if ((DialogsActivity.this.searchEmptyView != null) && (DialogsActivity.this.listView.getEmptyView() != DialogsActivity.this.searchEmptyView))
          {
            DialogsActivity.this.emptyView.setVisibility(8);
            DialogsActivity.this.progressView.setVisibility(8);
            DialogsActivity.this.searchEmptyView.showTextView();
            DialogsActivity.this.listView.setEmptyView(DialogsActivity.this.searchEmptyView);
          }
        }
        if (DialogsActivity.this.dialogsSearchAdapter != null) {
          DialogsActivity.this.dialogsSearchAdapter.searchDialogs(paramAnonymousEditText);
        }
        DialogsActivity.this.updateListBG();
      }
    });
    ((ActionBarMenuItem)localObject1).getSearchField().setHint(LocaleController.getString("Search", 2131166086));
    if (j != -1)
    {
      ((ActionBarMenuItem)localObject1).getSearchField().setTextColor(j);
      ((ActionBarMenuItem)localObject1).getSearchField().setHintTextColor(AndroidUtilities.getIntAlphaColor("chatsHeaderTitleColor", -1, 0.5F));
    }
    Object localObject2 = getParentActivity().getResources().getDrawable(2130837725);
    if (localObject2 != null) {
      ((Drawable)localObject2).setColorFilter(i, PorterDuff.Mode.MULTIPLY);
    }
    ((ActionBarMenuItem)localObject1).getClearButton().setImageDrawable((Drawable)localObject2);
    FrameLayout localFrameLayout;
    label473:
    label944:
    label1141:
    float f1;
    label1151:
    float f2;
    if (this.onlySelect)
    {
      localObject1 = getParentActivity().getResources().getDrawable(2130837707);
      if (localObject1 != null) {
        ((Drawable)localObject1).setColorFilter(i, PorterDuff.Mode.MULTIPLY);
      }
      this.actionBar.setBackButtonDrawable((Drawable)localObject1);
      this.actionBar.setTitle(LocaleController.getString("SelectChat", 2131166101));
      this.actionBar.setAllowOverlayTitle(true);
      this.actionBar.setActionBarMenuOnItemClick(new ActionBarMenuOnItemClick()
      {
        public void onItemClick(int paramAnonymousInt)
        {
          boolean bool = true;
          if (paramAnonymousInt == -1) {
            if (DialogsActivity.this.onlySelect) {
              DialogsActivity.this.finishFragment();
            }
          }
          while (paramAnonymousInt != 1)
          {
            do
            {
              return;
            } while (DialogsActivity.this.parentLayout == null);
            if (!DialogsActivity.this.hideTabs) {
              DialogsActivity.this.parentLayout.getDrawerLayoutContainer().setAllowOpenDrawer(true, false);
            }
            DialogsActivity.this.parentLayout.getDrawerLayoutContainer().openDrawer(false);
            return;
          }
          if (!UserConfig.appLocked) {}
          for (;;)
          {
            UserConfig.appLocked = bool;
            UserConfig.saveConfig(false);
            DialogsActivity.this.updatePasscodeButton();
            return;
            bool = false;
          }
        }
      });
      paintHeader(false);
      localFrameLayout = new FrameLayout(paramContext);
      this.fragmentView = localFrameLayout;
      this.listView = new RecyclerListView(paramContext);
      this.listView.setVerticalScrollBarEnabled(true);
      this.listView.setItemAnimator(null);
      this.listView.setInstantClick(true);
      this.listView.setLayoutAnimation(null);
      this.layoutManager = new LinearLayoutManager(paramContext)
      {
        public boolean supportsPredictiveItemAnimations()
        {
          return false;
        }
      };
      this.layoutManager.setOrientation(1);
      this.listView.setLayoutManager(this.layoutManager);
      if (Build.VERSION.SDK_INT >= 11)
      {
        localObject1 = this.listView;
        if (!LocaleController.isRTL) {
          break label1823;
        }
        i = 1;
        ((RecyclerListView)localObject1).setVerticalScrollbarPosition(i);
      }
      localFrameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1.0F));
      this.onTouchListener = new DialogsOnTouch(paramContext);
      this.listView.setOnTouchListener(this.onTouchListener);
      this.listView.setOnItemClickListener(new OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          if ((DialogsActivity.this.listView == null) || (DialogsActivity.this.listView.getAdapter() == null)) {}
          long l1;
          int i;
          label80:
          Object localObject2;
          label674:
          label707:
          label736:
          label806:
          do
          {
            do
            {
              long l2;
              do
              {
                return;
                l2 = 0L;
                j = 0;
                paramAnonymousView = DialogsActivity.this.listView.getAdapter();
                if (paramAnonymousView != DialogsActivity.this.dialogsAdapter) {
                  break;
                }
                localObject1 = DialogsActivity.this.dialogsAdapter.getItem(paramAnonymousInt);
              } while (localObject1 == null);
              l1 = ((TLRPC.Dialog)localObject1).id;
              i = j;
              if (l1 != 0L)
              {
                if (DialogsActivity.this.touchPositionDP >= 65.0F) {
                  break label957;
                }
                localObject1 = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", 0);
                DialogsActivity.access$2202(DialogsActivity.this, 0);
                DialogsActivity.access$2302(DialogsActivity.this, 0);
                paramAnonymousInt = (int)l1;
                j = (int)(l1 >> 32);
                if (paramAnonymousInt == 0) {
                  break label707;
                }
                if (j != 1) {
                  break label674;
                }
                DialogsActivity.access$2302(DialogsActivity.this, paramAnonymousInt);
              }
              for (;;)
              {
                if (DialogsActivity.this.user_id == 0) {
                  break label806;
                }
                paramAnonymousInt = ((SharedPreferences)localObject1).getInt("dialogsClickOnPic", 0);
                if (paramAnonymousInt != 2) {
                  break label736;
                }
                paramAnonymousView = new Bundle();
                paramAnonymousView.putInt("user_id", DialogsActivity.this.user_id);
                DialogsActivity.this.presentFragment(new ProfileActivity(paramAnonymousView));
                return;
                l1 = l2;
                i = j;
                if (paramAnonymousView != DialogsActivity.this.dialogsSearchAdapter) {
                  break label80;
                }
                localObject1 = DialogsActivity.this.dialogsSearchAdapter.getItem(paramAnonymousInt);
                if ((localObject1 instanceof TLRPC.User))
                {
                  l2 = ((TLRPC.User)localObject1).id;
                  if (DialogsActivity.this.dialogsSearchAdapter.isGlobalSearch(paramAnonymousInt))
                  {
                    localObject2 = new ArrayList();
                    ((ArrayList)localObject2).add((TLRPC.User)localObject1);
                    MessagesController.getInstance().putUsers((ArrayList)localObject2, false);
                    MessagesStorage.getInstance().putUsersAndChats((ArrayList)localObject2, null, false, true);
                  }
                  l1 = l2;
                  i = j;
                  if (DialogsActivity.this.onlySelect) {
                    break label80;
                  }
                  DialogsActivity.this.dialogsSearchAdapter.putRecentSearch(l2, (TLRPC.User)localObject1);
                  l1 = l2;
                  i = j;
                  break label80;
                }
                if ((localObject1 instanceof TLRPC.Chat))
                {
                  if (DialogsActivity.this.dialogsSearchAdapter.isGlobalSearch(paramAnonymousInt))
                  {
                    localObject2 = new ArrayList();
                    ((ArrayList)localObject2).add((TLRPC.Chat)localObject1);
                    MessagesController.getInstance().putChats((ArrayList)localObject2, false);
                    MessagesStorage.getInstance().putUsersAndChats(null, (ArrayList)localObject2, false, true);
                  }
                  if (((TLRPC.Chat)localObject1).id > 0) {}
                  for (l2 = -((TLRPC.Chat)localObject1).id;; l2 = AndroidUtilities.makeBroadcastId(((TLRPC.Chat)localObject1).id))
                  {
                    l1 = l2;
                    i = j;
                    if (DialogsActivity.this.onlySelect) {
                      break;
                    }
                    DialogsActivity.this.dialogsSearchAdapter.putRecentSearch(l2, (TLRPC.Chat)localObject1);
                    l1 = l2;
                    i = j;
                    break;
                  }
                }
                if ((localObject1 instanceof TLRPC.EncryptedChat))
                {
                  l2 = ((TLRPC.EncryptedChat)localObject1).id << 32;
                  l1 = l2;
                  i = j;
                  if (DialogsActivity.this.onlySelect) {
                    break label80;
                  }
                  DialogsActivity.this.dialogsSearchAdapter.putRecentSearch(l2, (TLRPC.EncryptedChat)localObject1);
                  l1 = l2;
                  i = j;
                  break label80;
                }
                if ((localObject1 instanceof MessageObject))
                {
                  localObject1 = (MessageObject)localObject1;
                  l1 = ((MessageObject)localObject1).getDialogId();
                  i = ((MessageObject)localObject1).getId();
                  DialogsActivity.this.dialogsSearchAdapter.addHashtagsFromMessage(DialogsActivity.this.dialogsSearchAdapter.getLastSearchString());
                  break label80;
                }
                l1 = l2;
                i = j;
                if (!(localObject1 instanceof String)) {
                  break label80;
                }
                DialogsActivity.this.actionBar.openSearchField((String)localObject1);
                l1 = l2;
                i = j;
                break label80;
                break;
                if (paramAnonymousInt > 0)
                {
                  DialogsActivity.access$2202(DialogsActivity.this, paramAnonymousInt);
                }
                else if (paramAnonymousInt < 0)
                {
                  DialogsActivity.access$2302(DialogsActivity.this, -paramAnonymousInt);
                  continue;
                  localObject2 = MessagesController.getInstance().getEncryptedChat(Integer.valueOf(j));
                  DialogsActivity.access$2202(DialogsActivity.this, ((TLRPC.EncryptedChat)localObject2).user_id);
                }
              }
              if (paramAnonymousInt != 1) {
                break label957;
              }
              paramAnonymousView = MessagesController.getInstance().getUser(Integer.valueOf(DialogsActivity.this.user_id));
            } while ((paramAnonymousView.photo == null) || (paramAnonymousView.photo.photo_big == null));
            PhotoViewer.getInstance().setParentActivity(DialogsActivity.this.getParentActivity());
            PhotoViewer.getInstance().openPhoto(paramAnonymousView.photo.photo_big, DialogsActivity.this);
            return;
            if (DialogsActivity.this.chat_id == 0) {
              break;
            }
            paramAnonymousInt = ((SharedPreferences)localObject1).getInt("dialogsClickOnGroupPic", 0);
            if (paramAnonymousInt == 2)
            {
              MessagesController.getInstance().loadChatInfo(DialogsActivity.this.chat_id, null, false);
              paramAnonymousView = new Bundle();
              paramAnonymousView.putInt("chat_id", DialogsActivity.this.chat_id);
              paramAnonymousView = new ProfileActivity(paramAnonymousView);
              DialogsActivity.this.presentFragment(paramAnonymousView);
              return;
            }
            if (paramAnonymousInt != 1) {
              break;
            }
            paramAnonymousView = MessagesController.getInstance().getChat(Integer.valueOf(DialogsActivity.this.chat_id));
          } while ((paramAnonymousView.photo == null) || (paramAnonymousView.photo.photo_big == null));
          PhotoViewer.getInstance().setParentActivity(DialogsActivity.this.getParentActivity());
          PhotoViewer.getInstance().openPhoto(paramAnonymousView.photo.photo_big, DialogsActivity.this);
          return;
          label957:
          if (DialogsActivity.this.onlySelect)
          {
            DialogsActivity.this.didSelectResult(l1, true, false);
            return;
          }
          Object localObject1 = new Bundle();
          int j = (int)l1;
          paramAnonymousInt = (int)(l1 >> 32);
          if (j != 0) {
            if (paramAnonymousInt == 1)
            {
              ((Bundle)localObject1).putInt("chat_id", j);
              label1019:
              if (i == 0) {
                break label1248;
              }
              ((Bundle)localObject1).putInt("message_id", i);
            }
          }
          for (;;)
          {
            if (AndroidUtilities.isTablet())
            {
              if ((DialogsActivity.this.openedDialogId == l1) && (paramAnonymousView != DialogsActivity.this.dialogsSearchAdapter)) {
                break;
              }
              if (DialogsActivity.this.dialogsAdapter != null)
              {
                DialogsActivity.this.dialogsAdapter.setOpenedDialogId(DialogsActivity.access$2702(DialogsActivity.this, l1));
                DialogsActivity.this.updateVisibleRows(512);
              }
            }
            if (DialogsActivity.this.searchString == null) {
              break label1271;
            }
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, new Object[0]);
            DialogsActivity.this.presentFragment(new ChatActivity((Bundle)localObject1));
            return;
            if (j > 0)
            {
              ((Bundle)localObject1).putInt("user_id", j);
              break label1019;
            }
            if (j >= 0) {
              break label1019;
            }
            paramAnonymousInt = j;
            if (i != 0)
            {
              localObject2 = MessagesController.getInstance().getChat(Integer.valueOf(-j));
              paramAnonymousInt = j;
              if (localObject2 != null)
              {
                paramAnonymousInt = j;
                if (((TLRPC.Chat)localObject2).migrated_to != null)
                {
                  ((Bundle)localObject1).putInt("migrated_to", j);
                  paramAnonymousInt = -((TLRPC.Chat)localObject2).migrated_to.channel_id;
                }
              }
            }
            ((Bundle)localObject1).putInt("chat_id", -paramAnonymousInt);
            break label1019;
            ((Bundle)localObject1).putInt("enc_id", paramAnonymousInt);
            break label1019;
            label1248:
            if (DialogsActivity.this.actionBar != null) {
              DialogsActivity.this.actionBar.closeSearchField();
            }
          }
          label1271:
          DialogsActivity.this.presentFragment(new ChatActivity((Bundle)localObject1));
        }
      });
      this.listView.setOnItemLongClickListener(new OnItemLongClickListener()
      {
        public boolean onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          if ((DialogsActivity.this.onlySelect) || ((DialogsActivity.this.searching) && (DialogsActivity.this.searchWas)) || (DialogsActivity.this.getParentActivity() == null))
          {
            if (((DialogsActivity.this.searchWas) && (DialogsActivity.this.searching)) || ((DialogsActivity.this.dialogsSearchAdapter.isRecentSearchDisplayed()) && (DialogsActivity.this.listView.getAdapter() == DialogsActivity.this.dialogsSearchAdapter) && (((DialogsActivity.this.dialogsSearchAdapter.getItem(paramAnonymousInt) instanceof String)) || (DialogsActivity.this.dialogsSearchAdapter.isRecentSearchDisplayed()))))
            {
              paramAnonymousView = new AlertDialog.Builder(DialogsActivity.this.getParentActivity());
              paramAnonymousView.setTitle(LocaleController.getString("AppName", 2131165280));
              paramAnonymousView.setMessage(LocaleController.getString("ClearSearch", 2131165462));
              paramAnonymousView.setPositiveButton(LocaleController.getString("ClearButton", 2131165457).toUpperCase(), new DialogInterface.OnClickListener()
              {
                public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                {
                  if (DialogsActivity.this.dialogsSearchAdapter.isRecentSearchDisplayed())
                  {
                    DialogsActivity.this.dialogsSearchAdapter.clearRecentSearch();
                    return;
                  }
                  DialogsActivity.this.dialogsSearchAdapter.clearRecentHashtags();
                }
              });
              paramAnonymousView.setNegativeButton(LocaleController.getString("Cancel", 2131165360), null);
              DialogsActivity.this.showDialog(paramAnonymousView.create());
              return true;
            }
            return false;
          }
          paramAnonymousView = DialogsActivity.this.getDialogsArray();
          if ((paramAnonymousInt < 0) || (paramAnonymousInt >= paramAnonymousView.size())) {
            return false;
          }
          Object localObject1 = (TLRPC.Dialog)paramAnonymousView.get(paramAnonymousInt);
          DialogsActivity.access$3002(DialogsActivity.this, ((TLRPC.Dialog)localObject1).id);
          BottomSheet.Builder localBuilder = new BottomSheet.Builder(DialogsActivity.this.getParentActivity());
          int i = (int)DialogsActivity.this.selectedDialog;
          int j = (int)(DialogsActivity.this.selectedDialog >> 32);
          final Object localObject3;
          final boolean bool1;
          label363:
          String str;
          label376:
          Object localObject2;
          if ((localObject1 instanceof TLRPC.TL_dialogChannel))
          {
            localObject3 = MessagesController.getInstance().getChat(Integer.valueOf(-i));
            bool1 = Favourite.isFavourite(Long.valueOf(((TLRPC.Dialog)localObject1).id));
            if (bool1)
            {
              paramAnonymousView = LocaleController.getString("DeleteFromFavorites", 2131166353);
              if (!MessagesController.getInstance().isDialogMuted(DialogsActivity.this.selectedDialog)) {
                break label504;
              }
              paramAnonymousInt = 2130837899;
              if (paramAnonymousInt == 0) {
                break label509;
              }
              str = LocaleController.getString("UnmuteNotifications", 2131166229);
              if ((localObject3 == null) || (!((TLRPC.Chat)localObject3).megagroup)) {
                break label533;
              }
              localObject2 = new CharSequence[5];
              localObject2[0] = LocaleController.getString("ClearHistoryCache", 2131165459);
              if ((localObject3 != null) && (((TLRPC.Chat)localObject3).creator)) {
                break label521;
              }
            }
            label504:
            label509:
            label521:
            for (localObject1 = LocaleController.getString("LeaveMegaMenu", 2131165763);; localObject1 = LocaleController.getString("DeleteMegaMenu", 2131165516))
            {
              localObject2[1] = localObject1;
              localObject2[2] = str;
              localObject2[3] = paramAnonymousView;
              localObject2[4] = LocaleController.getString("MarkAsRead", 2131166407);
              paramAnonymousView = (View)localObject2;
              localBuilder.setItems(paramAnonymousView, new DialogInterface.OnClickListener()
              {
                public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                {
                  if (paramAnonymous2Int == 3)
                  {
                    paramAnonymous2DialogInterface = (TLRPC.Dialog)MessagesController.getInstance().dialogs_dict.get(Long.valueOf(DialogsActivity.this.selectedDialog));
                    if (bool1)
                    {
                      Favourite.deleteFavourite(Long.valueOf(DialogsActivity.this.selectedDialog));
                      MessagesController.getInstance().dialogsFavs.remove(paramAnonymous2DialogInterface);
                    }
                    for (;;)
                    {
                      if (DialogsActivity.this.dialogsType == 8)
                      {
                        DialogsActivity.this.dialogsAdapter.notifyDataSetChanged();
                        if (!DialogsActivity.this.hideTabs) {
                          DialogsActivity.this.updateTabs();
                        }
                      }
                      return;
                      Favourite.addFavourite(Long.valueOf(DialogsActivity.this.selectedDialog));
                      MessagesController.getInstance().dialogsFavs.add(paramAnonymous2DialogInterface);
                    }
                  }
                  if (paramAnonymous2Int == 2)
                  {
                    if (!MessagesController.getInstance().isDialogMuted(DialogsActivity.this.selectedDialog))
                    {
                      DialogsActivity.this.showDialog(AlertsCreator.createMuteAlert(DialogsActivity.this.getParentActivity(), DialogsActivity.this.selectedDialog));
                      return;
                    }
                    paramAnonymous2DialogInterface = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0).edit();
                    paramAnonymous2DialogInterface.putInt("notify2_" + DialogsActivity.this.selectedDialog, 0);
                    MessagesStorage.getInstance().setDialogFlags(DialogsActivity.this.selectedDialog, 0L);
                    paramAnonymous2DialogInterface.commit();
                    paramAnonymous2DialogInterface = (TLRPC.Dialog)MessagesController.getInstance().dialogs_dict.get(Long.valueOf(DialogsActivity.this.selectedDialog));
                    if (paramAnonymous2DialogInterface != null) {
                      paramAnonymous2DialogInterface.notify_settings = new TLRPC.TL_peerNotifySettings();
                    }
                    NotificationsController.updateServerNotificationsSettings(DialogsActivity.this.selectedDialog);
                    return;
                  }
                  if (paramAnonymous2Int == 4)
                  {
                    DialogsActivity.this.markAsReadDialog(false);
                    return;
                  }
                  paramAnonymous2DialogInterface = new AlertDialog.Builder(DialogsActivity.this.getParentActivity());
                  paramAnonymous2DialogInterface.setTitle(LocaleController.getString("AppName", 2131165280));
                  if (paramAnonymous2Int == 0)
                  {
                    if ((localObject3 != null) && (localObject3.megagroup)) {
                      paramAnonymous2DialogInterface.setMessage(LocaleController.getString("AreYouSureClearHistorySuper", 2131165290));
                    }
                    for (;;)
                    {
                      paramAnonymous2DialogInterface.setPositiveButton(LocaleController.getString("OK", 2131165940), new DialogInterface.OnClickListener()
                      {
                        public void onClick(DialogInterface paramAnonymous3DialogInterface, int paramAnonymous3Int)
                        {
                          MessagesController.getInstance().deleteDialog(DialogsActivity.this.selectedDialog, 2);
                        }
                      });
                      paramAnonymous2DialogInterface.setNegativeButton(LocaleController.getString("Cancel", 2131165360), null);
                      DialogsActivity.this.showDialog(paramAnonymous2DialogInterface.create());
                      return;
                      paramAnonymous2DialogInterface.setMessage(LocaleController.getString("AreYouSureClearHistoryChannel", 2131165289));
                    }
                  }
                  if ((localObject3 != null) && (localObject3.megagroup)) {
                    if (!localObject3.creator) {
                      paramAnonymous2DialogInterface.setMessage(LocaleController.getString("MegaLeaveAlert", 2131165799));
                    }
                  }
                  for (;;)
                  {
                    paramAnonymous2DialogInterface.setPositiveButton(LocaleController.getString("OK", 2131165940), new DialogInterface.OnClickListener()
                    {
                      public void onClick(DialogInterface paramAnonymous3DialogInterface, int paramAnonymous3Int)
                      {
                        MessagesController.getInstance().deleteUserFromChat((int)-DialogsActivity.this.selectedDialog, UserConfig.getCurrentUser(), null);
                        if (AndroidUtilities.isTablet()) {
                          NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, new Object[] { Long.valueOf(DialogsActivity.this.selectedDialog) });
                        }
                      }
                    });
                    break;
                    paramAnonymous2DialogInterface.setMessage(LocaleController.getString("MegaDeleteAlert", 2131165797));
                    continue;
                    if ((localObject3 == null) || (!localObject3.creator)) {
                      paramAnonymous2DialogInterface.setMessage(LocaleController.getString("ChannelLeaveAlert", 2131165399));
                    } else {
                      paramAnonymous2DialogInterface.setMessage(LocaleController.getString("ChannelDeleteAlert", 2131165389));
                    }
                  }
                }
              });
              DialogsActivity.this.showDialog(localBuilder.create());
              return true;
              paramAnonymousView = LocaleController.getString("AddToFavorites", 2131166337);
              break;
              paramAnonymousInt = 0;
              break label363;
              str = LocaleController.getString("MuteNotifications", 2131165842);
              break label376;
            }
            label533:
            localObject2 = new CharSequence[5];
            localObject2[0] = LocaleController.getString("ClearHistoryCache", 2131165459);
            if ((localObject3 == null) || (!((TLRPC.Chat)localObject3).creator)) {}
            for (localObject1 = LocaleController.getString("LeaveChannelMenu", 2131165761);; localObject1 = LocaleController.getString("ChannelDeleteMenu", 2131165391))
            {
              localObject2[1] = localObject1;
              localObject2[2] = str;
              localObject2[3] = paramAnonymousView;
              localObject2[4] = LocaleController.getString("MarkAsRead", 2131166407);
              paramAnonymousView = (View)localObject2;
              break;
            }
          }
          label631:
          label650:
          final boolean bool2;
          label702:
          final boolean bool3;
          if ((i < 0) && (j != 1))
          {
            bool1 = true;
            if (!MessagesController.getInstance().isDialogMuted(DialogsActivity.this.selectedDialog)) {
              break label850;
            }
            paramAnonymousInt = 2130837899;
            str = null;
            paramAnonymousView = str;
            if (!bool1)
            {
              paramAnonymousView = str;
              if (i > 0)
              {
                paramAnonymousView = str;
                if (j != 1) {
                  paramAnonymousView = MessagesController.getInstance().getUser(Integer.valueOf(i));
                }
              }
            }
            if ((paramAnonymousView == null) || (!paramAnonymousView.bot)) {
              break label855;
            }
            bool2 = true;
            bool3 = Favourite.isFavourite(Long.valueOf(((TLRPC.Dialog)localObject1).id));
            if (!bool3) {
              break label861;
            }
            str = LocaleController.getString("DeleteFavourite", 2131166353);
            label729:
            localObject2 = LocaleController.getString("ClearHistory", 2131165458);
            if (!bool1) {
              break label874;
            }
            paramAnonymousView = LocaleController.getString("DeleteChat", 2131165510);
            label754:
            if (paramAnonymousInt == 0) {
              break label905;
            }
          }
          label850:
          label855:
          label861:
          label874:
          label905:
          for (localObject1 = LocaleController.getString("UnmuteNotifications", 2131166229);; localObject1 = LocaleController.getString("MuteNotifications", 2131165842))
          {
            localObject3 = LocaleController.getString("MarkAsRead", 2131166407);
            DialogInterface.OnClickListener local3 = new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, final int paramAnonymous2Int)
              {
                if (paramAnonymous2Int == 3)
                {
                  paramAnonymous2DialogInterface = (TLRPC.Dialog)MessagesController.getInstance().dialogs_dict.get(Long.valueOf(DialogsActivity.this.selectedDialog));
                  if (bool3)
                  {
                    Favourite.deleteFavourite(Long.valueOf(DialogsActivity.this.selectedDialog));
                    MessagesController.getInstance().dialogsFavs.remove(paramAnonymous2DialogInterface);
                  }
                  for (;;)
                  {
                    if (DialogsActivity.this.dialogsType == 8)
                    {
                      DialogsActivity.this.dialogsAdapter.notifyDataSetChanged();
                      if (!DialogsActivity.this.hideTabs) {
                        DialogsActivity.this.updateTabs();
                      }
                    }
                    return;
                    Favourite.addFavourite(Long.valueOf(DialogsActivity.this.selectedDialog));
                    MessagesController.getInstance().dialogsFavs.add(paramAnonymous2DialogInterface);
                  }
                }
                if (paramAnonymous2Int == 2)
                {
                  if (!MessagesController.getInstance().isDialogMuted(DialogsActivity.this.selectedDialog))
                  {
                    DialogsActivity.this.showDialog(AlertsCreator.createMuteAlert(DialogsActivity.this.getParentActivity(), DialogsActivity.this.selectedDialog));
                    return;
                  }
                  paramAnonymous2DialogInterface = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0).edit();
                  paramAnonymous2DialogInterface.putInt("notify2_" + DialogsActivity.this.selectedDialog, 0);
                  MessagesStorage.getInstance().setDialogFlags(DialogsActivity.this.selectedDialog, 0L);
                  paramAnonymous2DialogInterface.commit();
                  paramAnonymous2DialogInterface = (TLRPC.Dialog)MessagesController.getInstance().dialogs_dict.get(Long.valueOf(DialogsActivity.this.selectedDialog));
                  if (paramAnonymous2DialogInterface != null) {
                    paramAnonymous2DialogInterface.notify_settings = new TLRPC.TL_peerNotifySettings();
                  }
                  NotificationsController.updateServerNotificationsSettings(DialogsActivity.this.selectedDialog);
                  return;
                }
                if (paramAnonymous2Int == 4)
                {
                  DialogsActivity.this.markAsReadDialog(false);
                  return;
                }
                paramAnonymous2DialogInterface = new AlertDialog.Builder(DialogsActivity.this.getParentActivity());
                paramAnonymous2DialogInterface.setTitle(LocaleController.getString("AppName", 2131165280));
                if (paramAnonymous2Int == 0) {
                  paramAnonymous2DialogInterface.setMessage(LocaleController.getString("AreYouSureClearHistory", 2131165288));
                }
                for (;;)
                {
                  paramAnonymous2DialogInterface.setPositiveButton(LocaleController.getString("OK", 2131165940), new DialogInterface.OnClickListener()
                  {
                    public void onClick(DialogInterface paramAnonymous3DialogInterface, int paramAnonymous3Int)
                    {
                      if (paramAnonymous2Int != 0)
                      {
                        if (DialogsActivity.5.3.this.val$isChat)
                        {
                          paramAnonymous3DialogInterface = MessagesController.getInstance().getChat(Integer.valueOf((int)-DialogsActivity.this.selectedDialog));
                          if ((paramAnonymous3DialogInterface != null) && (ChatObject.isNotInChat(paramAnonymous3DialogInterface))) {
                            MessagesController.getInstance().deleteDialog(DialogsActivity.this.selectedDialog, 0);
                          }
                        }
                        for (;;)
                        {
                          if (DialogsActivity.5.3.this.val$isBot) {
                            MessagesController.getInstance().blockUser((int)DialogsActivity.this.selectedDialog);
                          }
                          if (AndroidUtilities.isTablet()) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, new Object[] { Long.valueOf(DialogsActivity.this.selectedDialog) });
                          }
                          return;
                          MessagesController.getInstance().deleteUserFromChat((int)-DialogsActivity.this.selectedDialog, MessagesController.getInstance().getUser(Integer.valueOf(UserConfig.getClientUserId())), null);
                          continue;
                          MessagesController.getInstance().deleteDialog(DialogsActivity.this.selectedDialog, 0);
                        }
                      }
                      MessagesController.getInstance().deleteDialog(DialogsActivity.this.selectedDialog, 1);
                    }
                  });
                  paramAnonymous2DialogInterface.setNegativeButton(LocaleController.getString("Cancel", 2131165360), null);
                  DialogsActivity.this.showDialog(paramAnonymous2DialogInterface.create());
                  return;
                  if (bool1) {
                    paramAnonymous2DialogInterface.setMessage(LocaleController.getString("AreYouSureDeleteAndExit", 2131165291));
                  } else {
                    paramAnonymous2DialogInterface.setMessage(LocaleController.getString("AreYouSureDeleteThisChat", 2131165295));
                  }
                }
              }
            };
            localBuilder.setItems(new CharSequence[] { localObject2, paramAnonymousView, localObject1, str, localObject3 }, local3);
            DialogsActivity.this.showDialog(localBuilder.create());
            break;
            bool1 = false;
            break label631;
            paramAnonymousInt = 0;
            break label650;
            bool2 = false;
            break label702;
            str = LocaleController.getString("AddFavourite", 2131166337);
            break label729;
            if (bool2)
            {
              paramAnonymousView = LocaleController.getString("DeleteAndStop", 2131165509);
              break label754;
            }
            paramAnonymousView = LocaleController.getString("Delete", 2131165503);
            break label754;
          }
        }
      });
      this.searchEmptyView = new EmptyTextProgressView(paramContext);
      this.searchEmptyView.setVisibility(8);
      this.searchEmptyView.setShowAtCenter(true);
      this.searchEmptyView.setText(LocaleController.getString("NoResult", 2131165883));
      localFrameLayout.addView(this.searchEmptyView, LayoutHelper.createFrame(-1, -1.0F));
      this.emptyView = new LinearLayout(paramContext);
      this.emptyView.setOrientation(1);
      this.emptyView.setVisibility(8);
      this.emptyView.setGravity(17);
      localFrameLayout.addView(this.emptyView, LayoutHelper.createFrame(-1, -1.0F));
      this.emptyView.setOnTouchListener(this.onTouchListener);
      localObject1 = new TextView(paramContext);
      ((TextView)localObject1).setText(LocaleController.getString("NoChats", 2131165865));
      ((TextView)localObject1).setTextColor(-6974059);
      ((TextView)localObject1).setGravity(17);
      ((TextView)localObject1).setTextSize(1, 20.0F);
      this.emptyView.addView((View)localObject1, LayoutHelper.createLinear(-2, -2));
      TextView localTextView = new TextView(paramContext);
      localObject2 = LocaleController.getString("NoChatsHelp", 2131165866);
      localObject1 = localObject2;
      if (AndroidUtilities.isTablet())
      {
        localObject1 = localObject2;
        if (!AndroidUtilities.isSmallTablet()) {
          localObject1 = ((String)localObject2).replace("\n", " ");
        }
      }
      localTextView.setText((CharSequence)localObject1);
      localTextView.setTextColor(-6974059);
      localTextView.setTextSize(1, 15.0F);
      localTextView.setGravity(17);
      localTextView.setPadding(AndroidUtilities.dp(8.0F), AndroidUtilities.dp(6.0F), AndroidUtilities.dp(8.0F), 0);
      localTextView.setLineSpacing(AndroidUtilities.dp(2.0F), 1.0F);
      this.emptyView.addView(localTextView, LayoutHelper.createLinear(-2, -2));
      this.progressView = new ProgressBar(paramContext);
      this.progressView.setVisibility(8);
      localFrameLayout.addView(this.progressView, LayoutHelper.createFrame(-2, -2, 17));
      this.floatingButton = new ImageView(paramContext);
      localObject1 = this.floatingButton;
      if (!this.onlySelect) {
        break label1829;
      }
      i = 8;
      ((ImageView)localObject1).setVisibility(i);
      this.floatingButton.setScaleType(ScaleType.CENTER);
      this.floatingButton.setBackgroundResource(2130837692);
      this.floatingButton.setImageResource(2130837690);
      if (Build.VERSION.SDK_INT >= 21)
      {
        localObject1 = new StateListAnimator();
        localObject2 = ObjectAnimator.ofFloat(this.floatingButton, "translationZ", new float[] { AndroidUtilities.dp(2.0F), AndroidUtilities.dp(4.0F) }).setDuration(200L);
        ((StateListAnimator)localObject1).addState(new int[] { 16842919 }, (Animator)localObject2);
        localObject2 = ObjectAnimator.ofFloat(this.floatingButton, "translationZ", new float[] { AndroidUtilities.dp(4.0F), AndroidUtilities.dp(2.0F) }).setDuration(200L);
        ((StateListAnimator)localObject1).addState(new int[0], (Animator)localObject2);
        this.floatingButton.setStateListAnimator((StateListAnimator)localObject1);
        this.floatingButton.setOutlineProvider(new ViewOutlineProvider()
        {
          @SuppressLint({"NewApi"})
          public void getOutline(View paramAnonymousView, Outline paramAnonymousOutline)
          {
            paramAnonymousOutline.setOval(0, 0, AndroidUtilities.dp(56.0F), AndroidUtilities.dp(56.0F));
          }
        });
      }
      localObject1 = this.floatingButton;
      if (!LocaleController.isRTL) {
        break label1835;
      }
      i = 3;
      if (!LocaleController.isRTL) {
        break label1841;
      }
      f1 = 14.0F;
      if (!LocaleController.isRTL) {
        break label1846;
      }
      f2 = 0.0F;
      label1159:
      localFrameLayout.addView((View)localObject1, LayoutHelper.createFrame(-2, -2.0F, i | 0x50, f1, 0.0F, f2, 14.0F));
      this.floatingButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          paramAnonymousView = new Bundle();
          paramAnonymousView.putBoolean("destroyAfterSelect", true);
          DialogsActivity.this.presentFragment(new ContactsActivity(paramAnonymousView));
        }
      });
      this.tabsView = new FrameLayout(paramContext);
      createTabs(paramContext);
      localFrameLayout.addView(this.tabsView, LayoutHelper.createFrame(-1, this.tabsHeight, 48, 0.0F, 0.0F, 0.0F, 0.0F));
      this.listView.setOnScrollListener(new OnScrollListener()
      {
        public void onScrollStateChanged(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt)
        {
          if ((paramAnonymousInt == 1) && (DialogsActivity.this.searching) && (DialogsActivity.this.searchWas)) {
            AndroidUtilities.hideKeyboard(DialogsActivity.this.getParentActivity().getCurrentFocus());
          }
        }
        
        public void onScrolled(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt1, int paramAnonymousInt2)
        {
          int j = DialogsActivity.this.layoutManager.findFirstVisibleItemPosition();
          paramAnonymousInt1 = Math.abs(DialogsActivity.this.layoutManager.findLastVisibleItemPosition() - j) + 1;
          int i = paramAnonymousRecyclerView.getAdapter().getItemCount();
          if ((DialogsActivity.this.searching) && (DialogsActivity.this.searchWas)) {
            if ((paramAnonymousInt1 > 0) && (DialogsActivity.this.layoutManager.findLastVisibleItemPosition() == i - 1) && (!DialogsActivity.this.dialogsSearchAdapter.isMessagesSearchEndReached())) {
              DialogsActivity.this.dialogsSearchAdapter.loadMoreSearchMessages();
            }
          }
          label152:
          label236:
          label249:
          label434:
          label440:
          label445:
          label469:
          label478:
          for (;;)
          {
            return;
            Object localObject;
            boolean bool;
            if ((paramAnonymousInt1 > 0) && (DialogsActivity.this.layoutManager.findLastVisibleItemPosition() >= DialogsActivity.this.getDialogsArray().size() - 10))
            {
              localObject = MessagesController.getInstance();
              if (!MessagesController.getInstance().dialogsEndReached)
              {
                bool = true;
                ((MessagesController)localObject).loadDialogs(-1, 100, bool);
              }
            }
            else
            {
              if (DialogsActivity.this.floatingButton.getVisibility() != 8)
              {
                localObject = paramAnonymousRecyclerView.getChildAt(0);
                i = 0;
                if (localObject != null) {
                  i = ((View)localObject).getTop();
                }
                paramAnonymousInt1 = 1;
                if (DialogsActivity.this.prevPosition != j) {
                  break label445;
                }
                paramAnonymousInt1 = DialogsActivity.this.prevTop;
                if (i >= DialogsActivity.this.prevTop) {
                  break label434;
                }
                bool = true;
                if (Math.abs(paramAnonymousInt1 - i) <= 1) {
                  break label440;
                }
                paramAnonymousInt1 = 1;
                if ((paramAnonymousInt1 != 0) && (DialogsActivity.this.scrollUpdated) && (((!DialogsActivity.this.hideTabs) && (!DialogsActivity.this.disableAnimation)) || (DialogsActivity.this.hideTabs))) {
                  DialogsActivity.this.hideFloatingButton(bool);
                }
                DialogsActivity.access$3502(DialogsActivity.this, j);
                DialogsActivity.access$3602(DialogsActivity.this, i);
                DialogsActivity.access$3702(DialogsActivity.this, true);
              }
              if (DialogsActivity.this.hideTabs) {
                continue;
              }
              if ((paramAnonymousInt2 > 1) && (paramAnonymousRecyclerView.getChildAt(0).getTop() < 0))
              {
                if (DialogsActivity.this.disableAnimation) {
                  break label469;
                }
                DialogsActivity.this.hideTabsAnimated(true);
              }
            }
            for (;;)
            {
              if (paramAnonymousInt2 >= -1) {
                break label478;
              }
              if (DialogsActivity.this.disableAnimation) {
                break label480;
              }
              DialogsActivity.this.hideTabsAnimated(false);
              if (j != 0) {
                break;
              }
              DialogsActivity.this.listView.setPadding(0, AndroidUtilities.dp(DialogsActivity.this.tabsHeight), 0, 0);
              return;
              bool = false;
              break label152;
              bool = false;
              break label236;
              paramAnonymousInt1 = 0;
              break label249;
              if (j > DialogsActivity.this.prevPosition) {}
              for (bool = true;; bool = false) {
                break;
              }
              DialogsActivity.this.hideFloatingButton(true);
            }
          }
          label480:
          DialogsActivity.this.hideFloatingButton(false);
        }
      });
      if (this.searchString == null)
      {
        this.dialogsAdapter = new DialogsAdapter(paramContext, this.dialogsType);
        if ((AndroidUtilities.isTablet()) && (this.openedDialogId != 0L)) {
          this.dialogsAdapter.setOpenedDialogId(this.openedDialogId);
        }
        this.listView.setAdapter(this.dialogsAdapter);
        this.dialogsBackupAdapter = this.dialogsAdapter;
      }
      i = 0;
      if (this.searchString == null) {
        break label1853;
      }
      i = 2;
      label1337:
      this.dialogsSearchAdapter = new DialogsSearchAdapter(paramContext, i, this.dialogsType);
      this.dialogsSearchAdapter.setDelegate(new MessagesActivitySearchAdapterDelegate()
      {
        public void searchStateChanged(boolean paramAnonymousBoolean)
        {
          if ((DialogsActivity.this.searching) && (DialogsActivity.this.searchWas) && (DialogsActivity.this.searchEmptyView != null))
          {
            if (paramAnonymousBoolean) {
              DialogsActivity.this.searchEmptyView.showProgress();
            }
          }
          else {
            return;
          }
          DialogsActivity.this.searchEmptyView.showTextView();
        }
      });
      if ((!MessagesController.getInstance().loadingDialogs) || (!MessagesController.getInstance().dialogs.isEmpty())) {
        break label1866;
      }
      this.searchEmptyView.setVisibility(8);
      this.emptyView.setVisibility(8);
      this.listView.setEmptyView(this.progressView);
      label1420:
      if (this.searchString != null) {
        this.actionBar.openSearchField(this.searchString);
      }
      if ((!this.onlySelect) && ((this.dialogsType == 0) || (this.dialogsType > 2))) {
        localFrameLayout.addView(new PlayerView(paramContext, this), LayoutHelper.createFrame(-1, 39.0F, 51, 0.0F, -36.0F, 0.0F, 0.0F));
      }
      this.toastBtn = new Button(paramContext);
      paramContext = this.toastBtn;
      if (!AndroidUtilities.themeUpdated) {
        break label1898;
      }
    }
    label1823:
    label1829:
    label1835:
    label1841:
    label1846:
    label1853:
    label1866:
    label1898:
    for (i = 0;; i = 8)
    {
      paramContext.setVisibility(i);
      if (AndroidUtilities.themeUpdated)
      {
        AndroidUtilities.themeUpdated = false;
        paramContext = localSharedPreferences.getString("themeName", "");
        i = localSharedPreferences.getInt("chatsHeaderColor", localSharedPreferences.getInt("themeColor", -16738680));
        this.toastBtn.setText(LocaleController.formatString("ThemeUpdated", 2131166402, new Object[] { paramContext }));
        if (Build.VERSION.SDK_INT >= 14) {
          this.toastBtn.setAllCaps(false);
        }
        paramContext = new GradientDrawable();
        paramContext.setCornerRadius(AndroidUtilities.dp(4.0F));
        paramContext.setColor(i);
        this.toastBtn.setBackgroundDrawable(paramContext);
        this.toastBtn.setTextColor(j);
        this.toastBtn.setTextSize(16.0F);
        ViewProxy.setTranslationY(this.toastBtn, -AndroidUtilities.dp(100.0F));
        ObjectAnimatorProxy.ofFloatProxy(this.toastBtn, "translationY", new float[] { 0.0F }).setDuration(500L).start();
        this.toastBtn.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            try
            {
              paramAnonymousView = ApplicationLoader.applicationContext.getPackageManager().getLaunchIntentForPackage("es.rafalense.themes");
              if (paramAnonymousView != null) {
                ApplicationLoader.applicationContext.startActivity(paramAnonymousView);
              }
              DialogsActivity.this.toastBtn.setVisibility(8);
              return;
            }
            catch (Exception paramAnonymousView)
            {
              FileLog.e("tmessages", paramAnonymousView);
            }
          }
        });
        localFrameLayout.addView(this.toastBtn, LayoutHelper.createFrame(-2, -2.0F, 49, 0.0F, 10.0F, 0.0F, 0.0F));
        new Timer().schedule(new TimerTask()
        {
          public void run()
          {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                ObjectAnimatorProxy.ofFloatProxy(DialogsActivity.this.toastBtn, "translationY", new float[] { -AndroidUtilities.dp(100.0F) }).setDuration(500L).start();
              }
            });
          }
        }, 4000L);
      }
      return this.fragmentView;
      if (this.searchString != null) {
        this.actionBar.setBackButtonImage(2130837707);
      }
      for (;;)
      {
        this.actionBar.setTitle(LocaleController.getString("AppName", 2131165280));
        break;
        this.actionBar.setBackButtonDrawable(new MenuDrawable());
      }
      i = 2;
      break label473;
      i = 0;
      break label944;
      i = 5;
      break label1141;
      f1 = 0.0F;
      break label1151;
      f2 = 14.0F;
      break label1159;
      if (this.onlySelect) {
        break label1337;
      }
      i = 1;
      break label1337;
      this.searchEmptyView.setVisibility(8);
      this.progressView.setVisibility(8);
      this.listView.setEmptyView(this.emptyView);
      break label1420;
    }
  }
  
  public void didReceivedNotification(int paramInt, Object... paramVarArgs)
  {
    if (paramInt == NotificationCenter.dialogsNeedReload) {
      if (this.dialogsAdapter != null)
      {
        if (this.dialogsAdapter.isDataSetChanged()) {
          this.dialogsAdapter.notifyDataSetChanged();
        }
      }
      else
      {
        if (this.dialogsSearchAdapter != null) {
          this.dialogsSearchAdapter.notifyDataSetChanged();
        }
        if (this.listView == null) {}
      }
    }
    for (;;)
    {
      try
      {
        if ((MessagesController.getInstance().loadingDialogs) && (MessagesController.getInstance().dialogs.isEmpty()))
        {
          this.searchEmptyView.setVisibility(8);
          this.emptyView.setVisibility(8);
          this.listView.setEmptyView(this.progressView);
          if ((paramInt == NotificationCenter.needReloadRecentDialogsSearch) && (this.dialogsSearchAdapter != null)) {
            this.dialogsSearchAdapter.loadRecentSearch();
          }
          return;
          updateVisibleRows(2048);
          break;
        }
        this.progressView.setVisibility(8);
        if ((this.searching) && (this.searchWas))
        {
          this.emptyView.setVisibility(8);
          this.listView.setEmptyView(this.searchEmptyView);
          continue;
        }
      }
      catch (Exception paramVarArgs)
      {
        FileLog.e("tmessages", paramVarArgs);
        continue;
        this.searchEmptyView.setVisibility(8);
        this.listView.setEmptyView(this.emptyView);
        continue;
      }
      if (paramInt == NotificationCenter.emojiDidLoaded)
      {
        if (this.listView != null) {
          updateVisibleRows(0);
        }
      }
      else if (paramInt == NotificationCenter.updateInterfaces)
      {
        updateVisibleRows(((Integer)paramVarArgs[0]).intValue());
      }
      else if (paramInt == NotificationCenter.appDidLogout)
      {
        dialogsLoaded = false;
      }
      else if (paramInt == NotificationCenter.encryptedChatUpdated)
      {
        updateVisibleRows(0);
      }
      else if (paramInt == NotificationCenter.contactsDidLoaded)
      {
        updateVisibleRows(0);
      }
      else if (paramInt == NotificationCenter.openedChatChanged)
      {
        if ((this.dialogsType == 0) && (AndroidUtilities.isTablet()))
        {
          boolean bool = ((Boolean)paramVarArgs[1]).booleanValue();
          long l = ((Long)paramVarArgs[0]).longValue();
          if (bool) {
            if (l != this.openedDialogId) {}
          }
          for (this.openedDialogId = 0L;; this.openedDialogId = l)
          {
            if (this.dialogsAdapter != null) {
              this.dialogsAdapter.setOpenedDialogId(this.openedDialogId);
            }
            updateVisibleRows(512);
            break;
          }
        }
      }
      else if (paramInt == NotificationCenter.notificationsSettingsUpdated)
      {
        updateVisibleRows(0);
      }
      else if ((paramInt == NotificationCenter.messageReceivedByAck) || (paramInt == NotificationCenter.messageReceivedByServer) || (paramInt == NotificationCenter.messageSendError))
      {
        updateVisibleRows(4096);
      }
      else if (paramInt == NotificationCenter.didSetPasscode)
      {
        updatePasscodeButton();
      }
      else if (paramInt == NotificationCenter.refreshTabs)
      {
        updateTabs();
        hideShowTabs();
      }
    }
  }
  
  public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject paramMessageObject, TLRPC.FileLocation paramFileLocation, int paramInt)
  {
    if (paramFileLocation == null) {}
    for (;;)
    {
      return null;
      Object localObject1 = null;
      Object localObject2;
      if (this.user_id != 0)
      {
        localObject2 = MessagesController.getInstance().getUser(Integer.valueOf(this.user_id));
        paramMessageObject = (MessageObject)localObject1;
        if (localObject2 != null)
        {
          paramMessageObject = (MessageObject)localObject1;
          if (((TLRPC.User)localObject2).photo != null)
          {
            paramMessageObject = (MessageObject)localObject1;
            if (((TLRPC.User)localObject2).photo.photo_big != null) {
              paramMessageObject = ((TLRPC.User)localObject2).photo.photo_big;
            }
          }
        }
      }
      while ((paramMessageObject != null) && (paramMessageObject.local_id == paramFileLocation.local_id) && (paramMessageObject.volume_id == paramFileLocation.volume_id) && (paramMessageObject.dc_id == paramFileLocation.dc_id))
      {
        paramMessageObject = new int[2];
        this.avatarImage.getLocationInWindow(paramMessageObject);
        paramFileLocation = new PhotoViewer.PlaceProviderObject();
        paramFileLocation.viewX = paramMessageObject[0];
        paramFileLocation.viewY = (paramMessageObject[1] - AndroidUtilities.statusBarHeight);
        paramFileLocation.parentView = this.avatarImage;
        paramFileLocation.imageReceiver = this.avatarImage.getImageReceiver();
        paramFileLocation.user_id = this.user_id;
        paramFileLocation.thumb = paramFileLocation.imageReceiver.getBitmap();
        paramFileLocation.size = -1;
        paramFileLocation.radius = this.avatarImage.getImageReceiver().getRoundRadius();
        return paramFileLocation;
        paramMessageObject = (MessageObject)localObject1;
        if (this.chat_id != 0)
        {
          localObject2 = MessagesController.getInstance().getChat(Integer.valueOf(this.chat_id));
          paramMessageObject = (MessageObject)localObject1;
          if (localObject2 != null)
          {
            paramMessageObject = (MessageObject)localObject1;
            if (((TLRPC.Chat)localObject2).photo != null)
            {
              paramMessageObject = (MessageObject)localObject1;
              if (((TLRPC.Chat)localObject2).photo.photo_big != null) {
                paramMessageObject = ((TLRPC.Chat)localObject2).photo.photo_big;
              }
            }
          }
        }
      }
    }
  }
  
  public int getSelectedCount()
  {
    return 0;
  }
  
  public Bitmap getThumbForPhoto(MessageObject paramMessageObject, TLRPC.FileLocation paramFileLocation, int paramInt)
  {
    return null;
  }
  
  public boolean isMainDialogList()
  {
    return (this.delegate == null) && (this.searchString == null);
  }
  
  public boolean isPhotoChecked(int paramInt)
  {
    return false;
  }
  
  public void onConfigurationChanged(Configuration paramConfiguration)
  {
    super.onConfigurationChanged(paramConfiguration);
    if ((!this.onlySelect) && (this.floatingButton != null)) {
      this.floatingButton.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
      {
        public void onGlobalLayout()
        {
          ImageView localImageView = DialogsActivity.this.floatingButton;
          float f;
          if (DialogsActivity.this.floatingHidden)
          {
            f = AndroidUtilities.dp(100.0F);
            ViewProxy.setTranslationY(localImageView, f);
            localImageView = DialogsActivity.this.floatingButton;
            if (DialogsActivity.this.floatingHidden) {
              break label93;
            }
          }
          label93:
          for (boolean bool = true;; bool = false)
          {
            localImageView.setClickable(bool);
            if (DialogsActivity.this.floatingButton != null)
            {
              if (Build.VERSION.SDK_INT >= 16) {
                break label98;
              }
              DialogsActivity.this.floatingButton.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
            return;
            f = 0.0F;
            break;
          }
          label98:
          DialogsActivity.this.floatingButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
      });
    }
  }
  
  protected void onDialogDismiss(Dialog paramDialog)
  {
    super.onDialogDismiss(paramDialog);
    if ((this.permissionDialog != null) && (paramDialog == this.permissionDialog) && (getParentActivity() != null)) {
      askForPermissons();
    }
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    if (getArguments() != null)
    {
      this.onlySelect = this.arguments.getBoolean("onlySelect", false);
      this.dialogsType = this.arguments.getInt("dialogsType", 0);
      this.selectAlertString = this.arguments.getString("selectAlertString");
      this.selectAlertStringGroup = this.arguments.getString("selectAlertStringGroup");
      this.addToGroupAlertString = this.arguments.getString("addToGroupAlertString");
    }
    if (this.searchString == null)
    {
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
      NotificationCenter.getInstance().addObserver(this, NotificationCenter.refreshTabs);
    }
    if (!dialogsLoaded)
    {
      MessagesController.getInstance().loadDialogs(0, 100, true);
      ContactsController.getInstance().checkInviteText();
      dialogsLoaded = true;
    }
    return true;
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    if (this.searchString == null)
    {
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
      NotificationCenter.getInstance().removeObserver(this, NotificationCenter.refreshTabs);
    }
    this.delegate = null;
  }
  
  public void onRequestPermissionsResultFragment(int paramInt, String[] paramArrayOfString, int[] paramArrayOfInt)
  {
    if (paramInt == 1)
    {
      int i = 0;
      if (i < paramArrayOfString.length)
      {
        if ((paramArrayOfInt.length <= i) || (paramArrayOfInt[i] != 0)) {}
        for (;;)
        {
          i += 1;
          break;
          String str = paramArrayOfString[i];
          paramInt = -1;
          switch (str.hashCode())
          {
          }
          for (;;)
          {
            switch (paramInt)
            {
            default: 
              break;
            case 0: 
              ContactsController.getInstance().readContacts();
              break;
              if (str.equals("android.permission.READ_CONTACTS"))
              {
                paramInt = 0;
                continue;
                if (str.equals("android.permission.WRITE_EXTERNAL_STORAGE")) {
                  paramInt = 1;
                }
              }
              break;
            }
          }
          ImageLoader.getInstance().createMediaPaths();
        }
      }
    }
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.dialogsAdapter != null) {
      this.dialogsAdapter.notifyDataSetChanged();
    }
    if (this.dialogsSearchAdapter != null) {
      this.dialogsSearchAdapter.notifyDataSetChanged();
    }
    Object localObject;
    if ((this.checkPermission) && (!this.onlySelect) && (Build.VERSION.SDK_INT >= 23))
    {
      localObject = getParentActivity();
      if (localObject != null)
      {
        this.checkPermission = false;
        if ((((Activity)localObject).checkSelfPermission("android.permission.READ_CONTACTS") != 0) || (((Activity)localObject).checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0))
        {
          if (!((Activity)localObject).shouldShowRequestPermissionRationale("android.permission.READ_CONTACTS")) {
            break label171;
          }
          localObject = new AlertDialog.Builder((Context)localObject);
          ((AlertDialog.Builder)localObject).setTitle(LocaleController.getString("AppName", 2131165280));
          ((AlertDialog.Builder)localObject).setMessage(LocaleController.getString("PermissionContacts", 2131165990));
          ((AlertDialog.Builder)localObject).setPositiveButton(LocaleController.getString("OK", 2131165940), null);
          localObject = ((AlertDialog.Builder)localObject).create();
          this.permissionDialog = ((AlertDialog)localObject);
          showDialog((Dialog)localObject);
        }
      }
    }
    for (;;)
    {
      updateTheme();
      return;
      label171:
      if (((Activity)localObject).shouldShowRequestPermissionRationale("android.permission.WRITE_EXTERNAL_STORAGE"))
      {
        localObject = new AlertDialog.Builder((Context)localObject);
        ((AlertDialog.Builder)localObject).setTitle(LocaleController.getString("AppName", 2131165280));
        ((AlertDialog.Builder)localObject).setMessage(LocaleController.getString("PermissionStorage", 2131165994));
        ((AlertDialog.Builder)localObject).setPositiveButton(LocaleController.getString("OK", 2131165940), null);
        localObject = ((AlertDialog.Builder)localObject).create();
        this.permissionDialog = ((AlertDialog)localObject);
        showDialog((Dialog)localObject);
      }
      else
      {
        askForPermissons();
      }
    }
  }
  
  public void sendButtonPressed(int paramInt) {}
  
  public void setDelegate(MessagesActivityDelegate paramMessagesActivityDelegate)
  {
    this.delegate = paramMessagesActivityDelegate;
  }
  
  public void setPhotoChecked(int paramInt) {}
  
  public void setSearchString(String paramString)
  {
    this.searchString = paramString;
  }
  
  public void updatePhotoAtIndex(int paramInt) {}
  
  public void willHidePhotoViewer() {}
  
  public void willSwitchFromPhoto(MessageObject paramMessageObject, TLRPC.FileLocation paramFileLocation, int paramInt) {}
  
  public class DialogsOnTouch
    implements OnTouchListener
  {
    private static final int MIN_DISTANCE_HIGH = 40;
    private static final int MIN_DISTANCE_HIGH_Y = 60;
    private DisplayMetrics displayMetrics;
    private float downX;
    private float downY;
    Context mContext;
    private float upX;
    private float upY;
    private float vDPI;
    
    public DialogsOnTouch(Context paramContext)
    {
      this.mContext = paramContext;
      this.displayMetrics = paramContext.getResources().getDisplayMetrics();
      this.vDPI = (this.displayMetrics.xdpi / 160.0F);
    }
    
    public boolean onTouch(View paramView, MotionEvent paramMotionEvent)
    {
      int i = 1;
      DialogsActivity.access$2102(DialogsActivity.this, Math.round(paramMotionEvent.getX() / this.vDPI));
      if (DialogsActivity.this.hideTabs) {}
      float f1;
      float f2;
      float f3;
      do
      {
        return false;
        switch (paramMotionEvent.getAction())
        {
        default: 
          return false;
        case 0: 
          this.downX = Math.round(paramMotionEvent.getX() / this.vDPI);
          this.downY = Math.round(paramMotionEvent.getY() / this.vDPI);
          if (this.downX > 50.0F) {
            DialogsActivity.this.parentLayout.getDrawerLayoutContainer().setAllowOpenDrawer(false, false);
          }
          while ((paramView instanceof LinearLayout))
          {
            return true;
            DialogsActivity.this.parentLayout.getDrawerLayoutContainer().setAllowOpenDrawer(true, false);
          }
        }
        this.upX = Math.round(paramMotionEvent.getX() / this.vDPI);
        this.upY = Math.round(paramMotionEvent.getY() / this.vDPI);
        f1 = this.downX - this.upX;
        f2 = this.downY;
        f3 = this.upY;
      } while ((Math.abs(f1) <= 40.0F) || (Math.abs(f2 - f3) >= 60.0F));
      paramView = DialogsActivity.this;
      if (f1 < 0.0F) {
        i = 0;
      }
      paramView.refreshDialogType(i);
      this.downX = Math.round(paramMotionEvent.getX() / this.vDPI);
      DialogsActivity.this.refreshAdapter(this.mContext);
      DialogsActivity.this.refreshTabAndListViews(false);
      return false;
    }
  }
  
  public static abstract interface MessagesActivityDelegate
  {
    public abstract void didSelectDialog(DialogsActivity paramDialogsActivity, long paramLong, boolean paramBoolean);
  }
}


/* Location:              D:\Develop\APK ReEngineering Tools\jd-gui-windows-1.0.0\classes-dex2jar.jar!\org\telegram\ui\DialogsActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */