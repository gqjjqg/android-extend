package com.guo.android_extend.widget.effective;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;

public class HCSGestureDetector extends GestureDetector {
	private final String TAG = this.getClass().toString();
	
	public static final int MOVE_UNKNOWN =	0;
	public static final int LEFT_TO_RIGHT = 1;
	public static final int RIGHT_TO_LEFT =	2;
	public static final int TOP_TO_BOTTOM =	3;
	public static final int BOTTOM_TO_TOP =	4;
	
	public interface OnCustomGestureListener extends OnGestureListener, OnDoubleTapListener{
		/**
		 * detected page turn. double finger move with the same direction.
		 * @param direction
		 * @param ev
		 */
		void OnDoubleFingerStartScroll(MotionEvent ev, int direction);
		/**
		 * detected page turn. double finger move with the different direction.
		 * @param ev
		 */
		void OnDoubleFingerStartZoom(MotionEvent ev);
		
		/**
		 * detected second finger down.
		 * @param ev
		 */
		void OnDoubleFingerDown(MotionEvent ev);
		
		/**
		 * detected finger from edge in screen.
		 * @param ev
		 */
		void OnSingleFingerEdgeIn(MotionEvent ev, int direction);
		
		/**
		 * @param ev
		 * @param direction
		 */
		boolean OnSingleFingerDrag(MotionEvent ev, int direction, float dx, float dy);
	}
	 
	OnCustomGestureListener mListener;
	
	PointF mPoint1Down;
	int mPoint1_ID;
	PointF mPoint2Down;
	int mPoint2_ID;
	PointF mPointMiddleDown;
	int mCurStatus;
	
	private int mScreenWidth;
	private int mScreenHeight;
	
	private static final int LEFT = 0;
	private static final int TOP = 1;
	private static final int RIGHT = 2;
	private static final int BOTTOM = 3;
	private static final int UNKNOWN = 4;
	private int mEdgeIn;
	
	private int mDragDirection;
	
	private int MAX_DISTANCE_MOVE;
	
