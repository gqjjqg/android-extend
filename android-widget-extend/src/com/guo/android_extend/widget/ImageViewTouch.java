package com.guo.android_extend.widget;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ImageViewTouch extends ImageView {
	private final String TAG = this.getClass().toString();
	// control
	protected boolean isCenter = true;
	protected boolean isScaleEnable = true;
	protected boolean isRotateEnable = true;
	
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
	
	// touch data.
	private PointF mCurPointDown;
	private PointF mMidPoint;
	private float mPreDist;
	private float mPreDegree;
	
	//Mode
	private MODE mMode;
	public enum MODE {
		NONE, FINGER, DOUBLE_FINGER, SCALE_MAX, SCALE_MIN
	}
	
	//temp
	float[] mMatrixData;
	Paint mPaint;
	
	//ImageData
  	RectF mDefImageBounds;
  	RectF mCurImageBounds;
  	
	public ImageViewTouch(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		preCreate();
	}

	public ImageViewTouch(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		preCreate();
	}

	public ImageViewTouch(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		preCreate();
	}
	
	protected void preCreate() {
		
		mCurPointDown = new PointF();
		mMidPoint = new PointF();

		mDefImageBounds = new RectF();
		mCurImageBounds = new RectF();
		
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
			this.invalidate();
		} else if (mMode == MODE.SCALE_MIN) {
			if ((mCurScale < MIN_SCALE)) {
				mCurScale += mStepScale;
			} 
			if (mCurScale > MIN_SCALE) {
				mScale = MIN_SCALE;
				mCurScale = mScale;
				mMode = MODE.NONE;
			}
			this.invalidate();
		}
	}
	
	/* (non-Javadoc)
	 * @see android.widget.ImageView#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		if (mDefImageBounds.isEmpty()) {
			mDefImageBounds.set(this.getDrawable().getBounds());
			Log.d(TAG, "mDefImageBounds=" + mDefImageBounds.toString());
			getImageMatrix().mapRect(mCurImageBounds, mDefImageBounds);
			Log.d(TAG, "mCurImageBounds=" + mCurImageBounds.toString());
		}
		
		canvas.save();
		if (isScaleEnable) {
			animScale();
			canvas.scale(mCurScale, mCurScale, mMidPoint.x, mMidPoint.y);
		}
		if (isRotateEnable) {
			canvas.rotate(mCurDegree, mMidPoint.x, mMidPoint.y);
		}
		super.onDraw(canvas);
		canvas.drawRect(mCurImageBounds, mPaint);
		canvas.restore();
		
	}
	
	/* (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:	
			if (mMode == MODE.NONE) {
				mCurPointDown.set(event.getX(), event.getY());
				mMode = MODE.FINGER;
			} else if (mMode == MODE.SCALE_MAX || mMode == MODE.SCALE_MIN) {
				mCurPointDown.set(event.getX(), event.getY());
				mScale = mCurScale;
				mMode = MODE.FINGER;
			}
			return true;
		case MotionEvent.ACTION_POINTER_DOWN:
			if (mMode == MODE.FINGER && event.getPointerCount() == 2) {
				if (getMiddlePoint(event, mMidPoint)) {
					mPreDist = getDistence(event);
					mPreDegree = getRotation(event);
					mMode = MODE.DOUBLE_FINGER;
				}
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
				invalidate();
			} else if (mMode == MODE.FINGER && event.getPointerCount() == 1) {
				
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (mMode == MODE.DOUBLE_FINGER) {
				mMode = MODE.NONE;
			}
			invalidate();
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
					mMode = MODE.FINGER;
				}
				
				mDegree = mCurDegree % 360;
			} else {
				Log.e(TAG, "error mMode =" + mMode);
			}
			invalidate();
			break;
		default : mMode = MODE.NONE;
		}
		
		return super.onTouchEvent(event);
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
	 * @param point
	 * @return 取手势中心点
	 */
	protected boolean getMiddlePoint(MotionEvent event, PointF out) {
		if (isCenter) {
			out.set(this.getWidth() / 2F, this.getHeight() / 2F);
		} else {
			if (event.getPointerCount() == 2) {
				out.set((event.getX(0) + event.getX(1)) / 2F, (event.getY(0) + event.getY(1)) / 2F);
			} else {
				out.set(0, 0);
				return false;
			}
		}
		return true;
	}

	
	/**
	 * @param event
	 * @return 取旋转角度
	 */
	public float getRotation(MotionEvent event) {
		if (event.getPointerCount() < 2) {
			return 0;
		}
		double radians = Math.atan2((event.getY(0) - event.getY(1)), (event.getX(0) - event.getX(1)));
		return (float) Math.toDegrees(radians);
	}
}
