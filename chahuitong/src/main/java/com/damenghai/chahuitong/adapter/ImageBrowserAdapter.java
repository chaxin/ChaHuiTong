package com.damenghai.chahuitong.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.damenghai.chahuitong.R;
import com.damenghai.chahuitong.bean.ImageUrls;
import com.damenghai.chahuitong.utils.DisplayUtils;
import com.damenghai.chahuitong.utils.L;
import com.damenghai.chahuitong.utils.T;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;

import java.util.ArrayList;

/**
 * Created by Sgun on 15/8/28.
 */
public class ImageBrowserAdapter extends PagerAdapter {
    private Context mContext;
    private ArrayList<ImageUrls> mImageUrlses;
    private ArrayList<View> mViews;

    public ImageBrowserAdapter(Context mContext, ArrayList<ImageUrls> imageUrls) {
        this.mContext = mContext;
        this.mImageUrlses = imageUrls;
        initView();
    }

    private void initView() {
        mViews = new ArrayList<View>();

        if(mImageUrlses != null) {
            for (int i = 0; i < mImageUrlses.size(); i++) {
                View view = View.inflate(mContext, R.layout.item_image_browser, null);
                mViews.add(view);
            }
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mViews.get(position);
        final ImageView iv = (ImageView) view.findViewById(R.id.iv_image_browser);
        BitmapUtils utils = new BitmapUtils(mContext);

        utils.display(iv, mImageUrlses.get(position).getBmiddle_pic(), new BitmapLoadCallBack<ImageView>() {
            @Override
            public void onLoadCompleted(ImageView imageView, String s, Bitmap bitmap, BitmapDisplayConfig bitmapDisplayConfig, BitmapLoadFrom bitmapLoadFrom) {
                float scale = bitmap.getHeight() / bitmap.getWidth();
                int screenWidthPixels = DisplayUtils.getScreenWidthPixels((Activity) mContext);
                int screenHeightPixels = DisplayUtils.getScreenHeightPixels((Activity) mContext);
                int height = (int) (screenWidthPixels * scale);

                if(height < screenHeightPixels) height = screenHeightPixels;

                ViewGroup.LayoutParams params = iv.getLayoutParams();
                params.width = screenWidthPixels;
                params.height = height;

                iv.setLayoutParams(params);
                iv.setImageBitmap(bitmap);
            }

            @Override
            public void onLoadFailed(ImageView imageView, String s, Drawable drawable) {
                T.showShort(mContext, "加载失败");
            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    ((Activity) mContext).finish();
                    return true;
                }
                return false;
            }
        });

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mImageUrlses.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
