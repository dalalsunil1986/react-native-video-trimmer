package com.creedon.reactlibrary.videotrimmer.widget;
/**
 * _   _ _______   ________ _       _____   __
 * | \ | |_   _\ \ / /| ___ \ |     / _ \ \ / /
 * |  \| | | |  \ V / | |_/ / |    / /_\ \ V /
 * | . ` | | |  /   \ |  __/| |    |  _  |\ /
 * | |\  |_| |_/ /^\ \| |   | |____| | | || |
 * \_| \_/\___/\/   \/\_|   \_____/\_| |_/\_/
 * <p>
 * modified by jameskong on 12/2/2019.
 */
 /**
 * author : J.Chou
 * e-mail : who_know_me@163.com
 * time   : 2019/01/21 6:02 PM
 * version: 1.0
 * description:
 */
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.creedon.reactlibrary.videotrimmer.R;
import com.creedon.reactlibrary.videotrimmer.features.trim.VideoTrimmerAdapter;
import com.creedon.reactlibrary.videotrimmer.features.trim.VideoTrimmerUtil;
import com.creedon.reactlibrary.videotrimmer.interfaces.IVideoTrimmerView;
import com.creedon.reactlibrary.videotrimmer.interfaces.VideoTrimListener;
import com.creedon.reactlibrary.videotrimmer.utils.StorageUtil;

import iknow.android.utils.DateUtil;
import iknow.android.utils.callback.SingleCallback;
import iknow.android.utils.thread.BackgroundExecutor;
import iknow.android.utils.thread.UiThreadExecutor;

import static com.creedon.reactlibrary.videotrimmer.features.trim.VideoTrimmerUtil.VIDEO_FRAMES_WIDTH;


public class VideoTrimmerView extends FrameLayout implements IVideoTrimmerView {

	private static final String TAG = VideoTrimmerView.class.getSimpleName();

	private int mMaxWidth = VIDEO_FRAMES_WIDTH;
	private Context mContext;
	private RelativeLayout mLinearVideo;
	private ZVideoView mVideoView;
	//	private ImageView mPlayView;
	private RecyclerView mVideoThumbRecyclerView;
	private RangeSeekBarView mRangeSeekBarView;
	private LinearLayout mSeekBarLayout;
	private ImageView mRedProgressIcon;
	//	private TextView mVideoShootTipTv;
	private TextView mVideoDurationTv;
	private float mAverageMsPx;//每毫秒所占的px
	private float averagePxMs;//每px所占用的ms毫秒
	private Uri mSourceUri;
	private VideoTrimListener mOnTrimVideoListener;
	private int mDuration = 0;
	private VideoTrimmerAdapter mVideoThumbAdapter;
	private boolean isFromRestore = false;
	//new
	private long mLeftProgressPos, mRightProgressPos;
	private long mRedProgressBarPos = 0;
	private long scrollPos = 0;
	private int mScaledTouchSlop;
	private int lastScrollX;
	private boolean isSeeking;
	private boolean isOverScaledTouchSlop;
	private int mThumbsTotalCount;
	private ValueAnimator mRedProgressAnimator;
	private Handler mAnimationHandler = new Handler();
	private TextView mVideoRangeTv;
	private long minLength = -1;
	private long maxLength = -1;
	private boolean bTranscode = false;
	private TextView finishBtn;
	private long mOrigLeftProgressPos;
	private long mOrigRightProgressPos;
	private int mOrigDuration;

	public VideoTrimmerView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VideoTrimmerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
		this.mContext = context;
		LayoutInflater.from(context).inflate(R.layout.video_trimmer_view, this, true);

