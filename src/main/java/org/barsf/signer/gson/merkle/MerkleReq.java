package org.barsf.signer.gson.merkle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.api.client.util.Key;
import org.barsf.signer.gson.BaseReq;

@JsonIgnoreProperties(ignoreUnknown=true)
public class MerkleReq extends BaseReq {

    @Key("hash")
    private String hash;
    @Key("nodeIndex")
    private Integer nodeIndex;
    @Key("treeIndex")
    private Integer treeIndex;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Integer getNodeIndex() {
        return nodeIndex;
    }

    public void setNodeIndex(Integer nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    public Integer getTreeIndex() {
        return treeIndex;
    }

    public void setTreeIndex(Integer treeIndex) {
        this.treeIndex = treeIndex;
    }
}
