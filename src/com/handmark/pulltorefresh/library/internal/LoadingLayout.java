

/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.handmark.pulltorefresh.library.internal;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.nostra13.universalimageloader.R;

public class LoadingLayout extends FrameLayout implements AnimationListener {

	static final int DEFAULT_ROTATION_ANIMATION_DURATION = 150;
	static final int DEFAULT_ROTATION_ARROW_ANIMATION_DURATION = 600;

	private Animation mInAnim, mOutAnim;
	private ImageView mArrowImageView;
	
	private AnimationDrawable refreshAnimationDrawable;
	private final Animation mRotateAnimation, mResetRotateAnimation;
	private final Animation mRotateLoadingAnimation;

	@SuppressWarnings("unused")
    private final Matrix mHeaderImageMatrix;
	private LinearLayout mLastRefresh;
	@SuppressWarnings("unused")
    private LinearLayout mHeader;
	private ViewGroup mLoadingTips;
	private TextView mLastRefreshTitleText;

	private final TextView mHeaderText;
	private final TextView mSubHeaderText;

	private String mPullLabel;
	private String mRefreshingLabel;
	private String mReleaseLabel;
	private String mLastRefreshTitleLabel;
	private Mode mMode;
	@SuppressWarnings("unused")
    private float mRotationPivotX, mRotationPivotY;

	public LoadingLayout(Context context, final Mode mode, TypedArray attrs) {
		super(context);
		ViewGroup header = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_header, this);
		mLoadingTips = (ViewGroup)header.findViewById(R.id.loading_tips);
		mHeaderText = (TextView) header.findViewById(R.id.pull_to_refresh_text);
		mSubHeaderText = (TextView) header.findViewById(R.id.pull_to_refresh_sub_text);
		mArrowImageView = (ImageView) header.findViewById(R.id.pull_to_refresh_image);
		mLastRefresh = (LinearLayout)header.findViewById(R.id.last_refresh);
		mLastRefreshTitleText = (TextView) header.findViewById(R.id.last_refresh_title);
		mHeader = (LinearLayout)header.findViewById(R.id.header);
		
//		mHeaderText.setTypeface(TypefaceHelper.getInstance(context).getTypeface());
//		mSubHeaderText.setTypeface(TypefaceHelper.getInstance(context).getTypeface());
//		mLastRefreshTitleText.setTypeface(TypefaceHelper.getInstance(context).getTypeface());
		
//		mArrowImageView.setScaleType(ScaleType.MATRIX);
		mHeaderImageMatrix = new Matrix();
		
		final Interpolator interpolator = new LinearInterpolator();
		mRotateLoadingAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateLoadingAnimation.setInterpolator(interpolator);
		mRotateLoadingAnimation.setDuration(DEFAULT_ROTATION_ARROW_ANIMATION_DURATION);
		mRotateLoadingAnimation.setRepeatCount(Animation.INFINITE);
		mRotateLoadingAnimation.setRepeatMode(Animation.RESTART);
		mMode = mode;
		
