package com.guo.android_extend.widget;

import android.view.View;
import android.view.animation.Animation;

/**
 * @author qijiang.guo
 * @see com.guo.android_extend.widget.ExtImageButton
 */
public class RotateRunable implements Runnable {
	/**
	 * DEFAULT ANIMATION TIME.
	 */
	static final int ANIMATION_TIME = 300;

	/**
	 * rotate animation.
	 */
	private Animation mAnimation;
	
	/**
	 * target view.
	 */
	private View mContextView;
	
	/**
	 * @deprecated param relayout not working.
	 * 
	 * @param animation
	 * @param v
	 * @param degree
	 * @param relayout not working.
	 */
	public RotateRunable(Animation animation, View v, int degree, boolean relayout) {
		super();
		// TODO Auto-generated constructor stub
		mAnimation = animation;
		mContextView = v;
	}
	
	/**
	 * @param animation
	 * @param v
	 * @param degree
	 */
	public RotateRunable(Animation animation, View v, int degree) {
		super();
		// TODO Auto-generated constructor stub
		mAnimation = animation;
		mContextView = v;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		mContextView.startAnimation(mAnimation);
	}
	
}
