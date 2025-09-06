package com.jme3.json;

import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSBoolean;
import org.teavm.jso.core.JSNumber;
import org.teavm.jso.core.JSString;

import com.jme3.plugins.json.JsonPrimitive;

public class TeaJSONPrimitive extends TeaJSONElement implements JsonPrimitive {

    public TeaJSONPrimitive(JSObject element) {
        super(element);
    }
 

    @Override
    public boolean isNumber() {
        return element instanceof JSNumber;
    }

    @Override
    public boolean isBoolean() {
        return element instanceof JSBoolean;
    }

    @Override
    public boolean isString() {
        return element instanceof JSString;
    }
}
