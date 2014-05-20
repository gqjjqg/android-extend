package com.guo.android_extend;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class ImageViewController extends AbstractController {
	private final String TAG = this.getClass().toString();
	// scale
	float mCurScale;
	float mScale;
	// animation
	float mStepScale;
	
	// drag
	float mCurOffsetX;
	float mCurOffsetY;
	float mOffsetX;
	float mOffsetY;
	// animation
	float mStepX;
	float mStepY;
	
	// Mode
	private MODE mMode;
	private enum MODE {
		IDEL, PRE_DRAG, DRAG, SCALE, ANIMATION
	}
	
	private int MAX_DISTANCE_MOVE;
	private float LIMIT_SCALE_MIN = 0.2F;
	private float LIMIT_SCALE_MAX = 5.0F;
	private float MIN_SCALE = 0.5F;
	private float MAX_SCALE = 3.0F;
	
	// animation
	private float MAX_STEP = 10.0F;
	
	public ImageViewController(ControllerListener mListener) {
		super(mListener);
		// TODO Auto-generated constructor stub
		mScale = 1.0F;
		mCurScale = mScale;
		mStepScale = 0F;
		
		mCurOffsetX = 0F;
		mCurOffsetY = 0F;
		mOffsetX = 0F;
		mOffsetY = 0F;
		
		mMode = MODE.IDEL;
	}

	public ImageViewController(Context context, ControllerListener mListener) {
		this(mListener);
		
		ViewConfiguration config = ViewConfiguration.get(context);
		MAX_DISTANCE_MOVE = config.getScaledTouchSlop();
		Log.i(TAG, "MAX_DISTANCE_MOVE = " + MAX_DISTANCE_MOVE);
	}
	
	@Override
	public void beforeDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		canvas.save();
		
		//Animation process.
		if (mMode == MODE.ANIMATION ) {
			boolean finshX = false;
			if (mCurOffsetX - PRECISION > 0F) {
				mCurOffsetX += mStepX; 
				if (mCurOffsetX - PRECISION <= 0F) {
					mCurOffsetX = 0F;
					finshX = true;
				}
			} else if (mCurOffsetX + PRECISION < 0F) {
				mCurOffsetX += mStepX; 
				if (mCurOffsetX + PRECISION >= 0F) {
					mCurOffsetX = 0F;
					finshX = true;
				}
			} else {
				finshX = true;
			}
			
			boolean finshY = false;
			if (mCurOffsetY - PRECISION > 0F) {
				mCurOffsetY += mStepY; 
				if (mCurOffsetY - PRECISION <= 0F) {
					mCurOffsetY = 0F;
					finshY = true;
				}
			} else if (mCurOffsetY + PRECISION < 0F) {
				mCurOffsetY += mStepY; 
				if (mCurOffsetY + PRECISION >= 0F) {
					mCurOffsetY = 0F;
					finshY = true;
				}
			} else {
				finshY = true;
			}
			
			boolean finshScale = false;
			if ((mCurScale > MAX_SCALE)) {
				mCurScale += mStepScale;
				if (mCurScale <= MAX_SCALE){
					mScale = MAX_SCALE;
					mCurScale = mScale;
					finshScale = true;
				}
			} else if ((mCurScale < MIN_SCALE)) {
				mCurScale += mStepScale;
				if (mCurScale >= MIN_SCALE) {
					mScale = MIN_SCALE;
					mCurScale = mScale;
					finshScale = true;
				}
			} else {
				finshScale = true;
			}
			if (finshScale && finshX && finshY) {
				mMode = MODE.IDEL;
			}
			super.mListener.invalidate();
		}
		
		canvas.translate(mCurOffsetX, mCurOffsetY);
		canvas.scale(mCurScale, mCurScale, mCurPointMidd.x, mCurPointMidd.y);
	}

	@Override
	public void afterDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		
		canvas.restore();
	}

	private void checkOffset() {
		if (mCurOffsetX - PRECISION > 0F || mCurOffsetX + PRECISION < 0F) {
			mStepX = -mCurOffsetX / MAX_STEP;
			mMode = MODE.ANIMATION;
		}
		if (mCurOffsetY - PRECISION > 0F || mCurOffsetY + PRECISION < 0F) {
			mStepY = -mCurOffsetY / MAX_STEP;
			mMode = MODE.ANIMATION;
		}
	}
	
	private void checkScale() {
		if ((mCurScale > MAX_SCALE)) {
			mStepScale = (MAX_SCALE - mCurScale) / MAX_STEP;
			mMode = MODE.ANIMATION;
		} else if ((mCurScale < MIN_SCALE)) {
			mStepScale = (MIN_SCALE - mCurScale) / MAX_STEP;
			mMode = MODE.ANIMATION;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			if (mMode == MODE.IDEL) {
				mCurPointDown.set(event.getX(0), event.getY(0));
				mMode = MODE.PRE_DRAG;
			}
		case MotionEvent.ACTION_POINTER_DOWN:
			if (mMode != MODE.ANIMATION && event.getPointerCount() == 2) {
				mPreDistance = getDistance(event);
				mCurPointMidd.set(super.mListener.getCenterPoint());
				mMode = MODE.SCALE;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mMode == MODE.PRE_DRAG && event.getPointerCount() == 1) {
				float x = event.getX(0) - mCurPointDown.x;
				float y = event.getY(0) - mCurPointDown.y;
				float d = (float) Math.hypot(x, y);
				if (d > MAX_DISTANCE_MOVE) {
					mMode = MODE.DRAG;
				}
			} else if (mMode == MODE.DRAG && event.getPointerCount() == 1) {
				mCurOffsetX = mOffsetX + event.getX(0) - mCurPointDown.x;
				mCurOffsetY = mOffsetY + event.getY(0) - mCurPointDown.y;
				super.mListener.invalidate();
			} else if (mMode == MODE.SCALE && event.getPointerCount() == 2) {
				mCurScale = mScale * getDistance(event) / mPreDistance;

				mCurScale = Math.min(LIMIT_SCALE_MAX, mCurScale);
				mCurScale = Math.max(LIMIT_SCALE_MIN, mCurScale);
				super.mListener.invalidate();
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (mMode == MODE.SCALE) {
				mMode = MODE.IDEL;
			} else if (mMode == MODE.DRAG) {
				checkOffset();

				mOffsetX = 0F;
				mOffsetY = 0F;
				if (mMode != MODE.ANIMATION) {
					mMode = MODE.IDEL;
				}
			}
			super.mListener.invalidate();
			break;
		case MotionEvent.ACTION_POINTER_UP:
			if (mMode == MODE.SCALE) {
				checkScale();

				checkOffset();

				mScale = mCurScale;
				if (mMode != MODE.ANIMATION) {
					mMode = MODE.IDEL;
				}
			} else {
				Log.e(TAG, "ACTION_POINTER_UP error mMode =" + mMode);
			}
			super.mListener.invalidate();
			break;
		default:
			mMode = MODE.IDEL;
		}

		return true;
	}
}
