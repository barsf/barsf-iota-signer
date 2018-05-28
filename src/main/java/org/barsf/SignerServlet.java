package org.barsf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.barsf.signer.Base;
import org.barsf.signer.Online;
import org.barsf.signer.exception.IncompatibleVersionException;
import org.barsf.signer.exception.PeerResetException;
import org.barsf.signer.exception.ReadTimeoutException;
import org.barsf.signer.exception.SystemBusyException;
import org.barsf.signer.gson.BaseReq;
import org.barsf.signer.gson.BaseRes;
import org.barsf.signer.gson.merkle.MerkleReq;
import org.barsf.signer.gson.merkle.MerkleRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SignerServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SignerServlet.class);
    private static final String CHARSET = "UTF-8";
    private Base base;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        if (base.getMode() != Base.Mode.ONLINE) {
            throw new ServletException("only online mode do accept http requests");
        }
        Online online = (Online) base;
        String result;
        try {
            String request = IOUtils.toString(req.getInputStream(), CHARSET);
            BaseReq baseReq = OBJECT_MAPPER.readValue(request, BaseReq.class);
            long now = System.currentTimeMillis();

            BaseRes baseRes;
            if (StringUtils.equals("merkle", baseReq.getCommand())) {
                MerkleReq merkleReq = OBJECT_MAPPER.readValue(request, MerkleReq.class);
                String[] signAndPath = online.signMileStone(merkleReq.getHash(), merkleReq.getNodeIndex());
                baseRes = new MerkleRes();
                ((MerkleRes) baseRes).setSignature(signAndPath[0]);
                ((MerkleRes) baseRes).setPath(signAndPath[1]);

            } else {
                throw new ServletException("unsupported command " + baseReq.getCommand());
            }

            baseRes.setDuration(System.currentTimeMillis() - now);
            result = OBJECT_MAPPER.writeValueAsString(baseRes);
            IOUtils.write(result, resp.getOutputStream(), CHARSET);
        } catch (ReadTimeoutException e) {
            new Thread(() -> reset(online)).start();
            throw new ServletException(e);
        } catch (PeerResetException | SystemBusyException | IOException | IncompatibleVersionException e) {
            throw new ServletException(e);
        }
    }

    private void reset(Online online) {
        while (true) {
            try {
                online.reset();
                break;
            } catch (SystemBusyException e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String mode = config.getInitParameter("mode");
        String keyFilePath = config.getInitParameter("keyFilePath");
        String merkleFilePath = config.getInitParameter("merkleFilePath");
        try {
            base = MainApplication.start(mode, keyFilePath, merkleFilePath);
        } catch (SystemBusyException e) {
            throw new ServletException(e);
        }
    }

}
