package org.ngengine.web.input;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface JSTouch extends JSObject {
    
    @JSProperty("clientX")
    public int getClientX();


    @JSProperty("clientY")
    public int getClientY();

    @JSProperty("identifier")
    public double getIdentifier();

    @JSProperty("force")
    public float getForce();

    

}