		mLinearVideo = findViewById(R.id.layout_surface_view);
		mVideoView = findViewById(R.id.video_loader);
//		mPlayView = findViewById(R.id.icon_video_play);
		mSeekBarLayout = findViewById(R.id.seekBarLayout);
		mRedProgressIcon = findViewById(R.id.positionIcon);
//		mVideoShootTipTv = findViewById(R.id.video_shoot_tip);
		mVideoDurationTv = findViewById(R.id.video_duration_tv);
		mVideoRangeTv = findViewById(R.id.video_range_tv);
		mVideoThumbRecyclerView = findViewById(R.id.video_frames_recyclerView);
		mVideoThumbRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
		mVideoThumbAdapter = new VideoTrimmerAdapter(mContext);
		mVideoThumbRecyclerView.setAdapter(mVideoThumbAdapter);
		mVideoThumbRecyclerView.addOnScrollListener(mOnScrollListener);
		setUpListeners();
	}

	private void initRangeSeekBarView(long minLength, long maxLength) {
		this.minLength = minLength;
		this.maxLength = maxLength;
		if (mRangeSeekBarView != null) return;
		int rangeWidth;
		mOrigLeftProgressPos = mLeftProgressPos = 0;
		if (mDuration <= getVideoMaxDuration()) {
			mThumbsTotalCount = VideoTrimmerUtil.MAX_COUNT_RANGE;
			rangeWidth = mMaxWidth;
			mOrigRightProgressPos = mRightProgressPos = mDuration;
		} else {
			mThumbsTotalCount = (int) (mDuration * 1.0f / (getVideoMaxDuration() * 1.0f) * VideoTrimmerUtil.MAX_COUNT_RANGE);
			rangeWidth = mMaxWidth / VideoTrimmerUtil.MAX_COUNT_RANGE * mThumbsTotalCount;
			mOrigRightProgressPos = mRightProgressPos = getVideoMaxDuration();
		}
		mVideoThumbRecyclerView.addItemDecoration(new SpacesItemDecoration2(VideoTrimmerUtil.RECYCLER_VIEW_PADDING, mThumbsTotalCount));
		mRangeSeekBarView = new RangeSeekBarView(mContext, mLeftProgressPos, mRightProgressPos);
		mRangeSeekBarView.setSelectedMinValue(mLeftProgressPos);
		mRangeSeekBarView.setSelectedMaxValue(mRightProgressPos);
		mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos);
		mRangeSeekBarView.setMinShootTime(getMinVideoDuration());
		mRangeSeekBarView.setNotifyWhileDragging(true);
		mRangeSeekBarView.setOnRangeSeekBarChangeListener(mOnRangeSeekBarChangeListener);
		mSeekBarLayout.addView(mRangeSeekBarView);

		mAverageMsPx = mDuration * 1.0f / rangeWidth * 1.0f;
		averagePxMs = (mMaxWidth * 1.0f / (mRightProgressPos - mLeftProgressPos));


		mOrigDuration = mDuration = Math.round(mRightProgressPos - mLeftProgressPos);
		String leftThumbsTime = DateUtil.convertSecondsToTime((long) Math.round(mLeftProgressPos / 1000.0f));
		String rightThumbsTime = DateUtil.convertSecondsToTime((long) Math.round(mRightProgressPos / 1000.0f));
		String durationTime = DateUtil.convertSecondsToTime((long) Math.round(mDuration / 1000.0f));
		mVideoRangeTv.setText(String.format("%s to %s", leftThumbsTime, rightThumbsTime));
		mVideoDurationTv.setText(durationTime);
	}

	public void initVideoByURI(final Uri videoURI, final long minLength, final long maxLength, boolean bTranscode) {
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.bTranscode = bTranscode;
		initVideoByURI(videoURI);
	}

	@SuppressLint("DefaultLocale")
	public void initVideoByURI(final Uri videoURI) {
		mSourceUri = videoURI;
		mVideoView.setVideoURI(videoURI);
		mVideoView.requestFocus();
//		mVideoShootTipTv.setText(String.format(mContext.getResources().getString(R.string.video_shoot_tip), VideoTrimmerUtil.VIDEO_MAX_TIME));
		mVideoDurationTv.setText(String.format("%d", getVideoMaxTime()));
	}

	private int getVideoMaxTime() {
		return this.maxLength > 0 ? (int) (maxLength / 1000) : VideoTrimmerUtil.VIDEO_MAX_TIME;
	}

	private void startShootVideoThumbs(final Context context, final Uri videoUri, int totalThumbsCount, long startPosition, long endPosition) {
		VideoTrimmerUtil.shootVideoThumbInBackground(context, videoUri, totalThumbsCount, startPosition, endPosition,
				new SingleCallback<Bitmap, Integer>() {
					@Override
					public void onSingleCallback(final Bitmap bitmap, final Integer interval) {
						if (bitmap != null) {
							UiThreadExecutor.runTask("", new Runnable() {
								@Override
								public void run() {
									mVideoThumbAdapter.addBitmaps(bitmap);
								}
							}, 0L);
						}
					}
				});
	}

	private void onCancelClicked() {
		mOnTrimVideoListener.onCancel();
	}

	private void videoPrepared(MediaPlayer mp) {
		ViewGroup.LayoutParams lp = mVideoView.getLayoutParams();
		int videoWidth = mp.getVideoWidth();
		int videoHeight = mp.getVideoHeight();

		float videoProportion = (float) videoWidth / (float) videoHeight;
		int screenWidth = mLinearVideo.getWidth();
		int screenHeight = mLinearVideo.getHeight();

		if (videoHeight > videoWidth) {
			lp.width = screenWidth;
			lp.height = screenHeight;
		} else {
			lp.width = screenWidth;
			float r = videoHeight / (float) videoWidth;
			lp.height = (int) (lp.width * r);
		}
		mVideoView.setLayoutParams(lp);
		mDuration = mVideoView.getDuration();
		if (!getRestoreState()) {
			seekTo((int) mRedProgressBarPos);
		} else {
			setRestoreState(false);
			seekTo((int) mRedProgressBarPos);
		}
		initRangeSeekBarView(this.minLength, this.maxLength);

		startShootVideoThumbs(mContext, mSourceUri, mThumbsTotalCount, 0, mDuration);

	}

	private void videoCompleted() {
		seekTo(mLeftProgressPos);
		setPlayPauseViewIcon(false);
	}

	private void onVideoReset() {
		mVideoView.pause();
		setPlayPauseViewIcon(false);
	}

	private void playVideoOrPause() {
		mRedProgressBarPos = mVideoView.getCurrentPosition();
		if (mVideoView.isPlaying()) {
			mVideoView.pause();
			pauseRedProgressAnimation();
		} else {
			mVideoView.start();
			playingRedProgressAnimation();
		}
		setPlayPauseViewIcon(mVideoView.isPlaying());
	}

	public void onVideoPause() {
		if (mVideoView.isPlaying()) {
			seekTo(mLeftProgressPos);//复位
			mVideoView.pause();
			setPlayPauseViewIcon(false);
			mRedProgressIcon.setVisibility(GONE);
		}
	}

	public void setOnTrimVideoListener(VideoTrimListener onTrimVideoListener) {
		mOnTrimVideoListener = onTrimVideoListener;
	}

	@SuppressLint("ClickableViewAccessibility")
	private void setUpListeners() {
		findViewById(R.id.cancelBtn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				onCancelClicked();
			}
		});

		finishBtn = findViewById(R.id.finishBtn);
		finishBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				onSaveClicked();
			}
		});
		mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
				videoPrepared(mp);
			}
		});
		mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				videoCompleted();
			}
		});
		mVideoView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				playVideoOrPause();
				return false;
			}
		});
