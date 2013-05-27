package com.fourmob.poppyview;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ScrollView;


public class PoppyViewHelper {

	private static final int SCROLL_TO_TOP = - 1;

	private static final int SCROLL_TO_BOTTOM = 1;

	private static final int SCROLL_DIRECTION_CHANGE_THRESHOLD = 5;

	private Activity mActivity;

	private LayoutInflater mLayoutInflater;

	private View mPoppyView;

	private int mScrollDirection = 0;

	private int mPoppyViewHeight = - 1;

	public PoppyViewHelper(Activity activity) {
		this.mActivity = activity;
		this.mLayoutInflater = LayoutInflater.from(activity);
	}

	// for scrollview

	public View createPoppyViewOnScrollView(int scrollViewId, int poppyViewResId) {
		mPoppyView = mLayoutInflater.inflate(poppyViewResId, null);
		final NotifyingScrollView scrollView = (NotifyingScrollView)mActivity.findViewById(scrollViewId);
		initPoppyViewOnScrollView(scrollView);
		return mPoppyView;
	}

	// for ListView

	public View createPoppyViewOnListView(int listViewId, int poppyViewResId) {
		final ListView listView = (ListView)mActivity.findViewById(listViewId);
		if(listView.getHeaderViewsCount() != 0) {
			throw new IllegalArgumentException("use createPoppyViewOnListView with headerResId parameter");
		}
		if(listView.getFooterViewsCount() != 0) {
			throw new IllegalArgumentException("poppyview library doesn't support listview with footer");
		}
		mPoppyView = mLayoutInflater.inflate(poppyViewResId, null);
		initPoppyViewOnListView(listView);
		return mPoppyView;
	}

	// common

	private void setPoppyViewOnView(View view) {
		LayoutParams lp = view.getLayoutParams();
		ViewParent parent = view.getParent();
		ViewGroup group = (ViewGroup)parent;
		int index = group.indexOfChild(view);
		final FrameLayout newContainer = new FrameLayout(mActivity);
		group.removeView(view);
		group.addView(newContainer, index, lp);
		newContainer.addView(view);
		final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.BOTTOM;
		newContainer.addView(mPoppyView, layoutParams);
		group.invalidate();
	}

	private void onScrollPositionChanged(int oldScrollPosition, int newScrollPosition) {
		int newScrollDirection;

		System.out.println(oldScrollPosition + " ->" + newScrollPosition);

		if(newScrollPosition < oldScrollPosition) {
			newScrollDirection = SCROLL_TO_TOP;
		} else {
			newScrollDirection = SCROLL_TO_BOTTOM;
		}

		if(newScrollDirection != mScrollDirection) {
			mScrollDirection = newScrollDirection;
			translateYPoppyView();
		}
	}

	private void translateYPoppyView() {
		mPoppyView.post(new Runnable() {

			@Override
			public void run() {
				if(mPoppyViewHeight <= 0) {
					mPoppyViewHeight = mPoppyView.getHeight();
				}
				final int translationY = mScrollDirection == SCROLL_TO_TOP ? 0 : mPoppyViewHeight;
				mPoppyView.animate().translationY(translationY);
			}
		});
	}

	// for ScrollView

	private void initPoppyViewOnScrollView(NotifyingScrollView scrollView) {
		setPoppyViewOnView(scrollView);
		scrollView.setOnScrollChangedListener(new NotifyingScrollView.OnScrollChangedListener() {

			int mScrollPosition;

			public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
				if(Math.abs(t - mScrollPosition) >= SCROLL_DIRECTION_CHANGE_THRESHOLD) {
					onScrollPositionChanged(mScrollPosition, t);
				}

				mScrollPosition = t;
			}
		});
	}

	// for ListView

	private void initPoppyViewOnListView(ListView listView) {
		setPoppyViewOnView(listView);
		listView.setOnScrollListener(new OnScrollListener() {

			int mScrollPosition;

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				View topChild = view.getChildAt(0);

				int newScrollPosition = 0;
				if(topChild == null) {
					newScrollPosition = 0;
				} else {
					newScrollPosition = - topChild.getTop() + view.getFirstVisiblePosition() * topChild.getHeight();
				}

				if(Math.abs(newScrollPosition - mScrollPosition) >= SCROLL_DIRECTION_CHANGE_THRESHOLD) {
					onScrollPositionChanged(mScrollPosition, newScrollPosition);
				}

				mScrollPosition = newScrollPosition;
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}
		});
	}
}