package com.damenghai.chahuitong.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.damenghai.chahuitong.base.BaseActivity;
import com.damenghai.chahuitong.R;
import com.damenghai.chahuitong.config.SessionKeeper;
import com.damenghai.chahuitong.utils.ShareManager;
import com.damenghai.chahuitong.view.NewWebView;
import com.damenghai.chahuitong.view.TopBar;
import com.damenghai.chahuitong.view.TopBar.OnLeftClickListener;
import com.umeng.socialize.controller.UMSocialService;

public class WebViewActivity extends Activity implements NewWebView.OnReceivedTitle {
	private String mUrl, mTitle;
	private TopBar mTopBar;
	private NewWebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_content);

		findViewById();
		initView();
	}

	private void checkLogin() {
		String key = SessionKeeper.readSession(this);
		if(key != null && !key.equals("")) {
			mWebView.setCookie("key", key);
		} else {
            mWebView.removeCookie();
        }
	}

	protected void findViewById() {
		mTopBar = (TopBar) findViewById(R.id.topBar);
		mWebView = (NewWebView) findViewById(R.id.new_webview);
	}

	protected void initView() {
		Intent intent = getIntent();
		Bundle extra = intent.getExtras();
		if(extra != null) {
			mUrl = extra.getString("url");
		}

		mWebView.loadUrl(mUrl);
		mWebView.setOnReceivedTitle(this);
        checkLogin();

		mTopBar.setOnLeftClickListener(new OnLeftClickListener() {
			@Override
			public void onLeftClick() {
				finish();
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
			}
		});

		if(mUrl.contains("goods")) {
			mTopBar.setRightSrc(R.drawable.icon_share);// 设置分享内容
			final UMSocialService controller = ShareManager.create(WebViewActivity.this);
			ShareManager.setShareContent(WebViewActivity.this, "", mUrl, mTitle, "");

			mTopBar.setOnRightClickListener(new TopBar.onRightClickListener() {
				@Override
				public void onRightClick() {
					if(mTitle != null) {
						controller.setShareContent(mTitle + "，" + mUrl);
					}
					// 是否只有已登录用户才能打开分享选择页
					controller.openShare(WebViewActivity.this, false);
				}
			});
		} else {
			mTopBar.setOnRightClickListener(new TopBar.onRightClickListener() {
				@Override
				public void onRightClick() {
					Intent intent = new Intent(WebViewActivity.this, HomeActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
			});
		}

	}

	@Override
	public void receivedTitle(String title) {
		if(title != null && !title.trim().equals("")) {
			mTitle = title;
			mTopBar.setTitle(title);
		}
	}

}
