package com.guo.android_extend;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.Log;
import android.view.MotionEvent;

public class CanvasController {
	private final String TAG = this.getClass().toString();

	// control
	protected boolean isCenter = true;
	protected boolean isScaleEnable = true;
	protected boolean isRotateEnable = true;
	protected boolean isDragEnable = true;

	protected float MIN_SCALE = 0.5F;
	protected float MAX_SCALE = 3.0F;
	protected float LIMIT_SCALE_MIN = 0.2F;
	protected float LIMIT_SCALE_MAX = 5.0F;
	protected float PRECISION = 0.001F;
	protected float MAX_STEP = 10.0F;

	// scale
	float mCurScale;
	float mScale;
	float mStepScale;

	// rotate
	float mCurDegree;
	float mDegree;

	// drag
	PointF mCenterPoint;
	float mCurOffsetX;
	float mCurOffsetY;
	float mOffsetX;
	float mOffsetY;

	// touch data.
	private PointF mCurPointDown;
	private PointF mCurPointMidd;

	private float mPreDist;
	private float mPreDegree;

	// Mode
	private MODE mMode;

	public enum MODE {
		NONE, FINGER, DOUBLE_FINGER, SCALE_MAX, SCALE_MIN
	}

	// temp
	float[] mMatrixData;
	Paint mPaint;
  	
	// ImageData
	RectF mDefImageBounds;
	RectF mCurImageBounds;

	private CanvasControllerListener mCCL;
	
	public interface CanvasControllerListener {
		public void canvasFlush();
		public Rect getDrawableBounds();
		public Matrix getDefMatrix();
	}
	
	public CanvasController(CanvasControllerListener ccl) {
		// TODO Auto-generated constructor stub
		mCurPointDown = new PointF();
		mCurPointMidd = new PointF();
		mCenterPoint = new PointF();
		mDefImageBounds = new RectF();
		mCurImageBounds = new RectF();
		
		mCurOffsetX = 0F;
		mCurOffsetY = 0F;
		mOffsetX = 0F;
		mOffsetY = 0F;
		
		mDegree = 0F;
		mCurDegree = mDegree;
				
		mScale = 1.0F;
		mCurScale = mScale;
		
		mMode = MODE.NONE;
		
		mMatrixData = new float[9];
		mPaint = new Paint();
		mPaint.setColor(Color.RED);
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeWidth(6);
		
		mCCL = ccl;
	}
	
	/**
	 * @param mid
	 */
	protected void animScale() {
		if (mMode == MODE.SCALE_MAX ) {
			if ((mCurScale > MAX_SCALE)) {
				mCurScale += mStepScale;
			}
			if (mCurScale < MAX_SCALE){
				mScale = MAX_SCALE;
				mCurScale = mScale;
				mMode = MODE.NONE;
			}
			mCCL.canvasFlush();
		} else if (mMode == MODE.SCALE_MIN) {
			if ((mCurScale < MIN_SCALE)) {
				mCurScale += mStepScale;
			} 
			if (mCurScale > MIN_SCALE) {
				mScale = MIN_SCALE;
				mCurScale = mScale;
				mMode = MODE.NONE;
			}
			mCCL.canvasFlush();
		}
	}
	
