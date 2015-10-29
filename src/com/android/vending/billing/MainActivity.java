package com.android.vending.billing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.test.demo.R;

public class MainActivity extends Activity {

	GooglePay.FinishedListener _mListener = new GooglePay.FinishedListener() {
		public void onFinished(int type, String signture_data, String signture)
		{
			Log.e("GooglePay.FinishedListener", signture_data);
			Log.e("GooglePay.FinishedListener", signture);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		GooglePay.sdkInit(this);
		
		findViewById(R.id.button1).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						GooglePay.startPurchase("com.test.demo.gold1", "oid", _mListener);
					}
				});
		
		findViewById(R.id.button2).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						
						GooglePay.startPurchase("com.test.demo.gold2", "oid", _mListener);
					}
				});
		
		findViewById(R.id.button3).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						
						GooglePay.startPurchase("com.test.demo.gold3", "oid", _mListener);
					}
				});
		
		findViewById(R.id.button4).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						
						GooglePay.startPurchase("com.test.demo.gold4", "oid", _mListener);
					}
				});
		
		findViewById(R.id.button5).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						
						GooglePay.startPurchase("com.test.demo.gold5", "oid", _mListener);
					}
				});
		
		findViewById(R.id.button6).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						
						GooglePay.startPurchase("com.test.demo.gold6", "oid", _mListener);
					}
				});
	}
	
//	// 获取价格
//	private void getPrice() {
//		ArrayList<String> skus = new ArrayList<String>();
//		skus.add("com.test.demo.test.gold1");
//		skus.add("com.test.demo.test.gold2");
//		skus.add("com.test.demo.test.gold3");
//		billingservice = mHelper.getService();
//		Bundle querySkus = new Bundle();
//		querySkus.putStringArrayList("ITEM_ID_LIST", skus);
//		try {
//			Bundle skuDetails = billingservice.getSkuDetails(3,
//					MainActivity.this.getPackageName(), "inapp", querySkus);
//			ArrayList<String> responseList = skuDetails
//					.getStringArrayList("DETAILS_LIST");
//			if (null != responseList) {
//				for (String thisResponse : responseList) {
//					try {
//						SkuDetails d = new SkuDetails(thisResponse);
//
//						for (int i = 0; i < sku_list.size(); i++) {
//							if (sku_list.get(i).equals(d.getSku())) {
//								price_list.set(i, d.getPrice());
//							}
//						}
//						iapHandler.sendEmptyMessage(0);
//
//					} catch (JSONException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//		} catch (RemoteException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		GooglePay.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		GooglePay.onDestroy();
	}
}
