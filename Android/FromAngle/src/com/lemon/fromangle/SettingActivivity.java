package com.lemon.fromangle;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.lemon.fromangle.config.FromAngleSharedPref;
import com.lemon.fromangle.config.GlobalValue;
import com.lemon.fromangle.config.WebServiceConfig;
import com.lemon.fromangle.network.AsyncHttpPost;
import com.lemon.fromangle.network.AsyncHttpResponseProcess;
import com.lemon.fromangle.network.ParameterFactory;
import com.lemon.fromangle.network.ParserUtility;
import com.lemon.fromangle.utility.DialogUtility;
import com.lemon.fromangle.utility.EmailValidator;
import com.lemon.fromangle.utility.StringUtility;

@SuppressLint("SimpleDateFormat")
public class SettingActivivity extends Activity {
	private EditText txtName;
	private EditText txtEmail;
	private EditText txtTel;
	private EditText txtDateSetting;
	private EditText txtTimeSetting;
	private EditText txtDayAfter;
	private Spinner spnSelectRingTune;
	private CheckBox chkVibrate;
	private com.lemon.fromangle.utility.AutoBGButton btnSave, btnLeft,
			btnRight;
	private com.lemon.fromangle.utility.AutoBGButton btnCancel;

	private TimePickerDialog timePicker;
	private DatePickerDialog datePicker;
	private ArrayList<String> listDaysAfter;
	private FromAngleSharedPref mFromAngleSharedPref;
	private EmailValidator mEmailValidator;

	private String userName, userId, tel, email, date, time, daysafter,
			uriRingtune;
	private boolean isVibrate = false;
	private Uri[] mListUriRingTone;

