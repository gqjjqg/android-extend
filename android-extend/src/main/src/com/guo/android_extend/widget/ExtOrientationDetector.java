package com.guo.android_extend.widget;

import android.content.Context;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * for orientation event detector.
 */

public class ExtOrientationDetector extends OrientationEventListener {
	private final String TAG = this.getClass().toString();

	public final static int ROTATE_POSITIVE = 0;
	public final static int ROTATE_NEGATIVE = 1;
	public final static int ROTATE_UNCHANGE = 2;
	public final static int ROTATE_FORCE_REDO = 3;
	
	private Display mDisplay;
	private Context mContext;
	private int mOrientation;
	private int mCompensation;
	private int mFixedDegree;
	private int mDegree;
	private int mMinDegree;
	private int mMaxDegree;
	private List<OnOrientationListener> mObjectes;

	/**
	 * divider for orientation.
	 */
	private int[] mDivider;
	private List<Integer> mWorkingDegree;
	
	/**
	 * the first area degree.
	 * 0 90 180 270
	 */
	private final static int ORIENTATION_START = 0;
	
	/**
	 * how many area in the circle.
	 */
	private final static int ORIENTATION_DIVIDE = 4;
	
	/**
	 * every area offset.
	 */
	private int mOffset;
	
	/**
	 * every area half offset.
	 */
	private int mHysteresis;
	/**
	 * @author Guo
	 * listener for orientation.
	 */
	public interface OnOrientationListener {
		/**
		 * @param degree degree
		 * @param offset offset
		 * @param flag flag
		 * @return true is visible and done.
		 */
		boolean OnOrientationChanged(int degree, int offset, int flag);
		/**
		 * @return the object's current degree.
		 */
		int	getCurrentOrientationDegree();
	}
	
	public ExtOrientationDetector(Context context, int rate) {
		super(context, rate);
		// TODO Auto-generated constructor stub
		PreCreate(context);
	}

	public ExtOrientationDetector(Context context, int first_degree, int count) {
		super(context);
		// TODO Auto-generated constructor stub
		PreCreate(context);
		config(first_degree, count);
	}

	public ExtOrientationDetector(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		PreCreate(context);
		config(ORIENTATION_START, ORIENTATION_DIVIDE);
	}

	private void PreCreate(Context context) {
		mContext = context;
		mObjectes = new ArrayList<OnOrientationListener>();
		mOrientation = ORIENTATION_UNKNOWN;
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		mDisplay = wm.getDefaultDisplay();
		mFixedDegree = getFixedRotation();
		Log.i(TAG, "FixedDegree = " + mFixedDegree);
	}

	private void def_config() {
		/**
		 * create divider by config.
		 */
		mDegree = ORIENTATION_START;
		mOffset = 360 / ORIENTATION_DIVIDE;
		mHysteresis = mOffset / 2;
		mDivider = new int[ORIENTATION_DIVIDE];
		mWorkingDegree = new ArrayList<Integer>();
		mDivider[0] = ORIENTATION_START + mHysteresis;
		mWorkingDegree.add(ORIENTATION_START);
		for (int i = 1; i < mDivider.length; i++) {
			mDivider[i] = mDivider[i - 1] + mOffset;
			mWorkingDegree.add(mDivider[i] - mHysteresis);
		}

		mMinDegree = ORIENTATION_START;
		mMaxDegree = mDivider[mDivider.length - 1] - mHysteresis;
	}

	private void config(int first_degree, int count) {

		mDegree = first_degree;
		mOffset = 360 / count;
		mHysteresis = mOffset / 2;
		mDivider = new int[count];
		mWorkingDegree = new ArrayList<Integer>();
		mDivider[0] = first_degree + mHysteresis;
		mWorkingDegree.add(first_degree);
		for (int i = 1; i < mDivider.length; i++) {
			mDivider[i] = mDivider[i - 1] + mOffset;
			mWorkingDegree.add(mDivider[i] - mHysteresis);
		}

		mMinDegree = first_degree;
		mMaxDegree = mDivider[mDivider.length - 1] - mHysteresis;
	}

	public List<Integer> getDegrees() {
		return mWorkingDegree;
	}

	/**
	 *
	 * @param listener
	 * @param degree
	 * @param flag
	 * @return
	 */
	private boolean forceOrientationChanged(OnOrientationListener listener, int degree, int flag) {
		int param = flag;
		int offset = mOffset;
		if (flag == ROTATE_FORCE_REDO) {
			param = getRotateFlag(listener.getCurrentOrientationDegree(), degree);
		}
		// 0 - 180 should rotate 180degree.otherwise 90 degree.
		offset = Math.abs(listener.getCurrentOrientationDegree() - degree);
		offset = offset > 180 ? (360 - offset) : offset;
		if (param == ROTATE_NEGATIVE) {
			offset = -offset;
		} else if (param == ROTATE_POSITIVE) {
			//offset = offset;
		} else {
			return true;
		}

		return listener.OnOrientationChanged(degree, offset, ROTATE_FORCE_REDO);
	}

