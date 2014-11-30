/**
 * Project : android-widget-extend
 * File : HListView.java
 * 
 * The MIT License
 * Copyright (c) 2014 QiJiang.Guo (qijiang.guo@gmail.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package com.guo.android_extend.widget;

import com.guo.android_widget_externed.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public class HListView extends AbsHAdapterView {
	
    /**
     * The drawable used to draw the divider
     */
    Drawable mDivider;
    int mDividerWidth;
    
    private boolean mClipDivider;
    
    private boolean mIsCacheColorOpaque;
    private Paint mDividerPaint;
    
    /**
     * The drawable used to draw the selector
     */
    Drawable mSelector;

    /**
     * The current position of the selector in the list.
     */
    int mSelectorPosition = INVALID_POSITION;

    /**
     * Defines the selector's location and dimension at drawing time
     */
    Rect mSelectorRect = new Rect();
    
    private final Rect mTempRect = new Rect();
    
	public HListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		onCreate(context, attrs);
	}

	public HListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		onCreate(context, attrs);
	}

	public HListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/**
	 * onCreate
	 */
	private void onCreate(Context context, AttributeSet attrs) {
		TypedArray att = context.obtainStyledAttributes(attrs,  
                R.styleable.HListView);  
		mSelector = att.getDrawable(R.styleable.HListView_listSelector);

		final int dividerWidth = att.getDimensionPixelSize(R.styleable.HListView_dividerHeight, 0);
		
		if (dividerWidth != 0) {
            setDividerWidth(dividerWidth);
            mDividerPaint = new Paint();
            mDividerPaint.setColor(Color.GRAY);
        }
		
		mDivider = null;
		
		att.recycle();
	}
	

	/* (non-Javadoc)
	 * @see com.guo.android_extend.widget.AbsHAdapterView#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int childWidth = 0;
		int childHeight = 0;

		mItemCount = mAdapter == null ? 0 : mAdapter.getCount();
		if (mItemCount > 0
				&& (widthMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.UNSPECIFIED)) {
			final View child = obtainView(0);

			measureScrapChild(child, 0, heightMeasureSpec);

			childWidth = child.getMeasuredWidth();
			childHeight = child.getMeasuredHeight();
				
			mRecycledViewQueue.offer(child);
		}

		if (widthMode == MeasureSpec.UNSPECIFIED) {
			widthSize = mListPadding.left + mListPadding.right + childWidth;
		}

		if (heightMode == MeasureSpec.UNSPECIFIED) {
			heightSize = mListPadding.top + mListPadding.bottom + childHeight;
		}
		
		if (widthMode == MeasureSpec.AT_MOST) {
			// TODO: after first layout we should maybe start at the first
			// visible position, not 0
			widthSize = measureWidthOfChildren(heightMeasureSpec, 0,
					NO_POSITION, widthSize, -1);
		}

		setMeasuredDimension(widthSize, heightSize);
		
		mWidthMeasureSpec = widthMeasureSpec;
		
		mHeightMeasureSpec = heightMeasureSpec;
		
		final View child = obtainView(0);
		if (child == null) {
			return ;
		}
		measureChild(child, 0);
		
		mMaxDistanceX = mAdapter.getCount() * child.getMeasuredWidth() - getMeasuredWidth();
		// add divider width.
		mMaxDistanceX += ( mAdapter.getCount() - 1 ) * mDividerWidth;
		
		mRecycledViewQueue.offer(child);
		
		mItemWidth = child.getMeasuredWidth();
		mItemHeight = child.getMeasuredHeight();

		//TODO over scroller distance use half width.
		if (mEnableOverScroll) {
			mOverScrollDistance = widthSize / 2;
		} else {
			mOverScrollDistance = 0;
		}
	}

	@Override
	protected void layoutChildren() {
		// TODO Auto-generated method stub
        final boolean blockLayoutRequests = mBlockLayoutRequests;
        if (blockLayoutRequests) {
            return;
        }

        mBlockLayoutRequests = true;

        try {
            super.layoutChildren();

            invalidate();

            if (mAdapter == null) {
                resetList();
                invokeOnItemScrollListener();
                return;
            }
            
            
            boolean dataChanged = mDataChanged;
            if (dataChanged) {
                scrollTo(mOldCurrentX);
            }
            
            scrollSnap();
            
            invokeOnItemScrollListener();
            
            mDataChanged = false;

        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
            if (!blockLayoutRequests) {
                mBlockLayoutRequests = false;
            }
        }
	}

	@Override
	public ListAdapter getAdapter() {
		// TODO Auto-generated method stub
		return super.mAdapter;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		// TODO Auto-generated method stub
		if (mAdapter != null && mDataSetObserver != null) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
		}

		resetList();

		super.setAdapter(adapter);
		
		if (mAdapter != null) {
            
            mOldItemCount = mItemCount;
            mItemCount = mAdapter.getCount();

            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);
            
		} 
		
		scrollTo(0);
	}

	/**
	 * @param dx
	 */
	void fillGapList(final int deltaX) {
		if (deltaX <= 0) {
			int right = super.mLastPosition + 1;
			final int top = super.mListPadding.top;
			final int childCount = getChildCount();
			final View child = this.getChildAt( childCount - 1);
			final int distanceRight = super.getWidth() - super.getPaddingRight();
			int rightEdge = child == null ? 0 : child.getRight() + mDividerWidth;
			while(rightEdge < distanceRight  && right < mAdapter.getCount()) {
				makeAndAddView(right, rightEdge, true, top);
				rightEdge += super.mItemWidth;
				rightEdge += mDividerWidth;
				super.mLastPosition = right;
				++right;
			}
		} else {
			int left = super.mFirstPosition - 1;
			final int top = super.mListPadding.top;
			final View child = this.getChildAt( 0);
			final int distanceLeft = 0 + super.getPaddingLeft();
			int leftEdge = child == null ? 0 : child.getLeft() - mDividerWidth;
			while(leftEdge > distanceLeft && left >= 0) {
				makeAndAddView(left, leftEdge, false, top);
				leftEdge -= super.mItemWidth;
				leftEdge -= mDividerWidth;
				super.mFirstPosition = left;
				--left;
			}
		}
		
	}

	/**
	 * @param dx
	 */
	void recycleChildren() {
		View child = getChildAt(0);
		while (child != null && child.getRight() + mDividerWidth <= super.getPaddingLeft()) {
			mRecycledViewQueue.offer(child);
			removeViewInLayout(child);
			child = getChildAt(0);
			super.mFirstPosition++;
		}

		child = getChildAt(getChildCount() - 1);
		while(child != null && child.getLeft() >= (getWidth() - super.getPaddingRight())) {
			mRecycledViewQueue.offer(child);
			removeViewInLayout(child);
			child = getChildAt(getChildCount() - 1);
			super.mLastPosition--;
		}
	}
	
	/**
     * Obtain the view and add it to our list of children. The view can be made
     * fresh, converted from an unused view, or used as is if it was in the
     * recycle bin.
     *
     * @param position Logical position in the list
     * @param y Top or bottom edge of the view to add
     * @param flow If flow is true, align top edge to y. If false, align bottom
     *        edge to y.
     * @param childrenLeft Left edge where children should be positioned
     * @param selected Is this position selected?
     * @return View that was added
     */
	private View makeAndAddView(int position, int x, boolean flowRight, int childrenTop) {
        View child = null;
        if (!mDataChanged) {
	        // Make a new view for this position, or convert an unused view if possible
	        child = obtainView(position);
	        if (child != null) {
	        	// This needs to be positioned and measured
	        	setupChild(child, position, x, flowRight, childrenTop, true);
	        }
        }
        return child;
    }

    /**
     * Add a view as a child and make sure it is measured (if necessary) and
     * positioned properly.
     *
     * @param child The view to add
     * @param position The position of this child
     * @param y The y position relative to which this view will be positioned
     * @param flowDown If true, align top edge to y. If false, align bottom
     *        edge to y.
     * @param childrenLeft Left edge where children should be positioned
     * @param selected Is this position selected?
     * @param recycled Has this view been pulled from the recycle bin? If so it
     *        does not need to be remeasured.
     */
    private void setupChild(View child, int position, int x, boolean flowRight, int childrenTop, boolean recycled) {
        // Respect layout params that are already in the view. Otherwise make some up...
        // noinspection unchecked
        LayoutParams p = (LayoutParams) child.getLayoutParams();
        if (p == null) {
            p = (LayoutParams) generateDefaultLayoutParams();
        }
        
        addViewInLayout(child, flowRight ? -1 : 0, p, true);

        int childHeightSpec = ViewGroup.getChildMeasureSpec(mHeightMeasureSpec,
                mListPadding.top + mListPadding.bottom, p.height);
        int lpWidth = p.width;
        int childWidthSpec;
        if (lpWidth > 0) {
        	childWidthSpec = MeasureSpec.makeMeasureSpec(lpWidth, MeasureSpec.EXACTLY);
        } else {
        	childWidthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);

        final int w = child.getMeasuredWidth();
        final int h = child.getMeasuredHeight();
        final int childLeft = flowRight ? x : x - w;


        final int childBottom = childrenTop + h;
        final int childRight = childLeft + w;
        child.layout(childLeft, childrenTop, childRight, childBottom);
    }
    
    /**
     * Returns the drawable that will be drawn between each item in the list.
     *
     * @return the current drawable drawn between list elements
     */
    public Drawable getDivider() {
        return mDivider;
    }

    /**
     * Sets the drawable that will be drawn between each item in the list. If the drawable does
     * not have an intrinsic height, you should also call {@link #setDividerHeight(int)}
     *
     * @param divider The drawable to use.
     */
    public void setDivider(Drawable divider) {
        if (divider != null) {
            mDividerWidth = divider.getIntrinsicWidth();
            mClipDivider = divider instanceof ColorDrawable;
        } else {
        	mDividerWidth = 0;
        	mClipDivider = false;
        }
        mDivider = divider;
        
        requestLayout();
        invalidate();
    }

    @Override
	public void setCacheColorHint(int color) {
		final boolean opaque = (color >>> 24) == 0xFF;
		mIsCacheColorOpaque = opaque;
		if (opaque) {
			if (mDividerPaint == null) {
				mDividerPaint = new Paint();
			}
			mDividerPaint.setColor(color);
		}
		super.setCacheColorHint(color);
	}
    
    /**
     * @return Returns the height of the divider that will be drawn between each item in the list.
     */
    public int getDividerWidth() {
        return mDividerWidth;
    }
    
    /**
     * Sets the height of the divider that will be drawn between each item in the list. Calling
     * this will override the intrinsic height as set by {@link #setDivider(Drawable)}
     *
     * @param height The new height of the divider in pixels.
     */
    public void setDividerWidth(int width) {
    	mDividerWidth = width;
        requestLayout();
        invalidate();
    }

    /**
     * Draws a divider for the given child in the given bounds.
     *
     * @param canvas The canvas to draw to.
     * @param bounds The bounds of the divider.
     * @param childIndex The index of child (of the View) above the divider.
     *            This will be -1 if there is no child above the divider to be
     *            drawn.
     */
    void drawDivider(Canvas canvas, Rect bounds, int childIndex) {
        // This widget draws the same divider for all children
        final Drawable divider = mDivider;
        final boolean clipDivider = mClipDivider;
       
		if (!clipDivider) {
			divider.setBounds(bounds);
		} else {
			canvas.save();
			canvas.clipRect(bounds);
		}

		divider.draw(canvas);

		if (clipDivider) {
			canvas.restore();
		}
		
		canvas.drawRect(bounds, new Paint());
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {

        // Draw the dividers
        final int dividerWidth = mDividerWidth;
        final boolean drawDividers = dividerWidth > 0;

        final int count = getChildCount();
        
        if (drawDividers && count > 0) {
            // Only modify the top and bottom in the loop, we set the left and right here
            final int top = this.getPaddingTop();
            final int bottom = this.getBottom() - this.getTop() - this.getPaddingBottom();
            
            final int itemCount = mItemCount;
            final int first = mFirstPosition;
        
            mTempRect.top = top;
        	mTempRect.bottom = bottom;
        	
        	mTempRect.left = this.getChildAt(0).getRight();
        	mTempRect.right = mTempRect.left + this.mDividerWidth;
        	
        	if (mDivider == null && mDividerPaint == null && mIsCacheColorOpaque) {
                mDividerPaint = new Paint();
                mDividerPaint.setColor(getCacheColorHint());
            }
            final Paint paint = mDividerPaint;
        	
            if ( first < itemCount - 1 && 
       			 mTempRect.left < (this.getWidth() - this.getPaddingRight()) ) {
	            if ( mDivider != null ) {
	            	this.drawDivider(canvas, mTempRect, -1);
	            } else {
	            	canvas.drawRect(mTempRect, paint);
	            }
            }
        	for (int i = 1; i < count; i++) {
        		mTempRect.left = this.getChildAt(i).getRight();
        		mTempRect.right = mTempRect.left + this.mDividerWidth;
        		if ( (first + i) < itemCount - 1 && 
        			 mTempRect.left < (this.getWidth() - this.getPaddingRight()) ) {
        			if ( mDivider != null ) {
                    	this.drawDivider(canvas, mTempRect, -1);
                    } else {
                    	canvas.drawRect(mTempRect, paint);
                    }
        		}
            }
            
        }
        
        super.dispatchDraw(canvas);
    }
    
    private void measureScrapChild(View child, int position, int heightMeasureSpec) {
		LayoutParams p = (LayoutParams) child.getLayoutParams();
		if (p == null) {
			p = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			child.setLayoutParams(p);
		}

		int childHeightSpec = ViewGroup.getChildMeasureSpec(heightMeasureSpec,
				mListPadding.top + mListPadding.bottom, p.height);
		int lpWidth = p.width;
		int childWidthSpec;
		if (lpWidth > 0) {
			childWidthSpec = MeasureSpec.makeMeasureSpec(lpWidth,
					MeasureSpec.EXACTLY);
		} else {
			childWidthSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}
    
	/**
     * Measures the height of the given range of children (inclusive) and
     * returns the height with this ListView's padding and divider heights
     * included. If maxHeight is provided, the measuring will stop when the
     * current height reaches maxHeight.
     *
     * @param widthMeasureSpec The width measure spec to be given to a child's
     *            {@link View#measure(int, int)}.
     * @param startPosition The position of the first child to be shown.
     * @param endPosition The (inclusive) position of the last child to be
     *            shown. Specify {@link #NO_POSITION} if the last child should be
     *            the last available child from the adapter.
     * @param maxHeight The maximum height that will be returned (if all the
     *            children don't fit in this value, this value will be
     *            returned).
     * @param disallowPartialChildPosition In general, whether the returned
     *            height should only contain entire children. This is more
     *            powerful--it is the first inclusive position at which partial
     *            children will not be allowed. Example: it looks nice to have
     *            at least 3 completely visible children, and in portrait this
     *            will most likely fit; but in landscape there could be times
     *            when even 2 children can not be completely shown, so a value
     *            of 2 (remember, inclusive) would be good (assuming
     *            startPosition is 0).
     * @return The height of this ListView with the given children.
     */
    final int measureWidthOfChildren(int heightMeasureSpec, int startPosition, int endPosition,
            final int maxWidth, int disallowPartialChildPosition) {

        final ListAdapter adapter = mAdapter;
        if (adapter == null) {
            return mListPadding.left + mListPadding.right;
        }

        // Include the padding of the list
        int returnedWidth = mListPadding.left + mListPadding.right;
        final int dividerWidth = ((mDividerWidth > 0) && mDivider != null) ? mDividerWidth : 0;
        
        // The previous height value that was less than maxHeight and contained
        // no partial children
        int prevHeightWithoutPartialChild = 0;
        int i;
        View child;

        // mItemCount - 1 since endPosition parameter is inclusive
        endPosition = (endPosition == NO_POSITION) ? adapter.getCount() - 1 : endPosition;

        for (i = startPosition; i <= endPosition; ++i) {
            child = obtainView(i);

            measureScrapChild(child, i, heightMeasureSpec);
            
            if (i > 0) {
                // Count the divider for all but one child
            	returnedWidth += dividerWidth;
            }
            
            // Recycle the view before we possibly return from the method
            mRecycledViewQueue.offer(child);
            
            returnedWidth += child.getMeasuredWidth();

            if (returnedWidth >= maxWidth) {
                // We went over, figure out which height to return.  If returnedHeight > maxHeight,
                // then the i'th position did not fit completely.
                return (disallowPartialChildPosition >= 0) // Disallowing is enabled (> -1)
                            && (i > disallowPartialChildPosition) // We've past the min pos
                            && (prevHeightWithoutPartialChild > 0) // We have a prev height
                            && (returnedWidth != maxWidth) // i'th child did not fit completely
                        ? prevHeightWithoutPartialChild
                        : maxWidth;
            }

            if ((disallowPartialChildPosition >= 0) && (i >= disallowPartialChildPosition)) {
                prevHeightWithoutPartialChild = returnedWidth;
            }
        }

        // At this point, we went through the range of children, and they each
        // completely fit, so return the returnedHeight
        return returnedWidth;
    }
    
	protected void measureChild(View child, int position) {
		if (child != null) {
			measureScrapChild(child, position, mHeightMeasureSpec);
		}
	}
	
	/**
     * Set the scrolled position of your view. This will cause a call to
     * {@link #onScrollChanged(int, int, int, int)} and the view will be
     * invalidated.
     * @param x the x position to scroll to
     * @param y the y position to scroll to
     */
    public void scrollTo(int x) {
    	resetList();
    	mNextPosX = x;
    	if (this.isShown()) {
    		int offset = (super.mItemWidth + mDividerWidth);
    		makeAndAddView(0, -offset, true, super.mListPadding.top);
    	} else {
    		final View child = obtainView(0);
			measureChild(child, 0);
			int offset = (child.getMeasuredWidth() + mDividerWidth);
			makeAndAddView(0, -offset, true, super.mListPadding.top);
    	}
    	mFirstPosition = -1;
    	
    	requestLayout();
    }
    
    /**
     * Move the scrolled position of your view. This will cause a call to
     * {@link #onScrollChanged(int, int, int, int)} and the view will be
     * invalidated.
     * @param x the amount of pixels to scroll by horizontally
     * @param y the amount of pixels to scroll by vertically
     */
    public void scrollBy(int deltaX) {
    	scrollTo(mNextPosX + deltaX);
    }
    
    /**
     * Return the scrolled left position of this view. This is the left edge of
     * the displayed part of your view. You do not need to draw any pixels
     * farther left, since those are outside of the frame of your view on
     * screen.
     *
     * @return The left edge of the displayed part of your view, in pixels.
     */
    public int getScroll() {
    	return mCurrentX;
    }
	
	/**
	 * 
	 * @param position
	 */
	public void scrollToItem(int position) {
		if (super.mAdapter == null) {
			return ;
		}
		if (position < super.mAdapter.getCount()) {
			if (this.isShown()) {
				final int x = position * (mItemWidth + mDividerWidth);
				scrollTo(x);
			} else {
				final View child = obtainView(0);
				measureChild(child, 0);
				final int x = position * (child.getMeasuredWidth() + mDividerWidth);
				scrollTo(x);
				super.mRecycledViewQueue.offer(child);
			}
		}
    }
	
	/**
	 * @param position
	 * @param duration
	 */
	public void scrollSmoothToItem(int position, int duration) {
		if (this.isShown()) {
			final int x = position * (mItemWidth + mDividerWidth);
			super.scrollSmoothTo(x, duration);
		} else {
			throw new RuntimeException("Don't call scrollSmoothToItem "
					+ "While it's not shown!");
		}
	}
	
	/**
	 * @param position
	 * @param duration
	 */
	public void scrollSmoothToItem(int position) {
		if (this.isShown()) {
			final int x = position * (mItemWidth + mDividerWidth);
			super.scrollSmoothTo(x);
		} else {
			throw new RuntimeException("Don't call scrollSmoothToItem "
					+ "While it's not shown!");
		}
	}
	
}
