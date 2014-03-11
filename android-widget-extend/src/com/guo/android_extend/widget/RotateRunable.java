package com.guo.android_extend.widget;

import android.os.Build;
import android.view.View;
import android.view.animation.Animation;

/**
 * @author qijiang.guo
 * if member:bReLayout is false. 
 * you will need to rotate the view in method onDraw.
 * otherwise, the RotateRunable will rotate the layout.
 * @see RotatableImageButton
 * @see SwitchImageButton
 */
public class RotateRunable implements Runnable {
	
	/**
	 * rotate animation.
	 */
	private Animation mAnimation;
	
	/**
	 * target view.
	 */
	private View mContextView;
	
	/**
	 * target rotate degree.
	 */
	private int mDegree;
	
	/**
	 * if layout changed.
	 */
	private boolean bReLayout;
	
	public RotateRunable(Animation animation, View v, int degree, boolean relayout) {
		super();
		// TODO Auto-generated constructor stub
		mAnimation = animation;
		mContextView = v;
		mDegree = degree;
		bReLayout = relayout;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (bReLayout) {
			// TODO : API Level 11+ will support rotate layout.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
				mContextView.setVisibility(View.GONE);
				mContextView.setPivotX(mContextView.getWidth() / 2);
				mContextView.setPivotY(mContextView.getHeight() / 2);
				mContextView.setRotation(-mDegree);
				mContextView.setVisibility(View.VISIBLE);
			}
		}
		mContextView.startAnimation(mAnimation);
	}
	
}
