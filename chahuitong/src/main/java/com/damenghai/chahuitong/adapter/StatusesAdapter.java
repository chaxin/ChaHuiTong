package com.damenghai.chahuitong.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.damenghai.chahuitong.R;
import com.damenghai.chahuitong.api.StatusAPI;
import com.damenghai.chahuitong.bean.ImageUrls;
import com.damenghai.chahuitong.bean.Status;
import com.damenghai.chahuitong.config.SessionKeeper;
import com.damenghai.chahuitong.request.VolleyRequest;
import com.damenghai.chahuitong.ui.activity.LeaderActivity;
import com.damenghai.chahuitong.ui.activity.PersonalActivity;
import com.damenghai.chahuitong.ui.activity.StatusDetailActivity;
import com.damenghai.chahuitong.ui.activity.ImageBrowserActivity;
import com.damenghai.chahuitong.ui.activity.LoginActivity;
import com.damenghai.chahuitong.ui.activity.WriteCommentActivity;
import com.damenghai.chahuitong.utils.ImageConfigHelper;
import com.damenghai.chahuitong.utils.L;
import com.damenghai.chahuitong.utils.ShareManager;
import com.damenghai.chahuitong.utils.T;
import com.damenghai.chahuitong.utils.ViewHolder;
import com.damenghai.chahuitong.view.WrapHeightGridView;
import com.lidroid.xutils.BitmapUtils;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.media.UMImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 微博列表
 *
 * Created by Sgun on 15/8/29.
 */
public class StatusesAdapter extends CommonAdapter<Status> {
    private boolean mShowUser;

    public StatusesAdapter(Context context, List<Status> data, int resId, boolean showUser) {
        super(context, data, resId);
        mShowUser = showUser;
    }

