/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.stk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.provider.Settings;

import com.android.internal.telephony.cat.CatLog;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.IccCardConstants;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccController;
import android.text.TextUtils;
import android.content.ComponentName;
/**
 * Boot completed receiver. used to reset the app install state every time the
 * device boots.
 *
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = new Object(){}.getClass().getEnclosingClass().getName();

    private boolean isCardReady(Context context) {
        int simCount = TelephonyManager.from(context).getSimCount();
        TelephonyManager tm = TelephonyManager.from(context);
        StkAppService appService = StkAppService.getInstance();
        CatLog.d(this, "simCount: " + simCount);
        for (int i = 0; i < simCount; i++) {
            // Check if the card is inserted.
            if (tm.hasIccCard(i)) {
                CatLog.d(this, "SIM " + i + " is inserted");
                if (tm.getSimState(i) == TelephonyManager.SIM_STATE_READY && appService != null
                        && appService.getStkContext(i) != null
                        && appService.getStkContext(i).mMainCmd != null) {
                    CatLog.d(this, "SIM " + i + " is ready.");
                    return true;
                }
            } else {
                CatLog.d(this, "SIM " + i + " is not inserted.");
            }
        }
        return false;
    }

    private boolean isCardReady(Context context, int slot) {
        TelephonyManager tm = TelephonyManager.from(context);
        StkAppService appService = StkAppService.getInstance();
        // Check if the card is inserted.
        if (slot < 0) {
            return false;
        }
        if (tm.getSimState(slot) == TelephonyManager.SIM_STATE_READY
                && appService != null
                && appService.getStkContext(slot) != null
                && appService.getStkContext(slot).mMainCmd != null) {
            CatLog.d(this, "SIM " + slot + " is ready.");
            return true;

        } else {
            CatLog.d(this, "SIM " + slot + " is not inserted.");
        }
        return false;
    }

    private Context mContext;
    private static final int EVENT_UPDATE_APN = 4;
    private boolean mHasAPNAdaptePrompt = false;
    private static final String SHOW_APN_ADAPTE_ACTION = "android.intent.action.SHOW_APN_ADAPTE_ACTION";
    public static final String PREFS_NAME = "iccid.before.apninfo";
    public static final String SIM_ICC_ID = "icc_id";
    private SharedPreferences mPreferences;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch(msg.what){
            case EVENT_UPDATE_APN:
                Intent tempIntent = (Intent) msg.obj;
                boolean isUsedBefore = isUsedBefore(mContext, tempIntent);
                CatLog.d(this, "EVENT_UPDATE_APN isUsedBefore = " + isUsedBefore);
                if (mContext != null && !isUsedBefore) {
                    mHasAPNAdaptePrompt = true;
                    Intent i = new Intent(SHOW_APN_ADAPTE_ACTION);
                    i.setComponent(new ComponentName("com.android.settings",
                            "com.android.settings.sim.ApnAdapteReceiver"));
                    i.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
                    mContext.sendBroadcast(i);
                }
                break;
             default: break;
            }

        }
    };

    /**
     * Store the current phone iccid to preference
     *
     * @parm phoneid phoneid
     *
     */
    public void putIccCardId(Context context, int phoneid, String iccid) {
        SharedPreferences.Editor editor = mPreferences.edit();
        // No need care that the iccid is null
        editor.putString(SIM_ICC_ID + phoneid, iccid);
        editor.commit();
    }

    private boolean isUsedBefore(Context context, Intent intent) {
        // set primary card phoneid as 0
        int phoneId = 0; // intent.getIntExtra(IccCardConstants.INTENT_KEY_PHONE_ID,
        // 0);
        String iccId = getIccId(phoneId);

        if (TextUtils.isEmpty(iccId))
            return true; // if the card is invalid, not prompt dialog

        mPreferences = mContext.getSharedPreferences(PREFS_NAME, 0);
        String beforeIccId = mPreferences.getString(SIM_ICC_ID + phoneId, "");
        CatLog.d(this, "phoneId:" + phoneId + ", iccId = " + iccId
                + ", beforeIccId = " + beforeIccId);
        if (!TextUtils.isEmpty(beforeIccId) && iccId.equals(beforeIccId)) {
            return true;
        } else {
            putIccCardId(context, phoneId, iccId);
            return false;
        }
    }

    private String getIccId(int phoneId) {
        String iccId = "";
        UiccController uc = UiccController.getInstance();
        if (uc != null) {
            IccRecords iccRecords = uc.getIccRecords(phoneId,UiccController.APP_FAM_3GPP);
            if (iccRecords != null) {
                iccId = iccRecords.getIccId();
            }
        }
        return iccId;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        CatLog.d(this, "action:" + action);
        if (action == null) {
            return;
        }

        mContext = context;
        mPreferences = mContext.getSharedPreferences(PREFS_NAME, 0);
        // make sure the app icon is removed every time the device boots.
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Bundle args = new Bundle();
            args.putInt(StkAppService.OPCODE, StkAppService.OP_BOOT_COMPLETED);
            context.startService(new Intent(context, StkAppService.class)
                    .putExtras(args));
            CatLog.d(this, "Tony onReceive:start to apapte apn. mHasAPNAdaptePrompt = " + mHasAPNAdaptePrompt);
            if (!mHasAPNAdaptePrompt) {
                Message msg = new Message();
                msg.what = EVENT_UPDATE_APN;
                msg.obj = intent;
                mHandler.sendMessageDelayed(msg, 0);
            }
            CatLog.d(LOG_TAG, "[ACTION_BOOT_COMPLETED]");
        } else if (action.equals(Intent.ACTION_USER_INITIALIZE)) {
            // TODO: http://b/25155491
            if (!android.os.Process.myUserHandle().isSystem()) {
                //Disable package for all secondary users. Package is only required for device
                //owner.
                context.getPackageManager().setApplicationEnabledSetting(context.getPackageName(),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
                return;
            }
        } else if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
            StkAppService appService = StkAppService.getInstance();
            if (null == appService) {
                CatLog.d(this, "appService is null...");
                return;
            }
            boolean isCUCC = appService.isCUCCOperator();
            boolean isAirPlaneModeOn = Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
            CatLog.d(this, "isCUCC: " + isCUCC + " and isAirPlaneModeOn: " + isAirPlaneModeOn);
            int slotId = intent.getIntExtra(PhoneConstants.SLOT_KEY, -1);
            String state = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
            CatLog.d(this, "slotId: " + slotId + " and state: " + state);
            if (slotId < 0 || isAirPlaneModeOn) {
                return;
            }
            if (isCUCC) {
                if (isCardReady(context, slotId)) {
                    StkAppInstaller.install(context, slotId);
                } else {
                    if (IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(state)) {
                        StkAppInstaller.unInstall(context, slotId);
                    }
                }
            } else {
                if (isCardReady(context)) {
                    StkAppInstaller.install(context);
                } else {
                    if (IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(state)) {
                        StkAppInstaller.unInstall(context);
                    }
                }
            }
        } else if (action.equals(TelephonyIntents.ACTION_SERVICE_STATE_CHANGED)) {
            CatLog.d(this, "Receive ACTION_SERVICE_STATE_CHANGED do nothing......");
            return;
       }
    }
}
