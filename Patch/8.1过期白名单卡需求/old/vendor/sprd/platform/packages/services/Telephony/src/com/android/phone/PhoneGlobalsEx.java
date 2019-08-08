
package com.android.phone;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.phone.CallForwardHelper;
import com.android.phone.CarrierConfigLoaderEx;
import com.android.phone.R;
import com.android.phone.SuppServiceConsumer;
import com.android.phone.TelephonyUIHelper;
import com.sprd.settings.plugin.TelephonyOrangeHelper;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManagerEx;
import com.android.internal.telephony.TelephonyIntentsEx;
import com.android.sprd.telephony.RadioInteractorFactory;
/**
 * It is a convenient class for other feature support. We had better put this class in the same
 * package with {@link PhoneGlobals}, so we can touch all the package permission variables and
 * methods in it.
 */
public class PhoneGlobalsEx extends ContextWrapper {
    private static final String TAG = "PhoneGlobalsEx";
    private static PhoneGlobalsEx mInstance;
    private Context mContext;
    // SPRD: added for sim lock
    private SimLockManager mSimLockManager;
    /* SPRD: add for bug490253 @{ */
    protected static final int EVENT_SEND_DISMISS_DIALOG_COMPLETE = 19;
    private static final long DELAY_MILLIS = 180000;
    private AlertDialog mMmiPreviousAlertDialog;
    /* @} */
    /* SPRD: add for bug620380 @{ */
    public static final int PHONE_STATE_CHANGED = 20;
    private CallManager mCM;
    /* @} */

    public static PhoneGlobalsEx getInstance() {
        return mInstance;
    }

    public PhoneGlobalsEx(Context context) {
        super(context);
        mInstance = this;
        mContext = context;
    }

    public void onCreate() {
        Log.d(TAG, "onCreate");
        RadioInteractorFactory.init(mContext);
        CallForwardHelper.getInstance();
        /* SPRD: add for bug620380 @{ */
        boolean isIncomingCallDialogHide
                = mContext.getResources().getBoolean(R.bool.config_hide_ussd_dialog_incommingcall);
        if (isIncomingCallDialogHide && mCM == null) {
            mCM = CallManager.getInstance();
            mCM.registerForPreciseCallStateChanged(mHandler,
                    PHONE_STATE_CHANGED, null);
        }
        /* @} */
        boolean showFdnNotifi
                = mContext.getResources().getBoolean(R.bool.config_show_fdn_notification);
        Log.d(TAG, "showFdnNotifi : " + showFdnNotifi);
        if (showFdnNotifi) {
            FdnHelper.getInstance();
        }
        // SPRD: add for bug689361, porting SS. Register Consumer Supplementary Service.
        for (Phone phone : PhoneFactory.getPhones()) {
            SuppServiceConsumer.getInstance(mInstance, phone);
        }
        //SPRD:Add for Carrier Config Loader
        CarrierConfigLoaderEx.init(mInstance);
        Log.d(TAG, "PhoneInterfaceManagerEx init");
        PhoneInterfaceManagerEx.init(this, PhoneFactory.getDefaultPhone());

        // Initialize anti-theft service if exists.
        TeleServicePluginsHelper.getInstanceForAntitheft().startAntitheftService(mContext);

        /* SPRD: added for sim lock @{ */
        IntentFilter simlockIntentFilter = new IntentFilter();
        simlockIntentFilter.addAction(TelephonyIntentsEx.SHOW_SIMLOCK_UNLOCK_SCREEN_ACTION);
        simlockIntentFilter.addAction(TelephonyIntentsEx.SHOW_SIMLOCK_UNLOCK_SCREEN_BYNV_ACTION);
        registerReceiver(mUnlockScreenReceiver, simlockIntentFilter);

        mSimLockManager = SimLockManager.getInstance(mContext, R.string.feature_support_simlock);
        if (SystemProperties.getBoolean("ro.simlock.unlock.autoshow", true)
                && !SystemProperties.getBoolean("ro.simlock.onekey.lock", false)
                && !SystemProperties.getBoolean("ro.simlock.unlock.bynv", false)) {
            mSimLockManager.registerForSimLocked(mContext);
        }
        /* @} */
        // SPRD: Add for fast shutdown
        FastShutdownHelper.init(this);
        // SPRD:Bug 693469 Reliance block notification.
        TelephonyUIHelper.init(this);
    }

    /** SPRD: added for sim lock @{ */
    private BroadcastReceiver mUnlockScreenReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action
                    .equals(TelephonyIntentsEx.SHOW_SIMLOCK_UNLOCK_SCREEN_ACTION)) {
                int simlockSlotFlag = intent.getIntExtra(
                        TelephonyIntentsEx.EXTRA_SIMLOCK_UNLOCK, 0);
                if (simlockSlotFlag == 0)
                    return;
                TelephonyManager tm = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                int phoneCount = tm.getPhoneCount();
                int allFlag = (1 << phoneCount) - 1;
                for (int i = 0; i < phoneCount; i++) {
                    if (((1 << i) & simlockSlotFlag) != 0) {
                        int simState = TelephonyManagerEx.getSimStateEx(i);
                        Log.d(TAG, "simState[" + i + "] = " + simState);
                        Message msg = mSimLockManager
                                .decodeMessage(simState, i);
                        if (msg != null && msg.what != 0) {
                            mHandler.sendMessage(msg);
                        }
                    }
                }
            } else if (action
                    .equals(TelephonyIntentsEx.SHOW_SIMLOCK_UNLOCK_SCREEN_BYNV_ACTION)) {
                mSimLockManager.showPanelForUnlockByNv(mContext);
            } 
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                /* SPRD: add for bug490253 & 620380 @{ */
                case EVENT_SEND_DISMISS_DIALOG_COMPLETE:
                    if (mMmiPreviousAlertDialog != null) {
                        mMmiPreviousAlertDialog.dismiss();
                        mMmiPreviousAlertDialog = null;
                    }
                    break;
                case PHONE_STATE_CHANGED:
                    onPhoneStateChanged();
                    break;
                /* @} */
                default:
                    mSimLockManager.showPanel(mContext, msg);
                    break;
            }
        }
    };
    /** @} */

    /* SPRD: add for bug620380 & 490253 @{ */
    public void handleMMIDialogDismiss(final Phone phone, Context context, final MmiCode mmiCode,
            Message dismissCallbackMessage, AlertDialog previousAlert) {
        mMmiPreviousAlertDialog = PhoneUtils.displayMMIComplete(mmiCode.getPhone(), context,
                mmiCode, null, null);
        mHandler.removeMessages(EVENT_SEND_DISMISS_DIALOG_COMPLETE);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_SEND_DISMISS_DIALOG_COMPLETE),
                DELAY_MILLIS);
    }

    private void onPhoneStateChanged() {
        PhoneConstants.State state = mCM.getState();
        if (state == PhoneConstants.State.RINGING) {
            hideUssdDialog();
        }
        if (state == PhoneConstants.State.IDLE) {
            showUssdDialog();
        }
    }

    public void hideUssdDialog() {
        if (mMmiPreviousAlertDialog != null && mMmiPreviousAlertDialog.isShowing()) {
            mMmiPreviousAlertDialog.hide();
        }
    }

    public void showUssdDialog() {
        if (mMmiPreviousAlertDialog != null && mMmiPreviousAlertDialog.isShowing()) {
            mMmiPreviousAlertDialog.show();
        }
    }
    /* @} */
}
