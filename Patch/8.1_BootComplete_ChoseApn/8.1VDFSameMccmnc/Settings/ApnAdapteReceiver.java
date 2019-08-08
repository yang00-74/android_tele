package com.android.settings.sim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;

public class ApnAdapteReceiver extends BroadcastReceiver {
    private static final String TAG = "ApnAdapteReceiver";
    private static final String SHOW_APN_ADAPTE_ACTION = "android.intent.action.SHOW_APN_ADAPTE_ACTION";
    private static boolean mBootCompleted = false;
    private static boolean mSimLoaded = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        TelephonyManager mTelephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (!mTelephonyManager.hasIccCard()) {
            Log.d(TAG, "Has no card,so return");
            return;
        }
        String action = intent.getAction();
        String stateExtra = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
        int slotId = intent.getIntExtra(PhoneConstants.SLOT_KEY, 0);
        int subId = intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY,
                SubscriptionManager.INVALID_SUBSCRIPTION_ID);
        Log.d(TAG, "ApnAdapteReceiver action = " + action);
        if (action.equals(SHOW_APN_ADAPTE_ACTION)) {
            mBootCompleted = true;
        } else if(action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
            if (IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(stateExtra)) {
                int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
                Log.d(TAG, "INTENT_VALUE_ICC_LOADED subId =" +subId
                        + ",defaultDataSubId =" + defaultDataSubId);
                if(subId == defaultDataSubId) {
                    mSimLoaded = true;
                }
            }
        }

        Log.d(TAG, "mBootCompleted =" + mBootCompleted  + ",,mSimLoaded ="+ mSimLoaded);
        if(mBootCompleted && mSimLoaded) {
            Log.d(TAG, "receive broadcast : " + SHOW_APN_ADAPTE_ACTION);
            Intent sentIntent = new Intent(context.getApplicationContext(), APNAdapteActivity.class);
            sentIntent.putExtra(APNAdapteActivity.BOOT_ADAPTE_APN, "boot_adapte_apn");
            sentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(sentIntent);
        }
    }
}