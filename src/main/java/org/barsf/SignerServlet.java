package org.barsf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.barsf.iota.lib.utils.Unsigned;
import org.barsf.signer.Base;
import org.barsf.signer.Online;
import org.barsf.signer.exception.*;
import org.barsf.signer.gson.BaseReq;
import org.barsf.signer.gson.BaseRes;
import org.barsf.signer.gson.address.AddressReq;
import org.barsf.signer.gson.address.AddressRes;
import org.barsf.signer.gson.milestone.MilestoneReq;
import org.barsf.signer.gson.milestone.MilestoneRes;
import org.barsf.signer.qrcode.address.AddressResponse;
import org.barsf.signer.qrcode.milestone.MileStoneResponse;
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
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
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
            BaseReq baseReq = OBJECT_MAPPER.readValue(request, BaseReq.class);
            long now = System.currentTimeMillis();

            BaseRes baseRes;
            if (StringUtils.equals(MilestoneReq.COMMAND, baseReq.getCommand())) {
                MilestoneReq milestoneReq = OBJECT_MAPPER.readValue(request, MilestoneReq.class);
                MileStoneResponse response = online.signMs(milestoneReq.getTreeIndex(), milestoneReq.getNodeIndex(),
                        milestoneReq.getHash());
                baseRes = new MilestoneRes();
                ((MilestoneRes) baseRes).setSignature(Unsigned.trytes(Unsigned.u10To27(response.getSign())));
                ((MilestoneRes) baseRes).setPath(Unsigned.trytes(Unsigned.u10To27(response.getPath())));
            } else if (StringUtils.equals(AddressReq.COMMAND, baseReq.getCommand())) {
                AddressReq addressReq = OBJECT_MAPPER.readValue(request, AddressReq.class);
                AddressResponse response = online.address(addressReq.getSeedIndex(), addressReq.getFromIndex(),
                        addressReq.getToIndex(), addressReq.getSecurity());
                baseRes = new AddressRes();
                List<String> addrez = new ArrayList<>();
                response.getAddresses().forEach(address -> addrez.add(Unsigned.trytes(Unsigned.u10To27(address))));
                ((AddressRes) baseRes).setAddresses(addrez);
            } else {
                throw new ServletException("unsupported command " + baseReq.getCommand());
            }

            baseRes.setDuration(System.currentTimeMillis() - now);
            result = OBJECT_MAPPER.writeValueAsString(baseRes);
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