	/**
	 * @param degree
	 * @param flag
	 */
	private void forceOrientationChanged(int degree, int flag) {
		synchronized(mObjectes) {
			Iterator<OnOrientationListener> it = mObjectes.iterator();
			int param = flag;
			int offset = mOffset;
			while (it.hasNext()) {
				OnOrientationListener listener = it.next();
				if (flag == ExtOrientationDetector.ROTATE_FORCE_REDO) {
					param = getRotateFlag(listener.getCurrentOrientationDegree(), degree);
				}
				// 0 - 180 should rotate 180degree.otherwise 90 degree.
				offset = Math.abs(listener.getCurrentOrientationDegree() - degree);
				offset = offset > 180 ? (360 - offset) : offset;
				if (param == ExtOrientationDetector.ROTATE_NEGATIVE) {
					offset = -offset;
				} else if (param == ExtOrientationDetector.ROTATE_POSITIVE) {
					//offset = offset;
				} else {
					continue;
				}

				if (!listener.OnOrientationChanged(degree, offset, param)) {
					//need remove from list.
					it.remove();
				}
			}
			mDegree = degree;
		}
	}
	
	@Override
	public void onOrientationChanged(int orientation) {
		// TODO Auto-generated method stub
		if (orientation == ORIENTATION_UNKNOWN) {
			return;
		}
		mOrientation = roundOrientation(orientation, mOrientation);
		if (mCompensation != mOrientation) {
			mCompensation = mOrientation;
			int degree = mCompensation % 360;
			int flag = getRotateFlag(mDegree, degree);
			
			forceOrientationChanged(degree, flag);
		}
	}
	
	/**
	 * use degree and flag to do again.
	 */
	public void forceOrientationChanged() {
		forceOrientationChanged(mDegree, ROTATE_FORCE_REDO);
	}
	
	/**
	 * 
	 * @return current degree;
	 */
	public int getCurrentOrientationDegree() {
		return mDegree;
	}
	
	/**
	 * add receiver.
	 * @param obj the orientation obj
	 * @return true if success.
	 */
	public boolean addReceiver(OnOrientationListener obj) {
		synchronized (mObjectes) {
			if (!mObjectes.contains(obj)) {
				forceOrientationChanged(obj, mDegree, ROTATE_FORCE_REDO);
				return mObjectes.add(obj);
			}
		}
		return false;
	}
	
	/**
	 * remove receiver.
	 * @param obj orientation obj
	 * @return success is true.
	 */
	public boolean removeReceiver(OnOrientationListener obj) {
		synchronized (mObjectes) {
			if (mObjectes.contains(obj)) {
				return mObjectes.remove(obj);
			}
		}
		return false;
	}
	
	/**
	 * clear all receiver.
	 */
	public void clearReceiver() {
		synchronized (mObjectes) {
			mObjectes.clear();
		}
	}
	
	/**
	 * @param degree
	 * @return
	 */
	private int getRotateFlag(int curdegree, int degree) {
		if (curdegree > degree
				&& !(curdegree == mMaxDegree && degree == mMinDegree)
				|| (curdegree == mMinDegree && degree == mMaxDegree)) {
			return ROTATE_NEGATIVE;
		} else if (curdegree < degree
				&& !(curdegree == mMinDegree && degree == mMaxDegree)
				|| (curdegree == mMaxDegree && degree == mMinDegree)) {
			return ROTATE_POSITIVE;
		}
		return ROTATE_UNCHANGE;
	}
	
	/**
	 * get default fixed degree.
	 * @return
	 */
	private int getFixedRotation() {
		/*switch (mDisplay.getRotation()) {
        case Surface.ROTATION_0: return 0;
        case Surface.ROTATION_90: return 90;
        case Surface.ROTATION_180: return 180;
        case Surface.ROTATION_270: return 270;
	    }*/
		switch (mDisplay.getRotation()) {
        case Surface.ROTATION_0: return 0;
        case Surface.ROTATION_90: return 270;
        case Surface.ROTATION_180: return 180;
        case Surface.ROTATION_270: return 90;
	    }
		return 0;
	}
	
	/**
	 * calculate the degree.
	 * 4 divider:  
	 * 			1. (45, 135]	return 90
	 * 			2. (135, 225]	return 180
	 * 			3. (225, 315]	return 270
	 * 			4. (315, 45]	return 0
	 * 
	 * example: degree = 60 in area 1, will return 90.
	 * 
	 * @param cur
	 * @param pre
	 * @return
	 */
	private int roundOrientation(int cur, int pre) {
		cur -= mFixedDegree;
		if (cur < 0) {
			cur += 360;
		}
		if (pre != ORIENTATION_UNKNOWN) {
			int dist = Math.abs(cur - pre);
            dist = Math.min( dist, 360 - dist );
            if ( dist < mHysteresis ) {
            	return pre;
            }
		}
		
		for (int i = 0; i < mDivider.length; i++) {
			if (cur <= mDivider[i]) {
				return mDivider[i] - mHysteresis;
			}
		}
		return mDivider[0] - mHysteresis;
    }
	
}
