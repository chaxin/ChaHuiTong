package com.damenghai.chahuitong.api;

import android.content.Context;
import android.content.Intent;

import com.damenghai.chahuitong.bean.Leader;
import com.damenghai.chahuitong.config.SessionKeeper;
import com.damenghai.chahuitong.request.VolleyRequest;
import com.damenghai.chahuitong.response.IResponseListener;
import com.damenghai.chahuitong.ui.activity.LoginActivity;
import com.damenghai.chahuitong.utils.L;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sgun on 15/9/22.
 */
public class UserAPI {

    /**
     * 发送验证码
     *
     * @param mobile
     * @param code
     * @param l
     */
    public static void sendSMS(String mobile, int code, VolleyRequest l) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("account", "cf_chahuitong");
        map.put("password", "chahuitong2015");
        map.put("mobile", mobile);
        map.put("content", "你申请的手机验证码是：" + code + "，请不要把验证码泄露给其他人。如非本人操作，请忽略本条消息！");
        HodorRequest.postRequest("http://106.ihuyi.cn/webservice/sms.php?method=Submit", map, l);
    }

    /**
     * 注册账号
     *
     * @param mobile
     * @param password
     * @param l
     */
    public static void register(String mobile, String password, VolleyRequest l) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("mobilenumber", mobile);
        map.put("password", password);
        map.put("check", "804451dc13014b1c785fb73b1617b760");
        HodorRequest.postRequest("http://www.chahuitong.com/wap/index.php/Home/Index/add_count_api", map, l);
    }

    /**
     * 登录
     *
     * @param username
     * @param password
     * @param l
     */
    public static void login(String username, String password, VolleyRequest l) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("usermobile", username);
        map.put("password", password);
        map.put("client", "android");
        HodorRequest.postRequest("http://www.chahuitong.com/mobile/index.php?act=login", map, l);
    }

    /**
     * 使用第三方登录
     *
     * post   op (sina  qq)      key(md5  shopnc)     username   password      (sinaopenid ,qqopenid)
     *
     */
    public static void createAccount(String type, String username, String password, String openid, VolleyRequest l) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("op", type);
        map.put("key", "804451dc13014b1c785fb73b1617b760");
        map.put("username", username);
        map.put("password", password);
        map.put("openid", openid);
        HodorRequest.postRequest("http://www.chahuitong.com/wap/index.php/Home/Index/account_create_api", map, l);
    }

    /**
     * 获取个人资料
     *
     * @param context
     */
    public static void showProfile(Context context, IResponseListener l) {
        Map<String, String> map = new HashMap<String, String>();
        if(SessionKeeper.readSession(context).equals("")) {
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
        } else {
            map.put("key", SessionKeeper.readSession(context));
            HodorRequest.postRequest("http://www.chahuitong.com/wap/index.php/Home/Index/mermber_info_api", map, l);
        }
    }

    /**
     * 更新个人资料
     *
     * @param context
     * @param leader
     * @param l
     */
    public static void updateProfile(Context context, Leader leader, IResponseListener l) {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting().create();
        String content = gson.toJson(leader);
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", SessionKeeper.readSession(context));
        map.put("content", content);
        HodorRequest.postRequest("http://www.chahuitong.com/wap/index.php/Home/Index/info_update_api", map, l);
    }

}
