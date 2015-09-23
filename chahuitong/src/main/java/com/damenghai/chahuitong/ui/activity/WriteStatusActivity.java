package com.damenghai.chahuitong.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.damenghai.chahuitong.api.HodorRequest;
import com.damenghai.chahuitong.base.BaseActivity;
import com.damenghai.chahuitong.R;
import com.damenghai.chahuitong.adapter.AddImageGridAdapter;
import com.damenghai.chahuitong.bean.Status;
import com.damenghai.chahuitong.request.VolleyRequest;
import com.damenghai.chahuitong.utils.DateUtils;
import com.damenghai.chahuitong.utils.ImageUtils;
import com.damenghai.chahuitong.utils.T;
import com.damenghai.chahuitong.view.TopBar;
import com.damenghai.chahuitong.view.WrapHeightGridView;

import java.util.ArrayList;

/**
 * Created by Sgun on 15/8/31.
 */
public class WriteStatusActivity extends BaseActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    private TopBar mTopBar;
    private TextView mText;
    private WrapHeightGridView mGv;
    private Button mCommit;

    private AddImageGridAdapter mAdapter;
    private ArrayList<Uri> mDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_status);

        findViewById();

        initView();
    }

    @Override
    protected void findViewById() {
        mTopBar = (TopBar) findViewById(R.id.write_status_bar);
        mText = (TextView) findViewById(R.id.write_status_text);
        mGv = (WrapHeightGridView) findViewById(R.id.gv_write_status);
        mCommit = (Button) findViewById(R.id.status_commit);
    }

    @Override
    protected void initView() {
        mTopBar.setOnLeftClickListener(new TopBar.OnLeftClickListener() {
            @Override
            public void onLeftClick() {
                finishActivity();
            }
        });
        mCommit.setOnClickListener(this);

        mDatas = new ArrayList<Uri>();
        mAdapter = new AddImageGridAdapter(this, mDatas, R.layout.gridview_item_image);
        mGv.setVisibility(View.VISIBLE);
        mGv.setAdapter(mAdapter);
        mGv.setOnItemClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ImageUtils.GALLERY_REQUEST_CODE :
                if(resultCode == Activity.RESULT_CANCELED) return;
                Uri uri = data.getData();
                mDatas.add(uri);
                mAdapter.notifyDataSetChanged();
                break;
            case ImageUtils.CAMERA_REQUEST_CODE :
                if(resultCode == Activity.RESULT_CANCELED) ImageUtils.deleteImageUri(this);

                mDatas.add(ImageUtils.imageUri);
                mAdapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(i == mDatas.size()) {
            ImageUtils.showImagePickDialog(this);
        }
    }

    @Override
    public void onClick(View view) {
        final Status status = new Status();
        String content = mText.getText().toString();
        String time = DateUtils.getCurrentTime();
        if(TextUtils.isEmpty(content)) T.showShort(this, "内容不能为空");
        else {
            status.setText(content);
            status.setCreated_at(time);

            if(mDatas.size() > 0) {
                StringBuilder images = new StringBuilder();
                for (int i = 0; i < mDatas.size(); i++) {
                    String image = ImageUtils.getBase64FromUri(mDatas.get(i), WriteStatusActivity.this);
                    if (i == mDatas.size() - 1)
                        images.append(image);
                    else
                        images.append(image + ",");
                }
                status.setImage(images.toString());
            }

            HodorRequest.uploadStatus(this, status, new VolleyRequest() {
                @Override
                public void onSuccess(String response) {
                    super.onSuccess(response);
                    T.showShort(WriteStatusActivity.this, "发送成功");
                    finishActivity();
                }
            });
        }
    }
}
