package com.android.sprd.telephony;

import com.android.sprd.telephony.IRadioInteractorCallback;
import android.telephony.IccOpenLogicalChannelResponse;

interface IRadioInteractor
{
    void listenForSlot(in int slotId, IRadioInteractorCallback callback, int events, boolean notifyNow);
    int sendAtCmd(in String oemReq, out String[] oemResp, int slotId);
    String getSimCapacity(int slotId);
    String getDefaultNetworkAccessName(int slotId);
    void enableRauNotify(int slotId);
    boolean queryHdVoiceState(int slotId);
    void setCallingNumberShownEnabled(int slotId, boolean enabled);
    boolean storeSmsToSim(boolean enable,int slotId);
    String querySmsStorageMode(int slotId);
    boolean requestShutdown(int slotId);
    String getBandInfo(int slotId);
    void setBandInfoMode(int type,int slotId);
    String iccGetAtr(int slotId);
    void explicitCallTransfer(int slotId);
    void switchMultiCalls(int slotId, int mode);
    int getSimLockRemainTimes(int type, int slotId);
    int getSimLockStatus(int type, int slotId);
    void setSimPower(String pkgname,int phoneId, boolean enabled);
    int setPreferredNetworkType(int phoneId, int networkType);
    int getRealSimSatus(int phoneId);
    int[] getSimlockDummys(int phoneId);
    String getSimlockWhitelist(int type, int phoneId);
    void setLocalTone(int data, int phoneId);
    int updatePlmn(int phoneId, int type, int action, String plmn,
                          int act1, int act2, int act3);
    String queryPlmn(int phoneId, int type);
    void setSimPowerReal(String pkgname, int phoneId, boolean enabled);
    String getRadioPreference(int PhoneId, String key);
    void setRadioPreference(int phoneId, String key, String value);
    int getPreferredNetworkType(int phoneId);
    void enableRadioPowerFallback(boolean enable, int phoneId);
}
