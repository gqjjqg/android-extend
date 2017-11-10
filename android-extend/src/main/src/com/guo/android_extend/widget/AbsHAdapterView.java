package com.guo.android_extend.widget;

import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.OverScroller;
import android.widget.Scroller;

public abstract class AbsHAdapterView extends AdapterView<ListAdapter> {
	
	private final String TAG = "AbsHAdapterView";
	/**
     * Maximum amount of time to spend in {@link #findSyncPosition()}
     */
    static final int DEFAULT_DURATION_MILLIS = 250;
    
	/**
     * Used to indicate a no preference for a position type.
     */
    static final int NO_POSITION = -1;
    
    /**
     * Indicates that we are not in the middle of a touch gesture
     */
    static final int TOUCH_MODE_REST = -1;
    
    /**
     * Indicates we just received the touch event and we are waiting to see if the it is a tap or a
     * scroll gesture.
     */
    static final int TOUCH_MODE_DOWN = 0;

    /**
     * Indicates the touch has been recognized as a tap and we are now waiting to see if the touch
     * is a longpress
     */
    static final int TOUCH_MODE_TAP = 1;

    /**
     * Indicates we have waited for everything we can wait for, but the user's finger is still down
     */
    static final int TOUCH_MODE_DONE_WAITING = 2;

    /**
     * Indicates the touch gesture is a scroll
     */
    static final int TOUCH_MODE_SCROLL = 3;
    
    /**
     * Indicates the view is in the process of being flung
     */
    static final int TOUCH_MODE_FLING = 4;
    
    /**
     * Indicates the touch gesture is an overscroll - a scroll beyond the beginning or end.
     */
    static final int TOUCH_MODE_OVERSCROLL = 5;
    
    /**
     * Indicates the view is being flung outside of normal content bounds and will spring back.
     */
    static final int TOUCH_MODE_OVERFLING = 6;
    
	/**
	 * The adapter containing the data to be displayed by this view
	 */
	ListAdapter mAdapter;
    
    /**
     * Should be used by subclasses to listen to changes in the dataset
     */
    AdapterDataSetObserver mDataSetObserver;
    
    /**
     * True if the data has changed since the last layout
     */
    boolean mDataChanged;
    
    /**
     * The position of the first child displayed
     */
    int mFirstPosition = 0;
    int mLastPosition = -1;
    
    /**
     * The number of items in the current adapter.
     */
	int mItemCount;
	/**
     * The number of items in the adapter before a data changed event occurred.
     */
    int mOldItemCount;
    
    /**
     * The selection's left padding
     */
    int mSelectionLeftPadding = 0;

    /**
     * The selection's top padding
     */
    int mSelectionTopPadding = 0;

    /**
     * The selection's right padding
     */
    int mSelectionRightPadding = 0;

    /**
     * The selection's bottom padding
     */
    int mSelectionBottomPadding = 0;
    
	/**
     * This view's padding
     */
    Rect mListPadding = new Rect();
    
    /**
     * Subclasses must retain their measure spec from onMeasure() into this member
     */
    int mWidthMeasureSpec = 0;
    int mHeightMeasureSpec = 0;
    
    int mItemWidth = 0;
    int mItemHeight = 0;
    
    /**
     * Indicates that this list is always drawn on top of a solid, single-color, opaque background
     */
    private int mCacheColorHint;
    
    /**
     * Handles one frame of a fling
     */
    private FlingRunnable mFlingRunnable;
    /**
     * Handles one frame of a overfling
     */
    private OverFlingRunnable mOverFlingRunnable;
    
    /**
     * The last CheckForTap runnable we posted, if any
     */
    private CheckForTap mPendingCheckForTap;
    
    /**
     * The last CheckForLongPress runnable we posted, if any
     */
    private CheckForLongPress mPendingCheckForLongPress;
    
    /**
     * Over Scroll distance.
     */
    int mOverScrollDistance;
    
    /**
     * enable over scroll.
     */
    boolean mEnableOverScroll;
    
	/**
     * Indicates that this view is currently being laid out.
     */
    boolean mInLayout = false;
    /**
     * When set to true, calls to requestLayout() will not propagate up the parent hierarchy.
     * This is used to layout the children during a layout pass.
     */
    boolean mBlockLayoutRequests = false;

    int mOldCurrentX = 0;
	int mCurrentX = 0;
	int mNextPosX = 0;
	int mMinDistanceX;
	int mMaxDistanceX;

