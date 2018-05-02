package org.barsf.signer;

import org.barsf.camera.main.Camera;
import org.barsf.screen.main.Screen;

public abstract class Base {

    // version should always consist with two numeric only
    protected String version = "00";
    protected Screen screen;
    protected Camera camera;
    protected String previousMessSent;
    protected String response;
    protected boolean isResponseCompleteRec;

    protected Base() {
        screen = new Screen();
        camera = new Camera();
    }

    public void reset() {
        isResponseCompleteRec = true;
        previousMessSent = null;
        response = null;
    }

}
