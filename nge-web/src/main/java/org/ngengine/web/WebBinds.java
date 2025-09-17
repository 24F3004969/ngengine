package org.ngengine.web;

import org.teavm.jso.JSByRef;
import org.teavm.jso.JSClass;
import org.teavm.jso.JSModule;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSTopLevel;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.function.JSConsumer;

@JSClass
public class WebBinds implements JSObject {

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void helloBinds();

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void decodeImageAsync( @JSByRef byte[] data, String filename, int targetWidth, int targetHeight, JSConsumer<WebDecodedImage> resolve, JSConsumer<Throwable> reject);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void canvasFitParent(HTMLCanvasElement canvas);


    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native int getPixelDeltaScroll(double deltaValue, int deltaMode);


    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void loadScriptAsync(String script, JSConsumer<String> resolve, JSConsumer<Throwable> reject);


    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void setPageTitle(String title);

}
