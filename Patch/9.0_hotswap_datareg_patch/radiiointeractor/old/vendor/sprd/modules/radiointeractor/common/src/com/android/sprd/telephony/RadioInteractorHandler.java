
package com.android.sprd.telephony;

import com.android.sprd.telephony.RadioInteractorCore.SuppService;
import com.android.sprd.telephony.uicc.IccIoResult;
import com.android.sprd.telephony.uicc.IccUtils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.telephony.data.DataProfile;
import android.telephony.IccOpenLogicalChannelResponse;
import android.telephony.TelephonyManager;
import com.android.sprd.telephony.uicc.IccCardApplicationStatusEx;
import com.android.sprd.telephony.uicc.IccCardApplicationStatusEx.AppState;
import com.android.sprd.telephony.uicc.IccCardApplicationStatusEx.PersoSubState;
import com.android.sprd.telephony.uicc.IccCardStatusEx;
import com.android.sprd.telephony.uicc.IccCardStatusEx.CardState;

public class RadioInteractorHandler extends Handler {
    public static final String TAG = "RadioInteractorHandler";

    RadioInteractorCore mRadioInteractorCore;
    RadioInteractorNotifier mRadioInteractorNotifier;
    SyncHandler mHandler;

 /*
  * This section defines all requests for events
  */
    protected static final int EVENT_GET_REQUEST_RADIOINTERACTOR_DONE = 1;
    protected static final int EVENT_INVOKE_OEM_RIL_REQUEST_STRINGS_DONE = 2;
    protected static final int EVENT_INVOKE_GET_SIM_CAPACITY_DONE = 3;
    protected static final int EVENT_INVOKE_ENABLE_RAU_NOTIFY_DONE = 4;
    protected static final int EVENT_GET_ATR_DONE = 5;

    // SPRD: add for HIGH_DEF_AUDIO
    protected static final int EVENT_GET_HD_VOICE_STATE_DONE = 6;
    // SPRD: Send request to set call forward number whether shown
    protected static final int EVENT_REQUEST_SET_COLP = 7;
    /*SPRD: Bug#542214 Add support for store SMS to Sim card @{*/
    protected static final int EVENT_REQUEST_STORE_SMS_TO_SIM_DONE = 8;
    protected static final int EVENT_QUERY_SMS_STORAGE_MODE_DONE = 9;
    /* @} */
    // Explicit Transfer Call REFACTORING
    protected static final int EVENT_ECT_RESULT = 10;
    // SPRD: add for trafficClass
    protected static final int EVENT_TRAFFIC_CLASS_DONE = 11;
    /* Add for Data Clear Code from Telcel @{ */
    protected static final int EVENT_SET_LTE_ENABLE_DONE = 12;
    protected static final int EVENT_ATTACH_DATA_DONE = 13;
    /* @} */
    // Add for shutdown optimization
    protected static final int EVENT_REQUEST_SHUTDOWN_DONE = 14;
    protected static final int EVENT_INVOKE_GET_DEFAULT_NAN_DONE = 15;
    protected static final int EVENT_GET_REMIAN_TIMES_DONE = 16;
    protected static final int EVENT_GET_SIMLOCK_STATUS_DONE = 17;
    protected static final int EVENT_GET_ICC_STATUS_DONE = 18;
    protected static final int EVENT_SIMLOCK_STATUS_CHANGED = 19;

    protected static final int EVENT_REQUEST_SET_SIM_POWER = 20;
    protected static final int EVENT_REQUEST_SET_PRE_NETWORK_TYPE = 21;
    protected static final int EVENT_REQUEST_UPDTAE_REAL_ECCLIST = 22;

    protected static final int EVENT_GET_BAND_INFO_DONE = 23;
    protected static final int EVENT_SET_BAND_INFO_MODE_DONE = 24;

    protected static final int EVENT_SET_SINGLE_PDN_DONE = 25;
    protected static final int EVENT_REQUEST_SET_SPECIAL_RATCAP = 26;

    protected static final int EVENT_REQUEST_QUERY_COLP = 27;
    protected static final int EVENT_REQUEST_QUERY_COLR = 28;
    protected static final int EVENT_REQUEST_MMI_ENTER_SIM = 29;
    protected static final int EVENT_REQUEST_UPDATE_OPERATOR_NAME = 30;
    protected static final int EVENT_GET_REALL_SIM_STATUS_DONE = 31;
    protected static final int EVENT_REATTACH_DONE = 32;
    protected static final int EVENT_SET_SMS_BEARER_DONE = 33;
    protected static final int EVENT_GET_SIMLOCK_DUMMYS = 34;
    protected static final int EVENT_GET_SIMLOCK_WHITE_LIST = 35;
    protected static final int EVENT_REQUEST_SET_VOICE_DOMAIN = 36;
    protected static final int EVENT_REQUEST_SET_LOCAL_TONE = 37;
    protected static final int EVENT_UPDATE_PLMN_DONE = 38;
    protected static final int EVENT_QUERY_PLMN_DONE = 39;
    protected static final int EVENT_GET_RADIO_PREFERENCE_DONE = 40;
    protected static final int EVENT_SET_RADIO_PREFERENCE_DONE = 41;
    protected static final int EVENT_GET_PREFERRED_NETWORK_TYPE_DONE = 42;

    protected static final int EVENT_UNSOL_RADIOINTERACTOR = 100;
    /**
     *  Listen for update the list of embms programs.
     */
    protected static final int EVENT_UNSOL_RADIOINTERACTOR_EMBMS = 101;
    /**
     *  Listen for RI has connected.
     */
    protected static final int EVENT_UNSOL_RI_CONNECTED = 102;
    protected static final int EVENT_SWITCH_MULTI_CALLS_DONE = 103;

    protected static final int EVENT_UNSOL_BAND_INFO = 104;
    protected static final int EVENT_UNSOL_SIMMGR_SIM_STATUS_CHANGED = 105;
    protected static final int EVENT_UNSOL_EXPIRE_SIM  = 106;
    protected static final int EVENT_UNSOL_EARLY_MEDIA = 107;
    protected static final int EVENT_UNSOL_HD_STATUS_INFO = 108;

    static protected final int RESPONSE_DATA_FILE_SIZE_1 = 2;
    static protected final int RESPONSE_DATA_FILE_SIZE_2 = 3;
    static protected final int COMMAND_GET_RESPONSE = 0xc0;
    static protected final int COMMAND_READ_BINARY = 0xb0;

    private RegistrantList mPersonalisationLockedRegistrants = new RegistrantList();
    private PersoSubState mPersoSubState;
    private AppState      mAppState;
    Context mContext;
    private CardState mCardState;

    public RadioInteractorHandler(RadioInteractorCore RadioInteractorCore,
            RadioInteractorNotifier RadioInteractorNotifier,Context context) {
        mRadioInteractorCore = RadioInteractorCore;
        mRadioInteractorNotifier = RadioInteractorNotifier;
        mContext = context;
        unsolicitedRegisters(this, EVENT_UNSOL_RADIOINTERACTOR);
        registerForRiConnected(this, EVENT_UNSOL_RI_CONNECTED);
        registerForRadioInteractorEmbms(this, EVENT_UNSOL_RADIOINTERACTOR_EMBMS);
        registerForBandInfo(this,EVENT_UNSOL_BAND_INFO);
        registerForRealSimStateChanged(this,EVENT_UNSOL_SIMMGR_SIM_STATUS_CHANGED);
        registerForEarlyMedia(this, EVENT_UNSOL_EARLY_MEDIA);
        registerForHdStatusChanged(this, EVENT_UNSOL_HD_STATUS_INFO);
        HandlerThread thread = new HandlerThread("RadioInteractor:SyncSender");
        thread.start();
        mHandler = new SyncHandler(thread.getLooper());
        registerForSimlockStatusChanged(this, EVENT_SIMLOCK_STATUS_CHANGED);
        registerForExpireSim(this, EVENT_UNSOL_EXPIRE_SIM);
    }

    public int invokeOemRILRequestStrings(String oemReq, String[] oemResp) {
        ThreadRequest request = new ThreadRequest(oemResp);
        synchronized (request) {
            Message response = mHandler.obtainMessage(EVENT_INVOKE_OEM_RIL_REQUEST_STRINGS_DONE,
                    request);
            mRadioInteractorCore.sendCmdAsync(oemReq, response);
            waitForResult(request);
        }
        try {
            return (Integer) request.result;
        } catch (ClassCastException e) {
            return -1;
        }
    }

    public String getSimCapacity() {
        ThreadRequest request = new ThreadRequest(null);
        synchronized (request) {
            Message response = mHandler.obtainMessage(EVENT_INVOKE_GET_SIM_CAPACITY_DONE, request);
            mRadioInteractorCore.getSimCapacity(response);
            waitForResult(request);
        }
        try {
            return (String) request.result;
        } catch (ClassCastException e) {
            return null;
        }
    }

    public String getDefaultNetworkAccessName() {
        ThreadRequest request = new ThreadRequest(null);
        synchronized (request) {
            Message response = mHandler.obtainMessage(EVENT_INVOKE_GET_DEFAULT_NAN_DONE, request);
            mRadioInteractorCore.getDefaultNAN(response);
            waitForResult(request);
        }
        try {
            return (String) request.result;
        } catch (ClassCastException e) {
            return null;
        }
    }

