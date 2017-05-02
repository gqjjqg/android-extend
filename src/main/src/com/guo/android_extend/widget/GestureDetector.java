package com.guo.android_extend.widget;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Created by gqj3375 on 2017/2/27.
 */

public class GestureDetector {
	private final String TAG = this.getClass().toString();
	private float sx, sy;
	private float mx, my;
	private float ex, ey;
	private float distance;
	private int touchMode;
	private int count_scale;
	private int count_translate;
	private GestureListener mGestureListener;

	public static int TOUCHMODE_ROTATE_RESTART = -2;
	public static int TOUCHMODE_DOUBLE_PRE = -1;
	public static int TOUCHMODE_NONE = 0;
	public static int TOUCHMODE_ROTATE = 1;
	public static int TOUCHMODE_DOUBLE_SCALE = 2;
	public static int TOUCHMODE_DOUBLE_MOVE = 3;

	private int MAX_DISTANCE_MOVE;
	private int MAX_DETECTED_COUNT;

	public interface GestureListener {
		public void single_drag_prepare(float x, float y);
		public void single_drag_process(float x, float y);

		public void double_scale_prepare(float scale);
		public void double_scale_process(float scale);

		public void double_drag_prepare(float x, float y);
		public void double_drag_process(float x, float y);
	}

	public GestureDetector(Context context) {
		mGestureListener = null;
		final ViewConfiguration configuration = ViewConfiguration.get(context);
		MAX_DISTANCE_MOVE = configuration.getScaledTouchSlop();
		MAX_DETECTED_COUNT = 3;
		Log.i(TAG, "MAX_DISTANCE_MOVE = " + MAX_DISTANCE_MOVE);
	}

	public void setGestureListener(GestureListener gestureListener) {
		mGestureListener = gestureListener;
	}

	public void onTouch(View view, MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				sx = event.getX(0);
				sy = event.getY(0);
				touchMode = TOUCHMODE_ROTATE;
				//start rotate
				if (mGestureListener != null) {
					mGestureListener.single_drag_prepare(sx, sy);
				}
				count_scale = MAX_DETECTED_COUNT;
				count_translate = MAX_DETECTED_COUNT;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				sx = event.getX(0);
				sy = event.getY(0);
				ex = event.getX(1);
				ey = event.getY(1);
				mx = (sx + ex) / 2.0f;
				my = (sy + ey) / 2.0f;
				distance = (float)Math.hypot(ex - sx, ey - sy);
				touchMode = TOUCHMODE_DOUBLE_PRE;
				break;
			case MotionEvent.ACTION_UP:
				touchMode = TOUCHMODE_NONE;
				break;
			case MotionEvent.ACTION_POINTER_UP:
				touchMode = TOUCHMODE_ROTATE_RESTART;
				break;
			case MotionEvent.ACTION_MOVE:
				if (touchMode == TOUCHMODE_ROTATE) {
					// ROTATE.
					if (mGestureListener != null) {
						mGestureListener.single_drag_process(event.getX(0), event.getY(0));
					}
				} else if (touchMode == TOUCHMODE_DOUBLE_PRE) {
					if (event.getPointerCount() == 2) {
						float temp = (float) Math.hypot(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1));
						float offset1 = Math.abs(distance - temp);
						if (offset1 > MAX_DISTANCE_MOVE) {
							count_scale--;
							distance = temp;
						}

						float tx = (event.getX(0) + event.getX(1)) / 2;
						float ty = (event.getY(0) + event.getY(1)) / 2;
						float offset2 = (float) Math.hypot(mx - tx, my - ty);
						if (offset2 > MAX_DISTANCE_MOVE) {
							count_translate--;
							mx = tx;
							my = ty;
						}

						if (count_scale == 0) {
								touchMode = TOUCHMODE_DOUBLE_SCALE;
								//start scale
								if (mGestureListener != null) {
									mGestureListener.double_scale_prepare(1.0f);
								}
						}

						if (count_translate == 0) {
							touchMode = TOUCHMODE_DOUBLE_MOVE;
							//start move
							if (mGestureListener != null) {
								mGestureListener.double_drag_prepare(mx, my);
							}
						}
					}
				} else if (touchMode == TOUCHMODE_ROTATE_RESTART) {
					if (mGestureListener != null) {
						mGestureListener.single_drag_prepare(event.getX(0), event.getY(0));
						mGestureListener.single_drag_process(event.getX(0), event.getY(0));
					}
					touchMode = TOUCHMODE_ROTATE;
				} else if (touchMode == TOUCHMODE_DOUBLE_SCALE) {
					float temp = (float)Math.hypot(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1));
					float scale = (temp / distance);
					if (mGestureListener != null) {
						mGestureListener.double_scale_process(scale);
					}
				} else if (touchMode == TOUCHMODE_DOUBLE_MOVE) {
					if (mGestureListener != null) {
						float tx = (event.getX(0) + event.getX(1)) / 2;
						float ty = (event.getY(0) + event.getY(1)) / 2;
						mGestureListener.double_drag_process(tx, ty);
					}
				}
				break;
			default:;
		}
	}

}
