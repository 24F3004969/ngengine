package org.ngengine.web;

import org.teavm.jso.JSByRef;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface WebDecodedImage extends JSObject {
    @JSProperty
    public int getWidth();
    @JSProperty
    public int getHeight();
    @JSProperty @JSByRef
    public byte[] getData();
}