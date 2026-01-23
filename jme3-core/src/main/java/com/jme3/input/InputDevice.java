package com.jme3.input;

public interface InputDevice {
    /**
     * Rumbles the device for the given amount/magnitude.
     *
     * @param amount The amount to rumble. Should be between 0 and 1.
     */
    public void rumble(float amount);
    
    /**
     * Rumbles the device with separate high and low frequency amounts.
     * @param amountHigh The amount to rumble the high frequency motor. Should be between 0 and 1.
     * @param amountLow The amount to rumble the low frequency motor. Should be between 0 and 1.
     * @param duration The duration to rumble in seconds.
     */
    public default void rumble(float amountHigh, float amountLow, float duration) {
        rumble(Math.max(amountHigh, amountLow));
    }

    /**
     * Returns the name of the input device.
     */
    public String getDeviceName();

    public int getId();

}
