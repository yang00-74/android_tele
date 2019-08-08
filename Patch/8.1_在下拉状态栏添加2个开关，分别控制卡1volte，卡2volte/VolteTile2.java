/*
 * Copyright (C) 2014 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.database.ContentObserver;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.systemui.R;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSHost;
import com.android.ims.ImsManager;
import android.service.quicksettings.Tile;
import android.provider.Settings.Global;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;    //add

import com.android.ims.ImsManager; //add

public class VolteTile2 extends QSTileImpl<BooleanState> {

    public static final int QS_VOLTE = 416;
    private final GlobalSetting mVolteSetting;
    private boolean mListening;
    private int mSlotId = 1;
    ImsManager mImsManager ;
    public VolteTile2(QSHost host) {
        super(host);
        mImsManager = ImsManager.getInstance(mContext, mSlotId);
        mVolteSetting = new GlobalSetting(mContext, mHandler,
                android.provider.Settings.Global.ENHANCED_4G_MODE_ENABLED + "_" + mSlotId) {
            @Override
            protected void handleValueChanged(int value) {
                Log.d(TAG, "mDataSetting handleValueChanged");
                mState.value = mImsManager.isEnhanced4gLteModeSettingEnabledByUserForSlot();;
                handleRefreshState(value);
            }
        };
       
    }


    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void handleSetListening(boolean listening) {
        if (mListening == listening) return;
        mListening = listening;
        if (listening) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            mContext.registerReceiver(mReceiver, filter);
        } else {
            mContext.unregisterReceiver(mReceiver);
        }
        mVolteSetting.setListening(listening);
    }


    @Override
    protected void handleClick() {
        if (ActivityManager.isUserAMonkey() || isAirplaneModeOn() || !isCurrentSimAvailable()) {
            Log.d(TAG, "isCurrentSimAvailable");
            return;
        }

        int defalutDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        int currentSubId = SubscriptionManager.getSubId(mSlotId)[0];
        Log.d(TAG, "handleClick defalutDataSubId = " + defalutDataSubId + " currentSubId = " + currentSubId);
        if (currentSubId == defalutDataSubId &&
               !isImsTurnOffAllowed(mContext) && 
               mImsManager.isEnhanced4gLteModeSettingEnabledByUserForSlot()) {
            /* @} */
            Toast.makeText(mContext, getString(R.string.turn_off_ims_error),
                   Toast.LENGTH_LONG).show();
            return ;
        }
        if (mImsManager.isEnhanced4gLteModeSettingEnabledByUserForSlot()){
            Log.d(TAG, "isEnhanced4gLteModeSettingEnabledByUserForSlot false");
            mImsManager.setEnhanced4gLteModeSettingForSlot(false);
        }else {
            Log.d(TAG, "isEnhanced4gLteModeSettingEnabledByUserForSlot true");
            mImsManager.setEnhanced4gLteModeSettingForSlot(true);
        }  

        boolean newState = !mState.value;
        refreshState(newState);
    }


    private boolean isImsTurnOffAllowed(Context context) {
        return !ImsManager.isWfcEnabledByPlatform(context)
                        || !ImsManager.isWfcEnabledByUser(context);
    }

    private boolean isCurrentSimAvailable() {
        int currentSubId = SubscriptionManager.getSubId(mSlotId)[0];
//        int defaultDataPhoneId = SubscriptionManager.getSlotId(defaultDataSubId);
        boolean isCurrentSimReady = SubscriptionManager
                .getSimStateForSlotIndex(mSlotId) == TelephonyManager.SIM_STATE_READY;
        boolean isCurrentSimSubIdValid = SubscriptionManager.isValidSubscriptionId(currentSubId);
        Log.d(TAG, "currentSubId = " + currentSubId + " isDefaultDataSimReady = "
                + isCurrentSimReady + " isDefaultDataStandby = " + isCurrentSimSubIdValid);
        return isCurrentSimReady && isCurrentSimSubIdValid;
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        if (mImsManager == null) {
            mImsManager = ImsManager.getInstance(mContext, mSlotId);
        }
        state.value = mImsManager.isEnhanced4gLteModeSettingEnabledByUserForSlot();
        state.label = mContext.getString(R.string.quick_settings_volte2);
//        ImsManager imsManager = ImsManager.getInstance(mContext, mSlotId);
        if (mImsManager.isEnhanced4gLteModeSettingEnabledByUserForSlot()
                && !isAirplaneModeOn()
                && isCurrentSimAvailable()){
            state.icon = ResourceIcon.get(R.drawable.ic_qs_mobile_data_on);
        }else {
            state.icon = ResourceIcon.get(R.drawable.ic_qs_mobile_data_off);
        }
    }

    @Override
    public Intent getLongClickIntent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CharSequence getTileLabel() {
        // TODO Auto-generated method stub
        return "VolteTile2";
    }

    @Override
    public int getMetricsCategory() {
        return QS_VOLTE;
    }

    public boolean isAirplaneModeOn() {
        return Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
                Log.d(TAG, "AIRPLANE_MODE_CHANGED");
                refreshState();
            }
        }
    };
}
