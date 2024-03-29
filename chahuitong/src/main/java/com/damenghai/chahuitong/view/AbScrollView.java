package com.damenghai.chahuitong.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;

import com.damenghai.chahuitong.utils.DensityUtils;
import com.damenghai.chahuitong.utils.L;


public class AbScrollView extends ScrollView {
    private static final int DEFAULT_DISTANCE = 400;

	private static final int DEFAULT_POSITION = -1;
	// 内部View
	private View inner;
	// 点击时y的坐标
	private float y = DEFAULT_POSITION;
	private Rect normal = new Rect();

	// 滑动距离及坐标
	private float xDistance, yDistance, xLast, yLast;

    private OnDismissListener l;

    public interface OnDismissListener {
        void onDismiss();
    }

    public void setOnDismissListenre(OnDismissListener listenre) {
        l = listenre;
    }

	public AbScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * 根据XML生成视图工作完成，该函数在生成视图的最后调用，在所有子视图添加完成之后， 即使子类覆盖了onFinishInflate
	 * 方法，也应该调用父类的方法， 使得该方法得以执行
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		if (getChildCount() > 0) {
			inner = getChildAt(0);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (inner == null) {
			return super.onTouchEvent(ev);
		} else {
			commOnTouchEvent(ev);
		}

		return super.onTouchEvent(ev);
	}

	public void commOnTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			y = ev.getY();
			break;
		case MotionEvent.ACTION_UP:
			if (isNeedAnimation()) {
				animation();
			}

			y = DEFAULT_POSITION;
			break;

		/**
		 * 排除第一次移动计算，因为第一次无法得知y左边，在MotionEvent.ACTION_DOWN中获取不到，
		 * 因为此时是MyScrollView的Tocuh时间传递到了ListView的孩子item上面。所以从第二次开始计算
		 * 然而我们也要进行初始化，就是第一次移动的时候让滑动距离归零，之后记录准确了就正常执行
		 */
		case MotionEvent.ACTION_MOVE:
			float preY = y;
			float nowY = ev.getY();
			if (isDefaultPosition(y)) {
				preY = nowY;
			}
			int deltaY = (int) (preY - nowY);
			scrollBy(0, deltaY);
			y = nowY;
			// 当滚动到最上或者最下时就不会再滚动，这时移动布局
			if (isNeedMove()) {
				if (normal.isEmpty()) {
					// 保存正常的布局位置
					normal.set(inner.getLeft(), inner.getTop(),
							inner.getRight(), inner.getBottom());

				}
				// 移动布局
				inner.layout(inner.getLeft(), inner.getTop() - deltaY,
                        inner.getRight(), inner.getBottom() - deltaY);
			}
			break;

		default:
			break;
		}
	}

	// 开启动画移动

	public void animation() {
        if((inner.getTop() - normal.top) > DEFAULT_DISTANCE) {
            TranslateAnimation ta = new TranslateAnimation(0, 0, inner.getTop(), getHeight() - inner.getTop());
            ta.setDuration(200);
            ta.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if(l != null) l.onDismiss();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            inner.startAnimation(ta);
        } else if(isNeedMove()){
            // 开启移动动画
            TranslateAnimation ta = new TranslateAnimation(0, 0, inner.getTop() - normal.top, 0);
            ta.setDuration(200);
            inner.startAnimation(ta);
            // 设置回到正常的布局位置
            inner.layout(normal.left, normal.top, normal.right, normal.bottom);
        }
		normal.setEmpty();
	}

	// 是否需要开启动画
	public boolean isNeedAnimation() {
		return !normal.isEmpty();
	}

	// 是否需要移动布局
	public boolean isNeedMove() {
		int offset = inner.getMeasuredHeight() - getHeight();
		int scrollY = getScrollY();
		return scrollY == 0;
	}

	// 检查是否处于默认位置
	private boolean isDefaultPosition(float position) {
		return position == DEFAULT_POSITION;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xDistance = yDistance = 0f;
			xLast = ev.getX();
			yLast = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			final float curX = ev.getX();
			final float curY = ev.getY();

			xDistance += Math.abs(curX - xLast);
			yDistance += Math.abs(curY - yLast);
			xLast = curX;
			yLast = curY;

			if (xDistance > yDistance) {
				return false;
			}
		}

		return super.onInterceptTouchEvent(ev);
	}

}
