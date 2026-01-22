/*
 * $Id$
 * 
 * Copyright (c) 2016, Simsilica, LLC
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package jme3test.lemur;

import org.ngengine.gui.NGEStyle;

import com.jme3.app.*;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.input.Joystick;
import com.jme3.input.JoystickAxis;
import com.jme3.input.JoystickButton;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.JoyAxisTrigger;
import com.jme3.input.controls.JoyButtonTrigger;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.UnifiedInputListener;
import com.jme3.input.event.InputEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.OptionPanelState;
import com.simsilica.lemur.focus.Navigator;
import com.simsilica.lemur.focus.ScrollDirection;
import com.simsilica.lemur.focus.TraversalDirection;
import com.simsilica.lemur.input.InputMapper.ViewState;

/**
 * The main bootstrap class for the SimEthereal networking example game.
 *
 * @author Paul Speed
 */
public class DemoLauncher extends SimpleApplication implements UnifiedInputListener {

    private Node logo;

    public static void main(String... args) throws Exception {
        DemoLauncher main = new DemoLauncher();
        AppSettings settings = new AppSettings(true);

        // Set some defaults that will get overwritten if
        // there were previously saved settings from the last time the user
        // ran.
        settings.setRenderer(AppSettings.LWJGL_OPENGL40);
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setVSync(true);

        settings.setTitle("Lemur Demos");
        // settings.setSettingsDialogImage("/sim-eth-es-splash-512.png");
        // settings.setUseJoysticks(true);
        /*
         * try { BufferedImage[] icons = new BufferedImage[] { ImageIO.read( TreeEditor.class.getResource(
         * "/-icon-128.png" ) ), ImageIO.read( TreeEditor.class.getResource( "/-icon-32.png" ) ),
         * ImageIO.read( TreeEditor.class.getResource( "/-icon-16.png" ) ) }; settings.setIcons(icons); }
         * catch( IOException e ) { log.warn( "Error loading globe icons", e ); }
         */

        main.setSettings(settings);

        main.start();
    }

    public DemoLauncher() {
        super(new StatsAppState(), new DebugKeysAppState(), new BasicProfilerState(false),
                new OptionPanelState(), // from Lemur
                new MainMenuState(), new ScreenshotAppState("", System.currentTimeMillis()));

    }

