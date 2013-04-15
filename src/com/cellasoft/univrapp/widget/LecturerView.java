package com.cellasoft.univrapp.widget;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cellasoft.univrapp.Application;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.activity.R;
import com.cellasoft.univrapp.utils.Utils;

public class LecturerView extends LinearLayout {

	private static int TOUCH_ADDITION = 20;
	private static final int COLOR_SELECT_AREA = Application.getInstance()
			.getResources()
			.getColor(Settings.getUniversity().color_from_resource);;// Color.argb(50,
																		// 255,
																		// 0,
																		// 0);

	private static class TouchDelegateRecord {
		public Rect rect;
		public int color;

		public TouchDelegateRecord(Rect _rect, int _color) {
			rect = _rect;
			color = _color;
		}
	}

	private final ArrayList<TouchDelegateRecord> mTouchDelegateRecords = new ArrayList<LecturerView.TouchDelegateRecord>();
	private final Paint mPaint = new Paint();

	private ImageButton mSelectButton;
	private TextView mTextView;
	private TextView description;

	private TouchDelegateGroup mTouchDelegateGroup;
	private OnLecturerViewListener lecturerListener;

	private int mTouchAddition;

	private boolean mIsSelected;

	private int mPreviousWidth = -1;
	private int mPreviousHeight = -1;

	public LecturerView(Context context) {
		super(context);
		init(context);
	}

	public LecturerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {

		setOrientation(LinearLayout.HORIZONTAL);
		setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

		mTouchDelegateGroup = new TouchDelegateGroup(this);
		mPaint.setStyle(Style.FILL);

		// Determine screen size
		switch (Utils.getScreenSize()) {
		case Configuration.SCREENLAYOUT_SIZE_LARGE:
		case Configuration.SCREENLAYOUT_SIZE_NORMAL:
			TOUCH_ADDITION = 20;
			break;
		case Configuration.SCREENLAYOUT_SIZE_SMALL:
			TOUCH_ADDITION = 15;
			break;
		default:
			TOUCH_ADDITION = 20;
		}

		final float density = context.getResources().getDisplayMetrics().density;
		mTouchAddition = (int) (density * TOUCH_ADDITION + 0.5f);

		LayoutInflater.from(context).inflate(R.layout.lecturer, this);

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mSelectButton = (ImageButton) findViewById(R.id.lecturer_check);
		mSelectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				setItemViewSelected(!mIsSelected);
				if (lecturerListener != null) {
					lecturerListener.onSelected(LecturerView.this, mIsSelected);
				}
			}
		});

		mTextView = (TextView) findViewById(R.id.lecturer_name);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		final int width = r - l;
		final int height = b - t;

		/*
		 * We can't use onSizeChanged here as this is called before the layout
		 * of child View is actually done ... Because we need the size of some
		 * child children we need to check for View size change manually
		 */
		if (width != mPreviousWidth || height != mPreviousHeight) {

			mPreviousWidth = width;
			mPreviousHeight = height;

			mTouchDelegateGroup.clearTouchDelegates();

			// @formatter:off
			addTouchDelegate(new Rect(width - mSelectButton.getWidth()
					- mTouchAddition, 0, width, height), COLOR_SELECT_AREA,
					mSelectButton);

			// @formatter:on

			setTouchDelegate(mTouchDelegateGroup);
		}
	}

	private void addTouchDelegate(Rect rect, int color, View delegateView) {
		mTouchDelegateGroup.addTouchDelegate(new TouchDelegate(rect,
				delegateView));
		mTouchDelegateRecords.add(new TouchDelegateRecord(rect, color));
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		for (TouchDelegateRecord record : mTouchDelegateRecords) {
			mPaint.setColor(record.color);
			canvas.drawRect(record.rect, mPaint);
		}
		super.dispatchDraw(canvas);
	}

	/**
	 * Register a listener to be notified of changes on this item view.
	 * 
	 * @param listener
	 *            The listener to set
	 */
	public void setLecturerListener(OnLecturerViewListener listener) {
		lecturerListener = listener;
	}

	/**
	 * Returns the underlying {@link TextView}.
	 */
	public TextView getTitleView() {
		return mTextView;
	}

	/**
	 * Select/unselect the view.
	 * 
	 * @param selected
	 *            The new selection state
	 */
	public void setItemViewSelected(boolean selected) {
		if (mIsSelected != selected) {
			mIsSelected = selected;
			mSelectButton
					.setImageResource(mIsSelected ? R.drawable.btn_check_on_normal
							: R.drawable.btn_check_off_normal);
		}
	}
}
