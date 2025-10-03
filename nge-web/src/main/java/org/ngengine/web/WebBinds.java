package org.ngengine.web;

import org.ngengine.web.context.WebCanvasElement;
import org.ngengine.web.context.WebContext.CanvasResizeHandler;
import org.ngengine.web.context.WebContext.CanvasSwapHandler;
import org.teavm.jso.JSByRef;
import org.teavm.jso.JSClass;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSModule;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSTopLevel;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.function.JSConsumer;
import org.teavm.jso.typedarrays.Float32Array;
import org.teavm.jso.typedarrays.Int8Array;

@JSClass
public class WebBinds implements JSObject {

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void helloBinds();

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void decodeImageAsync( @JSByRef byte[] data, String filename, int targetWidth, int targetHeight, JSConsumer<WebDecodedImage> resolve, JSConsumer<String> reject);

  


    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void loadScriptAsync(String script, JSConsumer<String> resolve, JSConsumer<String> reject);


    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void setPageTitle(String title);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void toggleFullscreen(boolean v);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void togglePointerLock(boolean v);

    // @JSTopLevel
    // @JSModule("./org/ngengine/web/WebBinds.js")
    // public static native void addEventListener(String event, Object fun);


        // WebBinds.addEventListener("resizeRenderTarget", new CanvasResizeHandler() {
        //     @Override
        //     public void onResize(int width, int height) {
        //         canvasWidth = width;
        //         canvasHeight = height;
        //         System.out.println("Resize render target: "+width+"x"+height);
        //     }
        // });

        // // WebBinds.addEventListener("swapRenderTarget", (CanvasSwapHandler)(c -> {
        // //     if (this.canvasTarget != c) {
        // //         this.canvasTarget = c;
        // //     }
        // // }));

        // WebBinds.addEventListener("swapRenderTarget", new CanvasSwapHandler() {
        //     @Override
    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void addResizeRenderTargetListener(CanvasResizeHandler fun);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void addSwapRenderTargetListener(CanvasSwapHandler fun);


    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void addInputEventListener(String event, EventListener fun);
   
    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void removeInputEventListener(String event, EventListener fun);


    // @JSTopLevel
    // @JSModule("./org/ngengine/web/WebBinds.js")
    // public static native void removeEventListener(String event, Object fun);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void waitNextFrame(JSConsumer<Void> callback);


    // @JSTopLevel
    // @JSModule("./org/ngengine/web/WebBinds.js")
    // public static native JSObject fireEventAsync(String event, JSArray<JSObject> args, JSConsumer<JSObject> resolve, JSConsumer<String> reject);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void getRenderTargetAsync(JSConsumer<WebCanvasElement> resolve, JSConsumer<String> reject);



    // audio binds

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void createAudioContextAsync(int sampleRate, int id, JSConsumer<Void> resolve, JSConsumer<String> reject);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void freeAudioContext(int ctxId);

    // Audio buffer management
    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void createAudioBufferAsync(int ctxId, int id, JSArray<Float32Array> f32channelData, int lengthInSamples, int sampleRate, JSConsumer<Void> resolve, JSConsumer<String> reject);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void freeAudioBuffer(int ctxId, int bufId);

    // Audio source management
    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void createAudioSourceAsync(int ctxId, int id, JSConsumer<Void> resolve, JSConsumer<String> reject);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void freeAudioSource(int ctxId, int srcId);

    // Source property setters
    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void setAudioBufferAsync(int ctxId, int srcId, int bufId, JSConsumer<Void> resolve, JSConsumer<String> reject);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void setAudioPositional(int ctxId, int srcId, boolean v);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void setContextAudioEnv(int ctxId, Int8Array i8data);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void setAudioPosition(int ctxId, int srcId, float x, float y, float z);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void setAudioVelocity(int ctxId, int srcId, float x, float y, float z);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void setAudioMaxDistance(int ctxId, int srcId, float v);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void setAudioRefDistance(int ctxId, int srcId, float v);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void setAudioDirection(int ctxId, int srcId, float x, float y, float z);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void setAudioConeInnerAngle(int ctxId, int srcId, float v);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void setAudioConeOuterAngle(int ctxId, int srcId, float v);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void setAudioConeOuterGain(int ctxId, int srcId, float v);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void setAudioLoop(int ctxId, int srcId, boolean v);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void setAudioPitch(int ctxId, int srcId, float v);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void setAudioVolume(int ctxId, int srcId, float v);

    // Playback control
    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void playAudioSourceAsync(int ctxId, int srcId, JSConsumer<Void> resolve, JSConsumer<String> reject);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void pauseAudioSourceAsync(int ctxId, int srcId, JSConsumer<Void> resolve, JSConsumer<String> reject);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void stopAudioSourceAsync(int ctxId, int srcId, JSConsumer<Void> resolve, JSConsumer<String> reject);

    // Getters
    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void getAudioPlaybackRateAsync(int ctxId, int srcId, JSConsumer<Float> resolve, JSConsumer<String> reject);


    @JSFunctor
    public static interface AudioEndEvent extends JSObject {
        void onAudioEnd(int ctxId, int srcId);
    }

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void addAudioEndListener(AudioEndEvent fun);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void setAudioContextListener(
        int ctxId, 
        float px, float py, float pz, 
        float dx, float dy, float dz, 
        float ux, float uy, float uz
    );

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void getBaseURLAsync(JSConsumer<String> resolve, JSConsumer<String> reject);


    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void connectNip07BackendAsync(JSConsumer<Void> resolve, JSConsumer<String> reject);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void connectWebRTCBackendAsync(JSConsumer<Void> resolve, JSConsumer<String> reject);

    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void pingFrontEnd();


    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void runWithDelay(JSConsumer<Void> callback, int msDelay);


    @JSTopLevel
    @JSModule("./org/ngengine/web/WebBinds.js")
    public static native void connectClipboardBackendAsync(JSConsumer<Void> resolve, JSConsumer<String> reject);
}

