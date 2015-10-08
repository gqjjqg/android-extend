package com.guo.android_extend.widget.controller;

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
	
	public abstract void initialize(float imageWidth, float imageHeight, float worldWidth, float worldHeight);
		
	public abstract void beforeDraw(Canvas canvas);
	
	public abstract void afterDraw(Canvas canvas);
	
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
	
	/**
	 * @param A
	 * @param B
	 * @return get rotation between point 0 and point 1 
	 */
	protected float getRotation(PointF A, PointF B) {
		double radians = Math.atan2((A.y - B.y), (A.x - B.x));
		return (float) Math.toDegrees(radians);
	}
	
	/**
	 * @param VA
	 * @param VB
	 * @return
	 */
	protected float getRotation(PointF A, PointF B, PointF Center) {
		PointF VCA = new PointF(A.x - Center.x, A.y - Center.y);
		PointF VCB = new PointF(B.x - Center.x, B.y - Center.y);
		float AXB = VCA.x * VCB.y - VCA.y * VCB.x;
		double ABS_VA = Math.hypot(VCA.x, VCA.y);
		double ABS_VB = Math.hypot(VCB.x, VCB.y);
		double radians = Math.asin( AXB / (ABS_VA * ABS_VB) );
		return (float) Math.toDegrees(radians);
	}
	
}