	public HCSGestureDetector(Context context, OnCustomGestureListener listener) {
		super(context, listener);
		// TODO Auto-generated constructor stub
		mListener = listener;
		mPoint1Down = new PointF();
		mPoint2Down = new PointF();
		mPointMiddleDown = new PointF();
		mPoint2_ID = 1;
		mPoint1_ID = 0;
		mCurStatus = 0;
		mEdgeIn = UNKNOWN;
		mDragDirection = MOVE_UNKNOWN;
		if (context != null) {
			final ViewConfiguration configuration = ViewConfiguration.get(context);
			MAX_DISTANCE_MOVE = configuration.getScaledTouchSlop();
			Log.i(TAG, "MAX_DISTANCE_MOVE = " + MAX_DISTANCE_MOVE);	
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			if (wm != null) {
				DisplayMetrics metric = new DisplayMetrics();
				Display d = wm.getDefaultDisplay();
				d.getMetrics(metric);
				// since SDK_INT = 1;
				mScreenWidth = metric.widthPixels;
				mScreenHeight = metric.heightPixels;
				// includes window decorations (statusbar bar/menu bar)
				if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17)
				try {
					mScreenWidth = (Integer) Display.class.getMethod("getRawWidth").invoke(d);
					mScreenHeight = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
				} catch (Exception ignored) {
				}
				// includes window decorations (statusbar bar/menu bar)
				if (Build.VERSION.SDK_INT >= 17)
				try {
				    Point realSize = new Point();
				    Display.class.getMethod("getRealSize", Point.class).invoke(d, realSize);
				    mScreenWidth = realSize.x;
				    mScreenHeight = realSize.y;
				} catch (Exception ignored) {
				}
			}
			Log.i(TAG, "SCREEN W=" + mScreenWidth + ",H=" + mScreenHeight);
		} else {
			Log.e(TAG, "context is null!");
		}
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		switch (ev.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN :
			mPoint1Down.set(ev.getX(mPoint1_ID), ev.getY(mPoint1_ID));
			mEdgeIn = getEdgeIn(mPoint1Down);
			mDragDirection = MOVE_UNKNOWN;
			Log.i(TAG, "DOWN= " + mPoint1Down.toString());
			break;
		case MotionEvent.ACTION_POINTER_DOWN : 
			mPoint2Down.set(ev.getX(mPoint2_ID), ev.getY(mPoint2_ID));
			mPointMiddleDown.set((mPoint1Down.x + mPoint2Down.x) / 2f, (mPoint1Down.y + mPoint2Down.y) / 2f);
			mCurStatus = 0;
			mListener.OnDoubleFingerDown(ev);
			break;
		case MotionEvent.ACTION_MOVE : 
			if (ev.getPointerCount() == 2) {
				PointF cur = getMiddlePoint(ev);
				float x = cur.x - mPointMiddleDown.x;
				float y = cur.y - mPointMiddleDown.y;
				float d = (float) Math.hypot(x, y);
				if (d > MAX_DISTANCE_MOVE) {
					if (mCurStatus == 0) {
						if (ev.getX(mPoint1_ID) < mPoint1Down.x && ev.getX(mPoint2_ID) < mPoint2Down.x) {
							mListener.OnDoubleFingerStartScroll(ev, RIGHT_TO_LEFT);
						} else if (ev.getX(mPoint1_ID) > mPoint1Down.x && ev.getX(mPoint2_ID) > mPoint2Down.x) {
							mListener.OnDoubleFingerStartScroll(ev, LEFT_TO_RIGHT);
						} else if (ev.getX(mPoint1_ID) < mPoint1Down.x && ev.getX(mPoint2_ID) > mPoint2Down.x ||
								ev.getX(mPoint1_ID) > mPoint1Down.x && ev.getX(mPoint2_ID) < mPoint2Down.x) {
							mListener.OnDoubleFingerStartZoom(ev);
						}
						mCurStatus = 1;
					}
				}
			} else if (ev.getPointerCount() == 1) {
				float x = ev.getX(mPoint1_ID) - mPoint1Down.x;
				float y = ev.getY(mPoint1_ID) - mPoint1Down.y;
				if  (mEdgeIn != UNKNOWN) {
					if (mEdgeIn == LEFT || mEdgeIn == RIGHT) {
						if (x > 0.001) {
							if ( /*Math.abs(x) > Math.abs(y) && */mEdgeIn == LEFT) {
								mListener.OnSingleFingerEdgeIn(ev, LEFT_TO_RIGHT);
							}
							mEdgeIn = UNKNOWN;
						} else if (x < -0.001) {
							if (/*Math.abs(x) > Math.abs(y) && */mEdgeIn == RIGHT) {
								mListener.OnSingleFingerEdgeIn(ev, RIGHT_TO_LEFT);
							}
							mEdgeIn = UNKNOWN;
						}
					} else if (mEdgeIn == TOP || mEdgeIn == BOTTOM) {
						if (y > 0.001) {
							if (/*Math.abs(y) > Math.abs(x) &&*/ mEdgeIn == TOP) {
								mListener.OnSingleFingerEdgeIn(ev, TOP_TO_BOTTOM);
							}
							mEdgeIn = UNKNOWN;
						} else if (y < -0.001) {
							if (/*Math.abs(y) > Math.abs(x) && */mEdgeIn == BOTTOM) {
								mListener.OnSingleFingerEdgeIn(ev, BOTTOM_TO_TOP);
							}
							mEdgeIn = UNKNOWN;
						}
					}
					Log.i(TAG, "MOVE= " + ev.getX(mPoint1_ID)+","+ev.getY(mPoint1_ID));
					
					mPoint1Down.set(ev.getX(mPoint1_ID), ev.getY(mPoint1_ID));
					
				} else if (mDragDirection == MOVE_UNKNOWN){
					float d = (float) Math.hypot(x, y);
					if (d > MAX_DISTANCE_MOVE) {
						if (Math.abs(y) > Math.abs(x)) {
							mDragDirection = y > 0 ? TOP_TO_BOTTOM : BOTTOM_TO_TOP;
						} else if (Math.abs(y) < Math.abs(x)) {
							mDragDirection = x > 0 ? LEFT_TO_RIGHT : RIGHT_TO_LEFT;
						} else {
							mDragDirection = MOVE_UNKNOWN;
						}
						mListener.OnSingleFingerDrag(ev, mDragDirection, x, y);
					}
				}
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			break;
		}
		return super.onTouchEvent(ev);
	}
	
	/**
	 * @param point
	 * @return 取手势中心点
	 */
	public PointF getMiddlePoint(MotionEvent event) {
		if (event.getPointerCount() < 2) {
			return new PointF(event.getX(0), event.getY(0));
		}
		return new PointF((event.getX(0) + event.getX(1)) / 2, (event.getY(0) + event.getY(1)) / 2);
	}
	
	/**
	 * 判断point 位置。
	 * @param p1
	 * @return LEFT, TOP, RIGHT, BOTTOM, UNKNOWN.
	 */
	public int getEdgeIn(PointF p1) {
		if (p1.x >= -0.001 && p1.x <= MAX_DISTANCE_MOVE) {
			return LEFT;
		} else if (p1.x <= mScreenWidth - 0.001 && 
				p1.x >= mScreenWidth - MAX_DISTANCE_MOVE) {	
			return RIGHT;
		} else if (p1.y >= -0.001 && mPoint1Down.y <= MAX_DISTANCE_MOVE) {
			return TOP;
		} else if (p1.y <= mScreenHeight - 0.001 && 
				p1.y >= mScreenHeight - MAX_DISTANCE_MOVE)  {
			return BOTTOM;
		}
		return UNKNOWN;
	}
}
