package org.ngengine.web.input;

import java.util.ArrayList;
import java.util.List;

import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.KeyboardEvent;

import com.jme3.input.KeyInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.KeyInputEvent;
import org.ngengine.web.WebBinds;

public class WebKeyInput implements KeyInput {
    private boolean initialized = false;
    private RawInputListener listener;

    private final List<KeyInputEvent> keyEvents = new ArrayList<>();
    @SuppressWarnings("rawtypes")
    private EventListener webListener = new EventListener() {
        @Override
        public void handleEvent(Event evt) {
            handleWebEvent(evt);
        }
    };
    public WebKeyInput() {
   }

    @Override
    public void initialize() {
       
 
        WebBinds.addInputEventListener("keyup", webListener);
        WebBinds.addInputEventListener("keydown", webListener);
        initialized = true;
    }

    @Override
    public void update() {
        for (KeyInputEvent evt : keyEvents) {
            if (listener != null) {
                listener.onKeyEvent(evt);
            }
        }
        keyEvents.clear();
    }

    @Override
    public void destroy() {
        WebBinds.removeInputEventListener("keyup", webListener);
        WebBinds.removeInputEventListener("keydown", webListener);      
        initialized = false;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void setInputListener(RawInputListener listener) {
        this.listener = listener;
    }

    @Override
    public long getInputTimeNanos() {
        
        return System.nanoTime();

    }

    private void handleWebEvent(Event evt) {
        long time = getInputTimeNanos();
        KeyboardEvent ev = (KeyboardEvent) evt;
        String keyCode = ev.getCode();
        String keyCharCode = ev.getKey();
        
        int jmeKeyCode=KeyMapper.jsCodeToJme(keyCode);
        char jmeKeyChar = keyCharCode.length() == 1 ? keyCharCode.charAt(0) : '\0';
        boolean isPressed = ev.getType().equals("keydown");

         
        KeyInputEvent jmeEvent=new KeyInputEvent(jmeKeyCode, jmeKeyChar, isPressed, false);
        jmeEvent.setTime(time);
        keyEvents.add(jmeEvent);
    }

    @Override
    public String getKeyName(int key) {
        return KeyMapper.getKeyNameJme(key);
    }
    
}
