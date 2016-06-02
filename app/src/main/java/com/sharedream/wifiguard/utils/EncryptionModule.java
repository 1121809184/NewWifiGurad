package com.sharedream.wifiguard.utils;

import android.annotation.SuppressLint;
import android.util.Base64;

import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionModule {
	private final static byte[] TAG_KEY = {42, 24, 10, -89, 36, 49, -90, -83, 53, 43, -90, 39, 50, -68, -67, 49, 56, -80, 39, 3, -120, 38, 61, -73, 63, 33, -93, -72, -67, 29, -106, 26};
	
	private final static byte[][] AES_KEY_POOL = {
			{-72, 18, 17, 23, -66, 23, -105, -67, -69, 49, -100, -103, 29, -68, -69, 57, 56, 57, -70, 56, -106, 20, -65, -111, 63, 61, 22, 25, 53, 22, -111, 86},
			{23, 19, 18, -102, -102, -71, -68, -65, -71, 56, -71, 19, -67, 56, -107, -69, 57, 20, 62, 17, 58, 22, 61, -108, 21, 19, 24, 30, -66, 17, -70, 2},
			{50, 55, 54, 50, 63, 62, 16, 17, 17, -71, -110, -75, 61, -76, 20, 29, -65, 56, 31, -66, -66, 29, 50, 57, -106, -104, -99, 23, 52, -80, -106, 61},
			{24, 27, 63, -99, -72, 28, -98, -66, -106, -112, -110, -112, -108, -105, -71, -98, 24, -97, 27, 21, 23, 26, -103, -72, -109, 19, 24, 59, -74, 49, 30, 47},
			{-111, -112, -108, -110, 56, -97, -101, 59, -104, -72, -69, -101, 63, -104, -98, 25, 24, 59, -101, 55, 63, 54, -77, -68, -69, -76, -68, -102, -106, -66, 18, -23},
			{49, -77, -101, -112, 16, 17, 19, 59, 56, 59, 25, 59, -101, -102, 57, 31, -66, -104, -75, 23, 16, -110, 20, -65, 17, -106, -68, -68, 21, -111, 48, -102},
			{18, 50, -71, -104, -70, 58, -105, 23, -65, -105, 62, 58, 23, 23, -105, -99, -74, -66, -68, -69, -79, 25, 56, -101, 51, -66, -65, -105, -106, -74, -68, 9},
			{20, -108, 20, -66, -111, 22, -69, -69, 58, 29, -97, -105, 20, -102, 27, -71, 27, -70, -73, 21, -104, 25, -72, 30, 24, 31, -78, -111, 57, 20, 54, 9},
			{23, 31, -80, 53, -101, 26, -103, 24, 59, 26, 25, 27, 18, -75, -72, 50, 59, 63, 54, -71, 61, -75, -65, -109, -69, 22, 22, -70, 28, -78, 22, -85},
			{-71, 56, -100, -105, -77, 24, 19, -72, 57, 17, 18, 56, 56, 59, 19, -101, 27, -72, 18, -69, 17, 18, 57, 56, 22, 23, -111, -70, 28, -104, -67, 125},
			{-105, 61, 51, 24, -105, 23, -71, -69, 23, -67, -72, -112, 62, 62, -72, -107, -73, -69, -69, 59, -101, 23, -75, -70, -76, -76, 25, -100, 60, 25, -72, 107},
			{23, -111, -106, 48, 51, 60, -109, 57, 23, -108, 62, 18, -105, 56, 58, 30, 28, -111, 16, -100, -98, -108, -76, 26, 59, 24, 27, 23, -69, 58, -105, 42},
			{-66, 57, 21, 50, -99, -108, 22, -105, -74, 63, 50, 21, 19, 16, -106, -108, 56, 58, 16, 58, -68, -70, 54, 56, -65, -66, -102, 28, 51, -74, 29, 123},
			{-65, 63, -80, 55, 55, 29, 25, 54, 54, -77, -108, -104, 30, -104, -78, -107, -69, -101, 58, 57, 24, -106, -108, 24, -66, 54, -111, -111, -110, 17, -111, -1},
			{-102, -103, 17, 60, -67, 59, 48, -74, -69, 21, -101, 27, -106, -105, -70, -106, 53, -102, -69, -112, -65, -66, 22, 23, 48, -104, 31, -66, 26, 20, -108, 109},
			{-68, 58, -109, 17, -109, -112, 59, 21, -107, -71, 22, 60, -109, 20, -106, -107, -66, 53, -67, 27, 56, -106, 22, -101, -101, -106, -106, -71, 18, -112, 23, -48},
			{26, 16, 62, 55, -73, 56, -69, -70, 23, 60, 20, -67, 17, 63, -103, 53, 22, 28, 29, 25, 25, -111, 21, -112, 16, -112, 53, -78, -73, -80, -74, -113},
			{-108, 59, -70, 16, 57, -99, 29, 17, 60, 20, -104, 52, 60, -110, 58, -107, 49, -98, -70, -69, -105, 23, 19, 53, -97, -72, 57, -102, 58, 30, 62, 104},
			{27, 27, -102, -103, -101, 27, 56, -102, -66, 53, 18, -112, -67, 55, -103, -69, 17, -80, -74, 21, 18, -68, -69, 53, 26, -104, 52, 23, 59, 18, 53, 9},
			{-102, -97, 20, 50, -103, 56, -112, -77, 60, 52, 52, 60, -104, -103, 29, 28, 54, 18, -72, -110, -107, -72, 18, -69, -112, -70, 22, -69, 17, -107, -69, -57},
			{63, -104, -72, 56, -104, 21, 21, 21, -109, -66, 20, 19, 63, 16, -108, 17, -66, -74, -79, -78, -74, -107, 23, 21, -110, 17, 58, -111, -112, -111, 49, 28},
			{-108, -105, -102, 30, 29, 59, 59, 29, 60, 60, -67, -107, -106, 31, 62, -65, -98, 27, 29, 56, 61, -67, 16, -79, 30, 31, 51, 57, 20, -69, -71, 106},
			{-112, -108, 58, 58, -108, 27, 27, -107, 23, -101, -69, -67, 52, 52, 29, -69, -109, 19, 56, 59, -99, -99, -71, 29, 30, 56, 58, 24, -100, -107, -71, 64},
			{60, -71, 56, -72, 22, 54, -102, -97, -97, -101, 55, -65, -112, -102, 24, -107, 23, 60, -112, 16, -110, 51, 29, 57, 26, -101, 17, 18, -72, -70, -71, 62},
			{50, 18, 27, 57, -75, 60, 23, -99, 26, 22, 17, -100, 26, 24, -103, 26, -71, -68, -74, 56, 60, -108, 22, 55, 58, -76, 57, -107, -112, -65, -105, -72},
			{58, -70, 26, 27, -100, 29, -101, -103, -112, 63, 23, 25, 24, -72, -72, -106, -76, -76, -105, 18, 62, 20, -103, -72, 49, 50, -102, -102, 26, 63, 31, 30},
			{-70, 56, -66, -67, 59, 23, 61, -69, 23, 28, -103, 58, -70, 19, 49, -103, 57, 58, -70, -108, 56, 23, -109, 19, -71, 28, -67, 57, 50, -109, 17, 84},
			{48, 22, 56, 20, 51, -79, 19, -110, -112, -102, 25, -70, -71, -110, -110, -98, 49, -107, 59, 23, 22, 17, -110, 18, -72, 58, -66, -72, -72, -110, 23, -109},
			{22, -70, -70, -106, -108, 18, -80, -75, 21, 60, -110, -105, 56, 17, 20, 31, 26, 18, -67, 23, -105, -78, -100, -106, 22, 24, 55, -107, 31, 57, 49, -122},
			{24, 55, 61, 17, -100, -98, -108, 63, 60, -107, 57, -70, 27, -79, 21, -67, -72, 24, 25, -68, -107, 56, 51, 56, 27, -70, 21, 21, 21, -108, 25, -113},
			{-71, -108, -106, 23, -112, 60, -71, -67, 30, -66, 28, -71, 25, -103, 58, -70, -104, -79, -108, 63, 58, -107, -69, 20, 26, -80, -80, -69, -73, 57, -74, 94},
			{59, -69, 59, -76, -76, -108, 22, 55, -66, 60, -70, 26, 24, -79, -110, 59, -100, -74, 22, -65, -109, -72, 19, -104, -104, -71, -69, 61, -65, -107, 59, 16}
	};

	private static final String CHARS = "0123456789abcdefghijklmnopqrxtuvwxyzABCDEFGHIJKLMNOPQRXTUVWXYZ";

	private EncryptionModule() {}

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
			SecretKey keyForTag = new SecretKeySpec(decodeBytesToString(TAG_KEY).getBytes(), decodeBytesToString(new byte[] { -70, -77, 76 }));
			byte[] encryptTag = encrypt(stringBuilder.toString().getBytes(), keyForTag);
			String finalTag = new String(Base64.encode(encryptTag, Base64.DEFAULT));
			
			SecretKey keyForJson = new SecretKeySpec(decodeBytesToString(AES_KEY).getBytes(), decodeBytesToString(new byte[] { -70, -77, 76 }));
			byte[] encryptValue = encrypt(jsonData.getBytes(), keyForJson);
			String finalValue = new String(Base64.encode(encryptValue, Base64.DEFAULT));
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(finalTag, finalValue);

			return jsonObject.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
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
			String decryptedJsonKey = new String(decrypt(Base64.decode(jsonKey, Base64.DEFAULT), aesKey));

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
			return new String(decrypt(Base64.decode(jsonValue, Base64.DEFAULT), finalAesKey));
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
	private static byte[] encrypt(byte[] bytes, SecretKey aesKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance(decodeBytesToString(new byte[]{-70, -77, -122, -115, -69, 56, 14, 7, 53, -68, -80, -117, 10, 32, 58, -72, 62, 59, 60, 86})); // AES/ECB/PKCS5Padding
		cipher.init(Cipher.ENCRYPT_MODE, aesKey);
		return cipher.doFinal(bytes);
	}

	// decrypt with aes algorithm
	private static byte[] decrypt(byte[] bytes, SecretKey aesKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance(decodeBytesToString(new byte[]{-70, -77, -122, -115, -69, 56, 14, 7, 53, -68, -80, -117, 10, 32, 58, -72, 62, 59, 60, 86})); // AES/ECB/PKCS5Padding
		cipher.init(Cipher.DECRYPT_MODE, aesKey);
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

}
