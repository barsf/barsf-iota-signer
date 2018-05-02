package org.barsf.camera.ds.dummy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.barsf.camera.webcam.WebcamDiscoverySupport;
import org.barsf.camera.webcam.WebcamDriver;
import org.barsf.camera.webcam.WebcamDevice;


public class WebcamDummyDriver implements WebcamDriver, WebcamDiscoverySupport {

	private int count;

	public WebcamDummyDriver(int count) {
		this.count = count;
	}

	@Override
	public long getScanInterval() {
		return 10000;
	}

	@Override
	public boolean isScanPossible() {
		return true;
	}

	@Override
	public List<WebcamDevice> getDevices() {
		List<WebcamDevice> devices = new ArrayList<WebcamDevice>();
		for (int i = 0; i < count; i++) {
			devices.add(new WebcamDummyDevice(i));
		}
		return Collections.unmodifiableList(devices);
	}

	@Override
	public boolean isThreadSafe() {
		return false;
	}
}
