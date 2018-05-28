package org.barsf.signer.gson.merkle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.api.client.util.Key;
import org.barsf.signer.gson.BaseRes;

@JsonIgnoreProperties(ignoreUnknown=true)
public class MerkleRes extends BaseRes {

    @Key("signature")
    private String signature;
    @Key("path")
    private String path;

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
