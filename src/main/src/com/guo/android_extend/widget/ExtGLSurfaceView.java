package com.guo.android_extend.widget;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class ExtGLSurfaceView extends GLSurfaceView {
	private final String TAG = this.getClass().getSimpleName();

	private double mAspectRatio;
	
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
	
	public void setAspectRatio(double ratio) {
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
	        if (width > height * mAspectRatio) {
	            width = (int) (height * mAspectRatio + .5);
	        } else {
	            height = (int) (width / mAspectRatio + .5);
	        }
        }

		widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
		heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        super.onMeasure(widthSpec, heightSpec);
	}

	
}
