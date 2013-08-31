package com.cellasoft.univrapp.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ListAdapter;

import com.cellasoft.univrapp.utils.UIUtils;

public class ContactListView extends BasePullListView<ContactItemInterface> {

	protected boolean mIsFastScrollEnabled = false;
	protected IndexScroller mScroller = null;
	protected GestureDetector mGestureDetector = null;

	// additional customization
	protected boolean inSearchMode = false; // whether is in search mode
	protected boolean autoHide = true; // alway show the scroller

	public ContactListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ContactListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void init(Context context) {
		super.init(context);
		this.setChoiceMode(CHOICE_MODE_SINGLE);
		this.setVerticalScrollBarEnabled(false);
	}

	public IndexScroller getScroller() {
		return mScroller;
	}

	public void setInSearchMode(boolean isInSearchMode) {
		this.inSearchMode = isInSearchMode;
	}

	public boolean isInSearchMode() {
		return inSearchMode;
	}

	@Override
	public boolean isFastScrollEnabled() {
		return mIsFastScrollEnabled;
	}

	// override this if necessary for custom scroller
	public void createScroller() {
		mScroller = new IndexScroller(getContext(), this);
		mScroller.setAutoHide(autoHide);
		mScroller.setShowIndexContainer(true);

		if (autoHide)
			mScroller.hide();
		else
			mScroller.show();
	}

	@Override
	public void setFastScrollEnabled(boolean enabled) {
		mIsFastScrollEnabled = enabled;
		if (mIsFastScrollEnabled) {
			if (mScroller == null) {
				createScroller();
			}
		} else {
			if (mScroller != null) {
				mScroller.hide();
				mScroller = null;
			}
		}
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);

		// Overlay index bar
		if (!inSearchMode) // dun draw the scroller if not in search mode
		{
			if (mScroller != null)
				mScroller.draw(canvas);
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (inSearchMode)
			return false;
		// Intercept ListView's touch event
		if (mScroller != null && mScroller.onTouchEvent(ev))
			return true;

		if (mGestureDetector == null) {
			mGestureDetector = new GestureDetector(getContext(),
					new GestureDetector.SimpleOnGestureListener() {

						@Override
						public boolean onFling(MotionEvent e1, MotionEvent e2,
								float velocityX, float velocityY) {
							// If fling happens, index bar shows
							mScroller.show();
							return super.onFling(e1, e2, velocityX, velocityY);
						}

					});
		}
		mGestureDetector.onTouchEvent(ev);

		return super.onTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (isHeader(ev) || isImageThumb(ev)) {
			return false;
		}
		return false;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		if (mScroller != null)
			mScroller.setAdapter(adapter);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mScroller != null)
			mScroller.onSizeChanged(w, h, oldw, oldh);
	}

	private boolean isHeader(MotionEvent ev) {
		return 0 == pointToPosition((int) ev.getX(), (int) ev.getY());
	}

	private boolean isImageThumb(MotionEvent ev) {
		return ev.getX() <= UIUtils.getTouchAddition(getContext());
	}

}
