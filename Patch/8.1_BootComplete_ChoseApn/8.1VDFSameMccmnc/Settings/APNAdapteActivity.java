package com.android.settings.sim;

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.android.settings.R;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import android.telephony.SubscriptionInfo;

/**
 +*
 +*This Activity is added by SPRD 2016-01-14 for ZTE Europe Request
 +*/

public class APNAdapteActivity extends Activity {
    private static final String TAG = "APNAdapteActivity";
    private static final boolean DBG = true;
    private Context mContext = null;
    protected ArrayList<APNType> mApnList = null;
    protected Dialog mAlertDialog;
    protected Dialog mChooseAlertDialog;
    private TelephonyManager mTelephonyManager = null;

    private int mPhoneId;
    public static final String APN_ID = "apn_id";
    public static final String STR_CONTENT_URI = "content://telephony/carriers";
    public static final String PREFERRED_APN_URI = "content://telephony/carriers/preferapn";
    public static final Uri CONTENT_URI = Uri.parse(STR_CONTENT_URI);
    public static final Uri PREFERAPN_URI = Uri.parse(PREFERRED_APN_URI);
    public static final String DEFAULT_SORT_ORDER = "name ASC";
    public static final String PATH_PREFERAPN = "preferapn";

    public static final String BOOT_ADAPTE_APN = "boot_adapte_apn";

    private static final int LOG_TYPE_INFO = 0;
    private static final int LOG_TYPE_ERROR = 1;

    private static final int UPDATE_DETECTED_DATA = 0;
    private static final int UPDATE_CHOOSE_DATA = 1;

    private static final int ID_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int APN_INDEX = 2;
    private static final int TYPES_INDEX = 3;
    private static final int MVNO_TYPE = 4;
    private static final int MVNO_MATCH_DATA = 5;
    private boolean hasData = false;
    private SubscriptionManager mSubscriptionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFinishOnTouchOutside(false);
        mContext = this;
        mSubscriptionManager = SubscriptionManager.from(mContext);
        mPhoneId = SubscriptionManager.getPhoneId(mSubscriptionManager.getDefaultDataSubscriptionId());
        mTelephonyManager = new TelephonyManager(APNAdapteActivity.this);
        /**
         * Get the value that from @ApnAdapteReceiver,if it is null,so no need to prompt
         * detected dialog
         */
        final String dialogFlag = getIntent().getStringExtra(BOOT_ADAPTE_APN);
        /**
         * As query is very fast, so no need to start a new thread to query
         * */
        boolean hasData = updateAPNList(dialogFlag != null);
        if (DBG)
            log(LOG_TYPE_INFO, "Detected data : " + hasData + ", mPhoneId = " + mPhoneId);
        if (!hasData) {
            finish();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    private String getApnIdByPhoneId(int phoneId) {
        switch (phoneId) {
            case 0:
                return APN_ID;
            default:
                return APN_ID + "_sim" + (phoneId + 1);
        }
    }

    private boolean updateAPNList(boolean needPreparedDialog) {
        String where = "";
        Uri contentUri = Uri.parse(CONTENT_URI.toString());
        final TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        final String mccmnc = tm.getSimOperator();
        Log.d(TAG, "mccmnc = " + mccmnc);
        where = "numeric=\"" + mccmnc + "\"";
        where += " and type like '%default%'";
        log(LOG_TYPE_INFO, "Tony where = " + where);
        Cursor cursor = getContentResolver().query(
                contentUri,
                new String[] { "_id", "name", "apn", "type", "mvno_type",
                        "mvno_match_data" }, where, null, DEFAULT_SORT_ORDER);
        mApnList = new ArrayList<APNType>();
        if (cursor != null) {
            log(LOG_TYPE_INFO, "cursor count : " + cursor.getCount());
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String name = cursor.getString(NAME_INDEX);
                String apn = cursor.getString(APN_INDEX);
                String key = cursor.getString(ID_INDEX);
                String type = cursor.getString(TYPES_INDEX);
                String mvnoType = cursor.getString(MVNO_TYPE);
                String mvnoMatchData = cursor.getString(MVNO_MATCH_DATA);
                /**
                 * Only for Contract WAP and PAYG WAP apns can be add in apnlist
                 **/
                log(LOG_TYPE_INFO, "name : " + name );
                if(!TextUtils.isEmpty(name) && ("Contract WAP".equals(name) ||
                        "PAYG WAP".equals(name))){
                    mApnList.add(new APNType(name, apn, key, type, mvnoType, mvnoMatchData));
                }
                cursor.moveToNext();
            }
            cursor.close();

            if (mApnList.size() > 0) {
                for (APNType apntype : mApnList) {
                    log(LOG_TYPE_INFO, "apntype : " + apntype.toString() );
                }
            }
        }

        log(LOG_TYPE_INFO, "mApnList : " + mApnList.size() + ", needPreparedDialog = " + needPreparedDialog);

        if (mApnList.size() > 0) {
            hasData = true;
            Dialog d = onCreateDialog(UPDATE_CHOOSE_DATA, null);
            d.setCanceledOnTouchOutside(false);
            d.show();
        } else {
            Toast.makeText(this, getResources().getString(R.string.apn_no_prefer_apn), Toast.LENGTH_LONG).show();
        }
        return hasData;
    }

