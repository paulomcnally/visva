package vn.com.shoppie.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import vn.com.shoppie.R;
import vn.com.shoppie.adapter.AdapterWelcomeImage;
import vn.com.shoppie.adapter.AdapterWelcomeImage.OnWelcomeRegisterListener;
import vn.com.shoppie.constant.GlobalValue;
import vn.com.shoppie.constant.ShopieSharePref;
import vn.com.shoppie.database.sobject.UserInfo;
import vn.com.shoppie.network.AsyncHttpPost;
import vn.com.shoppie.network.AsyncHttpResponseProcess;
import vn.com.shoppie.network.ParameterFactory;
import vn.com.shoppie.util.SUtil;
import vn.com.shoppie.webconfig.WebServiceConfig;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

public class ActivityWelcome extends Activity implements LocationListener,
		OnWelcomeRegisterListener {
	public static final String ERR_BLUETOOTH_NULL = "-2";
	public static final String ERR_UNKNOWN = "-1";
	public static final String ERR_SERVER = "-3";
	public static final String ERR_GCM = "-4";
	public static final String ERR_STILL_REGISTER = "-5";
	public static final String ERR_DATABASE_ERR = "-6";

	private ViewPager mPager;
	// CirclePageIndicator mIndicator;

	private AdapterWelcomeImage mAdapter;
	private ArrayList<Integer> mData = new ArrayList<Integer>();

	public static String regId;
	private String blueMac;
	private String emeil;
	private String custName;
	private String custAdress;
	private String gender;
	private String facebookId;
	private String custEmail;
	private String birthday;
	private String deviceImei;
	private String AppVersion;
	private String OSVersion;
	private String friendId;
	private ShopieSharePref mShopieSharePref;
	// FaceBook
	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(final Session session, final SessionState state,
				final Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		// Facebook
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(arg0);

		mShopieSharePref = new ShopieSharePref(this);

		setContentView(R.layout.activity_welcome);

		findViewById();

		addDataSample();

		mAdapter.setOnRegisterListener(this);

		// request Location
		mLocaMng = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		try {
			mLocaMng.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
					0, 0, this);
			mLocaMng.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
					this);
			mLocaMng.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					0, 0, this);
		} catch (IllegalArgumentException e) {

		}

		Location tmpLoc = mLocaMng
				.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		if (tmpLoc == null) {
			tmpLoc = mLocaMng
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (tmpLoc == null) {
				tmpLoc = mLocaMng
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}
		}
		if (tmpLoc != null) {
			ActivityShoppie.myLocation = new LatLng(tmpLoc.getLatitude(),
					tmpLoc.getLongitude());
		}

		// check network
		checkNetwork();

		retreviveGcm();

		blueMac = SUtil.getInstance().getBluetoothAddress(this, false);
		emeil = SUtil.getInstance().getDeviceId(getApplicationContext());
	}

	public void findViewById() {
		mPager = (ViewPager) findViewById(R.id.pager);
		// mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);

		mAdapter = new AdapterWelcomeImage(this, mData);
		mPager.setAdapter(mAdapter);
		// mIndicator.setViewPager(mPager);

		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				// Log.e("page change", "" + arg0);
				// mIndicator.setCurrentItem(arg0);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	public static int PAGE_REGISTER = 999999;

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		uiHelper.onSaveInstanceState(bundle);
	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();
		if (mShopieSharePref.getCustId() > 0) {
			startActivity(new Intent(this, HomeActivity.class));
			this.finish();
		}
		final Session session = Session.getActiveSession();
		if (session == null || session.isClosed() || !session.isOpened()) {
			uiHelper = new UiLifecycleHelper(this, callback);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	private void retreviveGcm() {
		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(this);

		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.
		GCMRegistrar.checkManifest(this);
		// Get GCM registration id
		regId = GCMRegistrar.getRegistrationId(this);
		// writeRegId(regId, name, email);
		Log.e("regId", "adufhd " + regId);

		// Check if regid already presents
		if (regId.equals("")) {
			// Registration is not present, register now with GCM
			GCMRegistrar.register(this, GlobalValue.SENDER_ID);
		} else {
			// Device is already registered on GCM
			if (GCMRegistrar.isRegisteredOnServer(this)) {
				// Skips registration.
				// Toast.makeText(getApplicationContext(),
				// "Already registered with GCM", Toast.LENGTH_LONG)
				// .show();
			} else {
			}
		}
	}

	public void addDataSample() {
		mData.clear();
		mData.add(R.drawable.slide1);
		mData.add(R.drawable.slide2);
		mData.add(R.drawable.slide3);
		mData.add(R.drawable.slide4);
		mData.add(R.drawable.slide5);
		mData.add(R.drawable.slide6);
		mData.add(PAGE_REGISTER);
		// mAdapter.notifyDataSetChanged();

		mAdapter = new AdapterWelcomeImage(this, mData);
		mPager.setAdapter(mAdapter);
		// mIndicator.setViewPager(mPager);
	}

	LocationManager mLocaMng;

	private void register(final String name, String email, String address,
			String gender, String birth, String facebookid, String AppVersion,
			String OSVersion, String friendId) {

		ActivityShoppie.myUser.custName = name;
		SettingPreference.setUserName(ActivityWelcome.this, name);
		// Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
		// Log.e("user name", name);
		if (regId.equals("")) {
			retreviveGcm();
		}
		String lat = ActivityShoppie.myLocation.latitude + "";
		String lng = ActivityShoppie.myLocation.longitude + "";
		BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mAdapter != null) {
			blueMac = mAdapter.getAddress();
			while (!mAdapter.isEnabled() || blueMac == null
					|| blueMac.equals("")) {

				mAdapter.enable();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				blueMac = SUtil.getInstance().getBluetoothAddress(
						ActivityWelcome.this, false);
			}
		}

		registerToSPServer(regId, blueMac, emeil, lat, lng, name, email,
				address, gender, birth, facebookid, emeil, AppVersion,
				OSVersion, friendId);
	}

	private void registerToSPServer(String deviceToken, String bluetoothId,
			String deviceId, String latitude, String longitude,
			String custName, String custEmail, String custAddress,
			String gender, String birthday, String facebookid,
			String deviceImei, String AppVersion, String OSVersion,
			String friendId) {
		// TODO Auto-generated method stub
		GCMRegistrar.setRegisteredOnServer(this, true);
		// TODO Auto-generated method stub
		List<NameValuePair> nameValuePairs = ParameterFactory
		// .createRegisterSPAccount(deviceToken, bluetoothId, deviceId,
		// latitude, longitude, custName);
				.createRegisterSPAccount(deviceToken, bluetoothId, deviceId,
						latitude, longitude, custName, custEmail, custAddress,
						gender, birthday, facebookid, deviceImei, AppVersion,
						OSVersion, friendId);
		AsyncHttpPost postUpdateStt = new AsyncHttpPost(ActivityWelcome.this,
				new AsyncHttpResponseProcess(ActivityWelcome.this) {
					@Override
					public void processIfResponseSuccess(String response) {
						try {
							JSONObject jsonObject = new JSONObject(response);
							Gson gson = new Gson();
							UserInfo userInfo = gson.fromJson(
									jsonObject.toString(), UserInfo.class);
							Log.e("custId", "custId "
									+ userInfo.getResult().getDataValue());
							mShopieSharePref.setCustId(userInfo.getResult()
									.getDataValue());
							showToast(getString(R.string.welcome_shoppie));
							Intent intent = new Intent(ActivityWelcome.this,
									HomeActivity.class);
							startActivity(intent);
							finish();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					@Override
					public void processIfResponseFail() {
						Log.e("failed ", "failed");
						finish();
					}
				}, nameValuePairs, true);
		postUpdateStt.execute(WebServiceConfig.URL_REGISTER);

	}

	final int REQ_NETWORK = 1;

	public void checkNetwork() {
		if (!SUtil.getInstance().isNetworkConnected(this)) {
			showToast("Báº¡n hÃ£y báº­t máº¡ng Ä‘á»ƒ sá»­ dá»¥ng Shoppie!");
			// Intent intent = new Intent(Intent.ACTION_MAIN);
			// intent.setClassName("com.android.phone",
			// "com.android.phone.NetworkSetting");
			// startActivityForResult(intent, REQ_NETWORK);
			return;
		}

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQ_NETWORK) {
			// checkNetwork();
		}
	}

	Toast mToast;

	private void showToast(String text) {
		if (mToast == null)
			mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
		mToast.cancel();
		mToast.setText(text);
		mToast.show();
	}

	@Override
	public void onLocationChanged(Location location) {
		ActivityShoppie.myLocation = new LatLng(location.getLatitude(),
				location.getLongitude());
		// mLyTop.setText(location.getLatitude() + "-" +
		// location.getLongitude());
	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	private void onSessionStateChange(final Session session,
			SessionState state, Exception exception) {
		if (session != null && session.isOpened()) {
			if (state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
				Session.getActiveSession();
			} else {
				Request request = Request.newMeRequest(session,
						new Request.GraphUserCallback() {
							@Override
							public void onCompleted(GraphUser user,
									Response response) {
								if (session == Session.getActiveSession()) {
									if (user != null) {
										custName = user.getName();
										custEmail = user.getId();
										custAdress = "";
										facebookId = user.getId();
										birthday = user.getBirthday();
										gender = "Male";
										if (mShopieSharePref.getCustId() == 0)
											register(custName, custEmail,
													custAdress, gender,
													birthday, facebookId,
													AppVersion, OSVersion,
													friendId);
									}
								}
							}
						});
				request.executeAsync();
			}
		} else {
		}
	}

	// @Override
	// public void btnRegisterClick(View v, String name, String email,
	// String phone, String address, String gender, String birth) {
	// // TODO Auto-generated method stub
	// if (mShopieSharePref.getCustId() == 0)
	// register(name.getText().toString());
	// }

	@Override
	public void btnRegisterClick(View v, String name, String email,
			String phone, String address, String gender, String birth,
			String friendId) {
		// TODO Auto-generated method stub
		if (mShopieSharePref.getCustId() == 0)
			register(name, email, address, gender, birth, "", "1", "", friendId);
	}
}