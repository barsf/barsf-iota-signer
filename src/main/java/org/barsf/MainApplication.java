package org.barsf;

import org.apache.commons.lang3.StringUtils;
import org.barsf.iota.lib.hash.MerkleTree;
import org.barsf.signer.Base;
import org.barsf.signer.Offline;
import org.barsf.signer.Online;
import org.barsf.signer.exception.SystemBusyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class MainApplication {

    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

    private static final String MODE_ONLINE = "online";
    private static final String MODE_OFFLINE = "offline";
    private static boolean isRunning = false;

    public static synchronized Base start(String mode, String keyFilePath, String merkleFilePath)
            throws SystemBusyException {
        if (isRunning) {
            throw new RuntimeException("system has already ran");
        }
        // chose running mode
        // if offline mode, then initial the merkle-tree
        Base returnValue;
        if (StringUtils.equals(mode, MODE_OFFLINE)) {
            // try to locate the key file and the merkle tree file
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL urlKeyFile, urlMerkleFile;
            if (loader != null) {
                urlKeyFile = loader.getResource(keyFilePath);
                urlMerkleFile = loader.getResource(merkleFilePath);
            } else {
                urlKeyFile = ClassLoader.getSystemResource(keyFilePath);
                urlMerkleFile = ClassLoader.getSystemResource(merkleFilePath);
            }
            if (urlKeyFile != null) {
                keyFilePath = urlKeyFile.getFile();
            } else {
                throw new RuntimeException("Cannot locate key file path " + keyFilePath);
            }
            if (urlMerkleFile != null) {
                merkleFilePath = urlMerkleFile.getFile();
            } else {
                throw new RuntimeException("Cannot locate merkle file path " + keyFilePath);
            }
            // file located, then initial MerkleTree with these two files
            try {
                MerkleTree.initial(keyFilePath, merkleFilePath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            returnValue = Offline.getInstance();
            Offline.getInstance().start();
            logger.info("offline started");
        } else if (StringUtils.equals(mode, MODE_ONLINE)) {
            returnValue = Online.getInstance();
            Online.getInstance().reset();
            logger.info("online started");
        } else {
            throw new RuntimeException("mode must be 'online' or 'offline'.");
        }

        isRunning = true;

        return returnValue;
    }

    public static void main(String[] args)
            throws SystemBusyException {
        start(args[0], args[1], args[2]);
    }

}
