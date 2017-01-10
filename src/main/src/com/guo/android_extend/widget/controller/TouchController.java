package com.guo.android_extend.widget.controller;

import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by gqj3375 on 2017/1/10.
 */

public class TouchController  {

	public interface OnDispatchTouchEventListener {
		public void onDispatchTouchEvent(View v, MotionEvent ev);
	}

	/**
	 * @param A
	 * @param B center point
	 * @param degree
	 * @return
	 */
	private PointF rotatePoint(PointF A, PointF B, float degree) {
		float radian = (float) Math.toRadians(degree);
		float cos = (float) Math.cos(radian);
		float sin = (float) Math.sin(radian);
		float x = (float) ((A.x - B.x)* cos +(A.y - B.y) * sin + B.x);
		float y = (float) (-(A.x - B.x)* sin + (A.y - B.y) * cos + B.y);
		return new PointF(x, y);
	}

	public MotionEvent obtainTouchEvent(MotionEvent ev, int view_w, int view_h, int cur_degree) {
		PointF newPoint = rotatePoint(new PointF(ev.getX(), ev.getY()),
				new PointF(view_w / 2F, view_h / 2F), -cur_degree);
		MotionEvent newEvent = MotionEvent.obtain(ev.getDownTime(),
				ev.getEventTime(), ev.getAction(), newPoint.x, newPoint.y,
				ev.getPressure(), ev.getSize(), ev.getMetaState(),
				ev.getXPrecision(), ev.getYPrecision(), ev.getDeviceId(),
				ev.getEdgeFlags());
		return newEvent;
	}
}
