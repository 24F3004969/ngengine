package com.jme3.input;

public interface InputDevice {
    /**
     * Rumbles the device for the given amount/magnitude.
     *
     * @param amount The amount to rumble. Should be between 0 and 1.
     */
    public void rumble(float amount);

    /**
     * Returns the name of the input device.
     */
    public String getDeviceName();

    public int getId();

}