    /**
	 * use queue for cache the view.
	 */
	Queue<View> mRecycledViewQueue;
    
	/**
     * One of TOUCH_MODE_REST, TOUCH_MODE_DOWN, TOUCH_MODE_TAP, TOUCH_MODE_SCROLL, or
     * TOUCH_MODE_DONE_WAITING
     */
    int mTouchMode = TOUCH_MODE_REST;
    
    private int mTouchSlop;
    private int mTouchSlopSquare;
    
    private float mLastFocusX;
    private float mLastFocusY;
    private float mDownFocusX;
    private float mDownFocusY;
    
    private boolean mAlwaysInTapRegion;
    
    /**
     * The position of the view that received the down motion event
     */
    int mMotionPosition;
    
    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;
    
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private float mVelocityScale = 1.0f;
    
    /**
     * Optional callback to notify client when scroll position has changed
     */
    private OnScrollListener mOnScrollListener;
    
    /**
     * The last scroll state reported to clients through {@link OnScrollListener}.
     */
    private int mLastScrollState = OnScrollListener.SCROLL_STATE_IDLE;
    
    /**
     * Interface definition for a callback to be invoked when the list or grid
     * has been scrolled.
     */
    public interface OnScrollListener {

        /**
         * The view is not scrolling. Note navigating the list using the trackball counts as
         * being in the idle state since these transitions are not animated.
         */
        public static int SCROLL_STATE_IDLE = 0;

        /**
         * The user is scrolling using touch, and their finger is still on the screen
         */
        public static int SCROLL_STATE_TOUCH_SCROLL = 1;

        /**
         * The user had previously been scrolling using touch and had performed a fling. The
         * animation is now coasting to a stop
         */
        public static int SCROLL_STATE_FLING = 2;

        /**
         * Callback method to be invoked while the list view or grid view is being scrolled. If the
         * view is being scrolled, this method will be called before the next frame of the scroll is
         * rendered. In particular, it will be called before any calls to
         * {@link Adapter#getView(int, View, ViewGroup)}.
         *
         * @param view The view whose scroll state is being reported
         *
         * @param scrollState The current scroll state. One of 
         * {@link #SCROLL_STATE_TOUCH_SCROLL} or {@link #SCROLL_STATE_IDLE}.
         */
        public void onScrollStateChanged(AbsHAdapterView  view, int scrollState);

        /**
         * Callback method to be invoked when the list or grid has been scrolled. This will be
         * called after the scroll has completed
         * @param view The view whose scroll state is being reported
         * @param firstVisibleItem the index of the first visible cell (ignore if
         *        visibleItemCount == 0)
         * @param visibleItemCount the number of visible cells
         * @param totalItemCount the number of items in the list adaptor
         */
        public void onScroll(AbsHAdapterView view, int firstVisibleItem, int visibleItemCount, int totalItemCount);
    }
    
	class AdapterDataSetObserver extends DataSetObserver {

		private Parcelable mInstanceState = null;

		@Override
		public void onChanged() {
			mDataChanged = true;
			mOldItemCount = mItemCount;
			mItemCount = getAdapter().getCount();

			mOldCurrentX = 0;
			
			// Detect the case where a cursor that was previously invalidated
			// has
			// been repopulated with new data.
			if (AbsHAdapterView.this.getAdapter().hasStableIds()
					&& mInstanceState != null && mOldItemCount == 0
					&& mItemCount > 0) {
				AbsHAdapterView.this.onRestoreInstanceState(mInstanceState);
				mInstanceState = null;
			}
			
			requestLayout();
		}

		@Override
		public void onInvalidated() {
			mDataChanged = true;

			if (AbsHAdapterView.this.getAdapter().hasStableIds()) {
				// Remember the current state for the case where our hosting
				// activity is being
				// stopped and later restarted
				mInstanceState = AbsHAdapterView.this.onSaveInstanceState();
			}

			mOldCurrentX = mCurrentX;
			
			// Data is invalid so we should reset our state
			mOldItemCount = mItemCount;
			mItemCount = 0;
			requestLayout();
		}

		public void clearSavedState() {
			mInstanceState = null;
		}
	}

