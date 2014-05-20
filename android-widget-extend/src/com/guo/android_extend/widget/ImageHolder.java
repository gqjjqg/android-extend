package com.guo.android_extend.widget;

import com.guo.android_extend.AbstractController.ControllerListener;
import com.guo.android_extend.ImageViewController;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ImageHolder extends ImageView implements ControllerListener {
	
	private ImageViewController mImageCtrl;
	
	public ImageHolder(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		preCreate();
	}

	public ImageHolder(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		preCreate();
	}

	public ImageHolder(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		preCreate();
	}
	
	protected void preCreate() {
		mImageCtrl = new ImageViewController(this.getContext(), this);
	}
	
	/* (non-Javadoc)
	 * @see android.widget.ImageView#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		mImageCtrl.beforeDraw(canvas);
		super.onDraw(canvas);
		mImageCtrl.afterDraw(canvas);
	}
	
	/* (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (!mImageCtrl.onTouchEvent(event)) {
			return super.onTouchEvent(event);
		}
		return true;
	}

	@Override
	public PointF getCenterPoint() {
		// TODO Auto-generated method stub
		RectF res = new RectF();
		res.set(this.getDrawable().getBounds());
		this.getImageMatrix().mapRect(res);
		return new PointF(res.centerX(), res.centerY());
	}

}
