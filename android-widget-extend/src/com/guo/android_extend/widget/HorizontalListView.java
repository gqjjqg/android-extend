package com.guo.android_extend.widget;

import java.util.LinkedList;
import java.util.Queue;

import com.guo.android_extend.CustomGestureDetector;
import com.guo.android_extend.CustomGestureDetector.OnCustomGestureListener;
import com.guo.android_widget_externed.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Scroller;

public class HorizontalListView extends AdapterView<ListAdapter> {
	
	private final String TAG = this.getClass().toString();

	/**
	 * USED for stop scroll. 
	 * the position will fixed by  flipping.
	 * this is the time to control the duration.
	 */
	private final static int FIX_FLIPING_DURATION = 500;
	
	/**
	 *  adapter for data list.
	 */
	protected ListAdapter mAdapter;
	/**
	 * View abstract child index 
	 */
	private int mLeftViewIndex;
	private int mRightViewIndex;
	private int mCurViewIdx;
	/**
	 * View display offset.
	 */
	protected int mCurrentX;
	protected int mNextX;
	private int mMaxX;
	private int mDisplayOffset;
	
	private CustomGestureDetector mGesture;
	
	/**
	 * Scroller for scroll and flipping.
	 */
	protected Scroller mScroller;
	private int mLastFinalX;
	private int mLastMaxX;
	/**
	 * use queue for cache the view.
	 */
	private Queue<View> mRemovedViewQueue;
	
	private OnItemSelectedListener mOnItemSelected;
	private OnItemClickListener mOnItemClicked;
	private OnItemLongClickListener mOnItemLongClicked;
	
	/**
	 * item view offset form this widget.
	 */
	private int mOffsetTop;
	private int mOffsetLeft;
	private int mOffsetRight;
	
	/**
	 * support center or not.
	 */
	private boolean mItemCenter;
	
	/**
	 * child view width.
	 */
	private int mItemWidth;
	
	/**
	 * layout is initialized or not.
	 */
	private boolean bLayoutInited;
	
	/**
	 * drag the item mode.
	 */
	private DRAG mCurDrag;
	private enum DRAG {
		/**
		 * DRAG_IDEL: current status is static.
		 */
		DRAG_IDEL,
		/**
		 * DRAG_FLIPPING: current status is flipping.
		 */
		DRAG_FLIPPING,
		/**
		 * DRAG_PRE: current status is pre scroll.
		 * DRAG_X: drag in x axis. 
		 * DRAG_Y: drag in y axis.
		 */
		DRAG_PRE,
		DRAG_X,
		DRAG_Y,
	}
	
	/**
	 * current mode for data change or other.
	 */
	private MODE mCurMode;
	private enum MODE {
		MODE_DATA_NORMAL,
		MODE_DATA_CHANGEED,
		MODE_DATA_INVILID
	} ;
	 
	/**
	 * scroll direction.
	 */
	private DIRECTION mScrollDirection;
	private enum DIRECTION {
		MOVE_UNKNOWN,
		LEFT_TO_RIGHT,
		RIGHT_TO_LEFT,
		TOP_TO_BOTTOM,
		BOTTOM_TO_TOP,
	}
	
	private OnItemScrollListener mOnItemScrollListener;
	public interface OnItemScrollListener {
		public void OnScrollCenter(AdapterView<ListAdapter> adp, View v, int pos, float percent);
		public void OnScrollStart(AdapterView<ListAdapter> adp);
		public void OnScrollEnd(AdapterView<ListAdapter> adp, int pos);
		public boolean OnDraging(AdapterView<ListAdapter> adp, float dx, float dy);
		/**
		 * @param adp
		 * @return true if drag is not used.
		 */
		public boolean OnDragingOver(AdapterView<ListAdapter> adp);
	}
	
