package org.barsf;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.barsf.iota.lib.utils.Unsigned;
import org.barsf.iota.signer.protocol.BaseReq;
import org.barsf.iota.signer.protocol.BaseRes;
import org.barsf.iota.signer.protocol.address.AddressReq;
import org.barsf.iota.signer.protocol.address.AddressRes;
import org.barsf.iota.signer.protocol.milestone.MilestoneReq;
import org.barsf.iota.signer.protocol.milestone.MilestoneRes;
import org.barsf.iota.signer.protocol.sign.SignReq;
import org.barsf.iota.signer.protocol.sign.SignRes;
import org.barsf.signer.Base;
import org.barsf.signer.Online;
import org.barsf.signer.exception.*;
import org.barsf.signer.qrcode.address.AddressResponse;
import org.barsf.signer.qrcode.milestone.MileStoneResponse;
import org.barsf.signer.qrcode.sign.SignResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SignerServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SignerServlet.class);
    private static final String CHARSET = "UTF-8";
    private static final Gson GSON = new Gson();
    private Base base;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        if (base.getMode() != Base.Mode.ONLINE) {
            throw new ServletException("only online mode do accept http requests");
        }
        Online online = (Online) base;
        String result;
        try {
            String request = IOUtils.toString(req.getInputStream(), CHARSET);
            BaseReq baseReq = GSON.fromJson(request, BaseReq.class);
            long now = System.currentTimeMillis();

            BaseRes baseRes;
            if (StringUtils.equals(MilestoneReq.COMMAND, baseReq.getCommand())) {
                MilestoneReq milestoneReq = GSON.fromJson(request, MilestoneReq.class);
                MileStoneResponse response = online.signMs(milestoneReq.getTreeIndex(), milestoneReq.getNodeIndex(),
                        milestoneReq.getHash());
                baseRes = new MilestoneRes();
                ((MilestoneRes) baseRes).setSignature(Unsigned.trytes(Unsigned.u10To27(response.getSign())));
                ((MilestoneRes) baseRes).setPath(Unsigned.trytes(Unsigned.u10To27(response.getPath())));
            } else if (StringUtils.equals(AddressReq.COMMAND, baseReq.getCommand())) {
                AddressReq addressReq = GSON.fromJson(request, AddressReq.class);
                AddressResponse response = online.address(addressReq.getSeedIndex(), addressReq.getFromIndex(),
                        addressReq.getToIndex(), addressReq.getSecurity());
                baseRes = new AddressRes();
                List<String> addrez = new ArrayList<>();
                response.getAddresses().forEach(address -> addrez.add(Unsigned.trytes(Unsigned.u10To27(address))));
                ((AddressRes) baseRes).setAddresses(addrez);
            } else if (StringUtils.equals(SignReq.COMMAND, baseReq.getCommand())) {
                SignReq signReq = GSON.fromJson(request, SignReq.class);
                SignResponse response = online.sign(signReq.getSeedIndex(), signReq.getAddressIndex(),
                        signReq.getSecurity(), signReq.getHash());
                baseRes = new SignRes();
                ((SignRes) baseRes).setSignature(Unsigned.trytes(Unsigned.u10To27(response.getSignature())));
            } else {
                throw new ServletException("unsupported command " + baseReq.getCommand());
            }

            baseRes.setDuration(System.currentTimeMillis() - now);
            result = GSON.toJson(baseRes);
            IOUtils.write(result, resp.getOutputStream(), CHARSET);
        } catch (ReadTimeoutException e) {
            new Thread(() -> reset(online)).start();
            throw new ServletException(e);
        } catch (PeerResetException | SystemBusyException | IOException | IncompatibleVersionException | PeerProcessException e) {
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
        try {
            base = MainApplication.start(mode);
        } catch (SystemBusyException e) {
            throw new ServletException(e);
        }
    }

}
