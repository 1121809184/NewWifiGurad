package com.sharedream.wifiguard.cmdws;

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
    private final static byte[] TAG_KEY = {-93, 53, 38, 38, 55, 59, -117, 9, 47, -81, -69, 36, -65, -67, -81, 72};

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
            {46, 57, 46, -96, -92, -92, -94, 0, 2, 57, -87, -79, -98, 16, -68, 31},
            {-100, 53, -88, 36, -68, -67, -80, -69, -77, 62, -101, 19, -112, 19, 35, -36},
            {23, -101, -102, -108, 45, -77, -120, -114, 49, 6, -105, 21, -71, 19, 16, 40},
            {38, -76, -69, 35, 45, 55, -76, -98, -124, -93, 25, 26, -103, -127, 4, -73},
            {-78, 34, -78, 63, -112, 2, -94, -68, 36, -72, -75, -73, 4, 22, -65, -46},
            {0, -70, -119, 40, 41, 54, 4, 27, 54, -71, 60, -79, 27, -103, 37, -100},
            {-92, 61, 38, 53, -96, 34, 32, -65, 53, 40, 42, 45, -89, -83, 48, -34},
            {8, 27, 25, 17, -65, 41, -70, -123, -71, 16, -71, -82, -67, -82, -71, -118},
            {58, 26, -70, 57, 56, 57, -70, 63, 32, 6, 9, 38, -69, -85, -76, -115},
            {-70, 60, 61, 46, 60, -108, 11, -93, -79, 49, -93, -114, -114, -79, -87, -41},
            {36, -101, 23, 43, -123, -112, 42, -128, -127, -88, 59, -88, -92, -65, -75, 28},
            {46, -81, 51, 60, -87, 58, -96, 59, -80, 51, -82, 23, -101, -74, -82, 15},
            {-108, 12, -68, 42, -96, -82, -91, 48, -75, 50, -65, 62, -73, 41, -76, -57},
            {-84, 6, 29, -67, -90, -72, 44, -110, -101, 37, -91, -76, 44, -68, 36, -43},
            {-76, -74, -80, 29, 57, 0, -83, -70, -83, 3, -126, -86, 44, 41, -73, -39},
            {3, -127, 57, 43, 43, 46, 22, -120, -113, -117, 55, 6, 59, -108, -76, 3},
            {52, -83, 46, 33, -71, 34, -72, 46, -69, -112, 22, 32, -96, -70, 16, -70},
            {37, -128, -123, -79, -118, -121, -76, 55, 62, 51, -88, 58, -88, 9, -124, 14},
            {-92, -97, 15, 15, -72, -125, -81, -90, -79, 48, 38, 41, -90, 40, -96, 91},
            {-72, -95, -91, -68, 14, -123, -92, 32, -82, -105, -102, -72, 33, 63, 42, 80},
            {-95, -65, -78, 40, 59, -84, -85, -71, 62, -82, -76, 39, -92, 54, 61, -40},
            {48, 61, -84, -81, -65, -96, -90, 43, 36, 34, -84, -75, 13, -109, -87, -122},
            {-73, 40, -91, -85, 37, 43, 43, -95, 50, 25, 1, -93, 50, -67, 20, 120},
            {8, 8, 56, 7, -125, 18, 50, 44, -88, -74, -86, -69, -93, -83, 48, 29},
            {60, -87, -84, -83, -68, 52, -77, 4, -106, -106, 17, 56, 41, 2, -105, 17},
            {32, -103, 9, -87, -89, 58, 3, -65, -103, -89, -65, 59, -71, 51, -91, -98},
            {-107, -102, -72, -102, 63, -108, 52, -67, -89, 38, 44, 35, -88, 42, -73, 72},
            {26, -101, 46, 32, 45, -96, 32, 1, -102, 54, 46, -69, -84, 58, -69, -127},
            {-119, 11, -97, 25, -110, -78, 56, 50, -106, 9, -71, -75, -121, 23, -108, -27},
            {47, -127, 59, -97, 45, -86, 62, 30, 23, 23, -72, -127, 41, -89, 51, -120},
            {5, -88, -90, 15, 19, 41, 47, -87, -79, 34, 47, -87, -86, 52, -69, 18},
            {3, 55, -89, -83, 7, -115, -91, 34, -79, -83, 54, -79, -69, -105, 23, 10}
    };

    private static final String CHARS = "0123456789abcdefghijklmnopqrxtuvwxyzABCDEFGHIJKLMNOPQRXTUVWXYZ";
    //    private static final byte[] IV = {27, 27, -102, -103, -101, 27, 56, -102, -66, 53, 18, -112, -67, 55, -103, 45};
    private static final byte[] IV = {43, -68, -93, 60, -77, -76, 40, -74, -92, -95, -94, 57, -81, -81, 11, -76};

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
