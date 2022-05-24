package pers.zcc.vertxprc.common.util;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class CipherUtil {

    public static String encryptByPBEWithMD5AndDES(String content, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
        SecretKey secKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
                .generateSecret(new PBEKeySpec(key.toCharArray()));
        PBEParameterSpec parameterspec = new PBEParameterSpec(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 }, 1000);
        cipher.init(Cipher.ENCRYPT_MODE, secKey, parameterspec);
        byte[] results = cipher.doFinal(content.getBytes());
        return new String(Base64.getEncoder().encode(results));

    }

    public static String decryptByPBEWithMD5AndDES(String content, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
        SecretKey secKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
                .generateSecret(new PBEKeySpec(key.toCharArray()));
        PBEParameterSpec parameterspec = new PBEParameterSpec(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 }, 1000);
        cipher.init(Cipher.DECRYPT_MODE, secKey, parameterspec);
        byte[] res = cipher.doFinal(Base64.getDecoder().decode(content.getBytes()));
        return new String(res);
    }

    public static void main(String[] args) {
        try {
            String key = EnvironmentProps.getApplicationProp("common.user.encrypt.des.key");
            String content = "zcc";
            String enc = encryptByPBEWithMD5AndDES(content, key);
            System.out.println(enc);
            String dec = decryptByPBEWithMD5AndDES(enc, key);
            System.out.println(dec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
