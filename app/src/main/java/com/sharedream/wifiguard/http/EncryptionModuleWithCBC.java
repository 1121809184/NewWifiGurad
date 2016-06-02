package com.sharedream.wifiguard.http;

import android.annotation.SuppressLint;
import android.util.Base64;

import com.sharedream.wifiguard.conf.Constant;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionModuleWithCBC {
//    private final static byte[] TAG_KEY = {48, 45, -74, -70, 39, 59, 63, 62, -96, -71, -77, 59, 59, -88, -109, -68};
    private final static byte[] TAG_KEY = {-112,-72,-110,-110,58,16,-110,59,63,56,-65,56,63,-72,60,104};

    /*private final static byte[][] AES_KEY_POOL = {
            {-105, 22, -76, 51, 63, -67, 60, 57, -67, -73, 52, 19, 19, -69, -79, -56},
            {24, -77, 55, 51, -73, 52, 20, 21, -69, -107, 16, 51, -103, 18, 62, -127},
            {48, 54, -110, -105, 63, -112, -72, -103, -76, -66, 50, -103, 29, -79, 58, 23},
            {-78, 52, 61, 18, -100, 60, -80, 51, 48, 57, 51, -69, -72, 49, -66, -43},
            {59, 27, 27, -74, 55, -68, 51, -69, -72, -75, -76, 57, -79, -72, -75, 20},
            {62, 56, 21, -98, -100, -100, -79, 59, 53, -69, -80, -76, -73, -80, -71, -100},
            {18, -106, 23, -97, 52, -69, -78, 58, -77, -111, 20, -75, 52, -79, -73, -126},
            {16, 50, 28, 18, -67, 23, 28, 51, -65, 56, -73, 50, -65, -75, 62, -115},
            {-65, 60, 52, 54, -69, -76, 63, -74, 52, -76, 60, -71, -71, 52, -66, -43},
            {49, 54, -75, -67, -74, -79, -74, 55, 61, 63, 21, 17, 57, 61, 52, 26},
            {-77, 48, 49, -104, 28, 55, 54, 29, 19, 16, 24, -66, -73, 61, 52, 90},
            {29, -65, -101, 19, -77, -79, 56, 51, 48, -71, -68, -69, 48, 49, -80, 11},
            {-69, -76, -68, 48, -101, 26, 56, -80, -65, 53, 60, 61, -67, -97, 23, 81},
            {20, 60, 48, 62, -67, 59, 26, -108, 23, -72, -108, -78, -71, -67, 24, 43},
            {22, 26, 59, 63, -78, -67, -111, 26, 57, 49, 51, 58, -102, 17, -65, -121},
            {52, -72, -65, -70, 50, 29, 28, 26, 27, 58, -77, 20, 17, 52, -71, -102},
            {-79, -77, 50, 48, -77, -73, 57, 50, -66, -72, 55, -107, -100, -72, -80, -44},
            {-66, 49, -67, -101, -107, 62, 18, 27, -77, 19, 26, -73, 62, 57, 16, 125},
            {-66, 55, -72, -76, -106, 19, -72, -73, 24, 21, 63, 18, -107, 60, 53, 95},
            {24, -109, 17, 18, 17, 19, 17, 22, 58, -72, -78, -66, -79, -79, 53, 2},
            {62, 18, 20, -111, 24, 55, -67, 55, 48, -65, -73, -71, 49, 53, 30, -67},
            {49, 59, -109, -98, -78, -74, 51, -79, 30, -107, -78, -77, 49, -104, 26, -101},
            {57, -68, 60, 54, -75, -105, -112, -71, -106, -68, -71, 23, 22, -99, -65, 30},
            {-68, 49, -69, -73, 59, -65, 19, -69, -108, 53, 53, 53, -72, -71, 60, -37},
            {49, -80, 22, -98, -79, -65, 55, 24, -69, -100, 50, -68, 22, -98, -71, 27},
            {56, -80, -67, -76, 26, 16, -68, 52, -77, 16, 59, 58, -101, 24, -107, 17},
            {-111, 17, 51, 48, 51, 25, -108, 57, -71, -68, -65, -67, 19, -102, -78, 65},
            {-78, 55, -71, 50, -98, 27, -69, 24, 56, 30, 57, 57, 53, -71, -72, 80},
            {16, -67, 58, 59, -78, -66, -65, 51, -77, 53, 53, 62, -101, -105, -77, -115},
            {-70, -106, -108, -73, 55, -77, 30, 20, -107, 59, 27, -72, 54, 62, 57, -41},
            {-69, 63, -97, -107, -67, -65, 62, -73, 50, -66, -77, -80, 17, 18, -107, -7},
            {57, 53, -109, 61, -110, -66, 57, -72, 58, 53, 52, 61, -109, 29, -74, 22},
    };*/

    private static final byte[][] AES_KEY_POOL = {
            {-110, 58, -69, -109, -72, 59, 56, -69, 18, -72, -70, -112, -70, -65, 22, 67},
            {59, 19, 59, 57, -112, -108, 56, 61, -112, 16, -67, -68, 16, 16, 56, -67},
            {-72, 19, 18, -71, -109, -107, 22, -70, 57, -70, 57, 16, 18, -110, 16, -42},
            {18, 18, 63, 56, -106, 56, 57, 16, -70, 61, -65, -112, 57, -70, -72, 1},
            {-70, -70, 58, 17, -111, 18, -111, 58, 60, 62, -112, 19, -69, 19, 19, -1},
            {-111, 18, -72, 58, -112, -71, 56, 18, 19, -109, -112, 19, 16, -110, -72, 67},
            {63, 56, -112, 18, -72, -67, 62, 57, 18, 19, 57, -112, -111, -111, 19, -65},
            {19, -70, 17, 17, 57, 59, -72, -109, -71, 18, 21, -68, 18, 56, 19, -88},
            {56, -71, -110, 19, -67, -66, 58, -110, -112, 16, -72, -112, 17, -105, 22, 22},
            {-112, 17, 20, 61, 56, -110, 59, -71, 59, 22, -108, -110, -66, 63, 63, 108},
            {-69, -109, 58, -112, -71, -112, 56, -106, -67, 61, -107, -110, -111, -69, -72, 84},
            {56, 56, -69, -70, -110, 16, 16, 57, -110, -70, -112, 56, -106, 60, -69, 63},
            {-66, -112, -110, 16, -108, 56, -107, 57, 16, -72, 17, 17, 17, 18, -111, -43},
            {-109, 56, 59, -110, -112, 57, -72, 59, 58, -70, -69, 19, -110, -70, 19, 64},
            {-106, -65, -111, -72, -106, 60, 16, 20, 22, -109, 56, 17, 57, -69, 18, 107},
            {18, -70, -109, 17, 58, 60, -68, 59, 56, -110, -69, 57, -72, 58, 17, -87},
            {-69, 62, 23, 59, -72, 18, 18, -110, 62, -72, -67, 56, 56, -72, 61, -7},
            {-71, -112, -69, 56, -68, 23, 17, 62, 63, -71, -69, 59, 62, 21, 17, -4},
            {63, 57, 58, -70, -69, 19, 59, -71, 17, -67, 61, -72, 19, 22, 56, 59},
            {-112, 16, -71, 19, 56, 58, 17, -72, -66, 56, 60, 18, -112, -109, 19, 106},
            {60, -65, 59, 57, 61, -65, 19, -109, -71, -70, -71, 19, 19, -71, -70, 60},
            {17, 23, -107, 19, -66, 60, 56, 60, 63, -112, 57, 19, -110, -105, -106, 2},
            {-72, 56, 56, -71, -112, 22, 63, 59, -110, 19, 19, -69, -107, 21, 59, -42},
            {61, 57, -70, 59, -109, 58, -111, -111, -111, 61, 63, 63, 20, -110, 57, 61},
            {-69, -69, 17, 16, -69, -69, -66, -106, -71, 58, -109, -72, 56, 62, 22, 87},
            {59, 17, 17, -69, -69, 22, 56, 21, -111, -69, -72, 59, 17, -72, 21, 57},
            {-72, -72, -111, -65, -65, 56, -69, 60, 56, -106, 57, -111, -72, 59, -72, 125},
            {-72, 57, 18, -112, 58, -70, -72, -72, -111, 59, -105, 22, 22, -67, -112, -43},
            {-66, 23, -109, -71, 56, 57, 16, -108, -108, 58, 17, -111, 18, -110, -112, -3},
            {-106, 21, -109, 62, -66, 62, -72, 61, 58, 17, -111, 59, 59, -72, -72, 104},
            {17, -69, 18, -72, -69, -72, -109, -72, 16, -110, -72, 58, -112, -112, -109, 1},
            {18, -107, -105, 57, 16, -112, -110, -70, 59, 18, 57, 57, -70, -107, -65, -86}
    };

    private static final String CHARS = "0123456789abcdefghijklmnopqrxtuvwxyzABCDEFGHIJKLMNOPQRXTUVWXYZ";
//    private static final byte[] IV = {27, 27, -102, -103, -101, 27, 56, -102, -66, 53, 18, -112, -67, 55, -103, 45};
    private static final byte[] IV = {57, 19, -112, -111, 22, -72, -65, 63, 56, -68, 58, 56, 19, -111, 58, -66};

    private EncryptionModuleWithCBC() {
    }

    public static String encryptJsonData(String jsonData) {
        StringBuilder stringBuilder = new StringBuilder();
        int len = CHARS.length();
        int indexSum = 0;
        for (int k = 0; k < len; k++) {
            int randomIndex = new Random().nextInt(len);
            char detail = CHARS.charAt(randomIndex);
            if (k == 7 || k == 16 || k == 21 || k == 23 || k == 26) {
                indexSum += detail;
            }
            stringBuilder.append(detail);
        }

        int index = indexSum % 32;
        final byte[] AES_KEY = AES_KEY_POOL[index];

        try {
            SecretKey keyForTag = new SecretKeySpec(getTagKey().getBytes(), decodeBytesToString(new byte[]{-70, -77, 76}));
            byte[] encryptTag = encrypt(stringBuilder.toString().getBytes(), keyForTag);
            String finalTag = new String(Base64.encode(encryptTag, Base64.NO_WRAP));

            SecretKey keyForJson = new SecretKeySpec(decodeBytesToString(AES_KEY).getBytes(), decodeBytesToString(new byte[]{-70, -77, 76}));
            byte[] encryptValue = encrypt(jsonData.getBytes(), keyForJson);
            String finalValue = new String(Base64.encode(encryptValue, Base64.NO_WRAP));

            JSONObject jsonObject = new JSONObject();
            jsonObject.put(finalTag, finalValue);

            return jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getTagKey() {
        String s = decodeBytesToString(TAG_KEY);
        return s;
    }

    public static String decryptJsonData(String jsonData) {
        try {
            String jsonKey = null;
            String jsonValue = null;

            JSONObject jsonObject = new JSONObject(jsonData);
            Iterator<String> it = jsonObject.keys();
            if (it.hasNext()) {
                jsonKey = it.next();
            }

            if (jsonKey != null) {
                jsonValue = jsonObject.optString(jsonKey);
            }

            if (jsonKey == null || jsonValue == null) {
                return null;
            }

            SecretKey aesKey = new SecretKeySpec(decodeBytesToString(TAG_KEY).getBytes(), decodeBytesToString(new byte[]{-70, -77, 76}));
            String decryptedJsonKey = new String(decrypt(Base64.decode(jsonKey, Base64.NO_WRAP), aesKey));

            int indexSum = 0;
            int len = decryptedJsonKey.length();
            for (int k = 0; k < len; k++) {
                if (k == 7 || k == 16 || k == 21 || k == 23 || k == 26) {
                    char detail = decryptedJsonKey.charAt(k);
                    indexSum += detail;
                }
            }
            int index = indexSum % 32;
            final byte[] AES_KEY = AES_KEY_POOL[index];

            SecretKey finalAesKey = new SecretKeySpec(decodeBytesToString(AES_KEY).getBytes(), decodeBytesToString(new byte[]{-70, -77, 76}));
            return new String(decrypt(Base64.decode(jsonValue, Base64.NO_WRAP), finalAesKey));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] encodeString(String string) {
        byte[] byteArray = string.getBytes();
        byte[] outputArray = new byte[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            if (i == byteArray.length - 1)
                outputArray[i] = rotateLeftByte((byte) (byteArray[i] ^ outputArray[0] ^ 0x71), 7);
            else
                outputArray[i] = rotateLeftByte((byte) (byteArray[i] ^ byteArray[i + 1] ^ 0x71), 7);
        }

        return outputArray;
    }

    // All string will be encoded to bytes to store in file
    public static String decodeBytesToString(byte[] byteArray) {
        byte[] tempArray = new byte[byteArray.length];
        byte[] outputArray = new byte[byteArray.length];
        for (int i = byteArray.length - 1; i >= 0; i--)
            tempArray[i] = byteArray[i];

        for (int i = byteArray.length - 1; i >= 0; i--) {
            tempArray[i] = rotateRightByte(byteArray[i], 7);
            if (i == tempArray.length - 1)
                outputArray[i] = (byte) (tempArray[i] ^ tempArray[0] ^ 0x71);
            else
                outputArray[i] = (byte) (tempArray[i] ^ outputArray[i + 1] ^ 0x71);
        }
        return new String(outputArray);
    }

    // encrypt with aes algorithm
    @SuppressLint("TrulyRandom")
    private static byte[] encrypt(byte[] bytes, SecretKey aesKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(decodeBytesToString(new byte[]{-70, -77, -122, -114, 56, 56, -114, 7, 53, -68, -80, -117, 10, 32, 58, -72, 62, 59, 60, 86})); // AES/CBC/PKCS5Padding
        IvParameterSpec ivSpec = new IvParameterSpec(decodeBytesToString(IV).getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
        return cipher.doFinal(bytes);
    }

    // decrypt with aes algorithm
    private static byte[] decrypt(byte[] bytes, SecretKey aesKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(decodeBytesToString(new byte[]{-70, -77, -122, -114, 56, 56, -114, 7, 53, -68, -80, -117, 10, 32, 58, -72, 62, 59, 60, 86})); // AES/CBC/PKCS5Padding
        IvParameterSpec ivSpec = new IvParameterSpec(decodeBytesToString(IV).getBytes());
        cipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);
        return cipher.doFinal(bytes);
    }

    private static byte rotateLeftByte(byte bt, int n) {
        int temp = bt & 0xFF;
        return (byte) (temp << n | temp >>> (8 - n));
    }

    private static byte rotateRightByte(byte bt, int n) {
        int temp = bt & 0xFF;
        return (byte) (temp >>> n | temp << (8 - n));
    }

    public static String encryptWithBase64AndChars(String str) {
        try {
            String strBase64Encoded = Base64.encodeToString(str.getBytes(Constant.SYS_ENCODING), Base64.DEFAULT);
            StringBuffer stringBuffer = new StringBuffer(strBase64Encoded);
            char firstChar = CHARS.charAt(new Random().nextInt(CHARS.length()));
            stringBuffer.insert(4, firstChar);
            char secondChar = CHARS.charAt(new Random().nextInt(CHARS.length()));
            stringBuffer.insert(1, secondChar);
            return stringBuffer.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String decryptWithBase64AndChars(String str) {
        StringBuffer stringBuffer = new StringBuffer(str);
        stringBuffer = stringBuffer.deleteCharAt(2);
        stringBuffer = stringBuffer.deleteCharAt(5);
        byte[] bytes = Base64.decode(stringBuffer.toString(), Base64.DEFAULT);
        return new String(bytes);
    }

}
