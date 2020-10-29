package com.floating;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.txusballesteros.bubbles.BubbleLayout;
import com.txusballesteros.bubbles.BubblesManager;
import com.txusballesteros.bubbles.OnInitializedCallback;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

public class RNFloatingBubbleModule extends ReactContextBaseJavaModule {

    private BubblesManager bubblesManager;
    private final ReactApplicationContext reactContext;
    private BubbleLayout bubbleView;

    public RNFloatingBubbleModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

        // try {
        //   initializeBubblesManager();
        // } catch (Exception e) {

        // }
    }

    @Override
    public String getName() {
        return "RNFloatingBubble";
    }

    @ReactMethod // Notates a method that should be exposed to React
    public void showFloatingBubble(int x, int y, final Promise promise) {
        try {
            this.addNewBubble(x, y);
            promise.resolve("");
        } catch (Exception e) {
            promise.reject("");
        }
    }

    @ReactMethod // Notates a method that should be exposed to React
    public void showFloatingBubbleText(String text, final Promise promise) {
        try {
            int[] point = new int[2];
            bubbleView.getLocationOnScreen(point);
            int x = point[0];
            int y = point[1];
            TextView txtView = x<100?(TextView)bubbleView.findViewById(R.id.floating_right_text_view):(TextView)bubbleView.findViewById(R.id.floating_left_text_view);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtView.setVisibility(View.VISIBLE);
                    AnimationSet set = new AnimationSet(true);
                    TranslateAnimation animate = new TranslateAnimation(0,0,50,0);
                    animate.setDuration(500);
                    animate.setFillAfter(true);
                    set.addAnimation(animate);
                    Animation anim = new AlphaAnimation(0.0f, 1.0f);
                    anim.setDuration(500);
                    set.addAnimation(anim);
                    txtView.startAnimation(set);
                    txtView.setText(text);

                }
            });

            Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {


                            txtView.setVisibility(View.GONE);
                        }
                    });
                }
                }, 3000);
            promise.resolve("");
        } catch (Exception e) {
            Log.d("bubble", e.getMessage());
            promise.reject(e.getMessage());
        }
    }

    @ReactMethod // Notates a method that should be exposed to React
    public void hideFloatingBubble(final Promise promise) {
        try {
            this.removeBubble();
            promise.resolve("");
        } catch (Exception e) {
            promise.reject("");
        }
    }

    @ReactMethod // Notates a method that should be exposed to React
    public void requestPermission(final Promise promise) {
        try {
            this.requestPermissionAction(promise);
        } catch (Exception e) {
        }
    }



    @ReactMethod // Notates a method that should be exposed to React
    public void checkPermission(final Promise promise) {
        try {
            promise.resolve(hasPermission());
        } catch (Exception e) {
            promise.reject("");
        }
    }

    @ReactMethod // Notates a method that should be exposed to React
    public void initialize(final Promise promise) {
        try {
            this.initializeBubblesManager();
            promise.resolve("");
        } catch (Exception e) {
            promise.reject("");
        }
    }

    private void addNewBubble(int x, int y) {
        this.removeBubble();
        bubbleView = (BubbleLayout) LayoutInflater.from(reactContext).inflate(R.layout.bubble_layout, null);
        bubbleView.setOnBubbleRemoveListener(new BubbleLayout.OnBubbleRemoveListener() {
            @Override
            public void onBubbleRemoved(BubbleLayout bubble) {
                bubbleView = null;
                sendEvent("floating-bubble-remove");
            }
        });
        bubbleView.setOnBubbleClickListener(new BubbleLayout.OnBubbleClickListener() {

            @Override
            public void onBubbleClick(BubbleLayout bubble) {
                sendEvent("floating-bubble-press");
            }
        });
        bubbleView.setShouldStickToWall(true);
        bubblesManager.addBubble(bubbleView, x, y);
    }


    private boolean hasPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(reactContext);
        }
        return true;
    }

    private void removeBubble() {
        if(bubbleView != null){
            try{
                bubblesManager.removeBubble(bubbleView);
            } catch(Exception e){

            }
        }
    }


    public void requestPermissionAction(final Promise promise) {
        if (!hasPermission()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + reactContext.getPackageName()));
            Bundle bundle = new Bundle();
            reactContext.startActivityForResult(intent, 0, bundle);
        }
        if (hasPermission()) {
            promise.resolve("");
        } else {
            promise.reject("");
        }
    }

    private void initializeBubblesManager() {
        bubblesManager = new BubblesManager.Builder(reactContext).setTrashLayout(R.layout.bubble_trash_layout)
                .setInitializationCallback(new OnInitializedCallback() {
                    @Override
                    public void onInitialized() {
                        // addNewBubble();
                    }
                }).build();
        bubblesManager.initialize();
    }

    private void sendEvent(String eventName) {
        WritableMap params = Arguments.createMap();
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }
}