		int inAnimResId, outAnimResId;
		switch (mode) {
			case PULL_UP_TO_REFRESH:
				// Load in labels
				inAnimResId = R.anim.slide_in_from_bottom;
				outAnimResId = R.anim.slide_out_to_bottom;
				mPullLabel = context.getString(R.string.pull_up_to_refresh_pull_label);
				mRefreshingLabel = context.getString(R.string.pull_up_to_refresh_refreshing_label);
				mReleaseLabel = context.getString(R.string.pull_up_to_refresh_release_label);
				mArrowImageView.setImageResource(R.drawable.recommend_list_arrow_down);
				mLastRefreshTitleLabel = context.getString(R.string.pull_up_last_refresh);
//				LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams)mHeader.getLayoutParams();
//				llp.bottomMargin = context.getResources().getDimensionPixelSize(R.dimen.pull_bottom_margin);
//				mHeader.setLayoutParams(llp);
				break;

			case PULL_DOWN_TO_REFRESH:
			default:
				// Load in labels
				inAnimResId = R.anim.slide_in_from_top;
				outAnimResId = R.anim.slide_out_to_top;
				mPullLabel = context.getString(R.string.pull_to_refresh_from_bottom_pull_label);
				mRefreshingLabel = context.getString(R.string.pull_to_refresh_from_bottom_refreshing_label); 
				mReleaseLabel = context.getString(R.string.pull_to_refresh_from_bottom_release_label); 
				mLastRefreshTitleLabel = context.getString(R.string.last_refresh);
				mArrowImageView.setImageResource(R.drawable.recommend_list_arrow_up);

				if (attrs.hasValue(R.styleable.PullToRefresh_ptrPullLabel)) 
				{
				    mPullLabel = attrs.getString(R.styleable.PullToRefresh_ptrPullLabel);
		        }
				
				if(attrs.hasValue(R.styleable.PullToRefresh_ptrRefreshingLabel))
				{
				    mRefreshingLabel = attrs.getString(R.styleable.PullToRefresh_ptrRefreshingLabel);
				}
				
				if(attrs.hasValue(R.styleable.PullToRefresh_ptrReleaseLabel))
				{
				    mReleaseLabel = attrs.getString(R.styleable.PullToRefresh_ptrReleaseLabel);
				}
				if(attrs.hasValue(R.styleable.PullToRefresh_ptrLastRefreshLabel))
				{
				    mLastRefreshTitleLabel = attrs.getString(R.styleable.PullToRefresh_ptrLastRefreshLabel);
				}
				break;
		}
		
		mLastRefreshTitleText.setText(mLastRefreshTitleLabel);
		
		mInAnim = AnimationUtils.loadAnimation(context, inAnimResId);
		mInAnim.setAnimationListener(this);

		mOutAnim = AnimationUtils.loadAnimation(context, outAnimResId);
		mOutAnim.setAnimationListener(this);

		final Interpolator rotateInterpolator = new LinearInterpolator();
		mRotateAnimation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateAnimation.setInterpolator(rotateInterpolator);
		mRotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
		mRotateAnimation.setFillAfter(true);

		mResetRotateAnimation = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		mResetRotateAnimation.setInterpolator(rotateInterpolator);
		mResetRotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
		mResetRotateAnimation.setFillAfter(true);

