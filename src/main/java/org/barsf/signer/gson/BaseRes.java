package org.barsf.signer.gson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.api.client.util.Key;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseRes {

    @Key("duration")
    private Long duration;

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

}