	public void beforeDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		canvas.save();
		if (isDragEnable) {
			canvas.translate(mCurOffsetX, mCurOffsetY);
		}
		if (isScaleEnable) {
			animScale();
			canvas.scale(mCurScale, mCurScale, mCurPointMidd.x, mCurPointMidd.y);
		}
		if (isRotateEnable) {
			canvas.rotate(mCurDegree, mCurPointMidd.x, mCurPointMidd.y);
		}
	}
	
	public void afterDraw(Canvas canvas) {
		if (mDefImageBounds.isEmpty()) {
			mDefImageBounds.set(mCCL.getDrawableBounds());
			Log.d(TAG, "mDefImageBounds=" + mDefImageBounds.toString());
			mCCL.getDefMatrix().mapRect(mCurImageBounds, mDefImageBounds);
			Log.d(TAG, "mCurImageBounds=" + mCurImageBounds.toString());
			
			mDefImageBounds.set(mCurImageBounds);
			mCenterPoint.set((mDefImageBounds.left + mDefImageBounds.right) / 2F, 
						(mDefImageBounds.top + mDefImageBounds.bottom) / 2F);
		} else {
			canvas.getMatrix().mapRect(mCurImageBounds, mDefImageBounds);
		}
		canvas.restore();
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:	
			if (mMode == MODE.NONE) {
				mCurPointDown.set(event.getX(), event.getY());
				mMode = MODE.FINGER;
			} else {
				Log.e(TAG, "ACTION_DOWN error mMode =" + mMode);
			}
			return true;
		case MotionEvent.ACTION_POINTER_DOWN:
			if (mMode == MODE.FINGER && event.getPointerCount() == 2) {
				if (isCenter) {
					mCurPointMidd.set(mCenterPoint);
				} else {
					mCurPointMidd.set((event.getX(0) + event.getX(1)) / 2F, (event.getY(0) + event.getY(1)) / 2F);	
				}
				mPreDist = getDistence(event);
				mPreDegree = getRotation(event);
				mMode = MODE.DOUBLE_FINGER;
			} else if (mMode == MODE.SCALE_MAX || mMode == MODE.SCALE_MIN) {
				mCurPointDown.set(event.getX(), event.getY());
				mScale = mCurScale;
				mMode = MODE.DOUBLE_FINGER;
			} 
			break;
		case MotionEvent.ACTION_MOVE:
			if (mMode == MODE.DOUBLE_FINGER && event.getPointerCount() == 2) {
				if (isRotateEnable) {
					mCurDegree = mDegree + getRotation(event) - mPreDegree;
				}
				if (isScaleEnable) {
					mCurScale = mScale * getDistence(event) / mPreDist;
					mCurScale = Math.min(LIMIT_SCALE_MAX, mCurScale);
					mCurScale = Math.max(LIMIT_SCALE_MIN, mCurScale);
				}
				mCCL.canvasFlush();
			} else if (mMode == MODE.FINGER && event.getPointerCount() == 1) {
				if (isDragEnable) {
					mCurOffsetX = mOffsetX + event.getX(0) - mCurPointDown.x;
					mCurOffsetY = mOffsetY + event.getY(0) - mCurPointDown.y;
				}
				mCCL.canvasFlush();
			} else {
				Log.e(TAG, "ACTION_UP error mMode =" + mMode);
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (mMode == MODE.DOUBLE_FINGER) {
				mMode = MODE.NONE;
			} else if (mMode == MODE.FINGER) {
				mOffsetX = mCurOffsetX;
				mOffsetY = mCurOffsetY;
				mMode = MODE.NONE;
			}
			mCCL.canvasFlush();
			break;
		case MotionEvent.ACTION_POINTER_UP:
			if (mMode == MODE.DOUBLE_FINGER) {
				if ((mCurScale > MAX_SCALE)) {
					mStepScale = (MAX_SCALE - mCurScale) / MAX_STEP;
					mMode = MODE.SCALE_MAX;
				} else if ((mCurScale < MIN_SCALE)) {
					mStepScale = (MIN_SCALE - mCurScale) / MAX_STEP;
					mMode = MODE.SCALE_MIN;
				} else {
					mScale = mCurScale;
					mMode = MODE.NONE;
				}
				
				mDegree = mCurDegree % 360;
			} else {
				Log.e(TAG, "ACTION_POINTER_UP error mMode =" + mMode);
			}
			mCCL.canvasFlush();
			break;
		default : mMode = MODE.NONE;
		}
		
		return true;
	}
	
	/**
	 * @param matrix
	 * @return
	 */
	protected boolean isScaleLimited(Matrix matrix) {
		matrix.getValues(mMatrixData);
		mScale = (mMatrixData[0] + mMatrixData[4]) / 2f;
		Log.d(TAG, "mScale=" + mScale);
		if ((mScale > MAX_SCALE) && (mScale - MAX_SCALE) > PRECISION) {
			return true;
		}
		if ((mScale < MIN_SCALE) && (MIN_SCALE - mScale) > PRECISION) {
			return true;
		}
		return false;
	}

	/**
	 * @param event
	 * @return 触碰两点间距离
	 */
	protected float getDistence(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	/**
	 * @param event
	 * @return 取旋转角度
	 */
	protected float getRotation(MotionEvent event) {
		if (event.getPointerCount() < 2) {
			return 0;
		}
		double radians = Math.atan2((event.getY(0) - event.getY(1)), (event.getX(0) - event.getX(1)));
		return (float) Math.toDegrees(radians);
	}
}
