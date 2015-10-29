package com.android.vending.billing;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class GooglePay {

	private final static int DoPurchase = 1;
	private final static int DoRestore = 2;
	private final static int DoConsume = 3;
	private final static int RC_REQUEST = 0;
	private final static String PRODUCT_ID = "PRODUCT_ID";
	private final static String ORDER_ID = "ORDER_ID";

	private static IabHelper _mHelper;
	private static boolean _iap_is_ok = false;
	private static String TAG = "GooglePay";
	private static Activity _act = null;
	private static ArrayList<Purchase> _consumeArr = null;
	private static FinishedListener _finishListener = null;
	private static boolean _restoreDoing = false;

	public interface FinishedListener {
		/**
		 * Called to notify that an inventory query operation completed.
		 * 
		 * @param result
		 *            The result of the operation.
		 * @param inv
		 *            The inventory.
		 */

		public void onFinished(int type, String signture_data, String signture);
	}
	
	public static void sdkInit(Activity act) {
		sdkInit(act, null);
	}
	
	public static void sdkInit(Activity act, FinishedListener listener) {
		_act = act;
		_finishListener = listener;
		_mHelper = new IabHelper(_act);
		_mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				Log.d(TAG, "Setup finished.");
				if (!result.isSuccess()) {
					// Oh noes, there was a problem.
					complain("Problem setting up in-app billing: " + result);
					return;
				}
				_iap_is_ok = true;

				// Hooray, IAB is fully set up. Now, let's get an inventory of
				Log.d(TAG, "Setup successful. Querying inventory.");
				_iapHandler.sendEmptyMessage(DoRestore);
			}
		});
	}

	public static void complain(final String message) {
		Log.e(TAG, "Error: " + message);
		_act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(_act, message, Toast.LENGTH_LONG).show();
			}
		});
	}

	public static void startPurchase(String pid, String orderid, FinishedListener listener) {
		_finishListener = listener;
		startPurchase(pid, orderid);
	}
	
	public static void startPurchase(String pid, String orderid) {
		Message msg = new Message();
		msg.what = DoPurchase;
		Bundle bundle = new Bundle();
		bundle.putString(PRODUCT_ID, pid);
		bundle.putString(ORDER_ID, orderid);
		msg.setData(bundle);
		_iapHandler.sendMessage(msg);
	}
	
	public static void startRestore() {
		_restoreDoing = true;
		_iapHandler.sendEmptyMessage(DoRestore);
	}

	private static Handler _iapHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (!_iap_is_ok) {
				complain("Google Play初始化失败,当前无法进行支付，请确定您所在地区支持Google Play支付或重启游戏再试！");
				return;
			}

			switch (msg.what) {
			case DoPurchase: {
				Bundle bundle = msg.getData();
				String pid = (String) bundle.get(PRODUCT_ID);
				String oid = (String) bundle.get(ORDER_ID);
				if (pid.length() > 0) {
					_mHelper.launchPurchaseFlow(_act, pid, RC_REQUEST,
							_mPurchaseFinishedListener, oid);
				}
			}
				break;
			case DoRestore: {
				_mHelper.queryInventoryAsync(_mGotInventoryListener);
			}
				break;
			case DoConsume: {
				if (_consumeArr.size() > 0) {
					Purchase p = _consumeArr.get(0);
					_consumeArr.remove(0);
					_mHelper.consumeAsync(p, _mConsumeFinishedListener);
				}
				else {
					if (_restoreDoing) {
						_restoreDoing = false;
						if (_finishListener != null) {
							_finishListener.onFinished(DoPurchase, "", "");
						}
					}
				}
			}
				break;
			default:
				break;
			}
		};
	};

	// Callback for when a purchase is finished
	static IabHelper.OnIabPurchaseFinishedListener _mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			Log.d(TAG, "Purchase finished: " + result + ", purchase: "
					+ purchase);
			if (result.isFailure()) {
				// Oh noes!
				if (result.getResponse() == 7) {
					_iapHandler.sendEmptyMessage(DoRestore);
				} else {
					complain("Error purchasing: " + result);
				}
				return;
			}

			Log.d(TAG, "Purchase successful.");
			if (_consumeArr == null) {
				_consumeArr = new ArrayList<Purchase>();
			}

			_consumeArr.add(purchase);

			if (!_mHelper.mAsyncInProgress) {
				_iapHandler.sendEmptyMessage(DoConsume);
			}
		}
	};
	// Called when consumption is complete
	static IabHelper.OnConsumeFinishedListener _mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
		public void onConsumeFinished(Purchase purchase, IabResult result) {
			Log.d(TAG, "Consumption finished. Purchase: " + purchase
					+ ", result: " + result);

			if (result.isSuccess()) {
				if (_finishListener != null) {
					_finishListener.onFinished(DoPurchase, purchase.mOriginalJson,
							purchase.mSignature);
				}

				_iapHandler.sendEmptyMessage(DoConsume);
			} else {
				complain("Error while consuming: " + result);
				if (_restoreDoing) {
					_restoreDoing = false;
					
				}
			}
		}
	};

	// Listener that's called when we finish querying the items we own
	static IabHelper.QueryInventoryFinishedListener _mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
		public void onQueryInventoryFinished(IabResult result,
				Inventory inventory) {
			Log.d(TAG, "Query inventory finished.");
			if (result.isFailure()) {
				complain("Failed to query inventory: " + result);
				if (_restoreDoing) {
					_restoreDoing = false;
					
				}
				return;
			}

			if (_consumeArr == null) {
				_consumeArr = new ArrayList<Purchase>();
			} else {
				_consumeArr.clear();
			}

			for (Purchase p : inventory.getAllPurchase()) {
				_consumeArr.add(p);
			}

			_iapHandler.sendEmptyMessage(DoConsume);

			Log.d(TAG, "Query inventory was successful.");
		}
	};

	public static void onDestroy() {
		if (_mHelper != null)
			_mHelper.dispose();
		_mHelper = null;

		_iapHandler = null;
	}

	public static void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		// Pass on the activity result to the helper for handling
		if (_mHelper.handleActivityResult(requestCode, resultCode, data)) {
			Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode
					+ "," + data);
		}
	}

}