    @Override
    public void convert(final ViewHolder holder, final Status status) {
        // 设置图片
        setImages(holder, status);

        // 设置信息显示
        if(!TextUtils.isEmpty(status.getTitle())) {
            holder.setText(R.id.status_title, status.getTitle())
                    .setVisibility(R.id.status_title, View.VISIBLE);
        } else {
            holder.setVisibility(R.id.status_title, View.GONE);
        }
        holder.setText(R.id.status_text, status.getText())
                .setText(R.id.status_source, status.getCreated_at() + "  来自" + (status.getSource() == null ? "Android客户端" : status.getSource()))
                .setText(R.id.control_tv_share, status.getShare() + "")
                .setText(R.id.control_tv_like, status.getView() + "")
                .setText(R.id.control_tv_comment, status.getComment() + "")
                .setOnClickListener(R.id.status_layout, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("status", status);
                        startActivity(StatusDetailActivity.class, bundle);
                    }
                });

        if(mShowUser) {
            holder.setVisibility(R.id.status_avatar, View.VISIBLE)
                    .setVisibility(R.id.status_user, View.VISIBLE);
            if(status.getMemberInfo() != null) {
                holder.setText(R.id.status_user, status.getMemberInfo().getMember_name())
                        .loadAvatarImage(R.id.status_avatar, status.getMemberInfo().getMember_avatar())
                        .setOnClickListener(R.id.include_status_user, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("leader", status.getMemberInfo());
                                startActivity(LeaderActivity.class, bundle);
                            }
                        });
            }
        } else if(mContext instanceof PersonalActivity){
            holder.setVisibility(R.id.status_delete, View.VISIBLE)
                    .setTextOnClickListener(R.id.status_delete, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                            dialog.setMessage("确定删除？")
                                    .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            StatusAPI.deleteStatus(status.getContent_id(), SessionKeeper.readSession(mContext),
                                                    SessionKeeper.readUsername(mContext), new VolleyRequest() {
                                                        @Override
                                                        public void onSuccess(String response) {
                                                            super.onSuccess(response);
                                                            try {
                                                                JSONObject obj = new JSONObject(response);
                                                                int code = obj.getInt("code");
                                                                if (code == 404) {
                                                                    T.showShort(mContext, obj.getString("content"));
                                                                } else {
                                                                    T.showShort(mContext, "删除成功");
                                                                    mData.remove(holder.getPosition());
                                                                    notifyDataSetChanged();
                                                                }
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    });
                                        }
                                    }).setPositiveButton("取消", null).create().show();
                        }
                    });
        }

        setControl(holder, status);

    }

    // 设置微博图片
    protected void setImages(ViewHolder holder, final Status status) {
        FrameLayout imgContainer = holder.getView(R.id.status_images);
        WrapHeightGridView gv_images = (WrapHeightGridView) imgContainer.findViewById(R.id.wrap_gridview_images);
        ImageView iv_image = (ImageView) imgContainer.findViewById(R.id.status_iv_item);

        final ArrayList<ImageUrls> picUrls = status.getImageUrls();
        if(picUrls != null && picUrls.size() > 1) {
            imgContainer.setVisibility(View.VISIBLE);
            gv_images.setVisibility(View.VISIBLE);
            iv_image.setVisibility(View.GONE);

            GridViewImagesAdapter mGvAdapter = new GridViewImagesAdapter(mContext, picUrls, R.layout.gridview_item_image);
            gv_images.setAdapter(mGvAdapter);
            gv_images.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(mContext, ImageBrowserActivity.class);
                    intent.putExtra("pic", picUrls);
                    intent.putExtra("position", i);
                    mContext.startActivity(intent);
                }
            });
        } else if(picUrls != null && picUrls.size() == 1) {
            imgContainer.setVisibility(View.VISIBLE);
            gv_images.setVisibility(View.GONE);
            iv_image.setVisibility(View.VISIBLE);

            BitmapUtils bitmapUtils = new BitmapUtils(mContext);
            bitmapUtils.display(iv_image, picUrls.get(0).getBmiddle_pic(), ImageConfigHelper.getImageConfig(mContext));
            iv_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, ImageBrowserActivity.class);
                    intent.putExtra("pic", picUrls);
                    intent.putExtra("position", 1);
                    mContext.startActivity(intent);
                }
            });
        } else {
            imgContainer.setVisibility(View.GONE);
        }
    }

    // 设置三个功能按钮
    protected void setControl(final ViewHolder holder, final Status status) {
        final LinearLayout statusComment = holder.getView(R.id.control_comment);
        LinearLayout statusShare = holder.getView(R.id.control_share);
        LinearLayout statusLike = holder.getView(R.id.control_like);
        statusShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SessionKeeper.readSession(mContext).equals("")) {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    mContext.startActivity(intent);
                }else {
                    UMSocialService controller = ShareManager.create(mContext);
                    ShareManager.setShareContent(mContext,
                            !TextUtils.isEmpty(status.getThumbImage()) ? status.getThumbImage() : "",
                            "http://t.cn/RyU8vSP",
                            status.getTitle(),
                            status.getText());

                    controller.setShareContent(status.getText() + ", http://t.cn/RyU8vSP");
                    if (!TextUtils.isEmpty(status.getThumbImage()))
                        controller.setShareImage(new UMImage(mContext, status.getThumbImage()));
                    else if(status.getMemberInfo() != null && !TextUtils.isEmpty(status.getMemberInfo().getMember_avatar()))
                        controller.setShareImage(new UMImage(mContext, status.getMemberInfo().getMember_avatar()));
                    controller.openShare((Activity) mContext, new SocializeListeners.SnsPostListener() {
                        @Override
                        public void onStart() {
                            L.d("开始分享");
                        }

                        @Override
                        public void onComplete(SHARE_MEDIA share_media, int i, SocializeEntity socializeEntity) {
                            StatusAPI.statusShare(status.getContent_id(), new VolleyRequest() {
                                @Override
                                public void onSuccess() {
                                    super.onSuccess();
                                    int shareCount = Integer.parseInt(((TextView) holder.getView(R.id.control_tv_share)).getText().toString());
                                    holder.setText(R.id.control_tv_share, shareCount + 1 + "");
                                }
                            });
                        }
                    });
                }
            }
        });

        statusComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SessionKeeper.readSession(mContext).equals("")) {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    mContext.startActivity(intent);
                } else if(status.getComment() != 0){
                    Intent intent = new Intent(mContext, StatusDetailActivity.class);
                    intent.putExtra("scroll2comment", true);
                    intent.putExtra("status", status);
                    mContext.startActivity(intent);
                } else {
                    Intent intent = new Intent(mContext, WriteCommentActivity.class);
                    intent.putExtra("status_id", status.getContent_id());
                    mContext.startActivity(intent);
                }
            }
        });

        statusLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SessionKeeper.readSession(mContext).equals("")) {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    mContext.startActivity(intent);
                } else {
                    StatusAPI.statusLike(mContext, status.getContent_id(), new VolleyRequest() {
                        @Override
                        public void onSuccess(String response) {
                            super.onSuccess(response);

                            try {
                                JSONObject obj = new JSONObject(response);
                                if (obj.getInt("code") != 404) {
                                    holder.setText(R.id.control_tv_like, status.getView() + 1 + "");
                                } else {
                                    T.showShort(mContext, obj.getString("content"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                }
            }
        });
    }

    private void startActivity(Class<? extends Activity> clazz, Bundle bundle) {
        Intent intent = new Intent(mContext, clazz);
        if(bundle != null) {
            intent.putExtras(bundle);
        }
        mContext.startActivity(intent);
        ((Activity) mContext).overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
    }

}