	private Runnable mRequestLayoutRunable = new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			requestLayout();
		}};
		
	private DataSetObserver mDataObserver = new DataSetObserver() {

			@Override
			public void onChanged() {
				synchronized(HorizontalListView.this){
					mCurMode = MODE.MODE_DATA_CHANGEED;
				}
				invalidate();
				requestLayout();
			}

			@Override
			public void onInvalidated() {
				synchronized(HorizontalListView.this){
					mCurMode = MODE.MODE_DATA_INVILID;
				}
				mLastFinalX = mScroller.getFinalX();
				mLastMaxX = mMaxX;
				invalidate();
				requestLayout();
			}

		};
		
	public HorizontalListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		TypedArray att = context.obtainStyledAttributes(attrs,  
                R.styleable.HorizontalListView);  
		mOffsetTop = (int) att.getDimension(R.styleable.HorizontalListView_itemOffsetTop, 0);
		Log.i(TAG, "mOffsetTop = " + mOffsetTop);
		mOffsetLeft = (int) att.getDimension(R.styleable.HorizontalListView_itemOffsetLeft, 0);
		Log.i(TAG, "mOffsetLeft = " + mOffsetLeft);
		mItemCenter = (boolean) att.getBoolean(R.styleable.HorizontalListView_itemCenter, false);
		att.recycle();
		onCreate();
	}

	public HorizontalListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mOffsetTop = 0;
		mOffsetLeft = 0;
		mItemCenter = false;
		onCreate();
	}

	public HorizontalListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		TypedArray att = context.obtainStyledAttributes(attrs,  
	                R.styleable.HorizontalListView);  
		mOffsetTop = (int) att.getDimension(R.styleable.HorizontalListView_itemOffsetTop, 0);
		Log.i(TAG, "mOffsetTop = " + mOffsetTop);
		mOffsetLeft = (int) att.getDimension(R.styleable.HorizontalListView_itemOffsetLeft, 0);
		Log.i(TAG, "mOffsetLeft = " + mOffsetLeft);
		mItemCenter = (boolean) att.getBoolean(R.styleable.HorizontalListView_itemCenter, false);
		att.recycle();
		onCreate();
	}

	/**
	 * onCreate
	 */
	private void onCreate() {
		// TODO in order to change the views drawing order.
		setChildrenDrawingOrderEnabled(true);
		
		mScroller = new Scroller(getContext());
		mGesture = new CustomGestureDetector(getContext(), mOnGesture);
		
		mLastFinalX = 0;
		mLastMaxX = Integer.MAX_VALUE;
		mLeftViewIndex = -1;
		mRightViewIndex = 0;
		mDisplayOffset = 0;
		mNextX = 0;
		mMaxX = Integer.MAX_VALUE;
		mCurViewIdx = 0;
		
		bLayoutInited = false;
		mCurDrag = DRAG.DRAG_IDEL;
		
		mCurMode = MODE.MODE_DATA_NORMAL;
		
		mScrollDirection = DIRECTION.MOVE_UNKNOWN;
		mRemovedViewQueue = new LinkedList<View>();
	}
	
	/**
	 * for reset the data adapter
	 */
	private synchronized void clearLayout() {
		mLeftViewIndex = -1;
		mRightViewIndex = 0;
		mDisplayOffset = 0;
		mCurrentX = mOffsetLeft;
		mNextX = 0;
		mMaxX = Integer.MAX_VALUE;
	}

	/**
	 * @return
	 */
	private boolean initLayout() {
		if (mAdapter == null) {
			return false;
		}
		if (!bLayoutInited && mAdapter.getCount() > 0) {
        	View child = mAdapter.getView(0, null, this);
    		LayoutParams lp = child.getLayoutParams(); 
    		if(lp == null) {
    			lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
    			child.setLayoutParams(lp);
    		}

    		// mPaddingRight
    		final int childWidthMeasureSpec = getChildMeasureSpec(
    				this.getMeasuredWidth(), this.getPaddingLeft() + this.getPaddingRight(), 
    				lp.width);
    		// mPaddingBottom
    		final int childHeightMeasureSpec = getChildMeasureSpec(
    				this.getMeasuredHeight(), this.getPaddingTop() + this.getPaddingBottom(),
    				lp.height);

    		child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    		mItemWidth = child.getMeasuredWidth();
    		
        	Log.i(TAG, "Item W=" + mItemWidth + "H=" + child.getMeasuredHeight());
        	Log.i(TAG, "View W=" + getMeasuredWidth() + ",H=" + getMeasuredHeight());
	        int showCount = 0;
        	if (mItemCenter) {
	        	mOffsetRight = (int) ((Math.ceil((getMeasuredWidth() - mItemWidth) / 2.0F)) - mOffsetLeft); 
	        	mOffsetLeft = mOffsetLeft + (int) ((Math.ceil((getMeasuredWidth() - mItemWidth) / 2.0F)));
	        	//Calculate the left view id.
	        	showCount = getMeasuredWidth() / mItemWidth;
	        	if (getMeasuredWidth() % mItemWidth != 0 ){
	        		showCount += 2;
	        	}
	        	int halfCount = showCount / 2;
	        	if (halfCount <= mCurViewIdx) {
	        		mLeftViewIndex = mCurViewIdx - halfCount;
	        	} else {
	        		mLeftViewIndex = mCurViewIdx - 1;
	        	}
	        	
	        } else {
	        	mOffsetRight = mOffsetLeft; 
	        	showCount = getMeasuredWidth() / mItemWidth;
	        	if (getMeasuredWidth() % mItemWidth != 0 ){
	        		showCount += 1;
	        	}
	        	//have not full test , may have some unknown issues.
	        	if (showCount <= mCurViewIdx) {
	        		mLeftViewIndex = mCurViewIdx - showCount;
	        	} else {
	        		mLeftViewIndex = -1;
	        	}
	        }
	        
			mRightViewIndex = mCurViewIdx;
			
			int fix = mCurViewIdx * mItemWidth;
			Log.i(TAG, "mCurrentX = " + mCurrentX + " fix = " + fix);
			Log.i(TAG, "mScroller = " + mScroller.getCurrX() + ",mLeftViewIndex ="+ mLeftViewIndex);
			
			mCurrentX = mOffsetLeft + (fix - mScroller.getCurrX());
			
			//mScroller.startScroll(mOffsetLeft, 0, fix - mOffsetLeft, 0);
			mScroller.setFinalX(fix);
			Log.i(TAG, "mScroller = " + mScroller.getCurrX());
			
	        bLayoutInited = true;
        }
		return bLayoutInited;
	}
	
	@Override
	public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
		mOnItemSelected = listener;
	}

	@Override
	public void setOnItemClickListener(AdapterView.OnItemClickListener listener){
		super.setOnItemClickListener(listener);
		mOnItemClicked = listener;
	}

	@Override
	public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
		super.setOnItemLongClickListener(listener);
		mOnItemLongClicked = listener;
	}

	/* (non-Javadoc)
	 * @see android.view.ViewGroup#getChildDrawingOrder(int, int)
	 */
	protected int getChildDrawingOrder(int childCount, int i) {
		int center = mCurViewIdx - getFirstVisiblePosition();
		if (center >= 0 && center < childCount) {
			if (i == (childCount - 1)) {
				return center;
			} 
			if (i == center) {
				return (childCount - 1);
			}
		}
		return i;
	}
	
	@Override
	public int getFirstVisiblePosition() {
		// TODO Auto-generated method stub
		//return super.getFirstVisiblePosition();
		return mLeftViewIndex < 0 ? 0 : mLeftViewIndex + 1;
	}

	@Override
	public ListAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if(mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mDataObserver);
		}
		mAdapter = adapter;
		mAdapter.registerDataSetObserver(mDataObserver);
		reset();
	}

	@Override
	public View getSelectedView() {
		//TODO: implement
		return null;
	}
	
	private synchronized void reset() {
		mScroller = new Scroller(getContext());
		mGesture = new CustomGestureDetector(getContext(), mOnGesture);
		clearLayout();
		removeAllViewsInLayout();
        requestLayout();
	}

	@Override
	public void setSelection(int position) {
		//TODO: implement
		if (mAdapter == null) {
			return ;
		}
		if (mAdapter.getCount() <= 0) {
			return ;
		}
		
		mCurDrag = DRAG.DRAG_FLIPPING;

		mCurViewIdx = position;
		
		/**
		 * not working well.
		 * is scroll to target position by one step.
		 */
		int fix = mCurViewIdx * getChildViewWidth();
		Log.i(TAG, "setSelection mCurrentX = " + mCurrentX + " fix = " + fix);
		Log.i(TAG, "setSelection mScroller = " + mScroller.getCurrX());
		
		mLeftViewIndex = mCurViewIdx - 1;
		mRightViewIndex = mCurViewIdx;
		mCurrentX = mOffsetLeft + (fix - mScroller.getCurrX());
		
		mScroller.setFinalX(fix);
		
		//mScroller.startScroll(mScroller.getCurrX(), 0, fix, 0);
		//ItemScrollFix(fix);
		
		post(mRequestLayoutRunable);
	}

	@Override
	public void addView(View child, int idx) {
		// TODO Auto-generated method stub
		//super.addView(child);
		addAndMeasureChild(child, idx);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	/**
	 * @return 
	 */
	private int getChildViewWidth() {
		if (bLayoutInited) {
			return mItemWidth;
		}
		return 0;
	}
	
	/**
	 * @param child
	 * @param viewPos
	 */
	private void addAndMeasureChild(final View child, int viewPos) {
		LayoutParams params = child.getLayoutParams();
		if (params == null) {
			params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		}
		addViewInLayout(child, viewPos, params, true);
		
		final LayoutParams lp = child.getLayoutParams(); 
		// mPaddingRight
		final int childWidthMeasureSpec = getChildMeasureSpec(
				this.getMeasuredWidth(), this.getPaddingLeft() + this.getPaddingRight(), 
				lp.width);
		// mPaddingBottom
		final int childHeightMeasureSpec = getChildMeasureSpec(
				this.getMeasuredHeight(), this.getPaddingTop() + this.getPaddingBottom(),
				lp.height);
		
		child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
	}
	
	/***
	 * @hit after fling over . fix the position in the screen.
	 * @param offset
	 */
	private void flipingFix(int offset) {
		int width = getChildViewWidth();
		if (width != 0 && mCurDrag == DRAG.DRAG_FLIPPING) {
			int fix = Math.round((float)offset / (float)width) * width;
			if (mScrollDirection == DIRECTION.RIGHT_TO_LEFT) {
				fix = (int) (Math.ceil((float)offset / (float)width) * width);
			} else if (mScrollDirection == DIRECTION.LEFT_TO_RIGHT) {
				fix = (int) (Math.floor((float)offset / (float)width) * width);
			}
			/**
			 * TODO  
			 * fix <= mMaxX avoid endless loop.
			 */
			if (fix != mCurrentX && fix <= mMaxX) {
				int time = Math.abs(fix  - offset) * FIX_FLIPING_DURATION / width;
				mScroller.startScroll(offset, 0, fix - offset, 0, time);
				post(mRequestLayoutRunable);
			} else {
				if (mOnItemScrollListener != null) {
					//Log.i(TAG, "mCurIdx = " + mCurIdx + ", mLeftViewIndex = "+ mLeftViewIndex);
					int center = mCurViewIdx - getFirstVisiblePosition();
					mOnItemScrollListener.OnScrollEnd(this, center);
					mScrollDirection = DIRECTION.MOVE_UNKNOWN;
				}
				mCurDrag = DRAG.DRAG_IDEL;
			}
		}
	}
	
	
	@Override
	protected synchronized void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		if (!initLayout()) {
			return;
		}
		
		if(mCurMode == MODE.MODE_DATA_CHANGEED){
			int oldCurrentX = mCurrentX;
			clearLayout();
			removeAllViewsInLayout();
			mNextX = oldCurrentX;
		} else if (mCurMode == MODE.MODE_DATA_INVILID) {
			clearLayout();
			removeAllViewsInLayout();
			mNextX = mLastFinalX;
			mMaxX = mLastMaxX;
		}

		if(mScroller.computeScrollOffset()){
			int scrollx = mScroller.getCurrX();
			mNextX = scrollx;
		}

		if(mNextX <= 0){
			mNextX = 0;
			mScroller.forceFinished(true);
		}
		if(mNextX >= mMaxX) {
			mNextX = mMaxX;
			mScroller.forceFinished(true);
		}

		int dx = mCurrentX - mNextX;

		removeNonVisibleItems(dx);
		fillList(dx);
		positionItems(dx);

		mCurrentX = mNextX;
		mCurMode = MODE.MODE_DATA_NORMAL;
		
		if(!mScroller.isFinished()){
			post(mRequestLayoutRunable);
		} else {
			if (mItemCenter) {
		
				/**
				 * this is fixed for flipping target position.
				 * @see onFling
				 */
				flipingFix(mCurrentX);
			} else {
				if (mOnItemScrollListener != null) {
					mOnItemScrollListener.OnScrollEnd(this, mCurViewIdx);
				}
			}
		}
	}

	/**
	 * @param dx
	 */
	private void fillList(final int dx) {
		int edge = 0;
		View child = getChildAt(getChildCount()-1);
		if(child != null) {
			edge = child.getRight();
		}
		fillListRight(edge, dx);

		edge = 0;
		child = getChildAt(0);
		if(child != null) {
			edge = child.getLeft();
		}
		fillListLeft(edge, dx);

	}

	/**
	 * @param rightEdge
	 * @param dx
	 */
	private void fillListRight(int rightEdge, final int dx) {
		while(rightEdge + dx < getWidth() && mRightViewIndex < mAdapter.getCount()) {

			View child = mAdapter.getView(mRightViewIndex, mRemovedViewQueue.poll(), this);
			addAndMeasureChild(child, -1);
			rightEdge += child.getMeasuredWidth();

			if(mRightViewIndex == mAdapter.getCount()-1) {
				mMaxX = mCurrentX + rightEdge - getWidth() + mOffsetRight;
			}

			if (mMaxX <= -mOffsetLeft) {
				mMaxX = -mOffsetLeft;
			}
			mRightViewIndex++;
		}

	}

	/**
	 * @param leftEdge
	 * @param dx
	 */
	private void fillListLeft(int leftEdge, final int dx) {
		while(leftEdge + dx > 0 && mLeftViewIndex >= 0) {
			View child = mAdapter.getView(mLeftViewIndex, mRemovedViewQueue.poll(), this);
			addAndMeasureChild(child, 0);
			leftEdge -= child.getMeasuredWidth();
			mLeftViewIndex--;
			mDisplayOffset -= child.getMeasuredWidth();
		}
	}

	/**
	 * @param dx
	 */
	private void removeNonVisibleItems(final int dx) {
		View child = getChildAt(0);
		while(child != null && child.getRight() + dx <= 0) {
			mDisplayOffset += child.getMeasuredWidth();
			mRemovedViewQueue.offer(child);
			//TODO : clear the animation.
			child.clearAnimation();
			removeViewInLayout(child);
			mLeftViewIndex++;
			child = getChildAt(0);

		}

		child = getChildAt(getChildCount()-1);
		while(child != null && child.getLeft() + dx >= getWidth()) {
			mRemovedViewQueue.offer(child);
			//TODO : clear the animation.
			child.clearAnimation();
			removeViewInLayout(child);
			mRightViewIndex--;
			child = getChildAt(getChildCount()-1);
		}
	}

	/**
	 * update selected.
	 */
	private void updateSelected() {
		View center = null;
		int start = getFirstVisiblePosition();
		for (int i = 0; i < this.getChildCount(); i++) {
			center = this.getChildAt(i);
			if (center.getLeft() <= mOffsetLeft && center.getRight() > mOffsetLeft) {
				int dstLeft = center.getRight() - mOffsetLeft;
				float percet = (float)dstLeft / (float)getChildViewWidth();	
				
				if (mOnItemScrollListener != null) {
					mOnItemScrollListener.OnScrollCenter(HorizontalListView.this, center, i, percet);
				}
				
				if(mCurMode != MODE.MODE_DATA_CHANGEED &&  mItemCenter){
					mCurViewIdx = start + i; 
					if (mOnItemSelected != null) {
						mOnItemSelected.onItemSelected(HorizontalListView.this, center, mCurViewIdx, mAdapter.getItemId( mCurViewIdx ));
					}
				}
				break;
			}
		}
	}
	
	/**
	 * position the items in layout.
	 * @param dx
	 */
	private void positionItems(final int dx) {
		if(getChildCount() > 0){
			mDisplayOffset += dx;
			int left = mDisplayOffset;
			for(int i=0;i<getChildCount();i++){
				View child = getChildAt(i);
				int childWidth = child.getMeasuredWidth();
				int childHeight =  child.getMeasuredHeight();
				child.layout(left, mOffsetTop, left + childWidth, mOffsetTop + childHeight);
				left += childWidth;
			}
			updateSelected();
		}
	}

	/**
	 * scroll to the position.
	 * @param x
	 */
	public void scrollTo(int x) {
		synchronized(HorizontalListView.this) {
			mScroller.startScroll(mNextX, 0, x - mNextX, 0);
			requestLayout();
		}
	}

	/**
	 * @param mOnItemScrollListener the mOnItemScrollListener to set
	 */
	public void setOnItemScrollListener(OnItemScrollListener mOnItemScrollListener) {
		this.mOnItemScrollListener = mOnItemScrollListener;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		super.dispatchTouchEvent(ev);
		//TODO gesture detected.
		mGesture.onTouchEvent(ev);
		
		switch (ev.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_CANCEL: 
		case MotionEvent.ACTION_UP: 
			Log.i(TAG, "mCurDrag = " + mCurDrag);
			if (mCurDrag == DRAG.DRAG_X) {
				/**
				 * TODO : last up.
				 * While drag for scroll and not flipping.
				 * need set the scroller's final position.
				 * if flipping , @see onFlipping() will update the final position. 
				 */
				mScroller.setFinalX(mNextX);
				mScroller.forceFinished(true);
				int time = FIX_FLIPING_DURATION;
				if (mItemCenter) {
					/**
					 * calculate the target position.
					 * set the final X.
					 */
					int width = getChildViewWidth();
					if (width != 0) {
						int fix = Math.round((float)mScroller.getFinalX()/ (float)width) * width;
						time = FIX_FLIPING_DURATION * Math.abs(width - Math.abs(fix - mScroller.getFinalX())) / width;
						mScroller.setFinalX(fix);
						mScroller.forceFinished(true);
					}
				}
				mScroller.startScroll(mNextX, 0, mScroller.getFinalX() - mNextX, 0, time);
				post(mRequestLayoutRunable);
				mCurDrag = DRAG.DRAG_FLIPPING;
				
			} else if (mCurDrag == DRAG.DRAG_Y) {
				mCurDrag = DRAG.DRAG_IDEL;
				if (mOnItemScrollListener != null) {
					if (mOnItemScrollListener.OnDragingOver(this)) {
						mScrollDirection = DIRECTION.MOVE_UNKNOWN;
					}
				}
			}
			break;
		}
		
		return true;
	}

	/**
	 * @param e1
	 * @param e2
	 * @param velocityX
	 * @param velocityY
	 * @return
	 */
	protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
		synchronized(HorizontalListView.this){
			mScroller.fling(mNextX, 0, (int)-velocityX, 0, 0, mMaxX, 0, 0);
			
			if (mItemCenter) {
				/**
				 * calculate the target position.
				 * set the final X.
				 */
				int width = getChildViewWidth();
				if (width != 0 && mCurDrag == DRAG.DRAG_X) {
					int fix = Math.round((float)mScroller.getFinalX() / (float)width) * width;
					mScroller.setFinalX(fix);
				}
			}
			mCurDrag = DRAG.DRAG_FLIPPING;
		}
		requestLayout();

		return true;
	}

	private OnCustomGestureListener mOnGesture = new CustomGestureDetector.OnCustomGestureListener() {
		
		@Override
		public boolean onDown(MotionEvent e) {
			if (mCurDrag == DRAG.DRAG_FLIPPING) {
				mScroller.forceFinished(true);
				mCurDrag = DRAG.DRAG_X;
			} else {
				mScroller.forceFinished(true);
				mCurDrag = DRAG.DRAG_PRE;
			}
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			Log.i(TAG, "onFling  mScrollDirection=" + mScrollDirection);
			if (mScrollDirection == DIRECTION.BOTTOM_TO_TOP ||
					mScrollDirection == DIRECTION.TOP_TO_BOTTOM ||
					mScrollDirection == DIRECTION.MOVE_UNKNOWN) {
				return false;
			}
			return HorizontalListView.this.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			Log.i(TAG, "onScroll mScrollDirection=" + mScrollDirection+"x="+distanceX+",y="+distanceY);
			
			if (mScrollDirection == DIRECTION.BOTTOM_TO_TOP ||
					mScrollDirection == DIRECTION.TOP_TO_BOTTOM) {
				if (mOnItemScrollListener != null) {
					mOnItemScrollListener.OnDraging(HorizontalListView.this, distanceX, distanceY);
				}
				if (mCurDrag == DRAG.DRAG_PRE) {
					mCurDrag = DRAG.DRAG_Y;
				}
				return false;
			} else {
				if (distanceX != 0) {
					if (mCurDrag == DRAG.DRAG_PRE) {
						if (mOnItemScrollListener != null) {
							mOnItemScrollListener.OnScrollStart(HorizontalListView.this);
						}
						mCurDrag = DRAG.DRAG_X;
					}
					synchronized(HorizontalListView.this){
						mNextX += (int)distanceX;
					}
					requestLayout();
				}
			}
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			Rect viewRect = new Rect();
			int start = getFirstVisiblePosition();
			for(int i=0;i<getChildCount();i++){
				View child = getChildAt(i);
				int left = child.getLeft();
				int right = child.getRight();
				int top = child.getTop();
				int bottom = child.getBottom();
				viewRect.set(left, top, right, bottom);
				if(viewRect.contains((int)e.getX(), (int)e.getY())){
					if(mOnItemClicked != null){
						mOnItemClicked.onItemClick(HorizontalListView.this, child, start + i, mAdapter.getItemId( start + i ));
					}
					break;
				}

			}
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			Rect viewRect = new Rect();
			int childCount = getChildCount();
			int start = getFirstVisiblePosition();
			for (int i = 0; i < childCount; i++) {
				View child = getChildAt(i);
				int left = child.getLeft();
				int right = child.getRight();
				int top = child.getTop();
				int bottom = child.getBottom();
				viewRect.set(left, top, right, bottom);
				if (viewRect.contains((int) e.getX(), (int) e.getY())) {
					if (mOnItemLongClicked != null) {
						mOnItemLongClicked.onItemLongClick(HorizontalListView.this, child, start + i, mAdapter.getItemId(start + i));
					}
					break;
				}

			}
		}

		@Override
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			Log.i(TAG, "onSingleTapUp");
			return false;
		}

		@Override
		public void OnDoubleFingerStartScroll(MotionEvent ev, int direction) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void OnDoubleFingerStartZoom(MotionEvent ev) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void OnDoubleFingerDown(MotionEvent ev) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void OnSingleFingerEdgeIn(MotionEvent ev, int direction) {
			// TODO Auto-generated method stub
			Log.i(TAG, "OnSingleFingerEdgeIn = " + direction);	
			mScroller.forceFinished(true);
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean OnSingleFingerDrag(MotionEvent ev, int direction, float dx,
				float dy) {
			// TODO Auto-generated method stub
			if (mScrollDirection == DIRECTION.MOVE_UNKNOWN) {
				mScrollDirection = DIRECTION.values()[direction];
			}
			return false;
		}

	};



}