package org.barsf.signer.gson.address;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.api.client.util.Key;
import org.barsf.signer.gson.BaseReq;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressReq extends BaseReq {

    public static final String COMMAND = "address";

    @Key("seedIndex")
    private Integer seedIndex;
    @Key("fromIndex")
    private Integer fromIndex;
    @Key("toIndex")
    private Integer toIndex;
    @Key("security")
    private Integer security;

    public Integer getSeedIndex() {
        return seedIndex;
    }

    public void setSeedIndex(Integer seedIndex) {
        this.seedIndex = seedIndex;
    }

    public Integer getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(Integer fromIndex) {
        this.fromIndex = fromIndex;
    }

    public Integer getToIndex() {
        return toIndex;
    }

    public void setToIndex(Integer toIndex) {
        this.toIndex = toIndex;
    }

    public Integer getSecurity() {
        return security;
    }

    public void setSecurity(Integer security) {
        this.security = security;
    }
}
