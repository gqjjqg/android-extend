package com.guo.android_extend.effect;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import com.guo.android_extend.widget.HorizontalListView;

public abstract class HLVEffectAdapter extends BaseAdapter implements HorizontalListView.OnItemScrollListener{
	private final String TAG = this.getClass().toString();
	/**
	 * animation during time.
	 */
	private final int ANIMATION_TIME = 200;
	private final int ANIMATION_DELETE_TIME = 350;
	
	private ACTION	mCurAction;
	private enum ACTION {
		NONE, DRAG_MOVE, DRAG_DEL_ING, DRAG_DEL_END,
		SCROLL, FLUSH,
	}
	
	public float SCAEL_PERCENT = 0.5F;
	
	/**
	 * Y OFFSET CONVERT TO X OFFSET.
	 */
	private float PRE_XY = 0.3F;
	
	private int   mMaxY;
	private int   mMinY;
	private int   mCurY;
	
	private int   mMaxX;
	private int   mMinX;
	private int   mCurX;
	
	private int   mCenterID;
	private int   mOffSetLeft;
	private int   mOffSetRight;
	
	protected Context mContext;
	protected HorizontalListView mHLV;
	
	public HLVEffectAdapter(HorizontalListView context) {
		// TODO Auto-generated constructor stub
		mHLV = context;
		mContext = mHLV.getContext();
		mCurAction = ACTION.NONE;
	}
	
	public abstract void frashViewList();
	public abstract void scaleView(View v, float percent);
	public abstract void animaView(View v, Animation ani);
	public abstract void animaClearView(View v);
	public abstract int removeView(View v);
	
