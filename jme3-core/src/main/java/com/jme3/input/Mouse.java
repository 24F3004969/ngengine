package com.jme3.input;

public class Mouse implements InputDevice {
    @Override
    public void rumble(float amount) {
        // Mice do not support rumble
    }

    @Override
    public String getDeviceName() {
        return "Mouse";
    }

    @Override
    public int getId() {
        return -2;
    }
    
}
