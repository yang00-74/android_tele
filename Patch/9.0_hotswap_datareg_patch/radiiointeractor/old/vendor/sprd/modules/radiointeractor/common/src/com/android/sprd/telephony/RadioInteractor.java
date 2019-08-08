
package com.android.sprd.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.data.DataProfile;
import android.telephony.IccOpenLogicalChannelResponse;

import static com.android.sprd.telephony.RIConstants.RADIOINTERACTOR_SERVER;

public class RadioInteractor {

    IRadioInteractor mRadioInteractorProxy;
    private static RadioInteractorFactory sRadioInteractorFactory;
    private static final String TAG = "RadioInteractor";

    Context mContext;
    public RadioInteractor(Context context) {
        mContext = context;
        mRadioInteractorProxy = IRadioInteractor.Stub
                .asInterface(ServiceManager.getService(RADIOINTERACTOR_SERVER));
    }

    public void listen(RadioInteractorCallbackListener radioInteractorCallbackListener,
            int events) {
        this.listen(radioInteractorCallbackListener, events, true);
    }

    public void listen(RadioInteractorCallbackListener radioInteractorCallbackListener,
            int events, boolean notifyNow) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(radioInteractorCallbackListener.mSlotId);
            if (rih != null) {
                if (events == RadioInteractorCallbackListener.LISTEN_NONE) {
                    rih.unregisterForUnsolRadioInteractor(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForRiConnected(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForRadioInteractorEmbms(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForsetOnVPCodec(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForsetOnVPFallBack(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForsetOnVPString(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForsetOnVPRemoteMedia(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForsetOnVPMMRing(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForsetOnVPFail(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForsetOnVPRecordVideo(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForsetOnVPMediaStart(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForEccNetChanged(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForClearCodeFallback(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForRauSuccess(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForPersonalisationLocked(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForBandInfo(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForSwitchPrimaryCard(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForRadioCapabilityChanged(radioInteractorCallbackListener.mHandler);
                    if (mRadioInteractorProxy != null) {
                        mRadioInteractorProxy.listenForSlot(radioInteractorCallbackListener.mSlotId,
                                radioInteractorCallbackListener.mCallback,
                                RadioInteractorCallbackListener.LISTEN_NONE, notifyNow);
                    }
                    rih.unregisterForExpireSim(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForEarlyMedia(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForNetowrkErrorCode(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForAvailableNetworks(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForImsCallStateChanged(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForImsVideoQos(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForImsBearerStateChanged(radioInteractorCallbackListener.mHandler);
                    rih.unregisterImsHandoverRequest(radioInteractorCallbackListener.mHandler);
                    rih.unregisterImsHandoverStatus(radioInteractorCallbackListener.mHandler);
                    rih.unregisterImsNetworkInfo(radioInteractorCallbackListener.mHandler);
                    rih.unregisterImsRegAddress(radioInteractorCallbackListener.mHandler);
                    rih.unregisterImsWiFiParam(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForImsNetworkStateChanged(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForDsdaStatus(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForHdStatusChanged(radioInteractorCallbackListener.mHandler);
                    return;
                }
                if (events == RadioInteractorCallbackListener.LISTEN_RADIOINTERACTOR_EVENT) {
                    rih.unsolicitedRegisters(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_RADIOINTERACTOR_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_RI_CONNECTED_EVENT) {
                    rih.registerForRiConnected(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_RI_CONNECTED_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_RADIOINTERACTOR_EMBMS_EVENT) {
                    rih.registerForRadioInteractorEmbms(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_RADIOINTERACTOR_EMBMS_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_VIDEOPHONE_CODEC_EVENT) {
                    rih.registerForsetOnVPCodec(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_VIDEOPHONE_CODEC_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_VIDEOPHONE_DSCI_EVENT) {
                    rih.registerForsetOnVPFallBack(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_VIDEOPHONE_DSCI_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_VIDEOPHONE_STRING_EVENT) {
                    rih.registerForsetOnVPString(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_VIDEOPHONE_STRING_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_VIDEOPHONE_REMOTE_MEDIA_EVENT) {
                    rih.registerForsetOnVPRemoteMedia(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_VIDEOPHONE_REMOTE_MEDIA_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_VIDEOPHONE_MM_RING_EVENT) {
                    rih.registerForsetOnVPMMRing(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_VIDEOPHONE_MM_RING_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_VIDEOPHONE_RELEASING_EVENT) {
                    rih.registerForsetOnVPFail(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_VIDEOPHONE_RELEASING_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_VIDEOPHONE_RECORD_VIDEO_EVENT) {
                    rih.registerForsetOnVPRecordVideo(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_VIDEOPHONE_RECORD_VIDEO_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_VIDEOPHONE_MEDIA_START_EVENT) {
                    rih.registerForsetOnVPMediaStart(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_VIDEOPHONE_MEDIA_START_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_ECC_NETWORK_CHANGED_EVENT) {
                    rih.registerForEccNetChanged(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_ECC_NETWORK_CHANGED_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_RAU_SUCCESS_EVENT) {
                    rih.registerForRauSuccess(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_RAU_SUCCESS_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_CLEAR_CODE_FALLBACK_EVENT) {
                    rih.registerForClearCodeFallback(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_CLEAR_CODE_FALLBACK_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_SIMLOCK_NOTIFY_EVENT) {
                    rih.registerForPersonalisationLocked(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_SIMLOCK_NOTIFY_EVENT,
                            radioInteractorCallbackListener.mSlotId);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_BAND_INFO_EVENT) {
                    rih.registerForBandInfo(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_BAND_INFO_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_SWITCH_PRIMARY_CARD) {
                    rih.registerForSwitchPrimaryCard(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_SWITCH_PRIMARY_CARD);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_SIMMGR_SIM_STATUS_CHANGED_EVENT) {
                    if (mRadioInteractorProxy != null) {
                        mRadioInteractorProxy.listenForSlot(radioInteractorCallbackListener.mSlotId,
                                radioInteractorCallbackListener.mCallback,
                                RadioInteractorCallbackListener.LISTEN_SIMMGR_SIM_STATUS_CHANGED_EVENT,
                                notifyNow);
                    }
                }
                if (events == RadioInteractorCallbackListener.LISTEN_RADIO_CAPABILITY_CHANGED_EVENT) {
                    rih.registerForRadioCapabilityChanged(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_RADIO_CAPABILITY_CHANGED_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_EXPIRE_SIM) {
                    rih.registerForExpireSim(
                            radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_EXPIRE_SIM);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_EARLY_MEDIA_EVENT) {
                    rih.registerForEarlyMedia(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_EARLY_MEDIA_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_NETWORK_ERROR_CODE_EVENT) {
                    rih.registerForNetowrkErrorCode(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_NETWORK_ERROR_CODE_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_AVAILAVLE_NETWORKS_EVENT) {
                    rih.registerForAvailableNetworks(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_AVAILAVLE_NETWORKS_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_IMS_CALL_STATE_CHANGED_EVENT) {
                    rih.registerForImsCallStateChanged(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_IMS_CALL_STATE_CHANGED_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_VIDEO_QUALITY_EVENT) {
                    rih.registerForImsVideoQos(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_VIDEO_QUALITY_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_IMS_BEARER_ESTABLISTED_EVENT) {
                    rih.registerForImsBearerStateChanged(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_IMS_BEARER_ESTABLISTED_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_IMS_HANDOVER_REQUEST_EVENT) {
                    rih.registerImsHandoverRequest(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_IMS_HANDOVER_REQUEST_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_IMS_HANDOVER_STATUS_CHANGE_EVENT) {
                    rih.registerImsHandoverStatus(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_IMS_HANDOVER_STATUS_CHANGE_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_IMS_NETWORK_INFO_CHANGE_EVENT) {
                    rih.registerImsNetworkInfo(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_IMS_NETWORK_INFO_CHANGE_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_IMS_REGISTER_ADDRESS_CHANGE_EVENT) {
                    rih.registerImsRegAddress(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_IMS_REGISTER_ADDRESS_CHANGE_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_IMS_WIFI_PARAM_EVENT) {
                    rih.registerImsWiFiParam(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_IMS_WIFI_PARAM_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_IMS_NETWORK_STATE_CHANGED_EVENT) {
                    rih.registerForImsNetworkStateChanged(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_IMS_NETWORK_STATE_CHANGED_EVENT);
                }
                if (events == RadioInteractorCallbackListener.LISTEN_DSDA_STATUS_EVENT) {
                    rih.registerForDsdaStatus(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_DSDA_STATUS_EVENT);
                }
                if ((events == RadioInteractorCallbackListener.LISTEN_HD_STATUS_CHANGED_EVENT)) {
                    rih.registerForHdStatusChanged(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_HD_STATUS_CHANGED_EVENT);
                }
                return;
            }
            if (mRadioInteractorProxy != null) {
                mRadioInteractorProxy.listenForSlot(radioInteractorCallbackListener.mSlotId,
                        radioInteractorCallbackListener.mCallback,
                        events, notifyNow);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private RadioInteractorHandler getRadioInteractorHandler(int slotId) {
        UtilLog.logd(TAG, "RadioInteractorFactory:  " + sRadioInteractorFactory
                + "  RadioInteractorFactory class " + RadioInteractor.class.hashCode());
        sRadioInteractorFactory = RadioInteractorFactory.getInstance();
        if (sRadioInteractorFactory != null) {
            return sRadioInteractorFactory.getRadioInteractorHandler(slotId);
        }
        return null;
    }

    // This interface will be eliminated
    public int sendAtCmd(String oemReq, String[] oemResp, int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.invokeOemRILRequestStrings(oemReq, oemResp);
            }
            if (mRadioInteractorProxy != null) {
                return mRadioInteractorProxy.sendAtCmd(oemReq, oemResp, slotId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getDefaultNetworkAccessName(int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.getDefaultNetworkAccessName();
            }
            if (mRadioInteractorProxy != null) {
                return mRadioInteractorProxy.getDefaultNetworkAccessName(slotId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getSimCapacity(int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.getSimCapacity();
            }
            if (mRadioInteractorProxy != null) {
                return mRadioInteractorProxy.getSimCapacity(slotId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void enableRauNotify(int slotId){
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                rih.enableRauNotify();
                return;
            }
            if (mRadioInteractorProxy != null) {
                mRadioInteractorProxy.enableRauNotify(slotId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the ATR of the UICC if available.
     * @param slotId int
     * @return The ATR of the UICC if available.
     */
    public String iccGetAtr(int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.iccGetAtr();
            }
            if (mRadioInteractorProxy != null) {
                return mRadioInteractorProxy.iccGetAtr(slotId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
    /* @} */

    public boolean queryHdVoiceState(int slotId){
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.queryHdVoiceState();
            }
            if (mRadioInteractorProxy != null) {
                return mRadioInteractorProxy.queryHdVoiceState(slotId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Send request to set call forward number whether shown.
     *
     * @param slotId int
     * @param enabled CMCC is true,other is false
     */
    public void setCallingNumberShownEnabled(int slotId, boolean enabled) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                rih.setCallingNumberShownEnabled(enabled);
                return;
            }
            if (mRadioInteractorProxy != null) {
                mRadioInteractorProxy.setCallingNumberShownEnabled(slotId, enabled);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Store Sms To Sim
     *
     * @param enable True is store SMS to SIM card,false is store to phone.
     * @param slotId int
     * @return whether successful store Sms To Sim
     */
    public boolean storeSmsToSim(boolean enable, int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.storeSmsToSim(enable);
            }
            if (mRadioInteractorProxy != null) {
                return mRadioInteractorProxy.storeSmsToSim(enable, slotId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Return SMS Storage Mode.
     *
     * @param slotId int
     * @return SMS Storage Mode
     */
    public String querySmsStorageMode(int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.querySmsStorageMode();
            }
            if (mRadioInteractorProxy != null) {
                return mRadioInteractorProxy.querySmsStorageMode(slotId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Explicit Transfer Call REFACTORING
     * @param slotId
     */
    public void explicitCallTransfer(int slotId) {
        try {
            if (mRadioInteractorProxy != null) {
                mRadioInteractorProxy.explicitCallTransfer(slotId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Multi Part Call
     * @param slotId
     * @param mode
     */
    public void switchMultiCalls(int slotId, int mode) {
        UtilLog.logd(TAG, "switchMultiCalls slotId = " + slotId + " mode = " + mode);
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                rih.switchMultiCalls(mode);
            } else {
                if (mRadioInteractorProxy != null) {
                    mRadioInteractorProxy.switchMultiCalls(slotId, mode);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    /* add for TV use in phone process@{*/
    public void dialVP(String address, String sub_address, int clirMode, Message result,
            int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.dialVP(address, sub_address, clirMode, result);
        }
    }

    public void codecVP(int type, Bundle param, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.codecVP(type, param, result);
        }
    }

    public void fallBackVP(Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.fallBackVP(result);
        }
    }

    public void sendVPString(String str, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.sendVPString(str, result);
        }
    }

    public void controlVPLocalMedia(int datatype, int sw, boolean bReplaceImg, Message result,
            int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.controlVPLocalMedia(datatype, sw, bReplaceImg, result);
        }
    }

    public void controlIFrame(boolean isIFrame, boolean needIFrame, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.controlIFrame(isIFrame, needIFrame, result);
        }
    }
    /* @} */

    /* Add for trafficClass @{ */
    public void requestDCTrafficClass(int type, int slotId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
        if (rih != null) {
            rih. requestDCTrafficClass(type);
        }
    }
    /* @} */

    /* Add for do recovery @{ */
    public void requestReattach(int slotId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
        if (rih != null) {
            rih.requestReattach();
        }
    }
    /* @} */

    /*SPRD: bug618350 add single pdp allowed by plmns feature@{*/
    public void requestSetSinglePDNByNetwork(boolean isSinglePDN, int slotId){
        RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
        if (rih != null) {
            rih.requestSetSinglePDNByNetwork(isSinglePDN);
        }
    }
    /* @} */

    /* Add for Data Clear Code from Telcel @{ */
    public void setLteEnabled(boolean enable, int slotId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
        if (rih != null) {
            rih.setLteEnabled(enable);
        }
    }

    public void attachDataConn(boolean enable, int slotId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
        if (rih != null) {
            rih.attachDataConn(enable);
        }
    }
    /* @} */

    /* Add for query network @{ */
    public void abortSearchNetwork(Message result, int slotId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
        if (rih != null) {
            rih.abortSearchNetwork(result);
        }
    }

    public void forceDetachDataConn(Message result, int slotId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
        if (rih != null) {
            rih.forceDetachDataConn(result);
        }
    }
    /* @} */

    /**
     * Add for shutdown optimization
     * @param slotId int
     */
    public boolean requestShutdown(int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.requestShutdown();
            }
            if (mRadioInteractorProxy != null) {
                return mRadioInteractorProxy.requestShutdown(slotId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get simlock remain times
     *
     * @param type ref to IccCardStatusEx.UNLOCK_XXXX
     * @param slotId int
     * @return remain times
     */
    public int getSimLockRemainTimes(int type, int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.getSimLockRemainTimes(type);
            }
            if (mRadioInteractorProxy != null) {
                return mRadioInteractorProxy.getSimLockRemainTimes(type, slotId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Get simlock status by nv
     *
     * @param type ref to IccCardStatusEx.UNLOCK_XXXX
     * @param slotId int
     * @return status: unlocked: 0 locked: 1
     */
    public int getSimLockStatus(int type, int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.getSimLockStatus(type);
            }
            if (mRadioInteractorProxy != null) {
                return mRadioInteractorProxy.getSimLockStatus(type, slotId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * lock/unlock for one key simlock
     *
     * @param facility String
     * @param lockState boolean: lock:true unlock: false
     * @param response Message
     * @param slotId int
     */
    public void setFacilityLockByUser(String facility, boolean lockState,
            Message response,int slotId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
        if (rih != null) {
            rih.setFacilityLockByUser(facility, lockState, response);
            return;
        }
        AsyncResult.forMessage(response, null, new Throwable(
               "simlock lock/unlock should be called in phone process!"));
        response.sendToTarget();
    }

    public void getSimStatus(int slotId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
        if (rih != null) {
            rih.getSimStatus();
        }
    }

    /**
     * Set SIM power
     */
    public void setSimPower(int phoneId, boolean enabled) {
        if (mContext != null) {
            try {
                RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
                if (rih != null) {
                    rih.setSimPower(mContext.getPackageName(), enabled);
                } else {
                    if (mRadioInteractorProxy != null) {
                        mRadioInteractorProxy.setSimPower(mContext.getPackageName(),phoneId, enabled);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public int setPreferredNetworkType(int phoneId, int networkType) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
            if (rih != null) {
                return rih.setPreferredNetworkType(networkType);
            } else {
                if (mRadioInteractorProxy != null) {
                    return mRadioInteractorProxy.setPreferredNetworkType(phoneId, networkType);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void updateRealEccList(String realEccList,int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.updateRealEccList(realEccList);
        }
    }

    public String getBandInfo(int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.getBandInfo();
            }
            if (mRadioInteractorProxy != null) {
                UtilLog.logd(TAG,"getBandInfo mRadioInteractorProxy"+mRadioInteractorProxy);
                return mRadioInteractorProxy.getBandInfo(slotId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setBandInfoMode(int type,int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                rih.setBandInfoMode(type);
            } else {
                UtilLog.logd(TAG,"setBandInfoMode mRadioInteractorProxy" + mRadioInteractorProxy);
                if (mRadioInteractorProxy != null) {
                    mRadioInteractorProxy.setBandInfoMode(type, slotId);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * set Preferred Network RAT
     */
    public void setNetworkSpecialRATCap(int type, int slotId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
        if (rih != null) {
            rih.setNetworkSpecialRATCap(type);
        }
    }

    public int queryColp(int slotId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
        if (rih != null) {
            return rih.queryColp();
        }
        return -1;
    }

    public int queryColr(int slotId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
        if (rih != null) {
            return rih.queryColr();
        }
        return -1;
    }

    public int mmiEnterSim(String data,int slotId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
        if (rih != null) {
            return rih.mmiEnterSim(data);
        }
        return -1;
    }

    public void updateOperatorName(String plmn,int phoneId){
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.updateOperatorName(plmn);
        }
    }

    /***
     * In the case of forbidden card state,get real sim state in slot.
     * @param phoneId
     * @return
     * IccCardStatusEx.CardState.CARDSTATE_ABSENT.ordinal()
     * IccCardStatusEx.CardState.CARDSTATE_PRESENT.ordinal()
     * IccCardStatusEx.CardState.CARDSTATE_ERROR.ordinal()
     */
    public int getRealSimSatus(int phoneId) {
        try {
            if (mRadioInteractorProxy != null) {
                return mRadioInteractorProxy.getRealSimSatus(phoneId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setXcapIPAddress(String cid, String ipv4Addr, String ipv6Addr,
            Message response, int phoneId){
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.setXcapIPAddress(cid, ipv4Addr, ipv6Addr, response);
        }
    }

    public void setSmsBearer(int type, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.setSmsBearer(type);
        }
    }

    public int[] getSimlockDummys(int phoneId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
            if (rih != null) {
                return rih.getSimlockDummys();
            } else {
                if (mRadioInteractorProxy != null) {
                    return mRadioInteractorProxy.getSimlockDummys(phoneId);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getSimlockWhitelist(int type, int phoneId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
            if (rih != null) {
                return rih.getSimlockWhitelist(type);
            } else {
                if (mRadioInteractorProxy != null) {
                    return mRadioInteractorProxy.getSimlockWhitelist(type, phoneId);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setVoiceDomain(int phoneId, int type) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.setVoiceDomain(type);
        }
    }

    public void updateCLIP(int enable, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.updateCLIP(enable, result);
        }
    }

    public void setTPMRState(int state, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.setTPMRState(state, result);
        }
    }

    public void getTPMRState(Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.getTPMRState(result);
        }
    }

    public void setVideoResolution(int resolution, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.setVideoResolution(resolution, result);
        }
    }

    public void enableLocalHold(boolean enable, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.enableLocalHold(enable, result);
        }
    }

    public void enableWiFiParamReport(boolean enable, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.enableWiFiParamReport(enable, result);
        }
    }

    public void callMediaChangeRequestTimeOut(int callId, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.callMediaChangeRequestTimeOut(callId, result);
        }
    }

    public void setDualVolteState(int state, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.setDualVolteState(state, result);
        }
    }

    public void setLocalTone(int data, int phoneId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
            if (rih != null) {
                rih.setLocalTone(data);
            } else {
                if (mRadioInteractorProxy != null) {
                    mRadioInteractorProxy.setLocalTone(data, phoneId);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public int updatePlmn(int phoneId, int type, int action, String plmn,
                          int act1, int act2, int act3){
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            return rih.updatePlmn(type, action, plmn, act1, act2, act3);
        }
        try {
            if (mRadioInteractorProxy != null) {
                return mRadioInteractorProxy.updatePlmn(phoneId, type, action, plmn, act1, act2, act3);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String queryPlmn(int phoneId, int type){
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            return rih.queryPlmn(type);
        }
        try {
            if (mRadioInteractorProxy != null) {
               return mRadioInteractorProxy.queryPlmn(phoneId, type);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setSimPowerReal(int phoneId, Boolean enable){
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.setSimPowerReal(mContext.getPackageName(), enable);
            return;
        }
        try {
            if (mRadioInteractorProxy != null) {
               mRadioInteractorProxy.setSimPowerReal(mContext.getPackageName(), phoneId, enable);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public String getRadioPreference(int phoneId, String key) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            return rih.getRadioPreference(key);
        }
        try {
            if (mRadioInteractorProxy != null) {
                return mRadioInteractorProxy.getRadioPreference(phoneId, key);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setRadioPreference(int phoneId, String key, String value) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.setRadioPreference(key, value);
        }
        try {
            if (mRadioInteractorProxy != null) {
                mRadioInteractorProxy.setRadioPreference(phoneId, key, value);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void getImsCurrentCalls (Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.getImsCurrentCalls(result);
        }
    }

    public void setImsVoiceCallAvailability(int state, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.setImsVoiceCallAvailability(state, result);
        }
    }

    public void getImsVoiceCallAvailability(Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.getImsVoiceCallAvailability(result);
        }
    }

    public void initISIM(String confUri, String instanceId, String impu,
            String impi, String domain, String xCap, String bspAddr,
            Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.initISIM(confUri, instanceId, impu, impi, domain, xCap, bspAddr,
                    result);
        }
    }

    public void requestVolteCallMediaChange(int action, int callId, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.requestVolteCallMediaChange(action, callId, result);
        }
    }

    public void responseVolteCallMediaChange(boolean isAccept, int callId, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.responseVolteCallMediaChange(isAccept, callId, result);
        }
    }

    public void setImsSmscAddress(String smsc, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.setImsSmscAddress(smsc, result);
        }
    }

    public void requestVolteCallFallBackToVoice(int callId, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.requestVolteCallFallBackToVoice(callId, result);
        }
    }

    public void setIMSInitialAttachApn(DataProfile dataProfileInfo, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.setIMSInitialAttachApn(dataProfileInfo, result);
        }
    }

    public void queryCallForwardStatus(int cfReason, int serviceClass,
            String number, String ruleSet, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.queryCallForwardStatus(cfReason, serviceClass, number, ruleSet, result);
        }
    }

    public void setCallForward(int action, int cfReason, int serviceClass,
            String number, int timeSeconds, String ruleSet, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.setCallForward(action, cfReason, serviceClass, number,
                    timeSeconds, ruleSet, result);
        }
    }

    public void requestInitialGroupCall(String numbers, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.requestInitialGroupCall(numbers, result);
        }
    }

    public void requestAddGroupCall(String numbers, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.requestAddGroupCall(numbers, result);
        }
    }

    public void enableIms(Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.enableIms(result);
        }
    }

    public void disableIms(Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.disableIms(result);
        }
    }

    public void getImsBearerState(Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.getImsBearerState(result);
        }
    }

    public void setInitialAttachSOSApn(DataProfile dataProfileInfo, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.setInitialAttachSOSApn(dataProfileInfo, result);
        }
    }

    public void requestImsHandover(int type, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.requestImsHandover(type, result);
        }
    }

    public void notifyImsHandoverStatus(int status, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.notifyImsHandoverStatus(status, result);
        }
    }

    public void notifyImsNetworkInfo(int type, String info, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.notifyImsNetworkInfo(type, info, result);
        }
    }

    public void notifyImsCallEnd(int type, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.notifyImsCallEnd(type, result);
        }
    }

    public void notifyVoWifiEnable(boolean enable, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.notifyVoWifiEnable(enable, result);
        }
    }

    public void notifyVoWifiCallStateChanged(boolean incall, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.notifyVoWifiCallStateChanged(incall, result);
        }
    }

    public void notifyDataRouter(Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.notifyDataRouter(result);
        }
    }

    public void imsHoldSingleCall(int callid, boolean enable, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.imsHoldSingleCall(callid, enable, result);
        }
    }

    public void imsMuteSingleCall(int callid, boolean enable, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.imsMuteSingleCall(callid, enable, result);
        }
    }

    public void imsSilenceSingleCall(int callid, boolean enable, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.imsSilenceSingleCall(callid, enable, result);
        }
    }

    public void imsEnableLocalConference(boolean enable, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.imsEnableLocalConference(enable, result);
        }
    }

    public void notifyHandoverCallInfo(String callInfo, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.notifyHandoverCallInfo(callInfo, result);
        }
    }

    public void getSrvccCapbility(Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.getSrvccCapbility(result);
        }
    }

    public void getImsPcscfAddress(Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.getImsPcscfAddress(result);
        }
    }

    public void setImsPcscfAddress(String addr, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.setImsPcscfAddress(addr, result);
        }
    }

    public void queryFacilityLockForAppExt(String facility, String password,
            int serviceClass, Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.queryFacilityLockForAppExt(facility, password, serviceClass, result);
        }
    }

    public void getImsRegAddress(Message result, int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            rih.getImsRegAddress(result);
        }
    }

    public int getPreferredNetworkType(int phoneId) {
        RadioInteractorHandler rih = getRadioInteractorHandler(phoneId);
        if (rih != null) {
            return rih.getPreferredNetworkType();
        }
        try {
            if (mRadioInteractorProxy != null) {
                return mRadioInteractorProxy.getPreferredNetworkType(phoneId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

}
