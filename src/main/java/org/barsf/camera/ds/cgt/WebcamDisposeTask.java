package org.barsf.camera.ds.cgt;

import org.barsf.camera.webcam.WebcamDriver;
import org.barsf.camera.webcam.WebcamTask;
import org.barsf.camera.webcam.WebcamDevice;


/**
 * Dispose webcam device.
 * 
 * @author Bartosz Firyn (sarxos)
 */
public class WebcamDisposeTask extends WebcamTask {

	public WebcamDisposeTask(WebcamDriver driver, WebcamDevice device) {
		super(driver, device);
	}

	public void dispose() throws InterruptedException {
		process();
	}

	@Override
	protected void handle() {
		getDevice().dispose();
	}
}
