package org.ngengine.web;

import org.teavm.jso.JSByRef;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.typedarrays.Float32Array;

public interface WebHdrDecodedImage extends JSObject {
    @JSProperty
    public int getWidth();
    @JSProperty
    public int getHeight();
    @JSProperty @JSByRef
    public Float32Array getData();
}