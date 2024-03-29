package com.damenghai.chahuitong.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.damenghai.chahuitong.R;
import com.damenghai.chahuitong.api.PaymentAPI;
import com.damenghai.chahuitong.base.BaseActivity;
import com.damenghai.chahuitong.request.VolleyRequest;
import com.damenghai.chahuitong.utils.L;
import com.unionpay.UPPayAssistEx;
import com.unionpay.uppay.PayActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Sgun on 15/9/15.
 */
public class PaymentActivity extends BaseActivity implements Runnable, Handler.Callback {
    // 00 银联正式环境，银联测试环境，该环境不产生真实交易
    private String mMode = "00";
    private static final String TN_URL_01 = "http://202.101.25.178:8080/sim/gettn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        getTn();
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void initView() {

    }

    private void getTn() {
        /*************************************************
         * 步骤1：从网络开始,获取交易流水号即TN
         ************************************************/
        new Thread(this).start();
//        PaymentAPI.getOrders(PaymentActivity.this, new VolleyRequest() {
//            @Override
//            public void onSuccess(String response) {
//                super.onSuccess(response);
////                try {
////                    JSONObject object = new JSONObject(response);
////                    JSONObject datas = object.getJSONObject("datas");
////                    JSONArray orders = datas.getJSONArray("order_group_list");
////                    String tn = orders.getJSONObject(0).getString("pay_sn");
//                    if(response != null && !response.equals("")) {
//                        /*************************************************
//                         * 步骤2：通过银联工具类启动支付插件
//                         ************************************************/
//                        doStartUnionPayPlugin(response);
//                    } else {
//                        AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this);
//                        builder.setTitle("错误提示");
//                        builder.setTitle("错误提示");
//                        builder.setMessage("网络连接失败,请重试!");
//                        builder.setNegativeButton("确定",
//                                new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        dialog.dismiss();
//                                    }
//                                });
//                        builder.create().showProfile();
//                    }
////                } catch (JSONException e) {
////                    e.printStackTrace();
////                }
//            }
//        });
    }

    private void doStartUnionPayPlugin(String tn) {
        UPPayAssistEx.startPayByJAR(PaymentActivity.this, PayActivity.class, null, null, tn, mMode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*************************************************
         * 步骤3：处理银联手机支付控件返回的支付结果
         ************************************************/
        if (data == null) {
            return;
        }

        String msg = "";
        /*
         * 支付控件返回字符串:success、fail、cancel 分别代表支付成功，支付失败，支付取消
         */
        String str = data.getExtras().getString("pay_result");
        if (str.equalsIgnoreCase("success")) {
            msg = "支付成功！";
        } else if (str.equalsIgnoreCase("fail")) {
            msg = "支付失败！";
        } else if (str.equalsIgnoreCase("cancel")) {
            msg = "用户取消了支付";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("支付结果通知");
        builder.setMessage(msg);
        builder.setInverseBackgroundForced(true);
        // builder.setCustomTitle();
        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public boolean handleMessage(Message msg) {
        L.d(" " + "" + msg.obj);

        String tn = "";
        if (msg.obj == null || ((String) msg.obj).length() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("错误提示");
            builder.setMessage("网络连接失败,请重试!");
            builder.setNegativeButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.create().show();
        } else {
            tn = (String) msg.obj;
            /*************************************************
             * 步骤2：通过银联工具类启动支付插件
             ************************************************/
            doStartUnionPayPlugin(tn);
        }

        return false;
    }

    @Override
    public void run() {
        String tn = null;
        InputStream is;
        try {

            String url = "http://202.101.25.178:8080/sim/gettn";

            URL myURL = new URL(url);
            URLConnection ucon = myURL.openConnection();
            ucon.setConnectTimeout(120000);
            is = ucon.getInputStream();
            int i = -1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((i = is.read()) != -1) {
                baos.write(i);
            }

            tn = baos.toString();
            is.close();
            baos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        doStartUnionPayPlugin(tn);
    }

}
