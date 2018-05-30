package org.barsf.signer.qrcode.milestone;

import org.barsf.signer.qrcode.BaseTransact;
import org.barsf.signer.qrcode.Command;

import java.math.BigInteger;
import java.text.NumberFormat;

public class MileStoneRequest extends BaseTransact {

    public static final Command COMMAND = Command.SIGN_MILESTONE;

    public static final int TREE_INDEX_OFFSET = COMMAND_OFFSET + COMMAND_LENGTH;
    public static final int TREE_INDEX_LENGTH = 2;
    public static final int NODE_INDEX_OFFSET = TREE_INDEX_OFFSET + TREE_INDEX_LENGTH;
    public static final int NODE_INDEX_LENGTH = String.valueOf(0x01 << 22).length();
    public static final int CONTENT_TRYTES_OFFSET = NODE_INDEX_OFFSET + NODE_INDEX_LENGTH;

    public static final NumberFormat TREE_INDEX_FORMAT = NumberFormat.getInstance();
    public static final NumberFormat NODE_INDEX_FORMAT = NumberFormat.getInstance();

    static {
        TREE_INDEX_FORMAT.setMinimumIntegerDigits(TREE_INDEX_LENGTH);
        TREE_INDEX_FORMAT.setMaximumIntegerDigits(TREE_INDEX_LENGTH);
        TREE_INDEX_FORMAT.setGroupingUsed(false);

        NODE_INDEX_FORMAT.setMinimumIntegerDigits(NODE_INDEX_LENGTH);
        NODE_INDEX_FORMAT.setMaximumIntegerDigits(NODE_INDEX_LENGTH);
        NODE_INDEX_FORMAT.setGroupingUsed(false);
    }

    private int treeIndex;
    private int nodeIndex;
    private BigInteger content;

    public MileStoneRequest() {
        super(COMMAND);
    }

    public int getTreeIndex() {
        return treeIndex;
    }

    public void setTreeIndex(int treeIndex) {
        this.treeIndex = treeIndex;
    }

    public int getNodeIndex() {
        return nodeIndex;
    }

    public void setNodeIndex(int nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    public BigInteger getContent() {
        return content;
    }

    public void setContent(BigInteger content) {
        this.content = content;
    }

    @Override
    public String toQrCode() {
        return COMMAND.commandCode() + TREE_INDEX_FORMAT.format(treeIndex) + NODE_INDEX_FORMAT.format(nodeIndex) + content.toString();
    }

    @Override
    public void parseFrom(String qrCode) {
        Command command = Command.values()[Integer.parseInt(qrCode.substring(COMMAND_OFFSET, COMMAND_OFFSET + COMMAND_LENGTH))];
        if (command != COMMAND) {
            throw new RuntimeException("ops, this is not a MileStoneRequest, actually the command is " + command);
        }
        treeIndex = Integer.parseInt(qrCode.substring(TREE_INDEX_OFFSET, TREE_INDEX_OFFSET + TREE_INDEX_LENGTH));
        nodeIndex = Integer.parseInt(qrCode.substring(NODE_INDEX_OFFSET, NODE_INDEX_OFFSET + NODE_INDEX_LENGTH));
        content = new BigInteger(qrCode.substring(CONTENT_TRYTES_OFFSET));
    }


}
