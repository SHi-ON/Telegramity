package org.gramity;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.net.URL;

public class HUDService extends Service {

    private WindowManager windowManager;
    private String[] payload = new String[4];
    private String plTitle;
    private String plBody;
    private String plLargeIcon;
    private String plBigPicture;
    private String plURL;
    private ImageView bubbleImageView;
    private TextView titleText;
    private ImageView bannerImage;
    private TextView messageText;

    public HUDService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        payload = intent.getExtras().getStringArray("CustomPayload");
        hudInflater(payload);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        int bubbleDims = 96;
        final WindowManager.LayoutParams bubbleWMParams = new WindowManager.LayoutParams(bubbleDims, bubbleDims, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        bubbleWMParams.gravity = Gravity.CENTER_VERTICAL;
        bubbleWMParams.x = point.x / 2 - 10;
        bubbleWMParams.y = 0;

        bubbleImageView = new ImageView(this);
        final ViewGroup.LayoutParams imageViewParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        bubbleImageView.setBackgroundDrawable(null);
        bubbleImageView.setLayoutParams(imageViewParams);

        windowManager.addView(bubbleImageView, bubbleWMParams);

        final WindowManager.LayoutParams parentWMParams = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.OPAQUE);
        parentWMParams.gravity = Gravity.CENTER;

        final LinearLayout topLayout = new LinearLayout(this);
        topLayout.setOrientation(LinearLayout.VERTICAL);
        topLayout.setBackgroundColor(Color.WHITE);
        topLayout.setGravity(Gravity.CENTER);

        LinearLayout headerLayout = new LinearLayout(this);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setBackgroundResource(R.drawable.regbtn2);
        headerLayout.setPadding(5, 5, 5, 5);
        headerLayout.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        headerLayout.setLayoutParams(headerParams);
        topLayout.addView(headerLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView headerText = new TextView(this);
        headerText.setText(LocaleController.getString("HUDHeaderTitle", R.string.HUDHeaderTitle));
        headerText.setTextColor(Color.WHITE);
        headerText.setTypeface(AndroidUtilities.getTypeface("fonts/IRANSansMobileRegular.ttf"));
        headerText.setPadding(25, 0, 25, 0);
        headerText.setTextSize(16);
        headerText.setGravity(Gravity.CENTER);
        headerLayout.addView(headerText, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView headerCloseButton = new TextView(this);
        headerCloseButton.setBackgroundDrawable(new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_close).color(Color.WHITE).sizeDp(20));
        headerCloseButton.setPadding(25, 0, 25, 0);
        headerLayout.addView(headerCloseButton, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        titleText = new TextView(this);
        titleText.setTypeface(AndroidUtilities.getTypeface("fonts/IRANSansMobileRegular.ttf"));
        titleText.setPadding(10, 5, 10, 5);
        titleText.setTextColor(Color.BLACK);
        titleText.setTextSize(16);
        titleText.setGravity(Gravity.CENTER_HORIZONTAL);
        topLayout.addView(titleText, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        bannerImage = new ImageView(this);
        bannerImage.setPadding(5, 5, 5, 5);
        topLayout.addView(bannerImage, new ViewGroup.LayoutParams(400, 200));

        messageText = new TextView(this);
        messageText.setTypeface(AndroidUtilities.getTypeface("fonts/IRANSansMobileRegular.ttf"));
        messageText.setTextSize(14);
        messageText.setPadding(10, 10, 10, 10);
        messageText.setTextColor(Color.BLACK);
        topLayout.addView(messageText, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final Button oKButton = new Button(this);
        oKButton.setText(LocaleController.getString("HUDButtonOK", R.string.HUDButtonOK));
        oKButton.setBackgroundResource(R.drawable.regbtn_states);
        oKButton.setTypeface(AndroidUtilities.getTypeface("fonts/IRANSansMobileRegular.ttf"));
        oKButton.setTextSize(16);
        oKButton.setTextColor(Color.WHITE);
        topLayout.addView(oKButton, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        final ScrollView topScrollView = new ScrollView(this);
        topScrollView.addView(topLayout);
        topScrollView.setBackgroundColor(Color.WHITE);

        bubbleImageView.setOnTouchListener(new View.OnTouchListener() {
            WindowManager.LayoutParams updatedParams = bubbleWMParams;
            double x, y;
            double pressedX, pressedY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = updatedParams.x;
                        y = updatedParams.y;
                        pressedX = event.getRawX();
                        pressedY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        updatedParams.x = (int) (x + (event.getRawX() - pressedX));
                        updatedParams.y = (int) (y + (event.getRawY() - pressedY));
                        windowManager.updateViewLayout(bubbleImageView, updatedParams);
                    default:
                        break;
                }
                return false;
            }
        });

        bubbleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windowManager.removeView(bubbleImageView);
                windowManager.addView(topScrollView, parentWMParams);
            }
        });

        headerCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windowManager.removeView(topScrollView);
                stopSelf();
            }
        });

        oKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (plURL != null) {
                    Intent urlIntent = new Intent(Intent.ACTION_VIEW);
                    urlIntent.setData(Uri.parse(plURL));
                    urlIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(urlIntent);
                }
                windowManager.removeView(topScrollView);
                stopSelf();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

    private void hudInflater(String[] payload) {
        if (payload != null) {
            plTitle = payload[0];
            plBody = payload[1];
            plLargeIcon = payload[2];
            plBigPicture = payload[3];
            plURL = payload[4];
        }
        if (plTitle != null) {
            titleText.setText(plTitle);
        }
        if (plBody != null) {
            messageText.setText(plBody);
        }
        Bitmap bmLargeIcon = getLargeIcon(plLargeIcon);
        Bitmap bmBigPicture = getBitmap(plBigPicture);
        if (bmLargeIcon != null) {
            bubbleImageView.setImageBitmap(bmLargeIcon);
        }
        if (bmBigPicture != null) {
            bannerImage.setImageBitmap(bmBigPicture);
        }

    }

    private Bitmap getLargeIcon(String plLargeIcon) {
        Bitmap bitmap = getBitmap(plLargeIcon);
        if (bitmap == null) {
            bitmap = getBitmapFromAssetsOrResourceName("ic_onesignal_large_icon_default");
        }

        if (bitmap == null) {
            return null;
        }

        // Resize to prevent extra cropping and boarders.
        try {
            int systemLargeIconHeight = (int) HUDService.this.getResources().getDimension(android.R.dimen.notification_large_icon_height);
            int systemLargeIconWidth = (int) HUDService.this.getResources().getDimension(android.R.dimen.notification_large_icon_width);
            int bitmapHeight = bitmap.getHeight();
            int bitmapWidth = bitmap.getWidth();

            if (bitmapWidth > systemLargeIconWidth || bitmapHeight > systemLargeIconHeight) {
                int newWidth = systemLargeIconWidth, newHeight = systemLargeIconHeight;
                if (bitmapHeight > bitmapWidth) {
                    float ratio = (float) bitmapWidth / (float) bitmapHeight;
                    newWidth = (int) (newHeight * ratio);
                } else if (bitmapWidth > bitmapHeight) {
                    float ratio = (float) bitmapHeight / (float) bitmapWidth;
                    newHeight = (int) (newWidth * ratio);
                }

                return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return bitmap;
    }

    private Bitmap getBitmap(String name) {
        Bitmap rawBitmap = null;
        if (name == null) {
            return null;
        }
        if (name.startsWith("http://") || name.startsWith("https://")) {
            try {
                rawBitmap = new DownloadHUDAssets().execute(name).get();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (rawBitmap != null) {
            Bitmap roundedBitmap = Bitmap.createBitmap(rawBitmap.getWidth(), rawBitmap.getHeight(), rawBitmap.getConfig());
            BitmapShader bitmapShader = new BitmapShader(rawBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Canvas canvas = new Canvas(roundedBitmap);
            Paint paint = new Paint();
            RectF rectF = new RectF(0, 0, rawBitmap.getWidth(), rawBitmap.getHeight());
            float cornerRadius = 30;
            paint.setAntiAlias(true);
            paint.setShader(bitmapShader);
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);

            return roundedBitmap;
        }
        return getBitmapFromAssetsOrResourceName(name);
    }

    private Bitmap getBitmapFromAssetsOrResourceName(String bitmapStr) {
        try {
            int bitmapId = getResourceIcon(bitmapStr);
            if (bitmapId != 0)
                return BitmapFactory.decodeResource(HUDService.this.getResources(), bitmapId);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    private int getResourceIcon(String iconName) {
        if (!isValidResourceName(iconName)) {
            return 0;
        }
        int notificationIcon = getDrawableId(iconName);
        if (notificationIcon != 0)
            return notificationIcon;

        // Get system icon resource
        try {
            return android.R.drawable.class.getField(iconName).getInt(null);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return 0;
    }

    private boolean isValidResourceName(String name) {
        return (name != null && !name.matches("^[0-9]"));
    }

    private int getDrawableId(String name) {
        return HUDService.this.getResources().getIdentifier(name, "drawable", HUDService.this.getPackageName());
    }

    private class DownloadHUDAssets extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                return BitmapFactory.decodeStream(new URL(params[0]).openConnection().getInputStream());
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
        }
    }

}