    public void enableRauNotify() {
        ThreadRequest request = new ThreadRequest(null);
        synchronized (request) {
            Message response = mHandler.obtainMessage(EVENT_INVOKE_ENABLE_RAU_NOTIFY_DONE, request);
            mRadioInteractorCore.enableRauNotify(response);
            waitForResult(request);
        }
    }

    public String iccGetAtr() {
        ThreadRequest request = new ThreadRequest(null);
        synchronized (request) {
            Message response = mHandler.obtainMessage(EVENT_GET_ATR_DONE, request);
            mRadioInteractorCore.simGetAtr(response);
            waitForResult(request);
        }
        try {
            return (String) request.result;
        } catch (ClassCastException e) {
            return null;
        }
    }

    public boolean queryHdVoiceState() {
        ThreadRequest request = new ThreadRequest(null);
        synchronized (request) {
            Message response = mHandler.obtainMessage(EVENT_GET_HD_VOICE_STATE_DONE, request);
            mRadioInteractorCore.getHDVoiceState(response);
            waitForResult(request);
        }
        try {
            return (boolean) request.result;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public void setCallingNumberShownEnabled(boolean enabled) {
        Message response = mHandler.obtainMessage(EVENT_REQUEST_SET_COLP);
        mRadioInteractorCore.setCOLP(enabled, response);
    }

    /* SPRD: Bug#542214 Add support for store SMS to Sim card @{ */
    public boolean storeSmsToSim(boolean enable) {
        ThreadRequest request = new ThreadRequest(null);
        synchronized (request) {
            Message response = mHandler.obtainMessage(EVENT_REQUEST_STORE_SMS_TO_SIM_DONE, request);
            mRadioInteractorCore.storeSmsToSim(enable, response);
            waitForResult(request);
        }
        try {
            return (boolean) request.result;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String querySmsStorageMode() {
        ThreadRequest request = new ThreadRequest(null);
        synchronized (request) {
            Message response = mHandler.obtainMessage(EVENT_QUERY_SMS_STORAGE_MODE_DONE, request);
            mRadioInteractorCore.querySmsStorageMode(response);
            waitForResult(request);
        }
        try {
            return (String) request.result;
        } catch (ClassCastException e) {
            return null;
        }
    }
    /* @} */

    public void setNetworkSpecialRATCap(int type){
        Message response = mHandler.obtainMessage(EVENT_REQUEST_SET_SPECIAL_RATCAP);
        mRadioInteractorCore.setSpecialRatcap(type, response);
    }

    /**
     * Explicit Transfer Call REFACTORING
     * @param result
     */
    public void explicitCallTransfer () {
        mRadioInteractorCore.explicitCallTransfer(mHandler.obtainMessage(EVENT_ECT_RESULT));
    }

    public void switchMultiCalls(int mode) {
        Message response = mHandler.obtainMessage(EVENT_SWITCH_MULTI_CALLS_DONE);
        mRadioInteractorCore.switchMultiCall(mode, response);
    }

    /* add for TV @{*/
    public void dialVP(String address, String sub_address, int clirMode, Message response) {
        mRadioInteractorCore.videoPhoneDial(address, sub_address, clirMode, response);
    }

    public void codecVP(int type, Bundle param, Message response) {
        mRadioInteractorCore.videoPhoneCodec(type, param, response);
    }

    public void fallBackVP(Message response) {
        mRadioInteractorCore.videoPhoneFallback(response);
    }

    public void sendVPString(String str, Message response) {
        mRadioInteractorCore.videoPhoneString(str, response);
    }

    public void controlVPLocalMedia(int datatype, int sw, boolean bReplaceImg, Message response) {
        mRadioInteractorCore.videoPhoneLocalMedia(datatype, sw, bReplaceImg, response);
    }

    public void controlIFrame(boolean isIFrame, boolean needIFrame, Message response) {
        mRadioInteractorCore.videoPhoneControlIFrame(isIFrame, needIFrame, response);
    }
    /* @} */
    /* Add for trafficClass @{ */
    public void requestDCTrafficClass(int type) {
        Message response = mHandler.obtainMessage(EVENT_TRAFFIC_CLASS_DONE);
        mRadioInteractorCore.setTrafficClass(type, response);
    }
    /* @} */

    /* Add for do recovery @{ */
    public void requestReattach() {
        Message response = mHandler.obtainMessage(EVENT_REATTACH_DONE);
        mRadioInteractorCore.reAttach(response);
    }
    /* @} */

    /*SPRD: bug618350 add single pdp allowed by plmns feature@{*/
    public void requestSetSinglePDNByNetwork(boolean isSinglePDN){
        Message response = mHandler.obtainMessage(EVENT_SET_SINGLE_PDN_DONE);
        mRadioInteractorCore.setSinglePDN(isSinglePDN, response);
    }
    /* @} */
    /* Add for Data Clear Code from Telcel @{ */
    public void setLteEnabled(boolean enable) {
        Message response = mHandler.obtainMessage(EVENT_SET_LTE_ENABLE_DONE);
        mRadioInteractorCore.enableLTE(enable, response);
    }

    public void attachDataConn(boolean enable) {
        Message response = mHandler.obtainMessage(EVENT_ATTACH_DATA_DONE);
        mRadioInteractorCore.attachData(enable, response);
    }
    /* @} */

    public void abortSearchNetwork(Message response) {
        mRadioInteractorCore.stopQueryNetwork(response);
    }

    public void forceDetachDataConn(Message response) {
        mRadioInteractorCore.forceDeatch(response);
    }

    public boolean requestShutdown() {
        ThreadRequest request = new ThreadRequest(null);
        synchronized (request) {
            Message response = mHandler.obtainMessage(EVENT_REQUEST_SHUTDOWN_DONE, request);
            mRadioInteractorCore.requestShutdown(response);
            waitForResult(request);
        }
        try {
            return (boolean) request.result;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public void setSimPower(String pkgname,boolean enabled) {
        enforceSprdModifyPermission(pkgname);
        Message response = mHandler.obtainMessage(EVENT_REQUEST_SET_SIM_POWER);
        mRadioInteractorCore.simmgrSimPower(enabled, response);
    }

    public int setPreferredNetworkType(int networkType) {
        ThreadRequest request = new ThreadRequest(null);
        synchronized (request) {
            Message response = mHandler.obtainMessage(EVENT_REQUEST_SET_PRE_NETWORK_TYPE, request);
            mRadioInteractorCore.setPreferredNetworkType(networkType, response);
            waitForResult(request);
        }
        try {
            return (Integer) request.result;
        } catch (ClassCastException e) {
            return -1;
        }
    }

    public void updateRealEccList(String realEccList) {
        Message response = mHandler.obtainMessage(EVENT_REQUEST_UPDTAE_REAL_ECCLIST);
        mRadioInteractorCore.updateEcclist(realEccList, response);
    }

    public String getBandInfo() {
        ThreadRequest request = new ThreadRequest(null);
        synchronized (request) {
            UtilLog.logd(TAG,"getBandInfo request:"+request);
            Message response = mHandler.obtainMessage(EVENT_GET_BAND_INFO_DONE, request);
            mRadioInteractorCore.getBandInfo(response);
            waitForResult(request);
        }
        try {
            return (String) request.result;
        } catch (ClassCastException e) {
            return null;
        }
    }

    public void setBandInfoMode(int type) {
        ThreadRequest request = new ThreadRequest(null);
        synchronized (request) {
            UtilLog.logd(TAG,"setBandInfoMode request:"+request);
            Message response = mHandler.obtainMessage(EVENT_SET_BAND_INFO_MODE_DONE, request);
            mRadioInteractorCore.setBandInfoMode(type,response);
            waitForResult(request);
        }
    }

    public int queryColp(){
        ThreadRequest request = new ThreadRequest(null);
        synchronized (request) {
            Message response = mHandler.obtainMessage(EVENT_REQUEST_QUERY_COLP,request);
            mRadioInteractorCore.queryColp(response);
            waitForResult(request);
        }
        try {
            return (Integer) request.result;
        } catch (ClassCastException e) {
            return -1;
        }
    }

    public int queryColr(){
        ThreadRequest request = new ThreadRequest(null);
        synchronized (request) {
            Message response = mHandler.obtainMessage(EVENT_REQUEST_QUERY_COLR,request);
            mRadioInteractorCore.queryColr(response);
            waitForResult(request);
        }
        try {
            return (Integer) request.result;
        } catch (ClassCastException e) {
            return -1;
        }
    }

    public int mmiEnterSim(String data){
        ThreadRequest request = new ThreadRequest(null);
        synchronized (request) {
            Message response = mHandler.obtainMessage(EVENT_REQUEST_MMI_ENTER_SIM,request);
            mRadioInteractorCore.mmiEnterSim(data,response);
            waitForResult(request);
        }
        try {
            return (Integer) request.result;
        } catch (ClassCastException e) {
            return -1;
        }
    }

    public void updateOperatorName(String plmn){
        Message response = mHandler.obtainMessage(EVENT_REQUEST_UPDATE_OPERATOR_NAME);
        mRadioInteractorCore.updateOperatorName(plmn, response);
    }

    public int getRealSimStatus() {
        return mCardState == null ? -1 : mCardState.ordinal();
    }

    public void setXcapIPAddress(String cid, String ipv4Addr, String ipv6Addr, Message response) {
        mRadioInteractorCore.setXcapIPAddress(cid, ipv4Addr, ipv6Addr, response);
    }

    public void setSmsBearer(int type) {
        Message response = mHandler.obtainMessage(EVENT_SET_SMS_BEARER_DONE);
        mRadioInteractorCore.setSmsBearer(type, response);
    }

    public int[] getSimlockDummys() {
        ThreadRequest request = new ThreadRequest(null);
        synchronized (request) {
            Message response = mHandler.obtainMessage(EVENT_GET_SIMLOCK_DUMMYS, request);
            mRadioInteractorCore.getSimlockDummys(response);
            waitForResult(request);
        }
        try {
            return (int[]) request.result;
        } catch (ClassCastException e) {
            return null;
        }
    }

    public String getSimlockWhitelist(int type) {
        ThreadRequest request = new ThreadRequest(null);
        synchronized (request) {
            Message response = mHandler.obtainMessage(EVENT_GET_SIMLOCK_WHITE_LIST, request);
            mRadioInteractorCore.getSimlockWhitelist(type, response);
            waitForResult(request);
        }
        try {
            return (String) request.result;
        } catch (ClassCastException e) {
            return null;
        }
    }

    public void setVoiceDomain(int type) {
        Message response = mHandler.obtainMessage(EVENT_REQUEST_SET_VOICE_DOMAIN);
        mRadioInteractorCore.setVoiceDomain(type, response);
    }

    public void updateCLIP(int enable, Message result) {
        mRadioInteractorCore.updateCLIP(enable, result);
    }

    public void setTPMRState(int state, Message result) {
        mRadioInteractorCore.setTPMRState(state, result);
    }

    public void getTPMRState(Message result) {
        mRadioInteractorCore.getTPMRState(result);
    }

    public void setVideoResolution(int resolution, Message result) {
        mRadioInteractorCore.setVideoResolution(resolution, result);
    }

    public void enableLocalHold(boolean enable, Message result) {
        mRadioInteractorCore.enableLocalHold(enable, result);
    }

    public void enableWiFiParamReport(boolean enable, Message result) {
        mRadioInteractorCore.enableWiFiParamReport(enable, result);
    }

    public void callMediaChangeRequestTimeOut(int callId, Message result) {
        mRadioInteractorCore.callMediaChangeRequestTimeOut(callId, result);
    }

    public void setDualVolteState(int state, Message result) {
        mRadioInteractorCore.setDualVolteState(state, result);
    }

    public void setLocalTone(int data) {
        Message response = mHandler.obtainMessage(EVENT_REQUEST_SET_LOCAL_TONE);
        mRadioInteractorCore.setLocalTone(data, response);
    }

    public int updatePlmn(int type, int action, String plmn,
                          int act1, int act2, int act3) {
        ThreadRequest request = new ThreadRequest("updatePlmn");
        synchronized (request) {
            Message response = mHandler.obtainMessage(EVENT_UPDATE_PLMN_DONE,
                    request);

            mRadioInteractorCore.updatePlmn(type, action, plmn, act1, act2, act3, response);
            waitForResult(request);
        }
        try {
            return (int)request.result;
        } catch (ClassCastException e) {
            return -1;
        }
    }

    public String queryPlmn(int type) {
        ThreadRequest request = new ThreadRequest("queryPlmn");

        synchronized (request) {
            Message response = mHandler.obtainMessage(EVENT_QUERY_PLMN_DONE,
                    request);
            mRadioInteractorCore.queryPlmn(type, response);
            waitForResult(request);
        }
        try {
            return (String) request.result;
        } catch (ClassCastException e) {
            return null;
        }
    }

    public void setSimPowerReal(String pkgname,boolean enabled) {
        enforceSprdModifyPermission(pkgname);
        Message response = mHandler.obtainMessage(EVENT_REQUEST_SET_SIM_POWER);
        mRadioInteractorCore.setSimPowerReal(enabled, response);
    }
    class SyncHandler extends Handler {
        SyncHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            AsyncResult ar;
            ThreadRequest request;
            String strCapacity[];
            UtilLog.logd(TAG, " handleMessage msg.what:" + msg.what);
            switch (msg.what) {

                case EVENT_GET_REQUEST_RADIOINTERACTOR_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    UtilLog.logd(TAG, "handleMessage EVENT_GET_REQUEST_RADIOINTERACTOR_DONE");
                    synchronized (request) {
                        if (ar.exception == null) {
                            request.result = (((int[]) ar.result))[0];
                        } else {
                            UtilLog.loge(TAG, "handleMessage registration state error!");
                            request.result = -1;
                        }
                        request.notifyAll();
                    }
                    break;
                case EVENT_INVOKE_OEM_RIL_REQUEST_STRINGS_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    String[] oemResp = (String[]) request.argument;
                    int returnOemValue = -1;
                    UtilLog.logd(TAG, "handleMessage EVENT_INVOKE_OEM_RIL_REQUEST_STRINGS_DONE");
                    synchronized (request) {
                        try {
                            if (ar.exception == null) {
                                if (ar.result != null) {
                                    String responseData = (String) (ar.result);
                                    UtilLog.logd(TAG, "responseData = " + responseData);
                                    oemResp[0] = responseData;
                                    returnOemValue = 0;
                                }
                            } else {
                                CommandException ex = (CommandException) ar.exception;
                                returnOemValue = ex.getCommandError().ordinal();
                                if (returnOemValue > 0)
                                    returnOemValue *= -1;
                            }
                        } catch (RuntimeException e) {
                            UtilLog.loge(TAG, "sendOemRilRequestRaw: Runtime Exception");
                            returnOemValue = (CommandException.Error.GENERIC_FAILURE.ordinal());
                            if (returnOemValue > 0)
                                returnOemValue *= -1;
                        }
                        request.result = returnOemValue;
                        request.notifyAll();
                    }
                    break;
                case EVENT_INVOKE_GET_SIM_CAPACITY_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    UtilLog.logd(TAG, "handleMessage EVENT_INVOKE_GET_SIM_CAPACITY_DONE");
                    synchronized (request) {
                        if (ar.exception == null) {
                            strCapacity = (String[]) ar.result;
                            if (strCapacity != null && strCapacity.length >= 2) {
                                UtilLog.logd(TAG, "[sms]sim used:" + strCapacity[0] + " total:"
                                        + strCapacity[1]);
                                request.result = strCapacity[0] + ":" + strCapacity[1];
                                UtilLog.logd(TAG, "[sms]simCapacity: " + request.result);
                            } else {
                                request.result = "ERROR";
                            }
                        } else {
                            request.result = "ERROR";
                            UtilLog.loge(TAG, "[sms]get sim capacity fail");
                        }
                        request.notifyAll();
                    }
                    break;
                case EVENT_INVOKE_ENABLE_RAU_NOTIFY_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    UtilLog.logd(TAG, "handleMessage EVENT_INVOKE_ENABLE_RAU_NOTIFY_DONE");
                    synchronized (request) {
                        if (ar.exception == null) {
                            request.result = ar;
                            UtilLog.logd(TAG, "enable rau: " + request.result);
                        } else {
                            UtilLog.loge(TAG, "enable rau:fail");
                        }
                        request.notifyAll();
                    }
                    break;

                    case EVENT_GET_ATR_DONE:
                        ar = (AsyncResult) msg.obj;
                        request = (ThreadRequest) ar.userObj;
                        UtilLog.logd(TAG, "handleMessage EVENT_GET_ATR_DONE");
                        synchronized (request) {
                            if (ar.exception == null && ar.result != null) {
                                request.result = (String) ar.result;
                            } else {
                                request.result = "ERROR";
                                if (ar.result == null) {
                                    UtilLog.loge(TAG, "iccGetAtr: Empty response");
                                }
                                if (ar.exception != null) {
                                    UtilLog.loge(TAG, "iccGetAtr: Exception: " + ar.exception);
                                }
                            }
                            request.notifyAll();
                        }
                        break;

                case EVENT_GET_HD_VOICE_STATE_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    UtilLog.logd(TAG, "handleMessage EVENT_GET_HD_VOICE_STATE_DONE");
                    synchronized (request) {
                        if (ar.exception == null && ar.result != null) {
                            int resultArray[] = (int[]) ar.result;
                            request.result = (resultArray[0] == 1);
                        } else {
                            request.result = false;
                            if (ar.exception != null) {
                                UtilLog.loge(TAG, "get HD Voice state fail: " + ar.exception);
                            }
                        }
                        request.notifyAll();
                    }
                    break;

                case EVENT_REQUEST_SET_COLP:
                    ar = (AsyncResult) msg.obj;
                    UtilLog.logd(TAG, "handleMessage EVENT_REQUEST_SET_COLP");
                    if (ar.exception == null) {
                        UtilLog.logd(TAG, "set colp　:success"+ ar.result);
                    } else {
                        UtilLog.loge(TAG, "set colp　:fail");
                    }
                    break;

                /* SPRD: Bug#542214 Add support for store SMS to Sim card @{ */
                case EVENT_REQUEST_STORE_SMS_TO_SIM_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    UtilLog.logd(TAG, "handleMessage EVENT_REQUEST_STORE_SMS_TO_SIM_DONE");
                    synchronized (request) {
                        if (ar.exception == null) {
                            request.result = true;
                            UtilLog.logd(TAG, "store sms to sim: " + request.result);
                        } else {
                            request.result = false;
                            UtilLog.loge(TAG, "store sms to sim:fail" + ar.exception);
                        }
                        request.notifyAll();
                    }
                    break;

                case EVENT_QUERY_SMS_STORAGE_MODE_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    UtilLog.logd(TAG, "handleMessage EVENT_QUERY_SMS_STORAGE_MODE_DONE");
                    synchronized (request) {
                        if (ar.exception == null && ar.result != null) {
                            request.result = (String) ar.result;
                        } else {
                            request.result = "ERROR";
                            if (ar.result == null) {
                                UtilLog.loge(TAG, "query sms storage mode: Empty response");
                            }
                            if (ar.exception != null) {
                                UtilLog.loge(TAG,
                                        "query sms storage mode: Exception: " + ar.exception);
                            }
                        }
                        request.notifyAll();
                    }
                    break;
                /* @} */

                case EVENT_ECT_RESULT:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        UtilLog.logd(TAG, "Explicit call failed: " + ar.exception + ", failed reason is : " + msg.what);
                        mRadioInteractorNotifier.notifySuppServiceFailed(mRadioInteractorCore.getPhoneId(),
                                getFailedService(msg.what));
                    }
                    break;

                case EVENT_SWITCH_MULTI_CALLS_DONE:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        UtilLog.logd(TAG, "Switch multi call failed: " + ar.exception + ", failed reason is : " + msg.what);
                        mRadioInteractorNotifier.notifySuppServiceFailed(mRadioInteractorCore.getPhoneId(),
                                getFailedService(msg.what));
                    }
                    break;

                case EVENT_TRAFFIC_CLASS_DONE:
                    ar = (AsyncResult) msg.obj;
                    UtilLog.logd(TAG, "handleMessage EVENT_TRAFFIC_CLASS_DONE");
                    if (ar.exception == null) {
                        UtilLog.logd(TAG, "traffic class :success");
                    } else {
                        UtilLog.loge(TAG, "traffic class :fail" + ar.exception);
                    }
                    break;

                case EVENT_SET_LTE_ENABLE_DONE:
                    ar = (AsyncResult) msg.obj;
                    UtilLog.logd(TAG, "handleMessage EVENT_SET_LTE_ENABLE_DONE");
                    if (ar.exception == null) {
                        UtilLog.logd(TAG, "set lte enable :success");
                    } else {
                        UtilLog.loge(TAG, "set lte enable :fail" + ar.exception);
                    }
                    break;

                case EVENT_ATTACH_DATA_DONE:
                    ar = (AsyncResult) msg.obj;
                    UtilLog.logd(TAG, "handleMessage EVENT_ATTACH_DATA_DONE");
                    if (ar.exception == null) {
                        UtilLog.logd(TAG, "attach data :success");
                    } else {
                        UtilLog.loge(TAG, "attach data :fail" + ar.exception);
                    }
                    break;

                case EVENT_REQUEST_SHUTDOWN_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    UtilLog.logd(TAG, "handleMessage EVENT_REQUEST_SHUTDOWN_DONE");
                    synchronized (request) {
                        if (ar.exception == null) {
                            request.result = true;
                        } else {
                            request.result = false;
                            UtilLog.loge(TAG, "shutdown fail: " + ar.exception);
                        }
                        request.notifyAll();
                    }
                    break;

                case EVENT_REQUEST_SET_SIM_POWER:
                    ar = (AsyncResult) msg.obj;
                    UtilLog.logd(TAG, "handleMessage EVENT_REQUEST_SET_SIM_POWER");
                    if (ar.exception == null) {
                        UtilLog.logd(TAG, "set sim power :success" + ar.result);
                    } else {
                        UtilLog.loge(TAG, "set sim power :fail");
                    }
                    break;

                case EVENT_GET_REMIAN_TIMES_DONE:
                case EVENT_GET_SIMLOCK_STATUS_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    synchronized (request) {
                        if (ar.exception == null && ar.result != null) {
                            request.result = ar.result;
                        } else {
                            request.result = -1;
                        }
                        request.notifyAll();
                    }
                    break;

                case EVENT_REQUEST_SET_PRE_NETWORK_TYPE:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    UtilLog.logd(TAG, "handleMessage EVENT_REQUEST_SET_PRE_NETWORK_TYPE");
                    synchronized (request) {
                        if (ar.exception == null){
                            request.result = 0;
                        } else {
                            request.result = -1;
                            UtilLog.loge(TAG, "set pre network type fail: " + ar.exception);
                        }
                        request.notifyAll();
                    }
                    break;

                case EVENT_INVOKE_GET_DEFAULT_NAN_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    UtilLog.logd(TAG, "handleMessage EVENT_INVOKE_GET_DEFAULT_NAN_DONE");
                    synchronized (request) {
                        if (ar.exception == null && ar.result != null) {
                            request.result = (String) ar.result;
                        } else {
                            request.result = "ERROR";
                            if (ar.result == null) {
                                UtilLog.loge(TAG, "getDefaultNetworkAccessName: Empty response");
                            }
                            if (ar.exception != null) {
                                UtilLog.loge(TAG, "getDefaultNetworkAccessName: Exception: " + ar.exception);
                            }
                        }
                        request.notifyAll();
                    }
                    break;

                case EVENT_REQUEST_UPDTAE_REAL_ECCLIST:
                    ar = (AsyncResult) msg.obj;
                    UtilLog.logd(TAG, "handleMessage EVENT_REQUEST_UPDTAE_REAL_ECCLIST");
                    if (ar.exception == null) {
                        UtilLog.logd(TAG, "update real ecclist :success");
                    } else {
                        UtilLog.loge(TAG, "update real ecclist :fail"+ ar.exception);
                    }
                    break;

                case EVENT_GET_BAND_INFO_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    UtilLog.logd(TAG, "handleMessage EVENT_GET_BAND_INFO_DONE");
                    UtilLog.logd(TAG,"EVENT_GET_BAND_INFO_DONE request:"+request);
                    synchronized (request) {
                        UtilLog.logd(TAG,"EVENT_GET_BAND_INFO_DONE");
                        if (ar.exception == null && ar.result != null) {
                            request.result = (String) ar.result;
                        } else {
                            request.result = "ERROR";
                            if (ar.result == null) {
                                UtilLog.loge(TAG, "get band info: Empty response");
                            }
                            if (ar.exception != null) {
                                UtilLog.loge(TAG,
                                        "get band info: Exception: " + ar.exception);
                            }
                        }
                        request.notifyAll();
                    }
                    break;
                case EVENT_SET_BAND_INFO_MODE_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    UtilLog.logd(TAG, "handleMessage EVENT_SET_BAND_INFO_MODE_DONE");
                    UtilLog.logd(TAG,"EVENT_SET_BAND_INFO_MODE_DONE request:"+request);
                    synchronized (request) {
                        UtilLog.logd(TAG,"EVENT_SET_BAND_INFO_MODE_DONE");
                        if (ar.exception == null) {
                            request.result = ar;
                            UtilLog.logd(TAG, "set band info mode: " + request.result);
                        } else {
                            UtilLog.loge(TAG, "set band info mode:fail");
                        }
                        request.notifyAll();
                    }
                    break;
                /*SPRD: bug618350 add single pdp allowed by plmns feature@{*/
                case EVENT_SET_SINGLE_PDN_DONE:
                    ar = (AsyncResult) msg.obj;
                    UtilLog.logd(TAG, "handleMessage EVENT_SET_SINGLE_PDN_DONE");
                    if (ar.exception == null) {
                        UtilLog.logd(TAG, "set single pdn :success");
                    } else {
                        UtilLog.loge(TAG, "set single pdn :fail" + ar.exception);
                    }
                    break;
                /* @} */
                case EVENT_REQUEST_SET_SPECIAL_RATCAP:
                    ar = (AsyncResult) msg.obj;
                    UtilLog.logd(TAG, "handleMessage EVENT_REQUEST_SET_SPECIAL_RATCAP");
                    if (ar.exception == null) {
                        UtilLog.logd(TAG, "set preferred network RAT:success");
                    } else {
                        UtilLog.loge(TAG, "set preferred network RAT:fail");
                    }
                    break;
                case EVENT_REQUEST_QUERY_COLP:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    UtilLog.logd(TAG, "handleMessage EVENT_REQUEST_QUERY_COLP");
                    synchronized (request) {
                        if (ar.exception == null && ar.result != null) {
                            int resultArray[] = (int[]) ar.result;
                            request.result = resultArray[0];
                        } else {
                            request.result = -1;
                            if (ar.exception != null) {
                                UtilLog.loge(TAG, "query colp fail: " + ar.exception);
                            }
                        }
                        request.notifyAll();
                    }
                    break;
                case EVENT_REQUEST_QUERY_COLR:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    UtilLog.logd(TAG, "handleMessage EVENT_REQUEST_QUERY_COLR");
                    synchronized (request) {
                        if (ar.exception == null && ar.result != null) {
                            int resultArray[] = (int[]) ar.result;
                            request.result = resultArray[0];
                        } else {
                            request.result = -1;
                            if (ar.exception != null) {
                                UtilLog.loge(TAG, "query colr fail: " + ar.exception);
                            }
                        }
                        request.notifyAll();
                    }
                    break;

                case EVENT_REQUEST_MMI_ENTER_SIM:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    UtilLog.logd(TAG, "handleMessage EVENT_REQUEST_MMI_ENTER_SIM");
                    synchronized (request) {
                        if (ar.exception == null) {
                            request.result = 0;
                            UtilLog.logd(TAG, "mmi enter sim: " + request.result);
                        } else {
                            if (ar.exception instanceof CommandException) {
                                CommandException.Error error = ((CommandException) (ar.exception))
                                        .getCommandError();
                                if (error == CommandException.Error.PASSWORD_INCORRECT) {
                                    request.result = 1;
                                } else {
                                    request.result = 2;
                                }
                            }else{
                                request.result = -1;
                            }
                            UtilLog.loge(TAG, "mmi enter sim:fail" +ar.exception);
                        }
                        request.notifyAll();
                    }
                    break;

                case EVENT_REQUEST_UPDATE_OPERATOR_NAME:
                    ar = (AsyncResult) msg.obj;
                    UtilLog.logd(TAG, "handleMessage EVENT_REQUEST_UPDATE_OPERATOR_NAME");
                    if (ar.exception == null) {
                        UtilLog.logd(TAG, "update operator name :success");
                    } else {
                        UtilLog.loge(TAG, "update operator name :fail" + ar.exception);
                    }
                    break;
                case EVENT_REATTACH_DONE:
                    ar = (AsyncResult) msg.obj;
                    UtilLog.logd(TAG, "handleMessage EVENT_REATTACH_DONE");
                    if (ar.exception == null) {
                        UtilLog.logd(TAG, "reattach :success");
                    } else {
                        UtilLog.loge(TAG, "reattach :fail" + ar.exception);
                    }
                    break;
                case EVENT_SET_SMS_BEARER_DONE:
                    ar = (AsyncResult) msg.obj;
                    UtilLog.logd(TAG, "handleMessage EVENT_SET_SMS_BEARER_DONE");
                    if (ar.exception == null) {
                        UtilLog.logd(TAG, "set sms bearer:success");
                    } else {
                        UtilLog.loge(TAG, "set sms bearer:fail");
                    }
                    break;
                case EVENT_GET_SIMLOCK_DUMMYS:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    UtilLog.logd(TAG, "handleMessage EVENT_GET_SIMLOCK_DUMMYS");
                    synchronized (request) {
                        if (ar.exception == null && ar.result != null) {
                            request.result = (int[])ar.result;
                        } else {
                            request.result = null;;
                            if (ar.result == null) {
                                UtilLog.loge(TAG, "get simlock dummys: Empty response");
                            }
                            if (ar.exception != null) {
                                UtilLog.loge(TAG,
                                        "get simlock dummys: Exception: " + ar.exception);
                            }
                        }
                        request.notifyAll();
                    }
                    break;
                case EVENT_GET_SIMLOCK_WHITE_LIST:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    UtilLog.logd(TAG, "handleMessage EVENT_GET_SIMLOCK_WHITE_LIST");
                    synchronized (request) {
                        if (ar.exception == null && ar.result != null) {
                            request.result = (String) ar.result;
                        } else {
                            request.result = "ERROR";
                            if (ar.result == null) {
                                UtilLog.loge(TAG, "get simlock white list: Empty response");
                            }
                            if (ar.exception != null) {
                                UtilLog.loge(TAG,
                                        "get simlock white list: Exception: " + ar.exception);
                            }
                        }
                        request.notifyAll();
                    }
                    break;
                case EVENT_REQUEST_SET_VOICE_DOMAIN:
                    ar = (AsyncResult) msg.obj;
                    UtilLog.logd(TAG, "handleMessage EVENT_REQUEST_SET_VOICE_DOMAIN");
                    if (ar.exception == null) {
                        UtilLog.logd(TAG, "set voice domain :success");
                    } else {
                        UtilLog.loge(TAG, "set voice domain :fail" + ar.exception);
                    }
                    break;
                case EVENT_REQUEST_SET_LOCAL_TONE:
                    ar = (AsyncResult) msg.obj;
                    UtilLog.logd(TAG, "handleMessage EVENT_REQUEST_SET_LOCAL_TONE");
                    if (ar.exception == null) {
                        UtilLog.logd(TAG, "set local tone :success");
                    } else {
                        UtilLog.loge(TAG, "set local tone :fail" + ar.exception);
                    }
                    break;
                case EVENT_UPDATE_PLMN_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    synchronized (request) {
                        if (ar.exception == null) {
                            request.result = 1;
                        } else {
                            request.result = -1;
                        }
                        request.notifyAll();
                    }
                    break;
                case EVENT_QUERY_PLMN_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    synchronized (request) {
                        if (ar.exception == null && ar.result != null) {
                            request.result = ar.result;
                        } else {
                            request.result = "";
                        }
                        request.notifyAll();
                    }
                    break;
                case EVENT_GET_RADIO_PREFERENCE_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    synchronized (request) {
                        if (ar.exception == null && ar.result != null) {
                            request.result = ar.result;
                        } else {
                            request.result = "";
                        }
                        request.notifyAll();
                    }
                    break;
                case EVENT_SET_RADIO_PREFERENCE_DONE:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        UtilLog.logd(TAG, "set radio preference: success");
                    } else {
                        UtilLog.loge(TAG, "set radio preference: fail" + ar.exception);
                    }
                    break;
                case EVENT_GET_PREFERRED_NETWORK_TYPE_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (ThreadRequest) ar.userObj;
                    UtilLog.logd(TAG, "handleMessage EVENT_GET_PREFERRED_NETWORK_TYPE_DONE");
                    synchronized (request) {
                        if (ar.exception == null && ar.result != null){
                            request.result = ar.result;
                        } else {
                            request.result = -1;
                            UtilLog.loge(TAG, "get pre network type fail: " + ar.exception);
                        }
                        request.notifyAll();
                    }
                    break;
                default:
                    throw new RuntimeException("Unrecognized request event radiointeractor: " + msg.what);
            }
        }
    }

    @Override
    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {

            case EVENT_UNSOL_RADIOINTERACTOR:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    UtilLog.logd(TAG, "EVENT_UNSOL_RADIOINTERACTOR");
                    mRadioInteractorNotifier
                            .notifyRadiointeractorEventForSubscriber(mRadioInteractorCore
                                    .getPhoneId());
                } else {
                    UtilLog.loge(TAG, "unsolicitedRegisters exception: " + ar.exception);
                }
                break;

            case EVENT_UNSOL_RI_CONNECTED:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    UtilLog.logd(TAG, "EVENT_UNSOL_RI_CONNECTED");
                } else {
                    UtilLog.loge(TAG, "unsolicitedRiConnected exception: " + ar.exception);
                }
                break;

            case EVENT_UNSOL_RADIOINTERACTOR_EMBMS:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    UtilLog.logd(TAG, "EVENT_UNSOL_RADIOINTERACTOR_EMBMS");
                    mRadioInteractorNotifier
                            .notifyRadiointeractorEventForEmbms(mRadioInteractorCore
                                    .getPhoneId());
                } else {
                    UtilLog.loge(TAG, "unsolicitedRadioInteractorEmbms exception: " + ar.exception);
                }
                break;

