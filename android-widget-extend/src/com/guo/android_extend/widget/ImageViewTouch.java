package com.guo.android_extend.widget;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
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
	protected float MIN_SCALE = 0.3F;
	protected float MAX_SCALE = 3.0F;
	
	// matrix
	Matrix mDefMatrix;
	Matrix mPreMatrix;
	Matrix mCurMatrix;
	
	// scale 
	float mCurScale;
	float mScale;
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
		NONE, FINGER, DOUBLE_FINGER, LIMITION
	}
	
	//ImageData
	RectF mDefImageBounds;
	RectF mCurImageBounds;
	
	//scale limitation
	float mMinWidth, mMinHeight;
	float mMaxWidth, mMaxHeight;
	
	//animation
	Handler mUIHandler;
	
	private final Runnable mInvalidateTask = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

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
		mCurMatrix = new Matrix(this.getImageMatrix());
		mDefMatrix = new Matrix(this.getImageMatrix());
		mPreMatrix = new Matrix();
		
		mCurPointDown = new PointF();
		mMidPoint = new PointF();

		mDefImageBounds = new RectF();
		mCurImageBounds = new RectF();
		
		mCurDegree = 0F;
		mCurScale = 0F;
		
		mMode = MODE.NONE;
		
		mUIHandler = new Handler();
	}
	
	/**
	 * initial scale data.
	 */
	protected void initialScale() {
		mDefImageBounds.set(this.getDrawable().getBounds());
		
		//Min scale
		mCurMatrix.set(mDefMatrix);
		mCurMatrix.postScale(MIN_SCALE, MIN_SCALE);
		mCurMatrix.mapRect(mCurImageBounds, mDefImageBounds);
		mMinWidth = mCurImageBounds.right - mCurImageBounds.left;
		mMinHeight = mCurImageBounds.bottom - mCurImageBounds.top;
		//Max scale
		mCurMatrix.set(mDefMatrix);
		mCurMatrix.postScale(MAX_SCALE, MAX_SCALE);
		mCurMatrix.mapRect(mCurImageBounds, mDefImageBounds);
		mMaxWidth = mCurImageBounds.right - mCurImageBounds.left;
		mMaxHeight = mCurImageBounds.bottom - mCurImageBounds.top;
		
		Log.d(TAG, "Min =(" + this.mMinWidth + "," + this.mMinHeight);
		Log.d(TAG, "Max =(" + this.mMaxWidth + "," + this.mMaxHeight);
		
		mCurMatrix.set(mDefMatrix);
	}
	
	/* (non-Javadoc)
	 * @see android.widget.ImageView#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		if (isScaleEnable) {
			if (mDefImageBounds.isEmpty()) {
				initialScale();
			}
			if (mMode == MODE.LIMITION) {
				if (isScaleLimited(mCurMatrix)) {
					mCurMatrix.set(mPreMatrix);
					float stepScale = 0f;
            		if (mScale > 3.0F) {
            			stepScale = -0.1F;
                	} else if (mScale > 2.0F) {
                		stepScale = -0.05F;
                	} else if (mScale > 1.0F) {
                		stepScale = -0.01F;
                	} else if (mScale > 0.6F) {
                		stepScale = 0.01F;
                	} else if (mScale > 0.2F) {
                		stepScale = 0.02F;
                	} else {
                		stepScale = 0.05F;
                	}
                	mCurScale = mCurScale + stepScale;
            		mCurMatrix.postScale(mCurScale, mCurScale, mMidPoint.x, mMidPoint.y);
            		mUIHandler.postDelayed(mInvalidateTask, 1);
				} else {
					mMode = MODE.NONE;
				}
			}
		}
		canvas.setMatrix(mCurMatrix);
		super.onDraw(canvas);
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
				mPreMatrix.set(mCurMatrix);
				mMode = MODE.FINGER;
			} else if (mMode == MODE.LIMITION) {
				mCurPointDown.set(event.getX(), event.getY());
				mMode = MODE.FINGER;
			}
			return true;
		case MotionEvent.ACTION_POINTER_DOWN:
			if (mMode == MODE.FINGER && event.getPointerCount() == 2) {
				if (getMiddlePoint(event, mMidPoint)) {
					mPreDist = getDistence(event);
					mPreDegree = getRotation(event);
					mMode = MODE.DOUBLE_FINGER;
					mPreMatrix.set(mCurMatrix);
				}
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mMode == MODE.DOUBLE_FINGER && event.getPointerCount() == 2) {
				mCurMatrix.set(mPreMatrix);
				if (isRotateEnable) {
					mCurDegree = getRotation(event) - mPreDegree;
					mCurMatrix.postRotate(mCurDegree, mMidPoint.x, mMidPoint.y);
				}
				if (isScaleEnable) {
					mCurScale = getDistence(event) / mPreDist;
					mCurMatrix.postScale(mCurScale, mCurScale, mMidPoint.x, mMidPoint.y);
				}
				invalidate();
			} else if (mMode == MODE.FINGER) {
				
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
				if (isScaleLimited(mCurMatrix)) {
					mMode = MODE.LIMITION;
				} else {
					mMode = MODE.FINGER;
				}
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
		matrix.mapRect(mCurImageBounds, mDefImageBounds);
		float w = mCurImageBounds.right - mCurImageBounds.left;
		float h = mCurImageBounds.bottom - mCurImageBounds.top;
		
		mScale = ( w / (mDefImageBounds.right - mDefImageBounds.left) + 
				h / (mDefImageBounds.bottom - mDefImageBounds.top) ) / 2f;
		Log.d(TAG, "wh=" + w +"," + h + "scale=" + mScale);
		
		if (w < this.mMinWidth || w > this.mMaxWidth) {
			return true;
		}
		if (h < this.mMinHeight || h > this.mMaxHeight) {
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
			out.set(this.getWidth() / 2, this.getHeight() / 2);
		} else {
			if (event.getPointerCount() == 2) {
				out.set((event.getX(0) + event.getX(1)) / 2, (event.getY(0) + event.getY(1)) / 2);
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
	
	public boolean inQuadrangle(PointF a, PointF b, PointF c,PointF d,PointF p){                 
        double dTriangle = triangleArea(a,b,p)+triangleArea(b,c,p)  
                    +triangleArea(c,d,p)+triangleArea(d,a,p);  
        double dQuadrangle = triangleArea(a,b,c)+triangleArea(c,d,a);         
        return (dTriangle < (dQuadrangle+1.0))&&(dTriangle > (dQuadrangle-1.0));    
    }  


    public double triangleArea(PointF a, PointF b, PointF c){  
        double result = Math.abs((a.x * b.y + b.x * c.y + c.x * a.y - b.x * a.y  
                - c.x * b.y - a.x * c.y) / 2.0D);  
        return result;  
    } 
    
}
