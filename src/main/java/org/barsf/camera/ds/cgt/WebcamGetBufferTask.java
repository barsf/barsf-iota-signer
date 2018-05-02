package org.barsf.camera.ds.cgt;

import java.nio.ByteBuffer;

import org.barsf.camera.webcam.WebcamTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.barsf.camera.webcam.WebcamDevice;
import org.barsf.camera.webcam.WebcamDevice.BufferAccess;
import org.barsf.camera.webcam.WebcamDriver;


public class WebcamGetBufferTask extends WebcamTask {

	private static final Logger LOG = LoggerFactory.getLogger(WebcamGetBufferTask.class);

	private volatile ByteBuffer buffer = null;

	public WebcamGetBufferTask(WebcamDriver driver, WebcamDevice device) {
		super(driver, device);
	}

	public ByteBuffer getBuffer() {
		try {
			process();
		} catch (InterruptedException e) {
			LOG.debug("Image buffer request interrupted", e);
			return null;
		}
		return buffer;
	}

	@Override
	protected void handle() {

		WebcamDevice device = getDevice();
		if (!device.isOpen()) {
			return;
		}

		if (!(device instanceof BufferAccess)) {
			return;
		}

		buffer = ((BufferAccess) device).getImageBytes();
	}
}