    protected ArrayList<String> getSpnList(ArrayList<APNType> apnList) {
        if (apnList.size() < 0)
            return null;
        ArrayList<String> spnList = new ArrayList<String>();
        for (APNType apn : apnList) {
            if(!TextUtils.isEmpty(apn.mvnoType) && !TextUtils.isEmpty(apn.mvnoMatchData)){
                apn.name = apn.mvnoMatchData;
            }
            spnList.add(apn.name);
        }
        return spnList;
    }
    private Dialog preparedDialog() {
        mAlertDialog = new Builder(this)
                .setCancelable(false)
                .setTitle(R.string.apnset_title)
                .setMessage(R.string.apnset_message)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(R.string.apn_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                mAlertDialog.dismiss();
                                showDialog(UPDATE_CHOOSE_DATA, null);
                            }
                        })
                .setNegativeButton(R.string.apn_cancle,
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                                int which) {
                                finish();
                            }
                        }).create();
        return mAlertDialog;
    }

    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id, Bundle bundle) {
        switch (id) {
            case UPDATE_CHOOSE_DATA:
                ListView apnListView = new ListView(this);
                apnListView.setAdapter(new ArrayAdapter<String>(mContext,
                        android.R.layout.simple_expandable_list_item_1,
                        getSpnList(mApnList)));
                Builder builder = new Builder(this);
                builder.setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle(R.string.apn_list_title).setView(apnListView);
                mChooseAlertDialog = builder.create();
                apnListView.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                                            int arg2, long arg3) {
                        String mSelectedKey = ((APNType) mApnList.get(arg2)).key;
                        if (DBG)
                            log(LOG_TYPE_INFO, "ListView mSelectedKey : "
                                    + mSelectedKey + ", id = " + arg2);
                        ContentResolver resolver = getContentResolver();

                        ContentValues values = new ContentValues();
                        values.put(getApnIdByPhoneId(mPhoneId), mSelectedKey);
                        resolver.update(PREFERAPN_URI, values, null, null);
                        mChooseAlertDialog.dismiss();
                        finish();
                    }
                });
                apnListView.setOnKeyListener(new OnKeyListener() {
                    @Override
                    public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                        if (arg2.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                            if(mAlertDialog != null && mAlertDialog.isShowing()){
                                mChooseAlertDialog.dismiss();
                            }
                            finish();
                            return true;
                        }
                        return false;
                    }
                });
                return mChooseAlertDialog;

            default:
                break;
        }
        return super.onCreateDialog(id);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    protected void log(int logType, String message) {
        switch (logType) {
            case LOG_TYPE_INFO:
                Log.i(TAG, message);
                break;
            case LOG_TYPE_ERROR:
                Log.e(TAG, message);
                break;
            default:
                Log.d(TAG, message);
                break;
        }
    }

    class APNType {
        String name = null;
        String apn = null;
        String key = null;
        String type = null;
        String mvnoType = null;
        String mvnoMatchData = null;

        public APNType(String name, String apn, String key, String type,
                       String mvnoType, String mvnoMatchData) {
            this.name = name;
            this.apn = apn;
            this.key = key;
            this.type = type;
            this.mvnoType = mvnoType;
            this.mvnoMatchData = mvnoMatchData;
        }

        public String toString() {
            return "APN: " + this.name + " : " + this.apn + " : "
                    + this.key + " : " + this.type + " : " + this.mvnoType
                    + " : " + this.mvnoMatchData;
        }
    }
}