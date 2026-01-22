package com.jme3.input;

public class Keyboard implements InputDevice {
    @Override
    public void rumble(float amount) {
        // Keyboards do not support rumble
    }

    @Override
    public String getDeviceName() {
        return "Keyboard";
    }

    @Override
    public int getId() {
        return -1;
    }
    
}