		if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderTextColor)) {
			ColorStateList colors = attrs.getColorStateList(R.styleable.PullToRefresh_ptrHeaderTextColor);
			setTextColor(null != colors ? colors : ColorStateList.valueOf(0xFF545454));
		}
		if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderSubTextColor)) {
			ColorStateList colors = attrs.getColorStateList(R.styleable.PullToRefresh_ptrHeaderSubTextColor);
			setSubTextColor(null != colors ? colors : ColorStateList.valueOf(0xFF7e7e7e));
		}
		
		// Try and get defined drawable from Attrs
		Drawable imageDrawable = null;
		if (attrs.hasValue(R.styleable.PullToRefresh_ptrDrawable)) {
			imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawable);
		}

		// If we don't have a user defined drawable, load the default
		if (null == imageDrawable) {
			imageDrawable = context.getResources().getDrawable(R.drawable.recommend_list_arrow_down);
		}
		
		

		// Set Drawable, and save width/height
		setLoadingDrawable(imageDrawable);

		reset();
	}
	
	
	public int getLoadingTipsHeight(){
	    return mLoadingTips.getMeasuredHeight();
	}
	
	public void reset() {
		mHeaderText.setText(mPullLabel);
		mArrowImageView.setVisibility(View.VISIBLE);
		switch (mMode) {
			case PULL_UP_TO_REFRESH:
				// Load in labels
				mArrowImageView.setImageResource(R.drawable.recommend_list_arrow_down);
				break;

			case PULL_DOWN_TO_REFRESH:
			default:
				// Load in labels
				mArrowImageView.setImageResource(R.drawable.recommend_list_arrow_up);
				break;
		}
		mArrowImageView.clearAnimation();
		// clear refresh animation
		if (refreshAnimationDrawable != null) {
			refreshAnimationDrawable.stop();
			refreshAnimationDrawable = null;
			mArrowImageView.setBackgroundResource(android.R.color.transparent);
		}
		resetImageRotation();

		if (TextUtils.isEmpty(mSubHeaderText.getText())) {
			mLastRefresh.setVisibility(View.INVISIBLE);
		} else {
			mLastRefresh.setVisibility(View.VISIBLE);
		}
	}

	public void releaseToRefresh() {
		mHeaderText.setText(mReleaseLabel);
		mArrowImageView.startAnimation(mRotateAnimation);
		// clear refresh animation
		if (refreshAnimationDrawable != null) {
			refreshAnimationDrawable.stop();
			refreshAnimationDrawable = null;
			mArrowImageView.setBackgroundResource(android.R.color.transparent);
		}		
	}

	public void setPullLabel(String pullLabel) {
		mPullLabel = pullLabel;
	}

	public void refreshing() {
		mHeaderText.setText(mRefreshingLabel);
//		mArrowImageView.setImageResource(R.drawable.default_ptr_drawable);
//		mArrowImageView.startAnimation(mRotateLoadingAnimation);
		mArrowImageView.setImageResource(android.R.color.transparent);
		mArrowImageView.setBackgroundResource(R.drawable.refresh_loading);
		refreshAnimationDrawable = (AnimationDrawable) mArrowImageView.getBackground();
		refreshAnimationDrawable.start();
		mLastRefresh.setVisibility(View.VISIBLE);
	}

	public void setRefreshingLabel(String refreshingLabel) {
		mRefreshingLabel = refreshingLabel;
	}

	public void setReleaseLabel(String releaseLabel) {
		mReleaseLabel = releaseLabel;
	}

	public void pullToRefresh() {
		mHeaderText.setText(mPullLabel);
		mArrowImageView.startAnimation(mResetRotateAnimation);
		// clear refresh animation
		if (refreshAnimationDrawable != null) {
			refreshAnimationDrawable.stop();
			refreshAnimationDrawable = null;
			mArrowImageView.setBackgroundResource(android.R.color.transparent);
		}
	}

	public void setTextColor(ColorStateList color) {
		mHeaderText.setTextColor(color);
	}

	public void setSubTextColor(ColorStateList color) {
		mSubHeaderText.setTextColor(color);
		mLastRefreshTitleText.setTextColor(color);
	}

	public void setTextColor(int color) {
		setTextColor(ColorStateList.valueOf(color));
	}

	public void setLoadingDrawable(Drawable imageDrawable) {
		// Set Drawable, and save width/height
		mArrowImageView.setImageDrawable(imageDrawable);
		mRotationPivotX = imageDrawable.getIntrinsicWidth() / 2f;
		mRotationPivotY = imageDrawable.getIntrinsicHeight() / 2f;
	}

	public void setSubTextColor(int color) {
		setSubTextColor(ColorStateList.valueOf(color));
	}

	public void setSubHeaderText(CharSequence label) {
		if (TextUtils.isEmpty(label)) {
			mLastRefresh.setVisibility(INVISIBLE);
		} else {
			mSubHeaderText.setText(label);
			mLastRefresh.setVisibility(View.VISIBLE);
		}
	}

	public void onPullY(float scaleOfHeight) {
	}

	private void resetImageRotation() {
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (animation == mOutAnim) {
			mArrowImageView.clearAnimation();
			setVisibility(View.INVISIBLE);
		} else if (animation == mInAnim) {
			setVisibility(View.VISIBLE);
		}

		clearAnimation();
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// NO-OP
	}

	@Override
	public void onAnimationStart(Animation animation) {
//		setVisibility(View.VISIBLE);
	}
}