	public AbsHAdapterView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initial();

	}

	public AbsHAdapterView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initial();
		
	}

	public AbsHAdapterView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initial();
	}
	
	private void initial() {
		mRecycledViewQueue = new LinkedList<View>();
		
		setClickable(true);
		setFocusableInTouchMode(true);
		
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		mOverScrollDistance = configuration.getScaledOverscrollDistance();
		mTouchSlopSquare = mTouchSlop * mTouchSlop;
		
		mEnableOverScroll = false;
	}

	@Override
	public View getSelectedView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSelection(int position) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see android.widget.AdapterView#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		
		mInLayout = true;
		final int childCount = getChildCount();
		if (changed) {
			for (int i = 0; i < childCount; i++) {
                getChildAt(i).forceLayout();
            }
		}
		
		layoutChildren();
		mInLayout = false;
		
	}
	
	@Override
    public void requestLayout() {
        if (!mBlockLayoutRequests && !mInLayout) {
            super.requestLayout();
        }
    }
    
	/**
     * Subclasses must override this method to layout their children.
     */
    protected void layoutChildren() {
    	
    }
    
    /**
     * The list is empty. Clear everything out.
     */
    void resetList() { 
    	if (mFlingRunnable != null) {
    		mFlingRunnable.endFling();
    	}
    	removeAllViewsInLayout();
    	mRecycledViewQueue.clear();
    	
        mFirstPosition = 0;
        mLastPosition = -1;
        mCurrentX = 0;
        mDataChanged = false;
        invalidate();
    }
    
	/* (non-Javadoc)
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		mListPadding.left = mSelectionLeftPadding + super.getPaddingLeft();
		mListPadding.top = mSelectionTopPadding + super.getPaddingTop();
		mListPadding.right = mSelectionRightPadding + super.getPaddingRight();
		mListPadding.bottom = mSelectionBottomPadding + super.getPaddingBottom();

	}
	
	/**
	 * Get a view and have it show the data associated with the specified
	 * position. This is called when we have already discovered that the view is
	 * not available for reuse in the recycle bin. The only choices left are
	 * converting an old view or making a new one.
	 * 
	 * @param position
	 *            The position to display
	 * 
	 * @return A view displaying the data associated with the specified position
	 */
	View obtainView(int position) {
		View child = null;
		if (mAdapter != null) {
			child = mAdapter.getView(position, mRecycledViewQueue.poll(), this);	
		}
		return child;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		// TODO Auto-generated method stub
		mAdapter = adapter;
		this.removeAllViewsInLayout();
		this.requestLayout();
	}

	@Override
	public abstract ListAdapter getAdapter();
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mAdapter != null && mDataSetObserver == null) {
            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);

            // Data may have changed while we were detached. Refresh.
            mDataChanged = true;
            mOldItemCount = mItemCount;
            mItemCount = mAdapter.getCount();
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // Detach any view left in the scrap heap
        this.mRecycledViewQueue.clear();

        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
            mDataSetObserver = null;
        }

    }
    
    /**
     * Sets a scale factor for the fling velocity. The initial scale
     * factor is 1.0.
     *
     * @param scale The scale factor to multiply the velocity by.
     */
    public void setVelocityScale(float scale) {
        mVelocityScale = scale;
    }

    /**
     * Analyzes the given motion event and if applicable triggers the
     * appropriate callbacks on the supplied.
     *
     * @param ev The current motion event.
     * @return true if the consumed the event,
     *              else false.
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final float focusX = ev.getX();
        final float focusY = ev.getY();

        boolean handled = true;

        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:

            mDownFocusX = mLastFocusX = focusX;
            mDownFocusY = mLastFocusY = focusY;

            final int motionPosition = pointToPosition((int)focusX, (int)focusY);
            
            mAlwaysInTapRegion = true;
            if (!mDataChanged) {
            	if (mTouchMode == TOUCH_MODE_FLING) {
                	mFlingRunnable.endFling();
                } else if (mTouchMode == TOUCH_MODE_OVERFLING) {
                	mOverFlingRunnable.endOverFling();
                } else {
                    mPendingCheckForTap = new CheckForTap();
                    postDelayed(mPendingCheckForTap, ViewConfiguration.getTapTimeout());
                }
            }
            
            mMotionPosition = motionPosition;
            mTouchMode = TOUCH_MODE_DOWN;
            break;
        case MotionEvent.ACTION_MOVE:
            final float scrollX = mLastFocusX - focusX;
            final float scrollY = mLastFocusY - focusY;
            
            final int deltaX = (int) (focusX - mDownFocusX);
            final int deltaY = (int) (focusY - mDownFocusY);
            
            if (mDataChanged) {
                // Re-sync everything if data has been changed
                // since the scroll operation can query the adapter.
                layoutChildren();
            }
            
            if (mAlwaysInTapRegion && mTouchMode == TOUCH_MODE_DOWN ) {
                
                int distance = (deltaX * deltaX) + (deltaY * deltaY);
                if (distance > mTouchSlopSquare) {
                	
                	removeCallbacks(mPendingCheckForTap);
                	removeCallbacks(mPendingCheckForLongPress);
                	
                	mTouchMode = TOUCH_MODE_SCROLL;
                	trackMotionScroll((int)scrollX, (int)scrollY);
                    
                	mLastFocusX = focusX;
                    mLastFocusY = focusY;
                    mAlwaysInTapRegion = false;
                }

			} else if ((Math.abs(scrollX) >= 1) || (Math.abs(scrollY) >= 1)) {

				trackMotionScroll((int) scrollX, (int) scrollY);

				mLastFocusX = focusX;
				mLastFocusY = focusY;

			}
			break;

        case MotionEvent.ACTION_UP:
             if (mAlwaysInTapRegion) {
            	
            	switch (mTouchMode) {
                case TOUCH_MODE_DOWN:
                case TOUCH_MODE_TAP:
                    final View child = getChildAt(mMotionPosition - mFirstPosition);
                    if (child != null) {
                        // setPressed(false);
                        child.setPressed(false);
                    	super.performItemClick(child, mMotionPosition, mAdapter.getItemId(mMotionPosition));
                    } else {
                    	mTouchMode = TOUCH_MODE_REST;
                    }
                	break;
                case TOUCH_MODE_DONE_WAITING:
                	mTouchMode = TOUCH_MODE_REST;
                	break;
                default:Log.e(TAG, "error mode =" + mTouchMode);
            	}
            	
            } else {
                // A fling must travel the minimum tap distance
                final VelocityTracker velocityTracker = mVelocityTracker;
                final int pointerId = ev.getPointerId(0);
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                final float velocityY = velocityTracker.getYVelocity(pointerId) * mVelocityScale;
                final float velocityX = velocityTracker.getXVelocity(pointerId) * mVelocityScale;

                if ((Math.abs(velocityY) > mMinimumVelocity)
                        || (Math.abs(velocityX) > mMinimumVelocity)){
                	mFlingRunnable = new FlingRunnable();
                	mFlingRunnable.startFling(velocityX, velocityY);
                } else {
                	if (checkOverScroll()) {
                		mOverFlingRunnable = new OverFlingRunnable();
    	        		mOverFlingRunnable.startOverFling(mCurrentX, 0, mMinDistanceX, mMaxDistanceX, 0, 0);
                	} else {
                		mTouchMode = TOUCH_MODE_REST;
                		reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                	}
                }
                
            }
            if (mVelocityTracker != null) {
                // This may have been cleared when we called out to the
                // application above.
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }

            removeCallbacks(mPendingCheckForLongPress);
            removeCallbacks(mPendingCheckForTap);
            break;

        case MotionEvent.ACTION_CANCEL:
            cancel();
            break;
        }

        return handled;
    }
    
    private void cancel() {
    	
    	removeCallbacks(mPendingCheckForTap);
        removeCallbacks(mPendingCheckForLongPress);
    	if (mVelocityTracker != null) {
    		mVelocityTracker.recycle();
    		mVelocityTracker = null;
    	}
        mAlwaysInTapRegion = false;
        if (mTouchMode == TOUCH_MODE_FLING ||
        		mTouchMode == TOUCH_MODE_SCROLL) {
        	reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
        }
        mTouchMode = TOUCH_MODE_REST;

    }
    
    /**
     * Maps a point to a position in the list.
     *
     * @param x X in local coordinate
     * @param y Y in local coordinate
     * @return The position of the item which contains the specified point, or
     *         {@link #INVALID_POSITION} if the point does not intersect an item.
     */
    public int pointToPosition(int x, int y) {
    	final int count = super.getChildCount();
    	for (int i = 0; i < count; i++) {
    		final View child = getChildAt(i);
			if (child.getVisibility() == View.VISIBLE) {
				if (child.getLeft() < x && x < child.getRight()) {
					if (this.mListPadding.top < y && y < this.getHeight() - this.mListPadding.bottom) {
						return mFirstPosition + i;
					}
				}
			}
    	}
    	return INVALID_POSITION;
    }
    
    /**
     * Track a motion scroll
     *
     * @param deltaY Amount to offset mMotionView. This is the accumulated delta since the motion
     *        began. Positive numbers mean the user's finger is moving down the screen.
     * @param incrementalDeltaY Change in deltaY from the previous event.
     * @return true if we're already at the beginning/end of the list and have nothing to do.
     */
    boolean trackMotionScroll(int deltaX, int deltaY) {
        final int childCount = getChildCount();
        if (childCount == 0) {
            return true;
        }

        if (mTouchMode == TOUCH_MODE_SCROLL) {
        	mNextPosX += (int)deltaX;
        	mBlockLayoutRequests = true;
        	scrollSnap();
        	mBlockLayoutRequests = false;
        	invalidate();
        	
        	reportScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
        	invokeOnItemScrollListener();
        	
        	if (checkOverScroll()) {
        		mTouchMode = TOUCH_MODE_OVERSCROLL;
        	}
        } else if (mTouchMode == TOUCH_MODE_OVERSCROLL) {
        	float per = 0.0f;
        	if (mNextPosX < mMinDistanceX) {
        		//int dst = mOverScrollDistance - Math.abs(mNextPosX);
				int dst = mOverScrollDistance - (mMinDistanceX - mNextPosX);
        		per = (float)dst / (float)mOverScrollDistance;
        	} else if (mNextPosX > mMaxDistanceX){
        		int dst = mNextPosX - mMaxDistanceX;
        		per = (float)(mOverScrollDistance - dst) / (float)mOverScrollDistance;
        	} else {
				throw new RuntimeException("exception from HListView TOUCH_MODE_OVERSCROLL");
			}
        	
        	deltaX = (int) (deltaX * per);
        	
        	mNextPosX += (int)deltaX;
        	mBlockLayoutRequests = true;
        	scrollSnap();
        	mBlockLayoutRequests = false;
        	invalidate();
        
        	if (checkOverScroll()) {
        		mTouchMode = TOUCH_MODE_OVERSCROLL;
        	} else {
        		mTouchMode = TOUCH_MODE_SCROLL;
        	}
        }
        
		return false;
    }
    
    abstract void recycleChildren();
    
    abstract void fillGapList(final int offsetX);
    
    /**
     * check if next movement is out of range. return true.
     * @return true or not
     */
    protected boolean checkOverScroll() {
    	if (mEnableOverScroll) {
	    	if (mNextPosX <= mMinDistanceX){
				return true;
			}
			if (mNextPosX >= mMaxDistanceX) {
				return true;
			}
    	}
		return false;
    }
    
    /**
     * 
     * @param enable enable over scroll.
     */
    public void enableOverScroll(boolean enable) {
    	mEnableOverScroll = enable;
    	super.requestLayout();
    }

    /**
     * 
     * @return set is true or not.
     */
    public boolean isOverScrollEnable() {
    	return mEnableOverScroll;
    }
    
    /**
     * mNextPosX : the next movement.
     * mCurrentX : the current position.
	 * @return do success or not.
     */
    protected boolean scrollSnap() {
    	boolean isEnd = false;
    	if (mNextPosX <= mMinDistanceX - mOverScrollDistance){
        	mNextPosX = mMinDistanceX - mOverScrollDistance;
        	isEnd = true;
		}
        
		if (mNextPosX >= mMaxDistanceX + mOverScrollDistance) {
			mNextPosX = mMaxDistanceX + mOverScrollDistance;
			isEnd = true;
		}
		
    	final int offsetX = mCurrentX - mNextPosX;
		
    	offsetChildrenLeftAndRight(offsetX);

        fillGapList(offsetX);
        
        recycleChildren();
        
        mCurrentX = mNextPosX;
        
        return isEnd;
    }
    
	/**
	 * @param offset offset of the view.
	 */
	public void offsetChildrenLeftAndRight(int offset) {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View v = getChildAt(i);
			v.layout(v.getLeft() + offset, v.getTop(), v.getRight() + offset, v.getBottom());
			invalidate();
		}
	}  
	
	@Override
    protected boolean canAnimate() {
        return getLayoutAnimation() != null && mItemCount > 0;
    }
	
	/**
	 * 
	 * Over Fling.
	 * @author gqj3375
	 *
	 */
	class OverFlingRunnable implements Runnable {

		private final OverScroller mOverScroller;
		
		public OverFlingRunnable() {
			// TODO Auto-generated constructor stub
			mOverScroller = new OverScroller(getContext());
		}
		
		public OverFlingRunnable(OverScroller overscroller) {
			mOverScroller = overscroller;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mTouchMode == TOUCH_MODE_OVERFLING ||
				mTouchMode == TOUCH_MODE_OVERSCROLL) {
				if (mDataChanged) {
	                layoutChildren();
	            }
	            if (mOverScroller.computeScrollOffset()) {
	            	mNextPosX = mOverScroller.getCurrX();
	    		}
	            mBlockLayoutRequests = true;
	  	        scrollSnap();
	        	mBlockLayoutRequests = false;
	        	invalidate();
	        	
	        	if (mOverScroller.isFinished()){
	        		mTouchMode = TOUCH_MODE_REST;
	        		reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
	    		} else {
	    			mOverFlingRunnable = new OverFlingRunnable(mOverScroller);
	    			post(mOverFlingRunnable);
	    			reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
	    			invokeOnItemScrollListener();
	    		}
			}
		}
		
		/**
		 * 
		 * @param startX
		 * @param startY
		 * @param minX
		 * @param maxX
		 * @param minY
		 * @param maxY
		 */
		public void startOverFling (int startX, int startY, int minX, int maxX, int minY, int maxY) {
			mTouchMode = TOUCH_MODE_OVERFLING;
			mOverScroller.springBack(startX, startY, minX, maxX, minY, maxY);
			post(this);
		}
		
		/**
		 * 
		 */
		public void endOverFling() {
			mOverScroller.forceFinished(true);
            removeCallbacks(this);
            removeCallbacks(mOverFlingRunnable);
		}
		
	}
	
	/**
     * Responsible for fling behavior. Use {@link #start(int)} to
     * initiate a fling. Each frame of the fling is handled in {@link #run()}.
     * A FlingRunnable will keep re-posting itself until the fling is done.
     *
     */
    class FlingRunnable implements Runnable {
    	/**
         * Tracks the decay of a fling scroll
         */
        private final Scroller mScroller;
        
		public FlingRunnable() {
			// TODO Auto-generated constructor stub
			mScroller = new Scroller(getContext());
		}
		
		public FlingRunnable(Scroller scroller) {
			mScroller = scroller;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mTouchMode == TOUCH_MODE_FLING || 
				mTouchMode == TOUCH_MODE_SCROLL ) {
				if (mDataChanged) {
	                layoutChildren();
	            }
	            if (mScroller.computeScrollOffset()) {
	            	mNextPosX = mScroller.getCurrX();
	    		}
	            mBlockLayoutRequests = true;
	  	        scrollSnap();
	        	mBlockLayoutRequests = false;
	        	invalidate();
	        	
	        	if (checkOverScroll()) {
	        		mFlingRunnable.endFling();
	        		mOverFlingRunnable = new OverFlingRunnable();
	        		mOverFlingRunnable.startOverFling(mCurrentX, 0, mMinDistanceX, mMaxDistanceX, 0, 0);
	        		return ;
	        	}

	        	if (mScroller.isFinished()){
	        		mTouchMode = TOUCH_MODE_REST;
	        		reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
	    		} else {
	    			mFlingRunnable = new FlingRunnable(mScroller);
	    			post(mFlingRunnable);
	    			reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
	    			invokeOnItemScrollListener();
	    		}
			}
		}
		
		public void startFling(float velocityX, float velocityY) {
			mTouchMode = TOUCH_MODE_FLING;
			mScroller.fling(mNextPosX, 0, (int)-velocityX, 0, mMinDistanceX - mOverScrollDistance, mMaxDistanceX + mOverScrollDistance, 0, 0);
			post(this);
		}
		
		public void startScroll(float deltaX, float deltaY, int duration) {
			mTouchMode = TOUCH_MODE_FLING;
			mScroller.startScroll(mNextPosX, 0, (int) deltaX, 0, duration);
			post(this);
		}
		
		public void endFling() {
			mScroller.forceFinished(true);
            removeCallbacks(this);
            removeCallbacks(mFlingRunnable);
		}
		
	}
    
    private class CheckForTap implements Runnable {
    	
        @Override
        public void run() {
            if (mTouchMode == TOUCH_MODE_DOWN) {
                final View child = getChildAt(mMotionPosition - mFirstPosition);
                if (child != null && !child.hasFocusable()) {
                    if (!mDataChanged) {
                        //setPressed(true);
                        child.setPressed(true);
                        layoutChildren();
                        
                        final int longPressTimeout = ViewConfiguration.getLongPressTimeout();
                        final boolean longClickable = isLongClickable();
                        
                        if (longClickable) {
                        	
                            if (mPendingCheckForLongPress == null) {
                                mPendingCheckForLongPress = new CheckForLongPress();
                            }
                           
                            postDelayed(mPendingCheckForLongPress, longPressTimeout);
                        } else {
                            mTouchMode = TOUCH_MODE_TAP;
                        }
                    } else {
                        mTouchMode = TOUCH_MODE_DONE_WAITING;
                    }
                }
            }
        }
        
    }
    
    private class CheckForLongPress implements Runnable {
    	
        @Override
        public void run() {
            final int motionPosition = mMotionPosition;
            final View child = getChildAt(motionPosition - mFirstPosition);
            if (child != null) {
                final int longPressPosition = mMotionPosition;
                final long longPressId = mAdapter.getItemId(mMotionPosition);

                boolean handled = false;
                if (!mDataChanged) {
                    handled = performLongPress(child, longPressPosition, longPressId);
                }
                if (handled) {
                    mTouchMode = TOUCH_MODE_REST;
                    //setPressed(false);
                    child.setPressed(false);
                } else {
                    mTouchMode = TOUCH_MODE_DONE_WAITING;
                }
            }
        }
        
    }
    
    boolean performLongPress(final View child,
            final int longPressPosition, final long longPressId) {
        // CHOICE_MODE_MULTIPLE_MODAL takes over long press.

        boolean handled = false;
        if (super.getOnItemLongClickListener() != null) {
            handled = super.getOnItemLongClickListener().onItemLongClick(this, child,
                    longPressPosition, longPressId);
        }

        if (handled) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
        
        return handled;
    }
    
    /**
     * Set the listener that will receive notifications every time the list scrolls.
     *
     * @param l the scroll listener
     */
	public void setOnScrollListener(OnScrollListener l) {
        mOnScrollListener = l;   
        
        invokeOnItemScrollListener();
    }
    
	/**
     * Notify our scroll listener (if there is one) of a change in scroll state
     */
    void invokeOnItemScrollListener() {

        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(this, mFirstPosition, getChildCount(), mItemCount);
        }
        onScrollChanged(0, 0, 0, 0); // dummy values, View's implementation does not use these.
    }
	
    /**
     * Fires an "on scroll state changed" event to the registered
     * {@link android.widget.AbsListView.OnScrollListener}, if any. The state change
     * is fired only if the specified state is different from the previously known state.
     *
     * @param newState The new scroll state.
     */
	void reportScrollStateChange(int newState) {
        if (newState != mLastScrollState) {
            if (mOnScrollListener != null) {
                mLastScrollState = newState;
                mOnScrollListener.onScrollStateChanged(this, newState);
            }
        }
    }
	
	/**
     * Smoothly scroll to the specified adapter position. The view will
     * scroll such that the indicated position is displayed.
     * @param x Scroll to this adapter position.
     * @param duration time flight
     */
    public void scrollSmoothTo(int x, int duration) {
    	if (mFlingRunnable != null) {
    		mFlingRunnable.endFling();
    	}
    	mFlingRunnable = new FlingRunnable();
    	mFlingRunnable.startScroll(x - mCurrentX, 0, duration);
    	reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
    }
    
    /**
     * Smoothly scroll to the specified adapter position. The view will
     * scroll such that the indicated position is displayed.
     * @param x Scroll to this adapter position.
     * 
     */
    public void scrollSmoothTo(int x) {
    	scrollSmoothTo(x, DEFAULT_DURATION_MILLIS);
    }


    /**
     *  When set to a non-zero value, the cache color hint indicates that this list is always drawn on top of a solid, single-color, opaque background
     *  Parameters:
     * @param color the hint color
     */
	public void setCacheColorHint(int color) {
		// TODO Auto-generated method stub
		 if (color != mCacheColorHint) {
             mCacheColorHint = color;
             int count = getChildCount();
             for (int i = 0; i < count; i++) {
                 getChildAt(i).setDrawingCacheBackgroundColor(color);
             }
         }
	}
	
	@Override
	public int getSolidColor() {
		return mCacheColorHint;
	}
	
	public int getCacheColorHint() {
        return mCacheColorHint;
    }
	
}
