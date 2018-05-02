package org.barsf.signer.util;

import java.math.BigInteger;
import java.security.MessageDigest;

public class MD5 {

    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(input.getBytes("utf-8"));
            return toDecimal(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String toDecimal(byte[] bytes) {
        return new BigInteger(bytes).toString();
    }

}
