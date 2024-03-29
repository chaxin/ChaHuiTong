package com.damenghai.chahuitong.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.damenghai.chahuitong.R;
import com.damenghai.chahuitong.api.TeaMarketAPI;
import com.damenghai.chahuitong.base.BaseActivity;
import com.damenghai.chahuitong.bean.Product;
import com.damenghai.chahuitong.config.Constants;
import com.damenghai.chahuitong.request.VolleyRequest;
import com.damenghai.chahuitong.utils.ImageUtils;
import com.damenghai.chahuitong.utils.L;
import com.damenghai.chahuitong.view.CustomSpinner;
import com.damenghai.chahuitong.view.TopBar;
import com.lidroid.xutils.BitmapUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class EditActivity extends BaseActivity implements OnClickListener {
	private String mSaleway = "1";
	private TopBar mTopBar;
	private RadioGroup mTabButtonGroup;
	private EditText mBrand, mName, mPrice, mQuantity, mAddress, mPhone, mDescBuy;
	private CustomSpinner mYearSpinner, mDetailSpinner;

    private LinearLayout mIvLl;

	private Button mBtnSubmit;
    private ArrayList<ImageView> mImageViews;
	private ArrayList<Uri> mImages;

	private Product mProduct;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_publish);

		mProduct = (Product) getIntent().getSerializableExtra("product");

		findViewById();

		initView();

        displayImage(mProduct.getPic());
	}

    @Override
	protected void findViewById() {
		mTopBar = (TopBar) findViewById(R.id.publish_topbar);
		mTabButtonGroup = (RadioGroup) findViewById(R.id.tab_group);

		//文本数据
		mBrand = (EditText) findViewById(R.id.id_input_brand); //品牌名编辑框
		mName = (EditText) findViewById(R.id.id_input_name); //品名编辑框
		mPrice = (EditText) findViewById(R.id.id_input_price); //价格
		mQuantity = (EditText) findViewById(R.id.id_input_quantity); //数量
		mAddress = (EditText) findViewById(R.id.id_input_address); //货源所在地
		mPhone = (EditText) findViewById(R.id.id_input_phone); //手机号码
		mYearSpinner = (CustomSpinner) findViewById(R.id.id_spinner_years); //年份下拉框
		mDetailSpinner = (CustomSpinner) findViewById(R.id.id_spinner_detail); //是否详谈下拉框
		mDescBuy = (EditText) findViewById(R.id.id_product_desc);

        mIvLl = (LinearLayout) findViewById(R.id.upload_image);

        mBtnSubmit = (Button) findViewById(R.id.id_btn_submit); //提交按钮

	}

    @Override
	protected void initView() {
        if(mProduct != null) {
            mBrand.setText(mProduct.getBrand() != null ? mProduct.getBrand() : "");
            mName.setText(mProduct.getName() != null ? mProduct.getName() : "");
            mPrice.setText(mProduct.getPrice() != null ? mProduct.getPrice() : "");
            mQuantity.setText(mProduct.getQuantity() + "");
            mAddress.setText(mProduct.getAddress() != null ? mProduct.getAddress() : "");
            mDescBuy.setText(mProduct.getDesc() != null ? mProduct.getDesc() : "");
            mPhone.setText(mProduct.getPhone());
        }

        mImageViews = new ArrayList<ImageView>();
        int count = mIvLl.getChildCount();
        for(int i=0; i<count; i++) {
            mImageViews.add((ImageView) mIvLl.getChildAt(i));
        }

        mImages = new ArrayList<Uri>();

		mTopBar.setOnLeftClickListener(new TopBar.OnLeftClickListener() {
			@Override
			public void onLeftClick() {
				finishActivity();
			}
		});

		mTabButtonGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.tab_sale :
						mDescBuy.setHint(getResources().getString(R.string.product_sale_desction));
						mSaleway = "1";
						break;
					case R.id.tab_buy :
						mDescBuy.setHint(getResources().getString(R.string.product_buy_desction));
						mSaleway = "2";
						break;
				}
			}
		});

		//设置年份下拉框为今年前20年
		ArrayList<CharSequence> years = new ArrayList<CharSequence>();
		Calendar cal = Calendar.getInstance();
		for(int i=0; i<20; i++) {
			years.add("" + (cal.get(Calendar.YEAR) - i));
		}
		mYearSpinner.setAdapter(years);

		//设置是否详谈
		ArrayList<CharSequence> details = new ArrayList<CharSequence>();
		details.add("是");
		details.add("否");
		mDetailSpinner.setAdapter(details);

        mIvLl.setOnClickListener(this);

		mBtnSubmit.setOnClickListener(new submitOnClickListener());
	}

    private void displayImage(String url) {
        if(url != null && !url.equals("")) {
            if(url.contains(",")) {
                String[] images = url.split(",");
                for(int i=0; i<images.length; i++) {
                    BitmapUtils utils = new BitmapUtils(EditActivity.this);
                    utils.display(mImageViews.get(i),  Constants.IMAGE_URL + images[i]);
                }
            } else {
                BitmapUtils utils = new BitmapUtils(EditActivity.this);
                utils.display(mImageViews.get(0), Constants.IMAGE_URL + url);
            }

        } else if(mImages.size() > 0 && mImageViews.size() > 0) {
            for (int i = 0; i < mImages.size(); i++) {
                ImageView iv = mImageViews.get(i);
                iv.setImageURI(mImages.get(i));
            }
        }
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case ImageUtils.GALLERY_REQUEST_CODE :
                if(resultCode == Activity.RESULT_CANCELED) return;
                Uri uri = data.getData();
                mImages.add(uri);
                break;
            case ImageUtils.CAMERA_REQUEST_CODE :
                if(resultCode == Activity.RESULT_CANCELED) {
                    ImageUtils.deleteImageUri(this);
                } else {
                    mImages.add(ImageUtils.imageUri);
                }
                break;
        }

        displayImage("");

	}

	@Override
	public void onClick(final View v) {
        ImageUtils.showImagePickDialog(this);
	}

	private class submitOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			final String brand = mBrand.getText().toString();
			final String name = mName.getText().toString();
			final String year = mYearSpinner.getSelectedItem();
			final String price = mPrice.getText().toString();
			final String detail = mDetailSpinner.getSelectedItem();
			final String quantity = mQuantity.getText().toString();
			String address = mAddress.getText().toString();
			String phone = mPhone.getText().toString();

            Product product = new Product();
            product.setId(mProduct.getId());
            product.setSaleway(mSaleway);
            product.setBrand(brand);
            product.setName(name);
            product.setYear(year);
            product.setPrice(price);
            product.setArrow_order(detail.equals("是") ? "1" : "0");
            product.setQuantity(quantity);
            product.setAddress(address);
            product.setPhone(phone);
            for(int i=0; i<mImages.size(); i++) {
                StringBuilder builder = new StringBuilder();
                if(i == mImages.size() - 1) {
                    builder.append(ImageUtils.getBase64FromUri(EditActivity.this, mImages.get(i)));
                } else {
                    builder.append(ImageUtils.getBase64FromUri(EditActivity.this, mImages.get(i)) + ",");
                }
                product.setPic(builder.toString());
            }

            TeaMarketAPI.editProduct(EditActivity.this, product, new VolleyRequest() {
                @Override
                public void onSuccess(String response) {
                    super.onSuccess(response);
                    try {
                        L.d(new JSONObject(response).toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finishActivity();
                }
            });
		}
		
	}
}