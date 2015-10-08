package com.guo.android_extend.widget.controller;


import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
/**
 * 
 * @author gqj3375
 *
 *	Base function for image view.
 *  1. double finger for scale.		OK
 *  2. single finger for drag.		OK
 *  3. double tap for reset.		OK
 *  4. automatic step aside.		OK
 *  5. animation for scale or drag.	OK
 *  
 *  if image is smaller than the canvas.
 *  image will center in the canvas.
 *  if image is bigger than the canvas.
 *  can drag and aside.
 *  
 */
public class ImageController extends AbstractController {
	private final String TAG = this.getClass().toString();
	// scale
	float mCurScale;

	// offset in view.
	float mDefOffsetX, mDefOffsetY;
	float mCurOffsetX, mCurOffsetY;
	float mMotionOffsetX, mMotionOffsetY;
	float mOffsetX, mOffsetY;
	float mDownOffsetX, mDownOffsetY;
	
	// area.
	float mWorldWidth, mWorldHeight;
	float mImageWidth, mImageHeight;
	
	// animation
	float mStepX, mStepY;
	float mAnimationOffsetX, mAnimationOffsetY;
	float mAnimationStartX, mAnimationStartY;
	// animation
	float mStepScale;
	float mAnimationScale;
	// animation status
	private STATUS mScaleStatus;
	private STATUS mOffsetXStatus;
	private STATUS mOffsetYStatus;
	private enum STATUS {
		IDEL, INCREASE, DECREASE
	}
	// animation frame.
	private float MAX_STEP = 10.0F;
		
	// Mode
	private MODE mMode;
	private enum MODE {
		IDEL, PRE_DRAG, DRAG, SCALE, ANIMATION
	}
	
	//drag
	private int MAX_DISTANCE_MOVE;
	//double click detect
	private int MAX_DOUBLE_TAP_TIME;
	private MotionEvent mCurrentDownEvent;  
	private MotionEvent mPreviousUpEvent;  
	
	//scale limit.
	private float LIMIT_SCALE_MIN;
	private float LIMIT_SCALE_MAX;
	private float MIN_SCALE;
	private float NORMAL_SCALE;
	private float MAX_SCALE;
	
	//double tap
	private float mScales[];
	private int mCurLevel;
	
	public ImageController(ControllerListener mListener) {
		super(mListener);
		// TODO Auto-generated constructor stub
		mWorldWidth = -1;
		mWorldHeight = -1;
		
		mMode = MODE.IDEL;
		
		mScales = null;
		mCurLevel = 0;
		mCurScale = 1.0F;
		
		mScaleStatus = STATUS.IDEL;
		mOffsetXStatus = STATUS.IDEL;
		mOffsetYStatus = STATUS.IDEL;

		MIN_SCALE = 0.5f;
		NORMAL_SCALE = 1.0f;
		MAX_SCALE = 2.0f;
	}

	public ImageController(Context context, ControllerListener mListener) {
		this(mListener);
		
		ViewConfiguration config = ViewConfiguration.get(context);
		MAX_DISTANCE_MOVE = config.getScaledTouchSlop();
		Log.i(TAG, "MAX_DISTANCE_MOVE = " + MAX_DISTANCE_MOVE);
		
		MAX_DOUBLE_TAP_TIME = ViewConfiguration.getDoubleTapTimeout();
		Log.i(TAG, "MAX_DOUBLE_TAP_TIME = " + MAX_DOUBLE_TAP_TIME);
	}
	
