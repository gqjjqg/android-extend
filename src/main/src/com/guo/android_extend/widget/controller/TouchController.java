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
		MotionEvent newEvent;
		if (false) {	// suport one touch
			PointF newPoint = rotatePoint(new PointF(ev.getX(), ev.getY()),
					new PointF(view_w / 2F, view_h / 2F), -cur_degree);
			newEvent = MotionEvent.obtain(ev.getDownTime(),
					ev.getEventTime(), ev.getAction(), newPoint.x, newPoint.y,
					ev.getPressure(), ev.getSize(), ev.getMetaState(),
					ev.getXPrecision(), ev.getYPrecision(), ev.getDeviceId(),
					ev.getEdgeFlags());
		} else {
			MotionEvent.PointerProperties[] preo = new MotionEvent.PointerProperties[2];
			MotionEvent.PointerCoords[] coor = new MotionEvent.PointerCoords[2];
			for (int i = 0; i < ev.getPointerCount(); i++) {
				preo[i] = new MotionEvent.PointerProperties();
				coor[i] = new MotionEvent.PointerCoords();
				ev.getPointerProperties(i, preo[i]);
				ev.getPointerCoords(i, coor[i]);
				PointF newPoint = rotatePoint(new PointF(coor[i].x, coor[i].y),
						new PointF(view_w / 2F, view_h / 2F), -cur_degree);
				coor[i].x = newPoint.x;
				coor[i].y = newPoint.y;
			}
			newEvent = MotionEvent.obtain(ev.getDownTime(), ev.getEventTime(), ev.getAction(), ev.getPointerCount(), preo,
					coor, ev.getMetaState(), ev.getButtonState(), ev.getXPrecision(), ev.getYPrecision(), ev.getDeviceId(),
					ev.getEdgeFlags(), ev.getSource(), ev.getFlags());
		}

		return newEvent;
	}
}
