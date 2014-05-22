package com.guo.android_extend.controller;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;

public abstract class AbstractController implements OnTouchListener {

	protected ControllerListener mListener;
	
	protected PointF mCurPointDown;
	protected PointF mCurPointMidd;

	protected float mPreDistance;
	protected float mPreDegree;
	
	protected final float PRECISION = 0.001F;
	
	public interface ControllerListener {
		public void 	invalidate();
		public PointF 	getCenterPoint();
	}
	
	public AbstractController(ControllerListener mListener) {
		// TODO Auto-generated constructor stub
		this.mListener = mListener;
		mCurPointDown = new PointF();
		mCurPointMidd = new PointF();
		mPreDistance = 0F;
		mPreDegree = 0F;
	}
	
	protected abstract void beforeDraw(Canvas canvas);
	
	protected abstract void afterDraw(Canvas canvas);
	
	/**
	 * @param event
	 * @return distance between point 0 and point 1 
	 */
	protected float getDistance(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	/**
	 * @param event
	 * @return get rotation between point 0 and point 1 
	 */
	protected float getRotation(MotionEvent event) {
		double radians = Math.atan2((event.getY(0) - event.getY(1)), (event.getX(0) - event.getX(1)));
		return (float) Math.toDegrees(radians);
	}
}
