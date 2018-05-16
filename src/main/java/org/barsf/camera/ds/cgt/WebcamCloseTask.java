package org.barsf.camera.ds.cgt;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.WebcamTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WebcamCloseTask extends WebcamTask {

    private static final Logger LOG = LoggerFactory.getLogger(WebcamCloseTask.class);

    public WebcamCloseTask(WebcamDriver driver, WebcamDevice device) {
        super(driver, device);
    }

    public void close() throws InterruptedException {
        process();
    }

    @Override
    protected void handle() {

        WebcamDevice device = getDevice();
        if (!device.isOpen()) {
            return;
        }

        LOG.info("Closing {}", device.getName());

        device.close();
    }
}
