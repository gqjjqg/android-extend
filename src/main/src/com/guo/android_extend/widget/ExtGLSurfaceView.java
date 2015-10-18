package com.guo.android_extend.widget;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * @Note create by gqjjqg,.
 *    easy to use opengl surface..
 */

public class ExtGLSurfaceView extends GLSurfaceView {
	private final String TAG = this.getClass().getSimpleName();

	private double mAspectRatio;
	private boolean mFitMaxArea;

	public ExtGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		onCreate();
	}

	public ExtGLSurfaceView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		onCreate();
	}

	private void onCreate() {
		mAspectRatio = 0.0;
	}

	public void setAutoFitMax(boolean enable) {
		mFitMaxArea = enable;
	}

	public void setAspectRatio(double ratio) {
		if (mAspectRatio != ratio) {
			mAspectRatio = ratio;
			requestLayout();
		}
	}

	public void setAspectRatio(int width, int height) {
		double ratio = ((double)width / (double)height);
        if (mAspectRatio != ratio) {
            mAspectRatio = ratio;
            requestLayout();
        }
    }
	
	/* (non-Javadoc)
	 * @see android.view.SurfaceView#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		// TODO Auto-generated method stub
		int width = MeasureSpec.getSize(widthSpec);
        int height = MeasureSpec.getSize(heightSpec);

        if (mAspectRatio != 0) {
			int w1 = width, h1 = height;
	        if (w1 > h1 * mAspectRatio) {
	            w1 = (int) (h1 * mAspectRatio + .5);
	        } else {
				h1 = (int) (w1 / mAspectRatio + .5);
	        }
			if (mFitMaxArea) {
				int w2 = width, h2 = height;
				double ratio = 1.0 / mAspectRatio;
				if (w2 > h2 * ratio) {
					w2 = (int) (h2 * ratio + .5);
				} else {
					h2 = (int) (w2 / ratio + .5);
				}
				if (w1 * h1 > w2 * h2) {
					width = w1;
					height = h1;
				} else {
					width = w2;
					height = h2;
				}
			} else {
				width = w1;
				height = h1;
			}
        }

		widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
		heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        super.onMeasure(widthSpec, heightSpec);
	}

	
}
