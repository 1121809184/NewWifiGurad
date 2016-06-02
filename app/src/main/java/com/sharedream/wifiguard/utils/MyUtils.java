package com.sharedream.wifiguard.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.sharedream.wifiguard.activity.WifiCheckActivity;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.listener.LoadingDialogSubject;
import com.sharedream.wifiguard.vo.WifiConnectVo;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.zip.Inflater;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MyUtils {

    public static String getDeviceUUID(Context context) {
        UUID uuid = null;
        String androidId = getAndroidId(context);
        if (androidId.equals("9774d56d682e549c")) {
            String deviceId = getImei(context);
            if (deviceId == null || deviceId.equals("")) {
                uuid = UUID.randomUUID();
            } else {
                uuid = UUID.nameUUIDFromBytes(deviceId.getBytes());
            }
        } else {
            uuid = UUID.nameUUIDFromBytes(androidId.getBytes());
        }
        return uuid.toString();
    }

    private static String getAndroidId(Context context) {
        String androidId = "9774d56d682e549c";
        try {
            androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return androidId;
    }

    public static String getImei(Context context) {
        String imei = "";
        try {
            imei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imei;
    }

    public static String getMacAddress(Context context) {
        String macAddress = "000000000000";
        try {
            WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
            if (null != info) {
                if (!TextUtils.isEmpty(info.getMacAddress())) {
                    //macAddress = info.getMacAddress().replace(":", "");
                    return info.getMacAddress();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getIpAddress(Context context) {
        try {
            WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
            if (null != info) {
                int ipInt = info.getIpAddress();
                StringBuilder sb = new StringBuilder();
                sb.append(ipInt & 0xFF).append(".");
                sb.append((ipInt >> 8) & 0xFF).append(".");
                sb.append((ipInt >> 16) & 0xFF).append(".");
                sb.append((ipInt >> 24) & 0xFF);
                return sb.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getWifiSecurityType(String capabilities) {
        if (capabilities == null) {
            return 0;
        }
        if (capabilities.toUpperCase().contains("WEP")) {
            return 1; // com.sharedream.wlan.sdk.conf.Constant.AP_SECURITY_WEP;
        } else if (capabilities.toUpperCase().contains("PSK")) {
            return 2; // com.sharedream.wlan.sdk.conf.Constant.AP_SECURITY_WPA;
        } else if (capabilities.toUpperCase().contains("EAP")) {
            return 3; // com.sharedream.wlan.sdk.conf.Constant.AP_SECURITY_EAP;
        }
        return 0; // com.sharedream.wlan.sdk.conf.Constant.AP_SECURITY_OPEN;
    }

    public static int convert2RssiLevel(int level) {
        if (level <= -80) {
            return 1;
        } else if (-80 < level && level <= -60) {
            return 2;
        } else if (level > -60) {
            return 3;
        }
        return 0;
    }

    public static boolean string2File(String res) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "category.txt";
        boolean flag = true;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        try {
            File distFile = new File(path);
            bufferedReader = new BufferedReader(new StringReader(res));
            bufferedWriter = new BufferedWriter(new FileWriter(distFile));
            char buf[] = new char[1024];
            int len;
            while ((len = bufferedReader.read(buf)) != -1) {
                bufferedWriter.write(buf, 0, len);
            }
            bufferedWriter.flush();
            bufferedReader.close();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            flag = false;
            return flag;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return flag;
    }

    public static void showToast(String info, int duration, Context context) {
        if (context == null) {
            return;
        }
        Toast toast = Toast.makeText(context, info, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showToast(String info, Context context) {
        showToast(info, Toast.LENGTH_LONG, context);
    }

    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    public static int getStatusHeight(Activity activity) {
        int statusHeight = 0;
        Rect localRect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        statusHeight = localRect.top;
        if (0 == statusHeight) {
            Class<?> localClass;
            try {
                localClass = Class.forName("com.android.internal.R$dimen");
                Object localObject = localClass.newInstance();
                int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
                statusHeight = activity.getResources().getDimensionPixelSize(i5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusHeight;
    }

    public static void showWifiPasswordCheckDialog(String info, ArrayList<WifiConnectVo> listWifi, int showDurationTime, Activity activity, int requestCode, int i) {
        Bundle bundle = new Bundle();
        bundle.putString(Constant.BUNDLE_KEY_DIALOG_INFO, info);
        bundle.putParcelableArrayList(Constant.BUNDLE_KEY_WIFI_LIST, listWifi);
        bundle.putInt(Constant.BUNDLE_KEY_SHOW_DURATION_TIME_ATFER_FINISH_ALL, showDurationTime);
        bundle.putBoolean(Constant.BUNDLE_KEY_CHECK_PASSWORD_ONLY, true);
        bundle.putInt(Constant.BUNDLE_KEY_INDEX, i);
        WifiCheckActivity.launch(activity, bundle, requestCode);
    }

    public static void hideLoadingDialog(View view) {
        if (view != null) {
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LoadingDialogSubject.getInstance().notifyRequestDialogDismiss();
                }
            }, 500);
        } else {
            LoadingDialogSubject.getInstance().notifyRequestDialogDismiss();
        }
    }

    public static String hmacSha1(String base, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        String type = "HmacSHA1";
        SecretKeySpec secret = new SecretKeySpec(key.getBytes(), type);
        Mac mac = Mac.getInstance(type);
        mac.init(secret);
        byte[] digest = mac.doFinal(base.getBytes());
        return Base64.encodeToString(digest, Base64.DEFAULT);
    }

    public static byte[] decompress(byte[] data) {
        byte[] output = new byte[0];

        int beforeLength = data.length;
        Inflater decompresser = new Inflater(false);    // no wrap header and tailer
        decompresser.reset();
        decompresser.setInput(data);
        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[Constant.BUFFER_LENGTH];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        decompresser.end();
        int resultLength = output.length;
        Log.d("WLANSDK", "Before: " + beforeLength + " After: " + resultLength);
        return output;
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return networkInfo.isConnected();
        }
        return false;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
            if (networkInfos != null) {
                for (NetworkInfo networkInfo : networkInfos) {
                    if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isIpReachable(String ip) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            if (inetAddress.isReachable(3000)) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isNetworkReachable(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Object data = conn.getContent();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static String getSysVersionName(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isWXAppInstalledAndSupported() {
        IWXAPI msgApi = WXAPIFactory.createWXAPI(AppContext.getContext(), null);
        msgApi.registerApp(Constant.WX_APP_ID);

        boolean sIsWXAppInstalledAndSupported = msgApi.isWXAppInstalled()
                && msgApi.isWXAppSupportAPI();

        return sIsWXAppInstalledAndSupported;
    }
}