	@Override
	public void beforeDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		
		//Animation process.
		if (mMode == MODE.ANIMATION ) {
			//TODO Offset animation.
			boolean finshX = calculateOffsetX();
			boolean finshY = calculateOffsetY();
			//TODO Scale animation.
			boolean finshScale = calculateScale();
			
			if (finshScale && finshX && finshY) {
				mMode = MODE.IDEL;
			}
			super.mListener.invalidate();
			
			canvas.save();
			canvas.translate(mAnimationOffsetX - mAnimationStartX + mOffsetX, mAnimationOffsetY - mAnimationStartY + mOffsetY);
			canvas.scale(mAnimationScale, mAnimationScale, mCurPointMidd.x, mCurPointMidd.y);
			
		} else {
			canvas.save();
			canvas.translate(mOffsetX, mOffsetY);
			canvas.scale(mCurScale, mCurScale, mCurPointMidd.x, mCurPointMidd.y);
		}
	}

	@Override
	public void afterDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		canvas.restore();
	}
	
	/**
	 * initial drag mode reference data.
	 */
	protected void initialInnerData() {
		
		mCurOffsetX = mDefOffsetX;
		mCurOffsetY = mDefOffsetY;
		
		mMotionOffsetX = mCurOffsetX;
		mMotionOffsetY = mCurOffsetY;
		
		mOffsetX = 0F;
		mOffsetY = 0F;
		
		mScales[0] = mCurScale;
		
		mCurPointMidd.set(mWorldWidth / 2.0f, mWorldHeight / 2.0f);
	}
	
	/**
	 * uninitial.
	 */
	protected void uninitialInnerData() {
		mWorldWidth = -1;
		mImageHeight = -1;
		mScales = null;
	}
	
	/**
	 * 
	 * @param scale
	 */
	private void initOffsetAnimation(float scale) {
		float w = mImageWidth * scale;
		float h = mImageHeight * scale;
		
		mDefOffsetX = (mWorldWidth - mImageWidth * scale) / 2f;
		mDefOffsetY = (mWorldHeight - mImageHeight * scale) / 2f;
		
		mAnimationOffsetX = mMotionOffsetX;
		mAnimationOffsetY = mMotionOffsetY;
		
		// is smaller than world.
		if (w <= mWorldWidth + PRECISION && h <= mWorldHeight + PRECISION) {
			//animation center 
			mStepX = -(mMotionOffsetX - mDefOffsetX) / MAX_STEP;
			mStepY = -(mMotionOffsetY - mDefOffsetY) / MAX_STEP;
			mMode = MODE.ANIMATION;
			
			mMotionOffsetX = mDefOffsetX;
			mMotionOffsetY = mDefOffsetY;
			mOffsetX = 0F;
			mOffsetY = 0F;
		} else {
			//aside. horizontal and vertical. or in center.
			if (w > mWorldWidth) {
				if (mMotionOffsetX > 0) {
					//animation left 
					mStepX = -(mMotionOffsetX - 0) / MAX_STEP;
					mMode = MODE.ANIMATION;
					
					mMotionOffsetX = 0;
					mOffsetX = -mDefOffsetX;
				} else if (mMotionOffsetX < mWorldWidth - w) {
					//animation right.
					mStepX = -(mMotionOffsetX - (mWorldWidth - w)) / MAX_STEP;
					mMode = MODE.ANIMATION;
					
					mMotionOffsetX = mWorldWidth - w;
					mOffsetX = mDefOffsetX;
				} else {
					//is normal way
				}
			} else {
				//animation center 
				mStepX = -(mMotionOffsetX - (mDefOffsetX)) / MAX_STEP;
				mMode = MODE.ANIMATION;
				
				mMotionOffsetX = mDefOffsetX;
				mOffsetX = 0F;
			}
			if (h > mWorldHeight) {
				if (mMotionOffsetY > 0) {
					//animation top 
					mStepY = -(mMotionOffsetY - 0) / MAX_STEP;
					mMode = MODE.ANIMATION;
					
					mMotionOffsetY = 0;
					mOffsetY = -mDefOffsetY;
				} else if (mMotionOffsetY < mWorldHeight - h) {
					//animation bottom.
					mStepY = -(mMotionOffsetY - (mWorldHeight - h)) / MAX_STEP;
					mMode = MODE.ANIMATION;
					
					mMotionOffsetY = mWorldHeight - h;
					mOffsetY = mDefOffsetY;
				} else {
					//is normal way
				}
			} else {
				//animation center.
				mStepY = -(mMotionOffsetY - mDefOffsetY) / MAX_STEP;
				mMode = MODE.ANIMATION;
				
				mMotionOffsetY = mDefOffsetY;
				mOffsetY = 0;
			}
		} 
		mAnimationStartX = mMotionOffsetX;
		mAnimationStartY = mMotionOffsetY;
	}
	
	/**
	 * check  limit
	 * @param scale
	 * @return
	 */
	private STATUS checkLimitScaleStatus(float scale) {
		if (scale > MAX_SCALE && scale - MAX_SCALE > PRECISION) {
			return  STATUS.DECREASE;
		} else if (scale < MIN_SCALE && MAX_SCALE - scale > PRECISION) {
			return STATUS.INCREASE;
		} else {
			return STATUS.IDEL;
		}
	}
	
	/**
	 *  judge DECREASE OR INCREASE
	 * @param start
	 * @param end
	 * @return
	 */
	private STATUS checkStatus(float start, float end) {
		if (Math.abs(start - end) < PRECISION) {
			return STATUS.IDEL;
		} else if (start > end) {
			return  STATUS.DECREASE;
		} else {
			return STATUS.INCREASE;
		}
	}
	
	/**
	 * scale animation.
	 * @param start
	 * @param end
	 */
	private void initScaleAnimation(float start, float end) {
		mAnimationScale = start;
		//animation scale down
		mStepScale = (end - start) / MAX_STEP;
		mMode = MODE.ANIMATION;
		mCurScale = end;		
	}
	
	/**
	 * fix offset 
	 * @param start
	 * @param end
	 */
	private void fixScaleOffsetAnimation(float start, float end) {
		// offset calculate.
		mMotionOffsetX -= mImageWidth * (end - start) / 2f;
		mMotionOffsetY -= mImageHeight * (end - start) / 2f;
	}
	
	/**
	 * do scale
	 * @return
	 */
	private boolean calculateScale() {
		boolean finshScale = true;
		if (mScaleStatus == STATUS.DECREASE) {
			mAnimationScale += mStepScale;
			if (mAnimationScale <= mCurScale){
				mAnimationScale = mCurScale;
			} else {
				finshScale = false;
			}
		} else if (mScaleStatus == STATUS.INCREASE) {
			mAnimationScale += mStepScale;
			if (mAnimationScale >= mCurScale) {
				mAnimationScale = mCurScale;
			} else {
				finshScale = false;
			}
		}
		return finshScale;
	}
	
	/**
	 * offset x
	 * @return
	 */
	private boolean calculateOffsetX() {
		boolean finsh = true;
		if (mOffsetXStatus == STATUS.DECREASE) {
			mAnimationOffsetX += mStepX; 
			if (mAnimationOffsetX <= mMotionOffsetX) {
				mAnimationOffsetX = mMotionOffsetX;
			} else {
				finsh = false;
			}
		} else if (mOffsetXStatus == STATUS.INCREASE) {
			mAnimationOffsetX += mStepX; 
			if (mAnimationOffsetX >= mMotionOffsetX) {
				mAnimationOffsetX = mMotionOffsetX;
			} else {
				finsh = false;
			}
		}
		return finsh;
	}
	
	/**
	 * offset y
	 * @return
	 */
	private boolean calculateOffsetY() {
		boolean finsh = true;
		if (mOffsetYStatus == STATUS.DECREASE) {
			mAnimationOffsetY += mStepY; 
			if (mAnimationOffsetY <= mMotionOffsetY) {
				mAnimationOffsetY = mMotionOffsetY;
			} else {
				finsh = false;
			}
		} else if (mOffsetYStatus == STATUS.INCREASE) {
			mAnimationOffsetY += mStepY; 
			if (mAnimationOffsetY >= mMotionOffsetY) {
				mAnimationOffsetY = mMotionOffsetY;
			} else {
				finsh = false;
			}
		}
		return finsh;
	}
	
	/**
	 * check double click.
	 * @see GestureDetector.
	 * 
	 * @param firstDown
	 * @param firstUp
	 * @param secondDown
	 * @return
	 */
	private boolean isConsideredDoubleTap(MotionEvent firstDown,  
	        MotionEvent firstUp, MotionEvent secondDown) {  
	    if (secondDown.getEventTime() - firstUp.getEventTime() > MAX_DOUBLE_TAP_TIME) {  
	        return false;  
	    }  
	    int deltaX = (int) firstUp.getX() - (int) secondDown.getX();  
	    int deltaY = (int) firstUp.getY() - (int) secondDown.getY();  
	    return deltaX * deltaX + deltaY * deltaY < MAX_DISTANCE_MOVE * MAX_DISTANCE_MOVE;  
	}  
	
	/**
	 * 
	 */
	private void initDoubleClick() {
		float fitin = Math.min(mWorldWidth / mImageWidth, mWorldHeight / mImageHeight);
		if (Math.abs(fitin - MIN_SCALE) < PRECISION || 
			Math.abs(fitin - NORMAL_SCALE) < PRECISION ||
			Math.abs(fitin - MAX_SCALE) < PRECISION) {
			if (Math.abs(MIN_SCALE - NORMAL_SCALE) < PRECISION || 
				Math.abs(MAX_SCALE - NORMAL_SCALE) < PRECISION) {
				mScales = new float[] {
					1.0F, MIN_SCALE, MAX_SCALE
				};
			} else {
				mScales = new float[] {
					1.0F, MIN_SCALE, NORMAL_SCALE, MAX_SCALE
				};
			}
		} else if (fitin < MIN_SCALE) {
			MIN_SCALE = fitin;
			if (Math.abs(MIN_SCALE - NORMAL_SCALE) < PRECISION || 
				Math.abs(MAX_SCALE - NORMAL_SCALE) < PRECISION) {
				mScales = new float[] {
					1.0F, MIN_SCALE, MAX_SCALE
				};
			} else {
				mScales = new float[] {
					1.0F, MIN_SCALE, NORMAL_SCALE, MAX_SCALE
				};
			}
		} else if (fitin > MAX_SCALE) {
			MAX_SCALE = fitin;
			if (Math.abs(MIN_SCALE - NORMAL_SCALE) < PRECISION || 
				Math.abs(MAX_SCALE - NORMAL_SCALE) < PRECISION) {
				mScales = new float[] {
					1.0F, MIN_SCALE, MAX_SCALE
				};
			} else {
				mScales = new float[] {
					1.0F, MIN_SCALE, NORMAL_SCALE, MAX_SCALE
				};
			}
		} else {
			if (Math.abs(MIN_SCALE - NORMAL_SCALE) < PRECISION || 
				Math.abs(MAX_SCALE - NORMAL_SCALE) < PRECISION) {
				mScales = new float[] {
					1.0F, MIN_SCALE, MAX_SCALE, fitin
				};
			} else {
				mScales = new float[] {
					1.0F, MIN_SCALE, NORMAL_SCALE, MAX_SCALE, fitin
				};
			}
		}
	}
	
	/**
	 * double tap action
	 */
	protected void doDoubleClick() {
		
		mCurLevel++;
		if (mCurLevel >= mScales.length) {
			mCurLevel = 1;
		}
		scaleTo(mScales[mCurLevel]);
	}
	
	/**
	 * 
	 * @param event
	 */
	protected void doTouchDown(MotionEvent event) {
		mCurrentDownEvent = MotionEvent.obtain(event);  
		
		mCurPointDown.set(event.getX(0), event.getY(0));
		mDownOffsetX = mOffsetX;
		mDownOffsetY = mOffsetY;
		mCurOffsetX = mMotionOffsetX;
		mCurOffsetY = mMotionOffsetY;
		mMode = MODE.PRE_DRAG;
	}
	
	/**
	 * 
	 * @param event
	 */
	protected void doScalePre(MotionEvent event) {
		mPreDistance = getDistance(event);
		mCurPointMidd.set(super.mListener.getCenterPoint());
		mCurLevel = 0;
		mScales[0] = mCurScale;
		mMode = MODE.SCALE;
	}
	
	/**
	 * 
	 * @param event
	 */
	protected void doScale(MotionEvent event) {
		mCurScale = mScales[0] * getDistance(event) / mPreDistance;

		mCurScale = Math.min(LIMIT_SCALE_MAX, mCurScale);
		mCurScale = Math.max(LIMIT_SCALE_MIN, mCurScale);
		super.mListener.invalidate();
	}
	
	protected void doScaleOver() {
		//TODO check if current scale is out of [MIN, MAX]
		mScaleStatus = checkLimitScaleStatus(mCurScale);
		//TODO set scale animation from current scale to [MIN, MAX]
		if (mScaleStatus == STATUS.DECREASE) {
			initScaleAnimation(mCurScale, MAX_SCALE);
		} else if (mScaleStatus == STATUS.INCREASE) {
			initScaleAnimation(mCurScale, MIN_SCALE);
		} else {
			mAnimationScale = mCurScale;
		}
		//TODO fix offset
		fixScaleOffsetAnimation(mScales[0], mCurScale);
		
		initOffsetAnimation(mCurScale);
		mOffsetXStatus = checkStatus(mAnimationOffsetX, mMotionOffsetX);
		mOffsetYStatus = checkStatus(mAnimationOffsetY, mMotionOffsetY);

		if (mMode != MODE.ANIMATION) {
			mMode = MODE.IDEL;
		}
	}
	
	/**
	 * 
	 * @param event
	 */
	protected void doDrag(MotionEvent event) {
		mMotionOffsetX = mCurOffsetX + event.getX(0) - mCurPointDown.x;
		mMotionOffsetY = mCurOffsetY + event.getY(0) - mCurPointDown.y;
		
		mOffsetX = mDownOffsetX + event.getX(0) - mCurPointDown.x;
		mOffsetY = mDownOffsetY + event.getY(0) - mCurPointDown.y;
		
		super.mListener.invalidate();
	}
	
	/**
	 * 
	 */
	protected void doDragOver() {
		mScaleStatus = STATUS.IDEL;
		mAnimationScale = mCurScale;
		
		initOffsetAnimation(mCurScale);
		mOffsetXStatus = checkStatus(mAnimationOffsetX, mMotionOffsetX);
		mOffsetYStatus = checkStatus(mAnimationOffsetY, mMotionOffsetY);

		if (mMode != MODE.ANIMATION) {
			mMode = MODE.IDEL;
		}
	}
	/**
	 * scale to with animation.
	 * @param scale
	 */
	public void scaleTo(float scale) {
		//TODO check if current scale is scale down or up.
		this.mScaleStatus = checkStatus(mCurScale, scale);
		//TODO set scale animation from current scale to target
		if (this.mScaleStatus != STATUS.IDEL) {
			initScaleAnimation(mCurScale, scale);
		} else {
			mAnimationScale = mCurScale;
		}
		//TODO fix offset
		fixScaleOffsetAnimation(mAnimationScale, mCurScale);
		//TODO check offset.
		initOffsetAnimation(mCurScale);
		mOffsetXStatus = checkStatus(mAnimationOffsetX, mMotionOffsetX);
		mOffsetYStatus = checkStatus(mAnimationOffsetY, mMotionOffsetY);
		
		super.mListener.invalidate();
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			if (mMode == MODE.IDEL) {
				//double tap
				if (mPreviousUpEvent != null  && mCurrentDownEvent != null && 
					isConsideredDoubleTap(mCurrentDownEvent, mPreviousUpEvent, event)) {  
					doDoubleClick();
	            } else {
	            	doTouchDown(event);
	            }
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			if (mMode != MODE.ANIMATION && event.getPointerCount() == 2) {
				doScalePre(event);		
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
				doDrag(event);
			} else if (mMode == MODE.SCALE && event.getPointerCount() == 2) {
				doScale(event);
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (mMode == MODE.SCALE) {
				mMode = MODE.IDEL;
			} else if (mMode == MODE.DRAG) {
				doDragOver();
			} else if (mMode == MODE.PRE_DRAG) {
				mMode = MODE.IDEL;
			}
			mPreviousUpEvent = MotionEvent.obtain(event);
			super.mListener.invalidate();
			break;
		case MotionEvent.ACTION_POINTER_UP:
			if (mMode == MODE.SCALE) {
				doScaleOver();
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

	@Override
	public void initialize(float imageWidth, float imageHeight, float worldWidth, float worldHeight) {
		// TODO Auto-generated method stub
		mWorldWidth = worldWidth;
		mWorldHeight = worldHeight;
		
		mImageWidth = imageWidth;
		mImageHeight = imageHeight;
		
		mDefOffsetX = (mWorldWidth - mImageWidth * mCurScale) / 2f;
		mDefOffsetY = (mWorldHeight - mImageHeight * mCurScale) / 2f;
		
		initDoubleClick();
		
		LIMIT_SCALE_MIN = Math.min(MIN_SCALE - 0.25F, 0.125F);
		LIMIT_SCALE_MAX = MAX_SCALE + 0.5F;
		
		initialInnerData();
	}
	
	public void setDefaultScale(float scale) {
		mCurScale = scale;
	}
	
	public boolean setDefaultLimit(float max, float min) {
		if (max >= min && min > super.PRECISION) {
			MIN_SCALE = min;
			MAX_SCALE = max;
			return true;
		}
		return false;
	}
}
