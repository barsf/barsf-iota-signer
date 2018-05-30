package org.barsf.signer.gson.address;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.api.client.util.Key;
import org.barsf.signer.gson.BaseRes;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressRes extends BaseRes {

    @Key("addresses")
    private List<String> addresses;

    public List<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }
}
