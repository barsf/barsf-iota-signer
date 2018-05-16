package org.barsf.signer.misc;

import org.apache.commons.lang3.StringUtils;
import org.barsf.signer.util.MD5;

import java.util.Random;

public class Fragment {

    public static final int VERSION_OFFSET = 0;
    public static final int VERSION_LENGTH = 2;
    public static final int PREVIOUS_CHECKSUM_OFFSET = VERSION_OFFSET + VERSION_LENGTH;
    public static final int PREVIOUS_CHECKSUM_LENGTH = 6;
    public static final int NONCE_OFFSET = PREVIOUS_CHECKSUM_OFFSET + PREVIOUS_CHECKSUM_LENGTH;
    public static final int NONCE_LENGTH = 6;
    public static final int FLAG_OFFSET = NONCE_OFFSET + NONCE_LENGTH;
    public static final int FLAG_LENGTH = 1;
    public static final int CONTENT_OFFSET = FLAG_OFFSET + FLAG_LENGTH;

    // the length should only be 2's power
    public static final int FRAGMENT_MAX_LENGTH = 2048;

    public static final int MAX_CONTENT_LENGTH = FRAGMENT_MAX_LENGTH - CONTENT_OFFSET;
    public static final int MAX_NONCE_PLUS_ONE = Integer.parseInt(StringUtils.repeat('9', NONCE_LENGTH)) + 1;
    public static final String VERSION = "00";

    private static final Random RANDOM = new Random();

    // version should always consist with two numeric only
    private String version = VERSION;
    private String previousChecksum = StringUtils.repeat('0', PREVIOUS_CHECKSUM_LENGTH);
    private String nonce = StringUtils.repeat('0', NONCE_LENGTH);
    private Flag flag;
    private String fragmentContent = "";

    public Fragment() {
    }

    public static Fragment parseQrCode(String input) {
        Fragment fragment = new Fragment();
        fragment.version = input.substring(VERSION_OFFSET, VERSION_OFFSET + VERSION_LENGTH);
        fragment.previousChecksum = input.substring(PREVIOUS_CHECKSUM_OFFSET, PREVIOUS_CHECKSUM_OFFSET + PREVIOUS_CHECKSUM_LENGTH);
        fragment.nonce = input.substring(NONCE_OFFSET, NONCE_OFFSET + NONCE_LENGTH);
        fragment.flag = Flag.values()[Integer.parseInt(input.substring(FLAG_OFFSET, FLAG_OFFSET + FLAG_LENGTH))];
        fragment.fragmentContent = input.substring(CONTENT_OFFSET);
        return fragment;
    }

    public void newNonce() {
        this.nonce = StringUtils.leftPad(String.valueOf(RANDOM.nextInt(MAX_NONCE_PLUS_ONE)), NONCE_LENGTH, '0');
    }

    public String toQrCode() {
        return version +
                previousChecksum +
                nonce +
                flag.opCode() +
                fragmentContent;
    }

    public String getVersion() {
        return version;
    }

    public String getPreviousChecksum() {
        return previousChecksum;
    }

    public void setPreviousChecksum(String previousChecksum) {
        this.previousChecksum = previousChecksum;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public Flag getFlag() {
        return flag;
    }

    public void setFlag(Flag flag) {
        this.flag = flag;
    }

    public String getFragmentContent() {
        return fragmentContent;
    }

    public void setFragmentContent(String fragmentContent) {
        fragmentContent = StringUtils.defaultIfBlank(fragmentContent, "");
        if (fragmentContent.length() > MAX_CONTENT_LENGTH) {
            throw new RuntimeException("fragment too long : " + fragmentContent);
        }
        this.fragmentContent = fragmentContent;
    }

    public String getChecksum() {
        return MD5.md5(nonce + flag.opCode() + fragmentContent, 6);
    }

}
