/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sch.trustworthysystems.smartconnectedhealth_client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.GLU;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.lang.ref.WeakReference;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Analog watch face with a ticking second hand. In ambient mode, the second hand isn't shown. On
 * devices with low-bit ambient mode, the hands are drawn without anti-aliasing in ambient mode.
 */
public class SCHGlucosePeaks extends CanvasWatchFaceService {
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
    /**
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    // Action string for generating action intents for the peak glucose changed message.
    public static final String ACTION_GLUCOSE_PEAK_CHANGED = "com.sch.trustworthysystems.smartconnectedhealth_client.glucose_peak_changed";
    public static final String GLUCOSE_PEAK_LEVEL_INTENT_KEY = "_glucose_peak_level_key";

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<SCHGlucosePeaks.Engine> mWeakReference;

        public EngineHandler(SCHGlucosePeaks.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            SCHGlucosePeaks.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        public static final String GLUCOSE_PEAK_LEVEL_HIGH  = "_high";
        public static final String GLUCOSE_PEAK_LEVEL_NORMAL  = "_normal";
        public static final String GLUCOSE_PEAK_LEVEL_DANGEROUS  = "_dangerous";

        final Handler mUpdateTimeHandler = new EngineHandler(this);
        boolean mRegisteredTimeZoneReceiver = false;
        Paint mBackgroundPaint;
        Paint mTextPaint;
        boolean mAmbient;
        Time mTime;
        /**
         * Custom, private objects for the wear app watch face.
         * */
        Bitmap mBackgroundBitmap = null;
        Bitmap mBackgroundScaledBitmap = null;
        String mCurrentPeakGlucoseLevel = GLUCOSE_PEAK_LEVEL_NORMAL;
        int mDisplayWidth = 0;
        int mDisplayHeight = 0;
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };
        final BroadcastReceiver mPeakGlucoseChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(ACTION_GLUCOSE_PEAK_CHANGED)){

                    String newLevel = intent.getStringExtra(GLUCOSE_PEAK_LEVEL_INTENT_KEY);
                    if (newLevel != mCurrentPeakGlucoseLevel) {
                        if (newLevel.equals(GLUCOSE_PEAK_LEVEL_NORMAL)) {
                            // Set the drawable background to normal.
                            Resources resources = SCHGlucosePeaks.this.getResources();
                            Drawable backgroundDrawable = resources.getDrawable(R.drawable.normal_watch_face, null);
                            mBackgroundBitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();
                            // Scale the image bitmap.
                            mBackgroundScaledBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap, mDisplayWidth, mDisplayHeight, true /* filter */);
                            mBackgroundBitmap = null;
                            mCurrentPeakGlucoseLevel = GLUCOSE_PEAK_LEVEL_NORMAL;
                            invalidate();
                        } else if (newLevel.equals(GLUCOSE_PEAK_LEVEL_HIGH)) {
                            // Set the drawable background to high.
                            Resources resources = SCHGlucosePeaks.this.getResources();
                            Drawable backgroundDrawable = resources.getDrawable(R.drawable.high_watch_face, null);
                            mBackgroundBitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();
                            // Scale the image bitmap.
                            mBackgroundScaledBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap, mDisplayWidth, mDisplayHeight, true /* filter */);
                            mBackgroundBitmap = null;
                            mCurrentPeakGlucoseLevel = GLUCOSE_PEAK_LEVEL_HIGH;
                            invalidate();
                        } else {
                            // Set the drawable background to dangerous.
                            Resources resources = SCHGlucosePeaks.this.getResources();
                            Drawable backgroundDrawable = resources.getDrawable(R.drawable.dangerous_watch_face, null);
                            mBackgroundBitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();
                            // Scale the image bitmap.
                            mBackgroundScaledBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap, mDisplayWidth, mDisplayHeight, true /* filter */);
                            mBackgroundBitmap = null;
                            mCurrentPeakGlucoseLevel = GLUCOSE_PEAK_LEVEL_DANGEROUS;
                            invalidate();
                        }
                    }
                }
            }
        };
        int mTapCount;
        // Offsets for drawing the time on the watch face.
        float mXOffset;
        float mYOffset;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(SCHGlucosePeaks.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());


            Resources resources = SCHGlucosePeaks.this.getResources();

            // Load the background image
            Drawable backgroundDrawable = resources.getDrawable(R.drawable.normal_watch_face, null);
            mBackgroundBitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();

            mYOffset = resources.getDimension(R.dimen.digital_y_offset);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(resources.getColor(R.color.background));

            mTextPaint = new Paint();
            mTextPaint = createTextPaint(resources.getColor(R.color.digital_text));

            mTime = new Time();

            /**
             * Register the peak change receiver. Note that if the watch face receives an intent before full initialization,
             * then it won't create the new watch face.
             * */
            registerPeakChangeReceiver();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            // Unregister the peak glucose receiver.
            unregisterPeakChangeReceiver();
            super.onDestroy();
        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mTextPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        /**
         * Captures tap event (and tap type) and toggles the background color if the user finishes
         * a tap.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            Resources resources = SCHGlucosePeaks.this.getResources();
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    mTapCount++;
                    mBackgroundPaint.setColor(resources.getColor(mTapCount % 2 == 0 ?
                            R.color.background : R.color.background2));
                    break;
            }
            invalidate();
        }

        /**
         * Used to initialize the scaled drawable bitmap image for the watchface.
         * */
        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            /**
             * If the current bitmap isn't the same dimensions as the watch face, then scale it so it fits.
             * */
            if (mBackgroundScaledBitmap == null
                    || mBackgroundScaledBitmap.getWidth() != width
                    || mBackgroundScaledBitmap.getHeight() != height) {
                mBackgroundScaledBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                        width, height, true /* filter */);
            }
            // Set the width and height so they can be used when drawing an image after a peak glucose intent is received.
            mDisplayHeight = height;
            mDisplayWidth = width;

            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            mTime.setToNow();

            // Draw the background.
            if (isInAmbientMode()) {
                canvas.drawColor(Color.BLACK);
            } else {
                // Draw the normal glucose peak background image.
                canvas.drawBitmap(mBackgroundScaledBitmap, 0, 0, null);

                //canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
            }

            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
            String text = "";
            mTime.setToNow();
            if (mTime.hour > 12) {
                text = mAmbient
                        ? String.format("%d:%02d", (mTime.hour % 12), mTime.minute)
                        : String.format("%d:%02d:%02d", (mTime. hour % 12), mTime.minute, mTime.second);
            }else if(mTime.hour == 12) {
                text = mAmbient
                        ? String.format("%d:%02d", mTime.hour, mTime.minute)
                        : String.format("%d:%02d:%02d", mTime. hour, mTime.minute, mTime.second);
            }else{
                text = mAmbient
                        ? String.format("%d:%02d", mTime.hour, mTime.minute)
                        : String.format("%d:%02d:%02d", mTime.hour, mTime.minute, mTime.second);
            }
            canvas.drawText(text, mXOffset, mYOffset, mTextPaint);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        /**
         * Register the peak blood glucose change broadcast receiver.
         * */
        private void registerPeakChangeReceiver(){
            IntentFilter filter = new IntentFilter(ACTION_GLUCOSE_PEAK_CHANGED);
            SCHGlucosePeaks.this.registerReceiver(mPeakGlucoseChangedReceiver, filter);
        }

        /**
         * Unregister the peak blood glucose change broadcast receiver.
         * */
        private void unregisterPeakChangeReceiver() {
            SCHGlucosePeaks.this.unregisterReceiver(mPeakGlucoseChangedReceiver);
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            SCHGlucosePeaks.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            SCHGlucosePeaks.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = SCHGlucosePeaks.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float textSize = resources.getDimension(isRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);

            mTextPaint.setTextSize(textSize);
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }
}
