package com.sharedream.wifiguard.vo;

import android.os.Parcel;
import android.os.Parcelable;

public class WifiConnectVo implements Parcelable, Cloneable {

    private String ssid;
    private String mac;
    private String realMac;
    private int iconResid;
    private int passwordType;
    private int status;
    private int networkId;
    private String password;
    private int frequency;
    private int businessId;
    private String time;
    private String statusDesc;
    private int signalLevel;
    private String maintainUserId;

    @Override
    public WifiConnectVo clone() {
        WifiConnectVo wifiConnectVo = null;
        try {
            wifiConnectVo = (WifiConnectVo) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return wifiConnectVo;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getRealMac() {
        return realMac;
    }

    public void setRealMac(String realMac) {
        this.realMac = realMac;
    }

    public int getIconResid() {
        return iconResid;
    }

    public void setIconResid(int iconResid) {
        this.iconResid = iconResid;
    }

    public int getPasswordType() {
        return passwordType;
    }

    public void setPasswordType(int passwordType) {
        this.passwordType = passwordType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getNetworkId() {
        return networkId;
    }

    public void setNetworkId(int networkId) {
        this.networkId = networkId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getBusinessId() {
        return businessId;
    }

    public void setBusinessId(int businessId) {
        this.businessId = businessId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public int getSignalLevel() {
        return signalLevel;
    }

    public void setSignalLevel(int signalLevel) {
        this.signalLevel = signalLevel;
    }

    public String getMaintainUserId() {
        return maintainUserId;
    }

    public void setMaintainUserId(String maintainUserId) {
        this.maintainUserId = maintainUserId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ssid);
        dest.writeString(mac);
        dest.writeString(realMac);
        dest.writeInt(iconResid);
        dest.writeInt(passwordType);
        dest.writeInt(status);
        dest.writeInt(networkId);
        dest.writeString(password);
        dest.writeInt(frequency);
        dest.writeInt(businessId);
        dest.writeString(time);
        dest.writeString(statusDesc);
        dest.writeInt(signalLevel);
        dest.writeString(maintainUserId);
    }


    public static Creator<WifiConnectVo> getCreator() {
        return CREATOR;
    }

    public static final Creator<WifiConnectVo> CREATOR = new Creator<WifiConnectVo>() {
        public WifiConnectVo createFromParcel(Parcel in) {
            WifiConnectVo vo = new WifiConnectVo();
            vo.ssid = in.readString();
            vo.mac = in.readString();
            vo.realMac = in.readString();
            vo.iconResid = in.readInt();
            vo.passwordType = in.readInt();
            vo.status = in.readInt();
            vo.networkId = in.readInt();
            vo.password = in.readString();
            vo.frequency = in.readInt();
            vo.businessId = in.readInt();
            vo.time = in.readString();
            vo.statusDesc = in.readString();
            vo.signalLevel = in.readInt();
            vo.maintainUserId = in.readString();
            return vo;
        }

        public WifiConnectVo[] newArray(int size) {
            return new WifiConnectVo[size];
        }
    };
}
