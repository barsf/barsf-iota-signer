package org.barsf;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.barsf.iota.lib.hash.MerkleTree;
import org.barsf.signer.Base;
import org.barsf.signer.Offline;
import org.barsf.signer.Online;
import org.barsf.signer.exception.SystemBusyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

public class MainApplication {

    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

    private static final String MODE_ONLINE = "online";
    private static final String MODE_OFFLINE = "offline";

    private static final String MERKLE_SEEDS = "merkle.seed";
    private static final String MERKLE_TREE = "merkle.tree";

    private static final String SEEDS_STORE = "seeds.store";

    private static boolean isRunning = false;

    public static synchronized Base start(String mode)
            throws SystemBusyException {
        if (isRunning) {
            throw new RuntimeException("system has already ran");
        }
        // chose running mode
        // if offline mode, then initial the milestone-tree
        Base returnValue;
        if (StringUtils.equals(mode, MODE_OFFLINE)) {
            // try to locate the key file and the milestone tree file
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL merkleSeedUrl, merkleTreeUrl, seedsStoreUrl;
            String merkleSeedPath, merkleTreePath, seedsStorePath;
            if (loader != null) {
                merkleSeedUrl = loader.getResource(MERKLE_SEEDS);
                merkleTreeUrl = loader.getResource(MERKLE_TREE);
                seedsStoreUrl = loader.getResource(SEEDS_STORE);
            } else {
                merkleSeedUrl = ClassLoader.getSystemResource(MERKLE_SEEDS);
                merkleTreeUrl = ClassLoader.getSystemResource(MERKLE_TREE);
                seedsStoreUrl = ClassLoader.getSystemResource(SEEDS_STORE);
            }
            if (merkleSeedUrl != null) {
                merkleSeedPath = merkleSeedUrl.getFile();
            } else {
                throw new RuntimeException("Cannot locate merkle seeds file path " + MERKLE_SEEDS);
            }
            if (merkleTreeUrl != null) {
                merkleTreePath = merkleTreeUrl.getFile();
            } else {
                throw new RuntimeException("Cannot locate merkle tree path " + MERKLE_TREE);
            }
            if (seedsStoreUrl != null) {
                seedsStorePath = seedsStoreUrl.getFile();
            } else {
                throw new RuntimeException("Cannot locate seed store file path " + SEEDS_STORE);
            }
            // file located, then initial MerkleTree with these two files
            try {
                MerkleTree.initial(merkleSeedPath, merkleTreePath);
                String[] seeds = StringUtils.split(FileUtils.readFileToString(new File(seedsStorePath), "UTF-8"), "\n");
                for (int i = 0; i < seeds.length; i++) {
                    seeds[i] = seeds[i].trim();
                }
                Offline.getInstance().setSeeds(seeds);
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
        start(args[0]);
    }

}
