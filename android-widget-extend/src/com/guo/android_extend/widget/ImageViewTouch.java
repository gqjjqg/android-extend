package com.guo.android_extend.widget;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

public class ImageViewTouch extends ImageView {
	private final String TAG = this.getClass().toString();
	// control
	protected boolean isCenter;
	protected float MIN_SCALE = 0.3F;
	protected float MAX_SCALE = 3.0F;
	
	// matrix
	Matrix mDefMatrix;
	Matrix mPreMatrix;
	Matrix mCurMatrix;
	
	float mCurScale;
	float mTargetScale;
	
	float mPreDegree;
	
	Drawable mCurDrawable;
	PointF mCurPointDown;
	PointF mMidPoint;
	float mPreDist;
	
	//Mode
	private MODE mPreMode;
	private MODE mMode;
	public enum MODE {
		NONE, DRAG, ZOOM
	}
	
	//ImageData
	RectF mDefImageBounds;
	RectF mCurImageBounds;
	
	//scale limitation
	float mMinWidth, mMinHeight;
	float mMaxWidth, mMaxHeight;
	
	//animation
	AnimationTask mAnimationTask;
	
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
		
		mCurDrawable = getDrawable();
		
		isCenter = true;
		
		mPreMode = MODE.NONE;
		
		mDefImageBounds = new RectF();
		mCurImageBounds = new RectF();
	}
	
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
		if (mDefImageBounds.isEmpty()) {
			initialScale();
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
			mCurPointDown.set(event.getX(), event.getY());
			if (mMode == MODE.NONE) {
				mPreMode = mMode;
				mMode = MODE.DRAG;
			}
			mPreMatrix.set(mCurMatrix);
			return true;
		case MotionEvent.ACTION_POINTER_DOWN:
			if (mMode == MODE.DRAG) {
				if (getMiddlePoint(event, mMidPoint)) {
					mPreDist = getDistence(event);
					mPreDegree = getRotation(event);
					mPreMode = mMode;
					mMode = MODE.ZOOM;
					mPreMatrix.set(mCurMatrix);
				}
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mMode == MODE.ZOOM) {
				mCurMatrix.set(mPreMatrix);
				//float rotation = getRotation(event) - mPreDegree;
				mTargetScale = getDistence(event) / mPreDist;
				mCurMatrix.postScale(mTargetScale, mTargetScale, mMidPoint.x, mMidPoint.y);
				invalidate();
			} else if (mMode == MODE.DRAG) {
				
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (mMode == MODE.ZOOM) {
				if (isScaleLimited(mCurMatrix)) {
					//mCurMatrix.set(mDefMatrix);
				}
			}
			mMode = MODE.NONE;
			
			invalidate();
			break;
		case MotionEvent.ACTION_POINTER_UP:
			if (mMode == MODE.ZOOM) {
				if (isScaleLimited(mCurMatrix)) {
					if (mAnimationTask == null) {
						mAnimationTask = new AnimationTask();
						mAnimationTask.execute("test");
					}
					//mCurMatrix.set(mDefMatrix);
				}
			}
			mMode = mPreMode;
			mPreMode = MODE.NONE;
			
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
		if (w < this.mMinWidth || w > this.mMaxWidth) {
			return true;
		}
		if (h < this.mMinHeight || h > this.mMaxHeight) {
			return true;
		}
		Log.d(TAG, "wh=" + w +"," + h);
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
	
	private class AnimationTask extends AsyncTask<String, Integer, String> {
		private boolean isCanceled = false;
		
		@Override  
        protected void onPreExecute() {  
            Log.i(TAG, "onPreExecute() called");  
        }  
		
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {   
				float step = mTargetScale > 1.0F ? -0.1F : 0.1F;
            	
                while ( isScaleLimited(mCurMatrix) && !isCanceled) {
                	if (mTargetScale > 3.0F) {
                		step = -0.8F;
                	} else if (mTargetScale > 2.0F) {
                		step = -0.4F;
                	} else if (mTargetScale > 1.0F) {
                		step = -0.1F;
                	} else if (mTargetScale > 0.6F) {
                		step = 0.04F;
                	} else if (mTargetScale > 0.2F) {
                		step = 0.08F;
                	} else {
                		step = 0.1F;
                	}
                	mTargetScale = mTargetScale + step;
                	
                	mCurMatrix.set(mPreMatrix);
    				mCurMatrix.postScale(mTargetScale, mTargetScale, mMidPoint.x, mMidPoint.y);
                    publishProgress();  
                    Thread.sleep(10);  
                }  
            } catch (Exception e) {  
                Log.e(TAG, e.getMessage());  
            }  
			return null;
		} 
		
        @Override  
        protected void onProgressUpdate(Integer... progresses) {  
        	ImageViewTouch.this.invalidate();
        }  
          
        @Override  
        protected void onPostExecute(String result) {
            mAnimationTask = null;
        }  
          
        @Override  
        protected void onCancelled() {  
        	isCanceled = true;
        }  
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
