package org.gramity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.hockeyapp.android.FeedbackManager;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BetterRatingView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MarketHandlerActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        super.onCreate(savedInstanceState);

        overridePendingTransition(0, 0);

        setContentView(new View(this));

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        int pad = AndroidUtilities.dp(16);
        ll.setPadding(pad, pad, pad, pad);

        TextView text = new TextView(this);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        text.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        text.setTypeface(AndroidUtilities.getTypeface(null));
        text.setGravity(Gravity.CENTER);
        text.setText(LocaleController.formatString("RateAlert", R.string.RateAlert, LocaleController.getString("AppName", R.string.AppName)));
        ll.addView(text);

        final BetterRatingView bar = new BetterRatingView(this);
        ll.addView(bar, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 0));

        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle(LocaleController.getString("RateTitle", R.string.RateTitle))
                .setView(ll)
                .setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (bar.getRating() > 4) {
                            rateApp();
                            Toast.makeText(MarketHandlerActivity.this, LocaleController.getString("RateThanksToast", R.string.RateThanksToast), Toast.LENGTH_SHORT).show();
                        } else {
                            FeedbackManager.showFeedbackActivity(MarketHandlerActivity.this);
                            Toast.makeText(MarketHandlerActivity.this, LocaleController.getString("RateFeedbackToast", R.string.RateFeedbackToast), Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    }
                })
                .setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
        alert.setCanceledOnTouchOutside(true);
        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        final View btn = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        btn.setEnabled(false);
        bar.setOnRatingChangeListener(new BetterRatingView.OnRatingChangeListener() {
            @Override
            public void onRatingChanged(int rating) {
                btn.setEnabled(rating > 0);
                /*commentBox.setVisibility(rating < 5 && rating > 0 ? View.VISIBLE : View.GONE);
                if (commentBox.getVisibility() == View.GONE) {
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(commentBox.getWindowToken(), 0);
                }*/
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    public static void rateApp() {
        String appId = ApplicationLoader.applicationContext.getPackageName();
        boolean foundBazaar = GramityUtilities.isPackageInstalled(GramityConstants.BAZAAR_PKG);
        if (appId.contains(GramityConstants.GRAMITY_PKG)) {
            boolean foundPlay = GramityUtilities.isPackageInstalled(GramityConstants.BAZAAR_PKG);
            if (foundBazaar) {
                ApplicationLoader.applicationContext.startActivity(getBazaarIntent(appId, true));
            } else if (foundPlay) {
                ApplicationLoader.applicationContext.startActivity(getPlayIntent(appId));
            } else {
                Intent webPlayIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + appId));
                ApplicationLoader.applicationContext.startActivity(webPlayIntent);
            }
        } else {
            if (foundBazaar) {
                ApplicationLoader.applicationContext.startActivity(getBazaarIntent(appId, true));
            } else {
                Toast.makeText(ApplicationLoader.applicationContext, LocaleController.getString("BazaarNotInstalled", R.string.BazaarNotInstalled), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void openAppPage(String packageName) {
        boolean foundBazaar = GramityUtilities.isPackageInstalled(GramityConstants.BAZAAR_PKG);
        boolean foundPlay = GramityUtilities.isPackageInstalled(GramityConstants.BAZAAR_PKG);
        if (foundBazaar) {
            ApplicationLoader.applicationContext.startActivity(getBazaarIntent(packageName, false));
        } else if (foundPlay) {
            ApplicationLoader.applicationContext.startActivity(getPlayIntent(packageName));
        } else {
            Intent webPlayIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            ApplicationLoader.applicationContext.startActivity(webPlayIntent);
        }
    }

    private static Intent getBazaarIntent(String packageName, boolean rate) {
        Intent bazaarIntent = new Intent();
        bazaarIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (rate) {
            bazaarIntent.setAction(Intent.ACTION_EDIT);
        } else {
            bazaarIntent.setAction(Intent.ACTION_VIEW);
        }
        bazaarIntent.setData(Uri.parse("bazaar://details?id=" + packageName));
        bazaarIntent.setPackage("com.farsitel.bazaar");
        return bazaarIntent;
    }

    private static Intent getPlayIntent(String packageName) {
        Intent playIntent = new Intent();
        playIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        playIntent.setAction(Intent.ACTION_VIEW);
        playIntent.setData(Uri.parse("market://details?id=" + packageName));
        playIntent.setPackage("com.android.vending");
        return playIntent;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
