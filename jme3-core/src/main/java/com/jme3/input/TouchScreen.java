package com.jme3.input;

public class TouchScreen implements InputDevice {
    @Override
    public void rumble(float amount) {
        // TouchScreens do not support rumble
    }

    @Override
    public String getDeviceName() {
        return "TouchScreen";
    }

    @Override
    public int getId() {
        return -3;
    }
    
}