	private MediaPlayer mMediaPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_setting);

		mEmailValidator = new EmailValidator();

		/* inti ui */
		initUI();

		/* check is update or register */
		checkIsUpdateOrRegister();
	}

	private void checkIsUpdateOrRegister() {
		String filePath = null;
		mFromAngleSharedPref = new FromAngleSharedPref(this);
		userId = mFromAngleSharedPref.getUserId();
		if (userId != null && !"".equals(userId)) {
			userName = mFromAngleSharedPref.getUserName();
			email = mFromAngleSharedPref.getEmail();
			tel = mFromAngleSharedPref.getPhone();
			date = mFromAngleSharedPref.getValidationDate();
			daysafter = mFromAngleSharedPref.getValidationDaysAfter();
			time = mFromAngleSharedPref.getValidationTime();
			uriRingtune = mFromAngleSharedPref.getRingTuneFile();
			isVibrate = mFromAngleSharedPref.getVibrateMode();

			txtDateSetting.setText(date);
			txtDayAfter.setText(daysafter);
			txtEmail.setText(email);
			txtName.setText(userName);
			txtTel.setText(tel);
			txtTimeSetting.setText(time);
			chkVibrate.setChecked(isVibrate);

			mMediaPlayer = new MediaPlayer();
			for (int i = 0; i < mListUriRingTone.length; i++) {
				if (uriRingtune.equals(mListUriRingTone[i].toString())) {
					spnSelectRingTune.setSelection(i);
					filePath = convertMediaUriToPath(mListUriRingTone[i]);
				}
			}
			Log.e("uriRingtone " + filePath, "uriRingtone " + uriRingtune);
			try {
				mMediaPlayer.setDataSource(filePath);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mMediaPlayer.start();

		}

	}

	protected String convertMediaUriToPath(Uri uri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String path = cursor.getString(column_index);
		cursor.close();
		return path;
	}

	private void initUI() {
		txtName = (EditText) findViewById(R.id.txtName);
		txtEmail = (EditText) findViewById(R.id.txtEmail);
		txtTel = (EditText) findViewById(R.id.txtTel);
		txtDateSetting = (EditText) findViewById(R.id.txtDateSetting);
		txtTimeSetting = (EditText) findViewById(R.id.txtTimeSetting);
		txtDayAfter = (EditText) findViewById(R.id.txtDayAfter);
		spnSelectRingTune = (Spinner) findViewById(R.id.spnSelectRingTune);
		chkVibrate = (CheckBox) findViewById(R.id.chkVibrate);
		btnSave = (com.lemon.fromangle.utility.AutoBGButton) findViewById(R.id.btnSave);
		btnCancel = (com.lemon.fromangle.utility.AutoBGButton) findViewById(R.id.btnCancel);
		btnLeft = (com.lemon.fromangle.utility.AutoBGButton) findViewById(R.id.btnLeft);
		btnLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				finish();
			}
		});
		btnRight = (com.lemon.fromangle.utility.AutoBGButton) findViewById(R.id.btnRight);
		btnRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse(getString(R.string.url_home));
				startActivity(new Intent(Intent.ACTION_VIEW, uri));
			}
		});
		btnCancel.setOnClickListener(onCancelClick);
		btnSave.setOnClickListener(onSaveClick);

		txtDateSetting.setOnTouchListener(showDatePicker);

		txtTimeSetting.setOnTouchListener(showTimePicker);

		txtDayAfter.setOnTouchListener(showDialogSelectDateAfter);

		txtDayAfter.setText("7");

		initSpinnerDaysAfter();
		initSpinnerRingtune();

	}

	@SuppressWarnings("deprecation")
	private void initSpinnerRingtune() {
		RingtoneManager mRingtoneManager2 = new RingtoneManager(this); // adds
		// ringtonemanager
		mRingtoneManager2.setType(RingtoneManager.TYPE_RINGTONE); // sets the
		// type to
		// ringtones
		mRingtoneManager2.setIncludeDrm(true); // get list of ringtones to
		// include DRM

		Cursor mCursor2 = mRingtoneManager2.getCursor(); // appends my cursor to
		// the
		// ringtonemanager
		startManagingCursor(mCursor2); // starts the cursor query

		// prints output for diagnostics
		String test = mCursor2.getString(mCursor2
				.getColumnIndexOrThrow(RingtoneManager.EXTRA_RINGTONE_TITLE));

		/* check list uri of cursor */
		mListUriRingTone = checkListUri(mRingtoneManager2, mCursor2);
		String[] from = { mCursor2
				.getColumnName(RingtoneManager.TITLE_COLUMN_INDEX) };
		// final String[] listUri = { mCursor2
		// .getColumnIndex(RingtoneManager.URI_COLUMN_INDEX) };
		Log.e("size of list", "size of list " + mListUriRingTone.length);

		int[] to = { android.R.id.text1 };

		// create simple cursor adapter
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_spinner_item, mCursor2, from, to);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// get reference to our spinner
		spnSelectRingTune.setAdapter(adapter);
		spnSelectRingTune
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub
						int pos = spnSelectRingTune.getSelectedItemPosition();
						uriRingtune = mListUriRingTone[pos].toString();
						// Log.e("uriRIngtune " + pos, "uriRingtune "
						// + listUri.length);
						Toast.makeText(SettingActivivity.this, pos + "",
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub

					}
				});
	}

	private Uri[] checkListUri(RingtoneManager mRingTone, Cursor mCursor2) {
		int alarmsCount = mCursor2.getCount();
		if (alarmsCount == 0 && !mCursor2.moveToFirst()) {
			return null;
		}
		Uri[] alarms = new Uri[alarmsCount];
		while (!mCursor2.isAfterLast() && mCursor2.moveToNext()) {
			int currentPosition = mCursor2.getPosition();
			alarms[currentPosition] = mRingTone.getRingtoneUri(currentPosition);
		}
		return alarms;
	}

	private void initSpinnerDaysAfter() {
		listDaysAfter = new ArrayList<String>();
		for (int i = 1; i < 100; i++) {
			listDaysAfter.add(i + "");
		}

	}

	OnClickListener onCancelClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			finish();

		}
	};
	OnClickListener onSaveClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (!checkValidateField()) {
				String email = txtEmail.getText().toString();
				if (checkValidateEmail(email))
					onSaveClick();
				else
					DialogUtility.alert(SettingActivivity.this,
							getString(R.string.email_not_validate));
			} else

				Toast.makeText(SettingActivivity.this,
						R.string.plz_input_required_field, Toast.LENGTH_LONG)
						.show();
		}
	};

	private boolean checkValidateEmail(String email) {
		// TODO Auto-generated method stub
		if (mEmailValidator.validate(email))
			return true;
		else
			return false;
	}

	private void onSaveClick() {

		String userName = txtName.getText().toString();
		String tel = txtTel.getText().toString();
		String email = txtEmail.getText().toString();
		String days = txtDateSetting.getText().toString();
		String times = txtTimeSetting.getText().toString();
		String daysAfter = txtDayAfter.getText().toString();
		String userId = mFromAngleSharedPref.getUserId();
		List<NameValuePair> params = null;
		if (userId != null && !StringUtility.isEmpty(userId)) {
			params = ParameterFactory.createUpdateSettingParam(userId,
					userName, tel, email, days, times, daysAfter);
		} else
			params = ParameterFactory.createRegisterSettingParam(userName, tel,
					email, days, times, daysAfter);
		AsyncHttpPost postRegister = new AsyncHttpPost(SettingActivivity.this,
				new AsyncHttpResponseProcess(SettingActivivity.this) {
					@Override
					public void processIfResponseSuccess(String response) {
						/* check info response from server */
						if (StringUtility
								.isEmpty(SettingActivivity.this.userId))
							checkInfoReponseFromServer(response);
						else
							checkInfoUserUpdate(response);
					}

					@Override
					public void processIfResponseFail() {
						// TODO Auto-generated method stub
						Log.e("failed ", "failed");
					}
				}, params, true);
		if (userId != null && !StringUtility.isEmpty(userId))
			postRegister.execute(WebServiceConfig.URL_UPDATE_REGISTER_SETTING);
		else
			postRegister.execute(WebServiceConfig.URL_REGISTER_SETTING);
	}

	/**
	 * check update info
	 * 
	 * @param response
	 */
	private void checkInfoUserUpdate(String response) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = null;
		String errorMsg = null;
		try {
			jsonObject = new JSONObject(response);
			if (jsonObject != null && jsonObject.length() > 0) {
				errorMsg = ParserUtility.getStringValue(jsonObject,
						GlobalValue.PARAM_ERROR);
				int error = Integer.parseInt(errorMsg);
				if (error == GlobalValue.MSG_RESPONSE_UPDATE_INFO_SUCESS) {
					showToast(getString(R.string.change_info_sucess));
					addDataToPreference();
				} else if (error == GlobalValue.MSG_RESPONSE_UPDATE_INFO_FAILED) {
					showToast(getString(R.string.duplicated_email));
				} else
					showToast(getString(R.string.failed_to_conect_server));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			DialogUtility.alert(SettingActivivity.this,
					getString(R.string.failed_to_conect_server));
		}
	}

	/**
	 * check validate info response from server
	 * 
	 * @param response
	 */
	private void checkInfoReponseFromServer(String response) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = null;
		JSONObject jsonId = null;
		String paramData = null;
		String userId = null;
		String userName = null;
		String errorMsg = null;
		try {
			jsonObject = new JSONObject(response);
			if (jsonObject != null && jsonObject.length() > 0) {
				errorMsg = ParserUtility.getStringValue(jsonObject,
						GlobalValue.PARAM_ERROR);
				int error = Integer.parseInt(errorMsg);
				if (error == GlobalValue.MSG_REPONSE_SUCESS) {
					paramData = ParserUtility.getStringValue(jsonObject,
							GlobalValue.PARAM_DATA);
					if (paramData != null) {
						jsonId = new JSONObject(paramData);
						userId = ParserUtility.getStringValue(jsonId,
								GlobalValue.PARAM_USER_ID);
						userName = ParserUtility.getStringValue(jsonId,
								GlobalValue.PARAM_USER_NAME);
					}
					if (userId != null) {
						/* add to preference */
						mFromAngleSharedPref.setUserId(userId);
						addDataToPreference();
						
						showToast("Sucessfully");
					}
				} else if (error == GlobalValue.MSG_REPONSE_FAILED) {
					showToast(getString(R.string.duplicated_email));
				} else
					DialogUtility.alert(SettingActivivity.this,
							getString(R.string.failed_to_conect_server));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			DialogUtility.alert(SettingActivivity.this,
					getString(R.string.failed_to_conect_server));
		}
	}

	private void addDataToPreference() {
		// TODO Auto-generated method stub
		GlobalValue.prefs.setVibrateMode(chkVibrate.isChecked());
		GlobalValue.prefs.setRingTuneFile(uriRingtune);
		GlobalValue.prefs.setUserName(txtName.getText().toString());
		GlobalValue.prefs.setEmail(txtEmail.getText().toString());
		GlobalValue.prefs.setPhone(txtTel.getText().toString());
		GlobalValue.prefs
				.setValidationDate(txtDateSetting.getText().toString());
		GlobalValue.prefs.setValidationDaysAfter(txtDayAfter.getText()
				.toString());
		GlobalValue.prefs
				.setValidationTime(txtTimeSetting.getText().toString());
		GlobalValue.prefs.setVibrateMode(chkVibrate.isChecked());
		GlobalValue.prefs.setRingTuneFile(uriRingtune);
		GlobalValue.prefs.setTopScreenFinalValidation(txtDateSetting.getText()
				.toString() + " " + txtTimeSetting.getText().toString());

		String dateStr = txtDateSetting.getText().toString();

		// Date date1 = new Date(txtDateSetting.getText().toString());
		Date date1 = new Date();
		int daysAfter = Integer.parseInt(txtDayAfter.getText().toString());
		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		try {
			date1 = df.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Date nextValidationDate = addDaysToDate(date1, daysAfter);
		Log.e("nextdate " + nextValidationDate, "nextdate "
				+ nextValidationDate);
		Log.e("ringtone", "ringtone " + uriRingtune);
		String nextValidationDateStr;
		nextValidationDateStr = df.format(nextValidationDate);

		GlobalValue.prefs.setTopScreenNextValidation(nextValidationDateStr
				+ " " + txtTimeSetting.getText().toString());
	}

	public static Date addDaysToDate(Date input, int numberDay) {

		Calendar defaulCalender = Calendar.getInstance();
		defaulCalender.setTime(input);
		defaulCalender.add(Calendar.DATE, numberDay);
		Date resultdate = new Date(defaulCalender.getTimeInMillis());
		return resultdate;
	}

	OnTouchListener showTimePicker = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				int hourStr = 10;
				int minuteStr = 0;

				timePicker = new TimePickerDialog(SettingActivivity.this,
						new OnTimeSetListener() {

							@Override
							public void onTimeSet(TimePicker view,
									int hourOfDay, int minute) {
								String hourStr = hourOfDay < 10 ? hourOfDay
										+ "0" : hourOfDay + "";
								String minuteStr = minute < 10 ? minute + "0"
										: minute + "";
								txtTimeSetting.setText(hourOfDay + ":"
										+ minuteStr);

							}
						}, hourStr, minuteStr, true);
				timePicker.show();
			}
			return false;
		}
	};
	OnTouchListener showDialogSelectDateAfter = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN)

				showDialogSelectDateAfter();
			return false;
		}
	};

	private void showDialogSelectDateAfter() {

		int selectIndex = 6;
		if (!txtDayAfter.getText().toString().equalsIgnoreCase("")) {
			selectIndex = listDaysAfter.indexOf(txtDayAfter.getText()
					.toString());
		}
		DialogUtility.showSimpleOptionDialog(SettingActivivity.this,
				R.string.app_name,
				listDaysAfter.toArray(new String[listDaysAfter.size()]),
				getString(R.string.btn_ok), selectIndex, onSelectGenre);

	}

	DialogInterface.OnClickListener onSelectGenre = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {

			int selectedPosition = ((AlertDialog) dialog).getListView()
					.getCheckedItemPosition();
			// DialogUtility.alert(SettingActivivity.this, "Which : " + which
			// + " : " + selectedPosition);
			txtDayAfter.setText(listDaysAfter.get(selectedPosition));

		}
	};

	OnTouchListener showDatePicker = new OnTouchListener() {

		@SuppressWarnings("deprecation")
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				// TODO Auto-generated method stub
				final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				Date dateCurrent = new Date();
				try {
					dateCurrent = df.parse(txtDateSetting.toString());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					dateCurrent = new Date();
					e.printStackTrace();
				}

				datePicker = new DatePickerDialog(SettingActivivity.this,
						new OnDateSetListener() {
							@Override
							public void onDateSet(DatePicker view, int year,
									int monthOfYear, int dayOfMonth) {
								Date d = new Date(year - 1900, monthOfYear,
										dayOfMonth);
								txtDateSetting.setText(df.format(d));
							}
						}, dateCurrent.getYear() + 1900,
						dateCurrent.getMonth(), dateCurrent.getDate());
				datePicker.show();
			}
			return false;
		}
	};

	private boolean checkValidateField() {
		return (StringUtility.isEmpty(txtName)
				|| StringUtility.isEmpty(txtEmail)
				|| StringUtility.isEmpty(txtDateSetting)
				|| StringUtility.isEmpty(txtTimeSetting) || StringUtility
					.isEmpty(txtDayAfter));
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
		case GlobalValue.DIALOG_FAILED_TO_CONNECT_SERVER:
			builder.setMessage(getString(R.string.failed_to_conect_server));
			builder.setPositiveButton(getString(R.string.btn_ok),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							return;
						}
					});
			return builder.create();

		default:
			return null;
		}
	}

	private void showToast(String string) {
		Toast.makeText(SettingActivivity.this, string, Toast.LENGTH_SHORT)
				.show();
	}
}