    public void simpleInitApp() {
        viewPort.setBackgroundColor(ColorRGBA.Gray);
        setPauseOnLostFocus(false);
        setDisplayFps(false);
        setDisplayStatView(false);

        GuiGlobals.initialize(this);

        NGEStyle.installAndUse();

        GuiGlobals.getInstance().getInputMapper().register(getGuiViewPort());
        // BaseStyles.loadGlassStyle();

        inputManager.addMapping("ui:navigateUp", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("ui:navigateDown", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("ui:navigateLeft", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("ui:navigateRight", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("ui:navigateNext", new KeyTrigger(KeyInput.KEY_TAB));
        inputManager.addMapping("ui:navigateByCursor", new MouseAxisTrigger(MouseInput.AXIS_X, true),
                new MouseAxisTrigger(MouseInput.AXIS_X, false), new MouseAxisTrigger(MouseInput.AXIS_Y, true),
                new MouseAxisTrigger(MouseInput.AXIS_Y, false));

        inputManager.addMapping("ui:scrollUp", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));

        inputManager.addMapping("ui:scrollDown", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));

        inputManager.addMapping("ui:confirm", new KeyTrigger(KeyInput.KEY_RETURN),
                new KeyTrigger(KeyInput.KEY_SPACE), new MouseButtonTrigger(MouseInput.BUTTON_LEFT)
        // TODO add touch trigger
        );

        inputManager.addListener(this, "ui:navigateUp", "ui:navigateDown", "ui:navigateLeft", "ui:navigateRight",
                "ui:navigateNext", "ui:scrollUp", "ui:scrollDown", "ui:confirm", "ui:navigateByCursor");

    }

    boolean joystickBound = false;

    @Override
    public void simpleUpdate(float tpf) {
        GuiGlobals.getInstance().getInputMapper().update(tpf);
        if (!joystickBound) {

            Joystick[] joysticks = inputManager.getJoysticks();
            if (joysticks != null && joysticks.length > 0) {
                joystickBound = true;
                Joystick joystick = joysticks[0];
                inputManager.addMapping("ui:navigateUp",
                        new JoyButtonTrigger(joystick, JoystickButton.BUTTON_XBOX_DPAD_UP),
                        new JoyAxisTrigger(joystick, JoystickAxis.AXIS_XBOX_LEFT_THUMB_STICK_Y, true));
                inputManager.addMapping("ui:navigateDown",
                        new JoyButtonTrigger(joystick, JoystickButton.BUTTON_XBOX_DPAD_DOWN),
                        new JoyAxisTrigger(joystick, JoystickAxis.AXIS_XBOX_LEFT_THUMB_STICK_Y, false));
                inputManager.addMapping("ui:navigateLeft",
                        new JoyButtonTrigger(joystick, JoystickButton.BUTTON_XBOX_DPAD_LEFT),
                        new JoyAxisTrigger(joystick, JoystickAxis.AXIS_XBOX_LEFT_THUMB_STICK_X, true));
                inputManager.addMapping("ui:navigateRight",
                        new JoyButtonTrigger(joystick, JoystickButton.BUTTON_XBOX_DPAD_RIGHT),
                        new JoyAxisTrigger(joystick, JoystickAxis.AXIS_XBOX_LEFT_THUMB_STICK_X, false));
                inputManager.addMapping("ui:navigateNext",
                        new JoyButtonTrigger(joystick, JoystickButton.BUTTON_XBOX_RB));
                inputManager.addMapping("ui:navigatePrevious",
                        new JoyButtonTrigger(joystick, JoystickButton.BUTTON_XBOX_LB));

                inputManager.addMapping("ui:scrollUp",
                        new JoyAxisTrigger(joystick, JoystickAxis.AXIS_XBOX_RIGHT_THUMB_STICK_Y, true));
                inputManager.addMapping("ui:scrollDown",
                        new JoyAxisTrigger(joystick, JoystickAxis.AXIS_XBOX_RIGHT_THUMB_STICK_Y, false));

                inputManager.addMapping("ui:confirm",
                        new JoyButtonTrigger(joystick, JoystickButton.BUTTON_XBOX_A));
            }
        }
    }

    @Override
    public void onUnifiedInput(String name, boolean toggled, float value, InputEvent<?> event, float tpf) {
  
         boolean isPressed = value>0;
        
        switch (name) {
            case "ui:scrollUp": {
                if(toggled&&isPressed){
                    ViewState state = GuiGlobals.getInstance().getInputMapper().get(getGuiViewPort());
                    Navigator navigator = state.getNavigator();
                    navigator.scroll(ScrollDirection.Up, 1);
                    System.out.println("scroll up");
                }
                break;
            }
            case "ui:scrollDown": {
                if(toggled&&isPressed){
                    ViewState state = GuiGlobals.getInstance().getInputMapper().get(getGuiViewPort());
                    Navigator navigator = state.getNavigator();
                    navigator.scroll(ScrollDirection.Down, 1);
                    System.out.println("scroll down");
                }
                break;
            }
            case "ui:confirm": {
                if(toggled){
                    ViewState state = GuiGlobals.getInstance().getInputMapper().get(getGuiViewPort());
                    Navigator navigator = state.getNavigator();
                    if (event instanceof TouchEvent) {
                        // if touch event we do a pick before action
                        TouchEvent te = (TouchEvent) event;
                        Spatial picked = state.pick((int) te.getX(), (int) te.getY());
                        navigator.focus(picked);
                    }
                    navigator.action(isPressed);
                }
                break;
            }
            case "ui:navigateUp": {
                if(toggled&&isPressed){
                    ViewState state = GuiGlobals.getInstance().getInputMapper().get(getGuiViewPort());
                    Navigator navigator = state.getNavigator();
                    navigator.navigate(TraversalDirection.Up);
                }
            
                break;
            }
            case "ui:navigateDown": {
                if(toggled&&isPressed){
                    ViewState state = GuiGlobals.getInstance().getInputMapper().get(getGuiViewPort());
                    Navigator navigator = state.getNavigator();
                    navigator.navigate(TraversalDirection.Down);
                }                    
                break;
            }
            case "ui:navigateLeft": {
                if(toggled&&isPressed){
                    ViewState state = GuiGlobals.getInstance().getInputMapper().get(getGuiViewPort());
                    Navigator navigator = state.getNavigator();
                    navigator.navigate(TraversalDirection.Left);
                }
                break;
            }
            case "ui:navigateRight": {
                if(toggled&&isPressed){
                    ViewState state = GuiGlobals.getInstance().getInputMapper().get(getGuiViewPort());
                    Navigator navigator = state.getNavigator();
                    navigator.navigate(TraversalDirection.Right);
                }
                break;
            }
            case "ui:navigateNext": {
                if(toggled&&isPressed){
                    ViewState state = GuiGlobals.getInstance().getInputMapper().get(getGuiViewPort());
                    Navigator navigator = state.getNavigator();
                    navigator.navigate(TraversalDirection.Next);
                }
                break;
            }
            case "ui:navigatePrevious": {
                if(toggled&&isPressed){
                    ViewState state = GuiGlobals.getInstance().getInputMapper().get(getGuiViewPort());
                    Navigator navigator = state.getNavigator();
                    navigator.navigate(TraversalDirection.Previous);
                }
                break;
            }
            case "ui:navigateByCursor": {
                if (event instanceof MouseMotionEvent) {
                    MouseMotionEvent mme = (MouseMotionEvent) event;
                    ViewState state = GuiGlobals.getInstance().getInputMapper().get(getGuiViewPort());
                    Spatial picked = state.pick(mme.getX(), mme.getY());
                    Navigator navigator = state.getNavigator();
                    navigator.focus(picked);
                
                }
                break;
            }
        }
    }

}
