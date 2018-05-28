package org.barsf.signer.gson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.api.client.util.Key;

@JsonIgnoreProperties(ignoreUnknown=true)
public class BaseReq {

    @Key("command")
    private String command;

    public BaseReq() {
    }

    public BaseReq(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

}
