package org.ngengine.web.context;
import org.ngengine.web.WebBinds;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.TextRectangle;

public abstract class WebCanvasElement extends HTMLCanvasElement {
    // public abstract void requestPointerLock(PointerLockOptions options);


    @JSProperty("width")
    public abstract void setWidth(int width);

    @JSProperty("height")
    public abstract void setHeight(int height);



    

}