//    mPlayView.setOnClickListener(new OnClickListener() {
//      @Override public void onClick(View v) {
//        playVideoOrPause();
//      }
//    });
	}

	private void onSaveClicked() {
		if (mRightProgressPos - mLeftProgressPos < getMinVideoDuration()) {
			Toast.makeText(mContext, "Video too short", Toast.LENGTH_SHORT).show();
		} else {
			mVideoView.pause();
			VideoTrimmerUtil.trim(mContext,
					mSourceUri.getPath(),
					StorageUtil.getCacheDir(),
					mLeftProgressPos,
					mRightProgressPos,
					bTranscode,
					mOnTrimVideoListener);
		}
	}

	private void seekTo(long msec) {
		mVideoView.seekTo((int) msec);
	}

	private boolean getRestoreState() {
		return isFromRestore;
	}

	public void setRestoreState(boolean fromRestore) {
		isFromRestore = fromRestore;
	}

	private void setPlayPauseViewIcon(boolean isPlaying) {
//		if (isPlaying) {
//			mPlayView.setImageDrawable(null);
//		} else {
//			mPlayView.setImageResource(R.drawable.icon_video_play_black);
//		}
	}

	private final RangeSeekBarView.OnRangeSeekBarChangeListener mOnRangeSeekBarChangeListener = new RangeSeekBarView.OnRangeSeekBarChangeListener() {
		@SuppressLint("DefaultLocale")
		@Override
		public void onRangeSeekBarValuesChanged(RangeSeekBarView bar, long minValue, long maxValue, int action, boolean isMin,
		                                        RangeSeekBarView.Thumb pressedThumb) {
//			Log.d(TAG, "-----minValue----->>>>>>" + minValue);
//			Log.d(TAG, "-----maxValue----->>>>>>" + maxValue);
			mLeftProgressPos = minValue + scrollPos;
			mRedProgressBarPos = mLeftProgressPos;
			mRightProgressPos = maxValue + scrollPos;
//			Log.d(TAG, "-----mLeftProgressPos----->>>>>>" + mLeftProgressPos);
//			Log.d(TAG, "-----mRightProgressPos----->>>>>>" + mRightProgressPos);
			switch (action) {
				case MotionEvent.ACTION_DOWN:
					isSeeking = false;
					break;
				case MotionEvent.ACTION_MOVE:
					isSeeking = true;
					seekTo((int) (pressedThumb == RangeSeekBarView.Thumb.MIN ? mLeftProgressPos : mRightProgressPos));
					break;
				case MotionEvent.ACTION_UP:
					isSeeking = false;
					seekTo((int) mLeftProgressPos);
					break;
				default:
					break;
			}

			String leftThumbsTime = DateUtil.convertSecondsToTime((long) Math.round(mLeftProgressPos / 1000.0f));
			String rightThumbsTime = DateUtil.convertSecondsToTime((long) Math.round(mRightProgressPos / 1000.0f));
			mDuration = Math.round(mRightProgressPos - mLeftProgressPos);
			String durationTime = DateUtil.convertSecondsToTime((long) Math.round(mDuration / 1000.0f));
			Log.d(TAG, "-----leftThumbsTime----->>>>>>" + leftThumbsTime);
			Log.d(TAG, "-----rightThumbsTime----->>>>>>" + rightThumbsTime);
			Log.d(TAG, "-----durationTime----->>>>>>" + durationTime);
			mVideoRangeTv.setText(String.format("%s to %s", leftThumbsTime, rightThumbsTime));
			mVideoDurationTv.setText(durationTime);
			mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos);
			if(Math.abs(mOrigLeftProgressPos-mLeftProgressPos) < 100  && Math.abs(mOrigRightProgressPos-mRightProgressPos) < 100 ) {
				finishBtn.setEnabled(false);
			} else {
				finishBtn.setEnabled(true);
			}
		}
	};

	private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
		@Override
		public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
			super.onScrollStateChanged(recyclerView, newState);
			Log.d(TAG, "newState = " + newState);
		}

		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			super.onScrolled(recyclerView, dx, dy);
			isSeeking = false;
			int scrollX = calcScrollXDistance();
			//达不到滑动的距离
			if (Math.abs(lastScrollX - scrollX) < mScaledTouchSlop) {
				isOverScaledTouchSlop = false;
				return;
			}
			isOverScaledTouchSlop = true;
			//初始状态,why ? 因为默认的时候有35dp的空白！
			if (scrollX == -VideoTrimmerUtil.RECYCLER_VIEW_PADDING) {
				scrollPos = 0;
				updateUI(scrollPos);
			} else {
				isSeeking = true;
				scrollPos = (long) (mAverageMsPx * (VideoTrimmerUtil.RECYCLER_VIEW_PADDING + scrollX));
				updateUI(scrollPos);
			}
			lastScrollX = scrollX;
		}
	};

	private void updateUI(long scrollPos) {
		mLeftProgressPos = mRangeSeekBarView.getSelectedMinValue() + scrollPos;
		mRightProgressPos = mRangeSeekBarView.getSelectedMaxValue() + scrollPos;
		mRedProgressBarPos = mLeftProgressPos;
		if (mVideoView.isPlaying()) {
			mVideoView.pause();
			setPlayPauseViewIcon(false);
		}
		mRedProgressIcon.setVisibility(GONE);
		seekTo(mLeftProgressPos);
		mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos);
		mRangeSeekBarView.invalidate();
		String leftThumbsTime = DateUtil.convertSecondsToTime((long) Math.round(mLeftProgressPos / 1000.0f));
		String rightThumbsTime = DateUtil.convertSecondsToTime((long) Math.round(mRightProgressPos / 1000.0f));
		mVideoRangeTv.setText(String.format("%s to %s", leftThumbsTime, rightThumbsTime));

		if(Math.abs(mOrigLeftProgressPos-mLeftProgressPos) < 100  && Math.abs(mOrigRightProgressPos-mRightProgressPos) < 100 ) {
			finishBtn.setEnabled(false);
		} else {
			finishBtn.setEnabled(true);
		}
	}

	/**
	 * 水平滑动了多少px
	 */
	private int calcScrollXDistance() {
		LinearLayoutManager layoutManager = (LinearLayoutManager) mVideoThumbRecyclerView.getLayoutManager();
		int position = layoutManager.findFirstVisibleItemPosition();
		View firstVisibleChildView = layoutManager.findViewByPosition(position);
		int itemWidth = firstVisibleChildView.getWidth();
		return (position) * itemWidth - firstVisibleChildView.getLeft();
	}

	private void playingRedProgressAnimation() {
		pauseRedProgressAnimation();
		playingAnimation();
		mAnimationHandler.post(mAnimationRunnable);
	}

	private void playingAnimation() {
		if (mRedProgressIcon.getVisibility() == View.GONE) {
			mRedProgressIcon.setVisibility(View.VISIBLE);
		}
		final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mRedProgressIcon.getLayoutParams();
		int start = (int) (VideoTrimmerUtil.RECYCLER_VIEW_PADDING + (mRedProgressBarPos - scrollPos) * averagePxMs);
		int end = (int) (VideoTrimmerUtil.RECYCLER_VIEW_PADDING + (mRightProgressPos - scrollPos) * averagePxMs);
		mRedProgressAnimator = ValueAnimator.ofInt(start, end).setDuration((mRightProgressPos - scrollPos) - (mRedProgressBarPos - scrollPos));
		mRedProgressAnimator.setInterpolator(new LinearInterpolator());
		mRedProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				params.leftMargin = (int) animation.getAnimatedValue();
				mRedProgressIcon.setLayoutParams(params);
//				Log.d(TAG, "----onAnimationUpdate--->>>>>>>" + mRedProgressBarPos);
			}
		});
		mRedProgressAnimator.start();
	}

	private void pauseRedProgressAnimation() {
		mRedProgressIcon.clearAnimation();
		if (mRedProgressAnimator != null && mRedProgressAnimator.isRunning()) {
			mAnimationHandler.removeCallbacks(mAnimationRunnable);
			mRedProgressAnimator.cancel();
		}
	}

	private Runnable mAnimationRunnable = new Runnable() {

		@Override
		public void run() {
			updateVideoProgress();
		}
	};

	private void updateVideoProgress() {
		long currentPosition = mVideoView.getCurrentPosition();
//		Log.d(TAG, "updateVideoProgress currentPosition = " + currentPosition);
		if (currentPosition >= (mRightProgressPos)) {
			mRedProgressBarPos = mLeftProgressPos;
			pauseRedProgressAnimation();
			onVideoPause();
		} else {
			mAnimationHandler.post(mAnimationRunnable);
		}
	}

	/**
	 * Cancel trim thread execut action when finish
	 */
	@Override
	public void onDestroy() {
		BackgroundExecutor.cancelAll("", true);
		UiThreadExecutor.cancelAll("");
	}

	long getMinVideoDuration() {
		return this.minLength > 0 ? minLength : VideoTrimmerUtil.MIN_SHOOT_DURATION;
	}

	long getVideoMaxDuration() {
		return this.maxLength > 0 ? maxLength : VideoTrimmerUtil.MAX_SHOOT_DURATION;
	}
}