	protected void dialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setMessage("remove");
		builder.setTitle("confirm");
		builder.setPositiveButton("yes", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				int id = removeView(mHLV.getChildAt(mCenterID));
				//TODO : start Y axis animation.
				endDelYAnimation(mHLV);
				mHLV.setSelection(id);
				frashViewList();
			}
			});
		builder.setNegativeButton("cancel", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				startDelAnimation(mHLV, false);
				
				}
			});
		builder.create().show();
	}
	
	@Override
	public void OnScrollCenter(AdapterView<ListAdapter> adp, View v,
			int pos, float percent) {
		// TODO Auto-generated method stub
		Log.i(TAG, "percent = " + percent+ "pos = " + pos);
		mCenterID = pos;
		/*
		 * float p = 0.72f; float p1 = p + (1f-p) * percent; float p2 = p +
		 * (1f-p)* (1.0f - percent); if (pos > 0) {
		 * mMemoListAdapter.setViewScale(adp.getChildAt(pos - 1), p); }
		 * Log.i(TAG, "v = " + v.toString());
		 * mMemoListAdapter.setViewScale(v, p1); //p1
		 * //mMemoListAdapter.setViewScale(adp.getChildAt(pos + 1), p1);//p2
		 * if (pos + 2 < adp.getChildCount()) {
		 * mMemoListAdapter.setViewScale(adp.getChildAt(pos + 2), p); }
		 */
	}

	@Override
	public void OnScrollStart(AdapterView<ListAdapter> adp) {
		// TODO Auto-generated method stub
		Log.i(TAG, "OnScrollStart mCenterID =" + mCenterID);
		//mIsDrag = true;
		mCurAction = ACTION.SCROLL;

		Animation ani = new ScaleAnimation(1.0F + SCAEL_PERCENT, 1F,
				1.0F + SCAEL_PERCENT, 1F, 
				Animation.RELATIVE_TO_SELF, 0.5F, 
				Animation.RELATIVE_TO_SELF, 0.5F);
		ani.setFillAfter(true);
		ani.setDuration(ANIMATION_TIME);
		
		View v = adp.getChildAt(mCenterID);
		scaleView(v, SCAEL_PERCENT); // p1
		animaView(v, ani);
		
		for (int j = 0; j < adp.getChildCount(); j++) {
			v = adp.getChildAt(j);
			if (j != mCenterID) {
				animaClearView(v);
			}
			//mMemoListAdapter.setClickable(v, false);
		}
	}

	@Override
	public void OnScrollEnd(AdapterView<ListAdapter> adp, int pos) {
		// TODO Auto-generated method stub
		Log.i(TAG, "OnScrollEnd pos = " + pos + "mCurAction=" + mCurAction);
		if (mCurAction == ACTION.DRAG_DEL_ING) {
			return;
		}
		if (mCurAction == ACTION.DRAG_DEL_END) {
			//TODO if current is drag delete mode. start X axis animation
			// and scroll end emulation.
			endDelXAnimation(adp);
			mCurAction = ACTION.SCROLL;
			OnScrollEnd(adp, pos);
			return ;
		}
		
		mCurAction = ACTION.NONE;
		if (pos >= adp.getChildCount()) {
			pos--;
		}
		View v = adp.getChildAt(pos);
		if (v != null) {
			Animation ani = new ScaleAnimation(SCAEL_PERCENT, 1.0f,
					SCAEL_PERCENT, 1.0F,
					Animation.RELATIVE_TO_SELF, 0.5F, 
					Animation.RELATIVE_TO_SELF, 0.5F);
			ani.setFillAfter(true);
			ani.setDuration(ANIMATION_TIME);
			
			scaleView(v, 1.0f); // p1
			animaView(v, ani);
			
			for (int i = 0; i < adp.getChildCount(); i++) {
				View v1 = adp.getChildAt(i);
				if (i != pos) {
					animaClearView(v1);
				}
				//mMemoListAdapter.setClickable(v1, true);
			}
			
			/**
			 * TODO for delete move initial.
			 */
			mMaxY = (int) (v.getTop() + (v.getHeight() * 0.4F));
			mMinY = 0;
			mCurY = 0;
			
			mMaxX = (int) (v.getWidth() * 0.4F);
			mMinX = 0;
			mCurX = 0;
			
		}
	}
	
	
	@Override
	public boolean OnDraging(AdapterView<ListAdapter> adp, float dx,
			float dy) {
		// TODO Auto-generated method stub
		int mTemp1, mTemp2;
		if ((dy <= 0 && mCurY == mMinY) || (dy >= 0 && mCurY == mMaxY) ||
				mCenterID == -1) {
			return false;
		}
		if (mCurAction != ACTION.NONE && mCurAction != ACTION.DRAG_DEL_ING) {
			return false;
		}
		mCurAction = ACTION.DRAG_DEL_ING;
		
		mTemp1 = (int) (dy * PRE_XY);
		mTemp2 = (int) (dy);
		if (mTemp1 < 0) {
			//MOVE left
			if ((mCurX + mTemp1) >= mMinX) {
				mCurX += mTemp1;
			} else {
				mTemp1 = mMinX - mCurX;
				mCurX = mMinX;
			}
		} else {
			//MOVE right
			if ((mCurX + mTemp1) <= mMaxX) {
				mCurX += mTemp1;
			} else {
				mTemp1 = mMaxX - mCurX;
				mCurX = mMaxX;
			}
		}
		
		if (mTemp2 < 0) {
			//MOVE down
			if ((mCurY + mTemp2) >= mMinY) {
				mCurY += mTemp2;
			} else {
				mTemp2 = mMinY - mCurY;
				mCurY = mMinY;
			}
		} else {
			//MOVE up
			if ((mCurY + mTemp2) <= mMaxY) {
				mCurY += mTemp2;
			} else {
				mTemp2 = mMaxY - mCurY;
				mCurY = mMaxY;
			}
		}
		
		boolean left = false;
		for (int i = 0; i < adp.getChildCount(); i++) {
			View v = adp.getChildAt(i);
			if (i == mCenterID) {
				if (Build.VERSION.SDK_INT > 10) {
					v.setTop(v.getTop() - mTemp2);
				} else {
					v.layout(v.getLeft(), v.getTop() - mTemp2, v.getRight(), v.getBottom());
				}
				left = true;
			} else if (left) {
				if (Build.VERSION.SDK_INT > 10) {
					v.setLeft(v.getLeft() - mTemp1);
					v.setRight(v.getRight() - mTemp1);
				} else {
					v.layout(v.getLeft() - mTemp1, v.getTop(), v.getRight() - mTemp1, v.getBottom());
				}
			} else {
				if (Build.VERSION.SDK_INT > 10) {
					v.setLeft(v.getLeft() + mTemp1);
					v.setRight(v.getRight() + mTemp1);
				} else {
					v.layout(v.getLeft() + mTemp1, v.getTop(), v.getRight() + mTemp1, v.getBottom());
				}
			}
		}
		return true;
	}

	@Override
	public boolean OnDragingOver(AdapterView<ListAdapter> adp) {
		// TODO Auto-generated method stub
		startDelAnimation(adp, mCurY * 2 >= mMaxY);
		return true;
	}

	/**
	 * delete memo animation.
	 * @param adp
	 * @param isOpen
	 */
	public void startDelAnimation(AdapterView<ListAdapter> adp, boolean isOpen) {
		int ani_time = ANIMATION_DELETE_TIME * mCurY / mMaxY;
		int mTemp1, mTemp2;
		
		if (isOpen) {
			//DELETE DIALOG.
			mTemp1 = mMaxX - mCurX;
			mTemp2 = mMaxY - mCurY;
			mCurY = mMaxY;
			mCurX = mMaxX;
		} else {
			//ANIMATION BACK.
			mTemp1 = mMinX - mCurX;
			mTemp2 = mMinY - mCurY;
			mCurY = mMinY;
			mCurX = mMinX;
		}
		mCurAction = ACTION.DRAG_DEL_ING;
		Animation mAniOut = new TranslateAnimation(Animation.ABSOLUTE, 0,
				Animation.ABSOLUTE, 0, Animation.ABSOLUTE, mTemp2,
				Animation.ABSOLUTE, 0 );
		mAniOut.setFillAfter(true);
		mAniOut.setDuration(ani_time);
		mAniOut.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				mCurAction = ACTION.NONE;
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		Animation mAniLeft = new TranslateAnimation(Animation.ABSOLUTE, mTemp1,
				Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
				Animation.ABSOLUTE, 0 );
		mAniLeft.setFillAfter(true);
		mAniLeft.setDuration(ani_time);
		
		Animation mAniRight = new TranslateAnimation(Animation.ABSOLUTE, -mTemp1,
				Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
				Animation.ABSOLUTE, 0 );
		mAniRight.setFillAfter(true);
		mAniRight.setDuration(ani_time);
		
		boolean left = false;
		for (int i = 0; i < adp.getChildCount(); i++) {
			View v = adp.getChildAt(i);
			if (i == mCenterID) {
				if (Build.VERSION.SDK_INT > 10) {
					v.setTop(v.getTop() - mTemp2);
				} else {
					v.layout(v.getLeft(), v.getTop() - mTemp2, v.getRight(), v.getBottom());
				}
				v.startAnimation(mAniOut);
				left = true;
			} else if (left) {
				if (Build.VERSION.SDK_INT > 10) {
					v.setLeft(v.getLeft() - mTemp1);
					v.setRight(v.getRight() - mTemp1);
				} else {
					v.layout(v.getLeft() - mTemp1, v.getTop(), v.getRight() - mTemp1, v.getBottom());
				}
				v.startAnimation(mAniLeft);
			} else {
				if (Build.VERSION.SDK_INT > 10) {
					v.setLeft(v.getLeft() + mTemp1);
					v.setRight(v.getRight() + mTemp1);
				} else {
					v.layout(v.getLeft() + mTemp1, v.getTop(), v.getRight() + mTemp1, v.getBottom());
				}
				v.startAnimation(mAniRight);
			}
		}
		
		if(mCurY == mMaxY) {
			dialog();
		}
	}
	
	/**
	 * @param adp
	 */
	public void endDelXAnimation(AdapterView<ListAdapter> adp) {
		int ani_time = ANIMATION_DELETE_TIME * mCurX / mMaxX;
		int mTemp1;
		
		if (mOffSetLeft != -1) {
			int offset = mOffSetLeft;
			Animation mAniRight = new TranslateAnimation(Animation.ABSOLUTE, -offset,
					Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
					Animation.ABSOLUTE, 0 );
			mAniRight.setFillAfter(true);
			mAniRight.setDuration(ani_time);
			Log.i(TAG, "mCenterID = " + mCenterID + ",Count=" + adp.getChildCount());
			for (int i = mCenterID; i < adp.getChildCount(); i++) {
				View v = adp.getChildAt(i);
				if (Build.VERSION.SDK_INT > 10) {
					v.setLeft(v.getLeft() + offset);
					v.setRight(v.getRight() + offset);
				} else {
					v.layout(v.getLeft() + offset, v.getTop(), v.getRight() + offset, v.getBottom());
				}
				v.startAnimation(mAniRight);
			}
			
			mTemp1 = (mMinX - mCurX);
			Animation mAniLeft = new TranslateAnimation(Animation.ABSOLUTE, -mTemp1,
					Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
					Animation.ABSOLUTE, 0 );
			mAniLeft.setFillAfter(true);
			mAniLeft.setDuration(ani_time);
			
			for (int i = 0; i < mCenterID; i++) {
				View v = adp.getChildAt(i);
				if (Build.VERSION.SDK_INT > 10) {
					v.setLeft(v.getLeft() - mTemp1);
					v.setRight(v.getRight() - mTemp1);
				} else {
					v.layout(v.getLeft() - mTemp1, v.getTop(), v.getRight() - mTemp1, v.getBottom());
				}
				v.startAnimation(mAniLeft);
			}
		} 
		
		if (mOffSetRight != -1) {
			int offset = 0; //mOffSetRight
			Animation mAniRight = new TranslateAnimation(Animation.ABSOLUTE, -mOffSetRight,
					Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
					Animation.ABSOLUTE, 0 );
			mAniRight.setFillAfter(true);
			mAniRight.setDuration(ani_time);
			for (int i = 0; i <= mCenterID; i++) {
				View v = adp.getChildAt(i);
				if (Build.VERSION.SDK_INT > 10) {
					v.setLeft(v.getLeft() + offset);
					v.setRight(v.getRight() + offset);
				} else {
					v.layout(v.getLeft() + offset, v.getTop(), v.getRight() + offset, v.getBottom());
				}
				v.startAnimation(mAniRight);
			}
		} 
		
		mCurX = mMinX;
		mCenterID = -1;
	}
	
	public void endDelYAnimation(AdapterView<ListAdapter> adp) {
		int ani_time = ANIMATION_DELETE_TIME * mCurY / mMaxY;
		int mTemp2;
		View center = adp.getChildAt(mCenterID);
		View left = null, right = null;
		if (mCenterID > 0) {
			left = adp.getChildAt(mCenterID - 1);
		} 
		if (mCenterID + 1 < adp.getChildCount()) {
			right = adp.getChildAt(mCenterID + 1);
		}
		
		if (mCenterID + 1 >= adp.getChildCount()) {
			mCenterID = mCenterID - 1;
		}
		
		mTemp2 = mMaxY;
		Animation mAniOut = new TranslateAnimation(Animation.ABSOLUTE, 0,
				Animation.ABSOLUTE, 0, Animation.ABSOLUTE, mTemp2,
				Animation.ABSOLUTE, 0 );
		mAniOut.setFillAfter(true);
		mAniOut.setDuration(ani_time);
		if (Build.VERSION.SDK_INT > 10) {
			center.setTop(center.getTop() - mTemp2);
		} else {
			center.layout(center.getLeft(), center.getTop() - mTemp2, center.getRight(), center.getBottom());
		}
		center.startAnimation(mAniOut);
		
		mOffSetLeft = -1;
		mOffSetRight = -1;
		if (right != null) {
			mOffSetLeft = center.getLeft() - right.getLeft();
		} else if (left != null) {
			mOffSetRight = center.getLeft() - left.getLeft();
		} 
		mCurY = mMinY;
		//mCurX = mMinX;
		mCurAction = ACTION.DRAG_DEL_END;
		
	}
}
