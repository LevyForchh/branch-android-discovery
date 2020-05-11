package io.branch.search;

import android.support.annotation.NonNull;
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class Branchcryption {
    private static final String utf8 = "utf-8";// getBytes() output depends on the system defaults. Use getBytes(\"utf-8\") - https://stackoverflow.com/a/30383887

    // json keys
    static final String jsonKeyIV = "iv";
    static final String jsonKeyData = "data";
    // header keys/values
    static final String headerKey = "X-Branch-Encryption";
    static final String keyId = "";

    // encryption schema
    private static final String encryptionCipher = "AES/CBC/PKCS5Padding";
    private static final String decryptionCipher = "AES/CBC/NoPadding";
    private static final String algorithm = "AES";
    private static final String key = "";

    public static String encrypt(@NonNull String textToEncrypt, @NonNull String iv) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(utf8), algorithm);
        Cipher cipher = Cipher.getInstance(encryptionCipher);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(iv.getBytes(utf8)));
        byte[] encrypted = cipher.doFinal(textToEncrypt.getBytes(utf8));
        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    public static String decrypt(String textToDecrypt, @NonNull String iv) throws Exception {
        byte[] encryted_bytes = Base64.decode(textToDecrypt, Base64.DEFAULT);
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(utf8), algorithm);
        Cipher cipher = Cipher.getInstance(decryptionCipher);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(iv.getBytes(utf8)));
        byte[] decrypted = cipher.doFinal(encryted_bytes);
        return new String(decrypted, utf8);
    }

}