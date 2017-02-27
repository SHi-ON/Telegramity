package com.ioton;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.util.IabHelper;
import org.telegram.messenger.util.IabResult;
import org.telegram.messenger.util.Inventory;
import org.telegram.messenger.util.Purchase;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;

import ir.adad.client.Adad;

public class PremiumActivity extends BaseFragment {

    private Context ctx;
    private int currentConnectionState;

    public static final String RSA_BAZAAR = "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwDA0j/fvC0lJwJDM+fTGMWr660orGF+K+aMF8vOgEORwkeyfY/TWXLlL3qJYK7gh2HxKM7zHQVEQ0XKWFHYMvk5+Ql5N5rMMYuA3M7dAcqnRgnf6Ke+Fh2RhmuKy8uJDnpYf7p2w7phwN0ATusMNEnhfNsuBWBRlDtWLlxHBz4UOLDV/9yy7Vp1oITrUZvHQZNBLrqKjBpPDxXdJAui14PEz8M2nBFyc3N/fYL8oDkCAwEAAQ==";
    public static final String TAG = "TELEGRAMITY";
    static final String SKU_PREMIUM = "telegramity_full_ver";
    boolean mIsPremium = false;
    static final int RC_REQUEST = 77177; //specific number for Telegramity
    IabHelper mHelper;
    private String payload;

    private ListView listView;
    private ProgressDialog progressDialog;
    private int openingSectionBottomRow;
    private int specialOffersSectionRow;
    private int removeAdsRow;
    private int removeAdsDetailRow;
    private int specialOffersSectionBottomRow;
    private int rowCount = 0;

