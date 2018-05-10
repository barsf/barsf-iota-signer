package org.barsf.signer.util;

import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.security.MessageDigest;

public class MD5 {

    public static String md5(String input, int length) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(input.getBytes("utf-8"));
            return toDecimal(bytes, length);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String toDecimal(byte[] bytes, int length) {
        String returnValue = new BigInteger(bytes).abs().toString();
        return StringUtils.leftPad(StringUtils.substring(returnValue, 0, length), length, '0');
    }

}