            case EVENT_SIMLOCK_STATUS_CHANGED:
                UtilLog.logd(TAG, "EVENT_SIMLOCK_STATUS_CHANGED");
                mRadioInteractorCore.getIccCardStatus(obtainMessage(EVENT_GET_ICC_STATUS_DONE));
                break;

            case EVENT_GET_ICC_STATUS_DONE:
                UtilLog.logd(TAG, "EVENT_GET_ICC_STATUS_DONE");
                ar = (AsyncResult) msg.obj;
                onGetIccCardStatusDone(ar);
                break;

            case EVENT_UNSOL_BAND_INFO:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null && ar.result != null) {
                    UtilLog.logd(TAG, "EVENT_UNSOL_BAND_INFO");
                    mRadioInteractorNotifier
                            .notifyRadiointeractorEventForbandInfo((String)ar.result,mRadioInteractorCore
                                    .getPhoneId());
                } else {
                    UtilLog.loge(TAG, "unsolicitedBandInfo exception: " + ar.exception);
                }
                break;

            case EVENT_UNSOL_SIMMGR_SIM_STATUS_CHANGED:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    UtilLog.logd(TAG, "EVENT_UNSOL_SIMMGR_SIM_STATUS_CHANGED - "
                            + mRadioInteractorCore.getPhoneId());
                    mRadioInteractorCore.simmgrGetSimStatus(obtainMessage(EVENT_GET_REALL_SIM_STATUS_DONE));
                } else {
                    UtilLog.loge(TAG, "unsolicitedRealSimStateChanged exception: " + ar.exception);
                }
                break;
            case EVENT_UNSOL_EXPIRE_SIM :
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null && ar.result != null) {
                    UtilLog.logd(TAG, "EVENT_UNSOL_SIMLOCK_SIM_EXPIRED ");
                    int result = (int) ar.result;
                    UtilLog.logd(TAG, "result = " + result);
                    mRadioInteractorNotifier.notifyExpireSimEvent(result,mRadioInteractorCore.getPhoneId());
                } else {
                    UtilLog.loge(TAG, "unsolicitedBandInfo exception: " + ar.exception);
                }
                break;

            case EVENT_UNSOL_EARLY_MEDIA:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null && ar.result != null) {
                    UtilLog.logd(TAG, "EVENT_UNSOL_EARLY_MEDIA");
                    int result = (int) ar.result;
                    UtilLog.logd(TAG, "result = " + result);
                    mRadioInteractorNotifier
                            .notifyRadiointeractorEventForEarlyMedia(result, mRadioInteractorCore
                                    .getPhoneId());
                } else {
                    UtilLog.loge(TAG, "unsolicited early media exception: " + ar.exception);
                }
                break;

            case EVENT_GET_REALL_SIM_STATUS_DONE:
                ar = (AsyncResult) msg.obj;
                onGetRealIccCardStatusDone(ar);
                break;

            case EVENT_UNSOL_HD_STATUS_INFO:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null && ar.result != null) {
                    UtilLog.logd(TAG, "EVENT_UNSOL_HD_STATUS_INFO");
                    mRadioInteractorNotifier
                            .notifyRadiointeractorHdStatusInfo((int)ar.result,mRadioInteractorCore
                                    .getPhoneId());
                } else {
                    UtilLog.loge(TAG, "unsolicitedHdStatusInfo exception: " + ar.exception);
                }
                break;

            default:
                throw new RuntimeException("Unrecognized event unsol radiointeractor: " + msg.what);

        }
    }

    private synchronized void onGetRealIccCardStatusDone(AsyncResult ar) {
        UtilLog.logd(TAG, "onGetRealIccCardStatusDone");
        if (ar.exception != null) {
            UtilLog.loge(TAG,"Error getting ICC status. "
                    + "RIL_REQUEST_GET_ICC_STATUS should "
                    + "never return an error :" + ar.exception);
            return;
        }
        IccCardStatusEx ics = (IccCardStatusEx) ar.result;
        if (!ics.mCardState.equals(mCardState)) {
            mCardState = ics.mCardState;
            UtilLog.logd(TAG, "Notify real SIM state changed: " + mCardState);
            mRadioInteractorNotifier.notifyRealSimStateChanged(mRadioInteractorCore.getPhoneId());
        }
    }

    public void unsolicitedRegisters(Handler h, int what) {
        mRadioInteractorCore
                .registerForUnsolRadioInteractor(h, what, null);
    }

    public void unregisterForUnsolRadioInteractor(Handler h) {
        mRadioInteractorCore.unregisterForUnsolRadioInteractor(h);
    }

    public void registerForRiConnected(Handler h, int what) {
        mRadioInteractorCore.registerForUnsolRiConnected(h, what, null);
    }

    public void unregisterForRiConnected(Handler h) {
        mRadioInteractorCore.unregisterForUnsolRiConnected(h);
    }

    public void registerForRadioInteractorEmbms(Handler h, int what) {
        mRadioInteractorCore.registerForUnsolRadioInteractor(h, what, null);
    }

    public void unregisterForRadioInteractorEmbms(Handler h) {
        mRadioInteractorCore.unregisterForUnsolRadioInteractor(h);
    }

    public void registerForsetOnVPCodec(Handler h, int what) {
        mRadioInteractorCore.registerForsetOnVPCodec(h, what, null);
    }

    public void unregisterForsetOnVPCodec(Handler h) {
        mRadioInteractorCore.unregisterForsetOnVPCodec(h);
    }

    public void registerForsetOnVPFallBack(Handler h, int what) {
        mRadioInteractorCore.registerForsetOnVPFallBack(h, what, null);
    }

    public void unregisterForsetOnVPFallBack(Handler h) {
        mRadioInteractorCore.unregisterForsetOnVPFallBack(h);
    }

    public void registerForsetOnVPString(Handler h, int what) {
        mRadioInteractorCore.registerForsetOnVPString(h, what, null);
    }

    public void unregisterForsetOnVPString(Handler h) {
        mRadioInteractorCore.unregisterForsetOnVPString(h);
    }

    public void registerForsetOnVPRemoteMedia(Handler h, int what) {
        mRadioInteractorCore.registerForsetOnVPRemoteMedia(h, what, null);
    }

    public void unregisterForsetOnVPRemoteMedia(Handler h) {
        mRadioInteractorCore.unregisterForsetOnVPRemoteMedia(h);
    }

    public void registerForsetOnVPMMRing(Handler h, int what) {
        mRadioInteractorCore.registerForsetOnVPMMRing(h, what, null);
    }

    public void unregisterForsetOnVPMMRing(Handler h) {
        mRadioInteractorCore.unregisterForsetOnVPMMRing(h);
    }

    public void registerForsetOnVPFail(Handler h, int what) {
        mRadioInteractorCore.registerForsetOnVPFail(h, what, null);
    }

    public void unregisterForsetOnVPFail(Handler h) {
        mRadioInteractorCore.unregisterForsetOnVPFail(h);
    }

    public void registerForsetOnVPRecordVideo(Handler h, int what) {
        mRadioInteractorCore.registerForsetOnVPRecordVideo(h, what, null);
    }

    public void unregisterForsetOnVPRecordVideo(Handler h) {
        mRadioInteractorCore.unregisterForsetOnVPRecordVideo(h);
    }

    public void registerForsetOnVPMediaStart(Handler h, int what) {
        mRadioInteractorCore.registerForsetOnVPMediaStart(h, what, null);
    }

    public void unregisterForsetOnVPMediaStart(Handler h) {
        mRadioInteractorCore.unregisterForsetOnVPMediaStart(h);
    }

    public void registerForEccNetChanged(Handler h, int what) {
        mRadioInteractorCore.registerForEccNetChanged(h, what, null);
    }

    public void unregisterForEccNetChanged(Handler h) {
        mRadioInteractorCore.unregisterForEccNetChanged(h);
    }

    public void registerForRauSuccess(Handler h, int what) {
        mRadioInteractorCore.registerForRauSuccess(h, what, null);
    }

    public void unregisterForRauSuccess(Handler h) {
        mRadioInteractorCore.unregisterForRauSuccess(h);
    }

    public void registerForClearCodeFallback(Handler h, int what) {
        mRadioInteractorCore.registerForClearCodeFallback(h, what, null);
    }

    public void unregisterForClearCodeFallback(Handler h) {
        mRadioInteractorCore.unregisterForClearCodeFallback(h);
    }

    public void registerForBandInfo(Handler h, int what) {
        mRadioInteractorCore.registerForBandInfo(h, what, null);
    }

    public void unregisterForBandInfo(Handler h) {
        mRadioInteractorCore.unregisterForBandInfo(h);
    }
    public void registerForSwitchPrimaryCard(Handler h, int what) {
        mRadioInteractorCore.registerForSwitchPrimaryCard(h, what, null);
    }

    public void unregisterForSwitchPrimaryCard(Handler h) {
        mRadioInteractorCore.unregisterForSwitchPrimaryCard(h);
    }

    public void registerForRealSimStateChanged(Handler h,int what) {
        mRadioInteractorCore.registerForRealSimStateChanged(h,what,null);
    }

    public void unregisterForRealSimStateChanged(Handler h) {
        mRadioInteractorCore.unregisterForRealSimStateChanged(h);
    }

    public void registerForRadioCapabilityChanged(Handler h,int what) {
        mRadioInteractorCore.registerForRadioCapabilityChanged(h,what,null);
    }

    public void unregisterForRadioCapabilityChanged(Handler h) {
        mRadioInteractorCore.unregisterForRadioCapabilityChanged(h);
    }

    private static final class ThreadRequest {
        public Object argument;
        public Object result;

        public ThreadRequest(Object argument) {
            this.argument = argument;
        }
    }

    private void waitForResult(ThreadRequest request) {
        try {
            request.wait();
        } catch (InterruptedException e) {
            UtilLog.logd(TAG, "interrupted while trying to get remain times");
        }
    }

    /**
     * Some fields (like ICC ID) in GSM SIMs are stored as nibble-swizzled BCH
     */
    private String
    bchToString(byte[] data, int offset, int length) {
        StringBuilder ret = new StringBuilder(length*2);

        for (int i = offset ; i < offset + length ; i++) {
            int v;

            v = data[i] & 0xf;
            ret.append("0123456789abcdef".charAt(v));

            v = (data[i] >> 4) & 0xf;
            ret.append("0123456789abcdef".charAt(v));
        }

        return ret.toString();
    }

    private SuppService getFailedService(int what) {
        switch (what) {
            case EVENT_ECT_RESULT:
                return SuppService.TRANSFER;
        }
        return SuppService.UNKNOWN;
    }

    public void setFacilityLockByUser(String facility, boolean lockState, Message response) {
         mRadioInteractorCore.setFacilityLockForUser(facility, lockState, response);
    }

    public int getSimLockRemainTimes(int type) {
        ThreadRequest request = new ThreadRequest("getSimLockRemainTimes");

        synchronized (request) {
            Message response = mHandler.obtainMessage(EVENT_GET_REMIAN_TIMES_DONE,
                    request);

            mRadioInteractorCore.getSimlockRemaintimes(type, response);
            waitForResult(request);
        }

        try {
            return ((int[])request.result)[0];
        } catch (ClassCastException e) {
            return -1;
        }
    }

    public int getSimLockStatus(int type) {
        ThreadRequest request = new ThreadRequest("getSimLockStatus");

        synchronized (request) {
            Message response = mHandler.obtainMessage(EVENT_GET_SIMLOCK_STATUS_DONE,
                    request);

            mRadioInteractorCore.getSimlockStatus(type, response);
            waitForResult(request);
        }

        try {
            return ((int[])request.result)[0];
        } catch (ClassCastException e) {
            return -1;
        }
    }

    public String getRadioPreference(String key) {
        ThreadRequest request = new ThreadRequest("getRadioPreference");

        synchronized (request) {
            Message response = mHandler.obtainMessage(EVENT_GET_RADIO_PREFERENCE_DONE, request);
            mRadioInteractorCore.getRadioPreference(key, response);
            waitForResult(request);
        }

        try {
            return (String) request.result;
        } catch (ClassCastException e) {
            return null;
        }
    }

    public void setRadioPreference(String key, String value) {
        Message response = mHandler.obtainMessage(EVENT_SET_RADIO_PREFERENCE_DONE);
        mRadioInteractorCore.setRadioPreference(key, value, response);
    }

    public void getImsCurrentCalls (Message result) {
        mRadioInteractorCore.getImsCurrentCalls(result);
    }

    public void setImsVoiceCallAvailability(int state, Message result) {
        mRadioInteractorCore.setImsVoiceCallAvailability(state, result);
    }

    public void getImsVoiceCallAvailability(Message result) {
        mRadioInteractorCore.getImsVoiceCallAvailability(result);
    }

    public void initISIM(String confUri, String instanceId, String impu,
            String impi, String domain, String xCap, String bspAddr,
            Message result) {
        mRadioInteractorCore.initISIM(confUri, instanceId, impu, impi, domain,
                xCap, bspAddr, result);
    }

    public void requestVolteCallMediaChange(int action, int callId, Message result) {
        mRadioInteractorCore.requestVolteCallMediaChange(action, callId, result);
    }

    public void responseVolteCallMediaChange(boolean isAccept, int callId, Message result) {
        mRadioInteractorCore.responseVolteCallMediaChange(isAccept, callId, result);
    }

    public void setImsSmscAddress(String smsc, Message result) {
        mRadioInteractorCore.setImsSmscAddress(smsc, result);
    }

    public void requestVolteCallFallBackToVoice(int callId, Message result) {
        mRadioInteractorCore.requestVolteCallFallBackToVoice(callId, result);
    }

    public void setIMSInitialAttachApn(DataProfile dataProfileInfo, Message result) {
        mRadioInteractorCore.setIMSInitialAttachApn(dataProfileInfo, result);
    }

    public void queryCallForwardStatus(int cfReason, int serviceClass,
            String number, String ruleSet, Message result) {
        mRadioInteractorCore.queryCallForwardStatus(cfReason, serviceClass, number, ruleSet, result);
    }

    public void setCallForward(int action, int cfReason, int serviceClass,
            String number, int timeSeconds, String ruleSet, Message result) {
        mRadioInteractorCore.setCallForward(action, cfReason, serviceClass,
                number, timeSeconds, ruleSet, result);
    }

    public void requestInitialGroupCall(String numbers, Message result) {
        mRadioInteractorCore.requestInitialGroupCall(numbers, result);
    }

    public void requestAddGroupCall(String numbers, Message result) {
        mRadioInteractorCore.requestAddGroupCall(numbers, result);
    }

    public void enableIms(Message result) {
        mRadioInteractorCore.enableIms(result);
    }

    public void disableIms(Message result) {
        mRadioInteractorCore.disableIms(result);
    }

    public void getImsBearerState(Message result) {
        mRadioInteractorCore.getImsBearerState(result);
    }

    public void setInitialAttachSOSApn(DataProfile dataProfileInfo, Message result) {
        mRadioInteractorCore.setInitialAttachSOSApn(dataProfileInfo, result);
    }

    public void requestImsHandover(int type, Message result) {
        mRadioInteractorCore.requestImsHandover(type, result);
    }

    public void notifyImsHandoverStatus(int status, Message result) {
        mRadioInteractorCore.notifyImsHandoverStatus(status, result);
    }

    public void notifyImsNetworkInfo(int type, String info, Message result) {
        mRadioInteractorCore.notifyImsNetworkInfo(type, info, result);
    }

    public void notifyImsCallEnd(int type, Message result) {
        mRadioInteractorCore.notifyImsCallEnd(type, result);
    }

    public void notifyVoWifiEnable(boolean enable, Message result) {
        mRadioInteractorCore.notifyVoWifiEnable(enable, result);
    }

    public void notifyVoWifiCallStateChanged(boolean incall, Message result) {
        mRadioInteractorCore.notifyVoWifiCallStateChanged(incall, result);
    }

    public void notifyDataRouter(Message result) {
        mRadioInteractorCore.notifyDataRouter(result);
    }

    public void imsHoldSingleCall(int callid, boolean enable, Message result) {
        mRadioInteractorCore.imsHoldSingleCall(callid, enable, result);
    }

    public void imsMuteSingleCall(int callid, boolean enable, Message result) {
        mRadioInteractorCore.imsMuteSingleCall(callid, enable, result);
    }

    public void imsSilenceSingleCall(int callid, boolean enable, Message result) {
        mRadioInteractorCore.imsSilenceSingleCall(callid, enable, result);
    }

    public void imsEnableLocalConference(boolean enable, Message result) {
        mRadioInteractorCore.imsEnableLocalConference(enable, result);
    }

    public void notifyHandoverCallInfo(String callInfo, Message result) {
        mRadioInteractorCore.notifyHandoverCallInfo(callInfo, result);
    }

    public void getSrvccCapbility(Message result) {
        mRadioInteractorCore.getSrvccCapbility(result);
    }

    public void getImsPcscfAddress(Message result) {
        mRadioInteractorCore.getImsPcscfAddress(result);
    }

    public void setImsPcscfAddress(String addr, Message result) {
        mRadioInteractorCore.setImsPcscfAddress(addr, result);
    }

    public void queryFacilityLockForAppExt(String facility, String password, int serviceClass,
            Message result) {
        mRadioInteractorCore.queryFacilityLockForAppExt(facility, password, serviceClass, result);
    }

    public void getImsRegAddress(Message result) {
        mRadioInteractorCore.getImsRegAddress(result);
    }

    public int getPreferredNetworkType() {
        ThreadRequest request = new ThreadRequest(null);
        synchronized (request) {
            Message response = mHandler.obtainMessage(EVENT_GET_PREFERRED_NETWORK_TYPE_DONE, request);
            mRadioInteractorCore.getPreferredNetworkType(response);
            waitForResult(request);
        }
        try {
            return (Integer) request.result;
        } catch (ClassCastException e) {
            return -1;
        }
    }

    private synchronized void onGetIccCardStatusDone(AsyncResult ar) {
        if (ar.exception != null) {
            UtilLog.loge(TAG,"Error getting ICC status. "
                    + "RIL_REQUEST_GET_ICC_STATUS should "
                    + "never return an error :" + ar.exception);
            return;
        }
        IccCardStatusEx ics = (IccCardStatusEx)ar.result;
        if (ics.mApplications.length == 0) {
            return;
        }
        IccCardApplicationStatusEx icas = ics.mApplications[0];
        UtilLog.logd(TAG,"result =  "  + icas.perso_substate);

        mAppState = icas.app_state;
        PersoSubState oldPersoSubState = mPersoSubState;
        mPersoSubState = icas.perso_substate;
        notifySimLockedRegistrantsIfNeeded(null);
    }

    private void notifySimLockedRegistrantsIfNeeded(Registrant r) {
        if (mAppState == AppState.APPSTATE_SUBSCRIPTION_PERSO) {
            if (r == null) {
                int simlockType =  getSimlockTypes(mPersoSubState);
                if (simlockType != 0) {
                    mPersonalisationLockedRegistrants.notifyRegistrants(
                            new AsyncResult(mRadioInteractorCore.getPhoneId(),simlockType,null));
                }
            } else {
                r.notifyRegistrant(new AsyncResult(null, null, null));
            }
        }
    }

    public int getSimlockTypes(PersoSubState persoSubState ) {
       int simlockType= 0;
       if (persoSubState == PersoSubState.PERSOSUBSTATE_SIM_NETWORK) {
            UtilLog.logd(TAG, "Notifying registrants: NetworkLocked");
            simlockType = TelephonyManager.SIM_STATE_NETWORK_LOCKED;
       } else if (persoSubState == PersoSubState.PERSOSUBSTATE_SIM_NETWORK_SUBSET) {
            UtilLog.logd(TAG, "Notifying registrants: NetworkSubsetLocked");
            simlockType = IccCardStatusEx.SIM_STATE_NETWORKSUBSET_LOCKED;
        } else if (persoSubState == PersoSubState.PERSOSUBSTATE_SIM_SERVICE_PROVIDER) {
            UtilLog.logd(TAG, "Notifying registrants: ServiceProviderLocked");
            simlockType = IccCardStatusEx.SIM_STATE_SERVICEPROVIDER_LOCKED;
        } else if (persoSubState == PersoSubState.PERSOSUBSTATE_SIM_CORPORATE) {
            UtilLog.logd(TAG, "Notifying registrants: corporateLocked");
            simlockType = IccCardStatusEx.SIM_STATE_CORPORATE_LOCKED;
        } else if (persoSubState == PersoSubState.PERSOSUBSTATE_SIM_SIM){
            UtilLog.logd(TAG, "Notifying registrants: simLocked");
            simlockType = IccCardStatusEx.SIM_STATE_SIM_LOCKED;
        }  else if(persoSubState == PersoSubState.PERSOSUBSTATE_SIM_NETWORK_PUK) {
            UtilLog.logd(TAG, "Notifying registrants: NetworkLocked puk");
            simlockType = IccCardStatusEx.SIM_STATE_NETWORK_LOCKED_PUK;
        }  else if(persoSubState == PersoSubState.PERSOSUBSTATE_SIM_NETWORK_SUBSET_PUK) {
            UtilLog.logd(TAG, "Notifying registrants: NetworkSubsetLocked puk");
            simlockType = IccCardStatusEx.SIM_STATE_NETWORK_SUBSET_LOCKED_PUK;
        }  else if(persoSubState == PersoSubState.PERSOSUBSTATE_SIM_CORPORATE_PUK) {
            UtilLog.logd(TAG, "Notifying registrants: corporateLocked puk");
            simlockType = IccCardStatusEx.SIM_STATE_CORPORATE_LOCKED_PUK;
        }  else if(persoSubState == PersoSubState.PERSOSUBSTATE_SIM_SERVICE_PROVIDER_PUK) {
            UtilLog.logd(TAG, "Notifying registrants: ServiceProviderLocked puk");
            simlockType = IccCardStatusEx.SIM_STATE_SERVICE_PROVIDER_LOCKED_PUK;
        }  else if(persoSubState == PersoSubState.PERSOSUBSTATE_SIM_SIM_PUK) {
            UtilLog.logd(TAG, "Notifying registrants: simLocked puk");
            simlockType = IccCardStatusEx.SIM_STATE_SIM_LOCKED_PUK;
        }  else if(persoSubState == PersoSubState.PERSOSUBSTATE_SIM_LOCK_FOREVER) {
            UtilLog.logd(TAG, "Notifying registrants: simlock forever");
            simlockType = IccCardStatusEx.SIM_STATE_SIM_LOCKED_FOREVER;
        }else{
            simlockType = 0;
        }
       return simlockType;
    }

    private int parsePinPukErrorResultEx(AsyncResult ar) {
        int[] result = (int[]) ar.result;
        if (result == null) {
            return -1;
        } else {
            int length = result.length;
            int attemptsRemaining = -1;
            if (length > 0) {
                attemptsRemaining = result[0];
            }
            UtilLog.logd(TAG, "parsePinPukErrorResult: attemptsRemaining=" + attemptsRemaining);
            return attemptsRemaining;
        }
    }

    public void registerForSimlockStatusChanged(Handler h, int what) {
        mRadioInteractorCore.registerForSimlockStatusChanged(h, what, null);
    }

    public void unregisterForSimlockStatusChanged(Handler h) {
        mRadioInteractorCore.unregisterForSimlockStatusChanged(h);
    }

    public synchronized void registerForPersonalisationLocked(Handler h, int what, Object obj) {
        Registrant r = new Registrant (h, what, obj);
        mPersonalisationLockedRegistrants.add(r);
        notifySimLockedRegistrantsIfNeeded(null);
    }

    public synchronized void unregisterForPersonalisationLocked(Handler h) {
        mPersonalisationLockedRegistrants.remove(h);
    }

    public void registerForExpireSim(Handler h, int what) {
        mRadioInteractorCore.registerForExpireSim(h, what, null);
    }

    public void unregisterForExpireSim(Handler h) {
        mRadioInteractorCore.unregisterForExpireSim(h);
    }

    public void registerForEarlyMedia(Handler h, int what) {
        mRadioInteractorCore.registerForEarlyMedia(h, what, null);
    }

    public void unregisterForEarlyMedia(Handler h) {
        mRadioInteractorCore.unregisterForEarlyMedia(h);
    }

    public void registerForHdStatusChanged(Handler h, int what) {
        mRadioInteractorCore.registerForHdStautsChanged(h, what, null);
    }

    public void unregisterForHdStatusChanged(Handler h) {
        mRadioInteractorCore.unregisterForHdStautsChanged(h);
    }

    public void getSimStatus() {
        mRadioInteractorCore.getIccCardStatus(obtainMessage(EVENT_GET_ICC_STATUS_DONE));
    }

    public void registerForNetowrkErrorCode(Handler h,int what) {
        mRadioInteractorCore.registerForNetowrkErrorCode(h, what, null);
    }

    public void unregisterForNetowrkErrorCode(Handler h) {
        mRadioInteractorCore.unregisterForNetowrkErrorCode(h);
    }

    public void registerForAvailableNetworks(Handler h, int what) {
        mRadioInteractorCore.registerForAvailableNetworks(h, what, null);
    }

    public void unregisterForAvailableNetworks(Handler h) {
        mRadioInteractorCore.unregisterForAvailableNetworks(h);
    }

    public void registerForImsCallStateChanged(Handler h, int what) {
        mRadioInteractorCore.registerForImsCallStateChanged(h, what, null);
    }

    public void unregisterForImsCallStateChanged(Handler h) {
        mRadioInteractorCore.unregisterForImsCallStateChanged(h);
    }

    public void registerForImsVideoQos(Handler h, int what) {
        mRadioInteractorCore.registerForImsVideoQos(h, what, null);
    }

    public void unregisterForImsVideoQos(Handler h) {
        mRadioInteractorCore.unregisterForImsVideoQos(h);
    }

    public void registerForImsBearerStateChanged(Handler h, int what) {
        mRadioInteractorCore.registerForImsBearerStateChanged(h, what, null);
    }

    public void unregisterForImsBearerStateChanged(Handler h) {
        mRadioInteractorCore.unregisterForImsBearerStateChanged(h);
    }

    public void registerImsHandoverRequest(Handler h, int what) {
        mRadioInteractorCore.registerImsHandoverRequest(h, what, null);
    }

    public void unregisterImsHandoverRequest(Handler h) {
        mRadioInteractorCore.unregisterImsHandoverRequest(h);
    }

    public void registerImsHandoverStatus(Handler h, int what) {
        mRadioInteractorCore.registerImsHandoverStatus(h, what, null);
    }

    public void unregisterImsHandoverStatus(Handler h){
        mRadioInteractorCore.unregisterImsHandoverStatus(h);
    }

    public void registerImsNetworkInfo(Handler h, int what) {
        mRadioInteractorCore.registerImsNetworkInfo(h, what, null);
    }

    public void unregisterImsNetworkInfo(Handler h){
        mRadioInteractorCore.unregisterImsNetworkInfo(h);
    }

    public void registerImsRegAddress(Handler h, int what) {
        mRadioInteractorCore.registerImsRegAddress(h, what, null);
    }

    public void unregisterImsRegAddress(Handler h){
        mRadioInteractorCore.unregisterImsRegAddress(h);
    }

    public void registerImsWiFiParam(Handler h, int what) {
        mRadioInteractorCore.registerImsWiFiParam(h, what, null);
    }

    public void unregisterImsWiFiParam(Handler h){
        mRadioInteractorCore.unregisterImsWiFiParam(h);
    }

    public void registerForImsNetworkStateChanged(Handler h, int what) {
        mRadioInteractorCore.registerForImsNetworkStateChanged(h, what, null);
    }

    public void unregisterForImsNetworkStateChanged(Handler h) {
        mRadioInteractorCore.unregisterForImsNetworkStateChanged(h);
    }

    public void registerForDsdaStatus(Handler h, int what) {
        mRadioInteractorCore.registerForDsdaStatus(h, what, null);
    }

    public void unregisterForDsdaStatus(Handler h) {
        mRadioInteractorCore.unregisterForDsdaStatus(h);
    }

    /**
     * Make sure the caller has the SPRD_MODIFY_PHONE_STATE permission.
     * @throws SecurityException if the caller does not have the required permission
     */
    private void enforceSprdModifyPermission(String packageName) {
        /*int resultOfCheck = mContext.getPackageManager()
                .checkPermission(android.Manifest.permission.SPRD_MODIFY_PHONE_STATE, packageName);
        if (resultOfCheck != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException(
                    "Neither user " + Binder.getCallingUid() + " nor current process has " +
                            android.Manifest.permission.SPRD_MODIFY_PHONE_STATE +
                            ".");
        }*/
    }
}