    @Override
    public boolean onFragmentCreate() {
        currentConnectionState = ConnectionsManager.getInstance().getConnectionState();

        openingSectionBottomRow = rowCount++;
        specialOffersSectionRow = rowCount++;
        removeAdsRow = rowCount++;
        removeAdsDetailRow = rowCount++;
        specialOffersSectionBottomRow = rowCount++;

        return super.onFragmentCreate();
    }


    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }

    @Override
    public View createView(final Context context) {
        ctx = context;

        progressDialog = new ProgressDialog(ctx);
        progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        final SharedPreferences premPreferences = ctx.getSharedPreferences("PremiumState", ctx.MODE_PRIVATE);

        TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(ctx.TELEPHONY_SERVICE);
        String deviceID = telephonyManager.getDeviceId();
        String androidID = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
        payload = androidID + deviceID;

        mIsPremium = premPreferences.getBoolean("isUserPremium", false);

        Log.d(TAG, "Creating IAB helper.");

        mHelper = new IabHelper(ctx, RSA_BAZAAR);

        // TODO: enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(false);

        Log.d(TAG, "Starting setup.");
        try {
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    Log.d(TAG, "Setup finished.");
                    if (!result.isSuccess()) {
                        complain("Problem setting up in-app billing: " + result);
                        return;
                    }
                    if (mHelper == null) return;
                    Log.d(TAG, "Setup successful. Querying inventory.");
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("PremiumTitle", R.string.PremiumTitle));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = new FrameLayout(ctx);
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new ListView(ctx);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setVerticalScrollBarEnabled(false);
        frameLayout.addView(listView);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
        layoutParams.width = LayoutHelper.MATCH_PARENT;
        layoutParams.height = LayoutHelper.MATCH_PARENT;
        listView.setLayoutParams(layoutParams);
        listView.setAdapter(new ListAdapter(ctx));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, final int i, long l) {
                currentConnectionState = ConnectionsManager.getInstance().getConnectionState();
                if (i == removeAdsRow) {
                    if (!(premPreferences.getBoolean("isUserPremium", false))) {
                        if (isPackageInstalled("com.farsitel.bazaar")) {
                            if (currentConnectionState == ConnectionsManager.ConnectionStateConnected) {
                                Log.d(TAG, "Upgrade button clicked; launching purchase flow for upgrade.");
                                try {
                                    mHelper.launchPurchaseFlow(getParentActivity(), SKU_PREMIUM, RC_REQUEST,
                                            mPurchaseFinishedListener, payload);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    complain(ctx.getString(R.string.PremiumPurchaseUnknownError));
                                }
                            } else {
                                complain(ctx.getString(R.string.PremiumNoInternetAccess));
                            }
                        } else {
                            complain(ctx.getString(R.string.PremiumMarketAppNotInstalled));
                        }
                    } else {
                        alert(ctx.getString(R.string.PremiumPurchaseAlreadyPurchased));
                        afterPurchase();
                    }

                    if (listView != null) {
                        listView.invalidateViews();
                    }
                }
            }
        });

        return fragmentView;
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");
            progressDialog.dismiss();
            if (mHelper == null) return;
            if (result.isFailure()) {
                if (result.toString().contains("querying owned item")) {
                    complain(ctx.getString(R.string.PremiumQueryOwnedItemsError));
                    return;
                } else {
                    complain("Failed to query inventory: " + result);
                    return;
                }
            } else {
                Log.d(TAG, "Query inventory was successful.");
                mIsPremium = inventory.hasPurchase(SKU_PREMIUM);
                /*if (mIsPremium) {
                    MasrafSeke(inventory.getPurchase(SKU_PREMIUM));
                }*/
                if (mIsPremium) {
                    SharedPreferences premPreferences = ctx.getSharedPreferences("PremiumState", ctx.MODE_PRIVATE);
                    if (!premPreferences.getBoolean("isUserPremium", false)) {
                        SharedPreferences.Editor editor = premPreferences.edit();
                        editor.putBoolean("isUserPremium", true);
                        editor.apply();
                        alert(ctx.getString(R.string.PremiumPurchasedCongrats));
                        afterPurchase();
                    }
                    Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));
                }
            }
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            if (mHelper == null) return;
            if (!result.isFailure()) {
                if (purchase.getOrderId().equals(purchase.getToken())) {
                    if (purchase.getSku().equals(SKU_PREMIUM) && purchase.getDeveloperPayload().equals(payload)) {

                        SharedPreferences premPreferences = ctx.getSharedPreferences("PremiumState", ctx.MODE_PRIVATE);
                        SharedPreferences.Editor editor = premPreferences.edit();
                        editor.putBoolean("isUserPremium", true);
                        editor.apply();

                        afterPurchase();
                        Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
                        alert(ctx.getString(R.string.PremiumPurchasedCongrats));
//                        mIsPremium = true;
                    }
                } else {
                    complain(ctx.getString(R.string.PremiumPurchaseUnsuccessful));
                }
            } else if (result.isFailure()) {
                if (result.toString().contains("User canceled")) {
                    complain(ctx.getString(R.string.PremiumPurchaseUserCanceled));
                } else {
                    complain("Error purchasing: " + result);
                }
                return;
            }
            /*if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
//                setWaitScreen(false);
                return;
            }*/
        }
    };

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;
        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResultFragment(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    public boolean isPackageInstalled(String PackageName) {
        PackageManager manager = ctx.getPackageManager();
        boolean isAppInstalled = false;
        try {
            manager.getPackageInfo(PackageName, PackageManager.GET_ACTIVITIES);
            isAppInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return isAppInstalled;
    }

    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */
        return true;
    }

    private void afterPurchase() {
        if (Adad.areBannerAdsEnabled()) {
            Adad.disableBannerAds();
        }
        if (listView != null) {
            listView.invalidateViews();
        }
    }

    void complain(String compMessage) {
        Log.e(TAG, "**** Telegramity Error: " + compMessage);
        alert(compMessage);
    }

    void alert(String alertMessage) {
        Log.d(TAG, "Showing alert dialog: " + alertMessage);
        TextView text_tv = new TextView(ctx);
        text_tv.setPadding(25, 5, 25, 5);
        text_tv.setText(alertMessage);
        text_tv.setTypeface(AndroidUtilities.getTypeface());

        SharedPreferences themePreferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);
        int aBBackgroundColor = themePreferences.getInt("actionBarBackgroundColor", TelegramityUtilities.colorABBG());
        TextView titleTextView = new TextView(ctx);
        titleTextView.setText(LocaleController.getString("AppName", R.string.AppName));
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        titleTextView.setTextColor(aBBackgroundColor);
        titleTextView.setTypeface(AndroidUtilities.getTypeface());
        titleTextView.setPadding(24, 18, 24, 0);

        AlertDialog.Builder bld = new AlertDialog.Builder(ctx);
        bld.setCustomTitle(titleTextView);
        bld.setView(text_tv);
        bld.setNeutralButton(LocaleController.getString("OK", R.string.OK), null);
        bld.create().show();
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
            return i == removeAdsRow;
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
                    view = new HeaderCell(mContext);
                }
                if (i == specialOffersSectionRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("PremiumOffersHeader", R.string.PremiumOffersHeader));
                }
            } else if (type == 1) {
                if (view == null) {
                    view = new TextSettingsCell(mContext);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                if (i == removeAdsRow) {
                    SharedPreferences premPreferences = ctx.getSharedPreferences("PremiumState", ctx.MODE_PRIVATE);
                    String userStatusStr;
                    if (premPreferences.getBoolean("isUserPremium", false)) {
                        userStatusStr = LocaleController.getString("PremiumStatusPro", R.string.PremiumStatusPro);
                    } else {
                        userStatusStr = LocaleController.getString("PremiumStatusFree", R.string.PremiumStatusFree);
                    }
                    textCell.setTextAndValue(LocaleController.getString("PremiumRemoveAds", R.string.PremiumRemoveAds),  userStatusStr, true);
                }
            } else if (type == 3) {
                if (view == null) {
                    view = new TextInfoPrivacyCell(mContext);
                }
                if (i == removeAdsDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("PremiumRemoveAdsDetails", R.string.PremiumRemoveAdsDetails));
                    view.setBackgroundResource(R.drawable.greydivider_bottom);
                }
            } else if (type == 4) {
                if (view == null) {
                    view = new ShadowSectionCell(mContext);
                }
            }
            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if (i == specialOffersSectionRow) {
                return 0;
            } else if (i == removeAdsDetailRow) {
                return 3;
            } else if (i == openingSectionBottomRow || i == specialOffersSectionBottomRow) {
                return 4;
            } else {
                return 1;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 5;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}
