package org.ngengine.web;

import org.ngengine.web.context.WebCanvasElement;
import org.teavm.interop.Async;
import org.teavm.interop.AsyncCallback;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.typedarrays.Float32Array;

public class WebBindsAsync {
    @Async
    public static native WebDecodedImage decodeImage(byte[] data, String filename, int targetWidth, int targetHeight);

    private static void decodeImage( byte[] data, String filename, int targetWidth, int targetHeight, AsyncCallback<WebDecodedImage> callback) {
        WebBinds.decodeImageAsync(data, filename, targetWidth, targetHeight, (res) -> {
            callback.complete(res);
        }, (rej) -> {
            callback.error(new Exception(rej));
        });
    }


    @Async
    public static native String loadScript(String script);

    private static void loadScript(String script, AsyncCallback<String> callback) {
        WebBinds.loadScriptAsync(script, (res) -> {
            callback.complete(res);
        }, (rej) -> {
            callback.error(new Exception(rej));
        });
    }



    @Async
    public static native WebCanvasElement getRenderTarget();


    private static void getRenderTarget(AsyncCallback<WebCanvasElement> callback) {
        WebBinds.getRenderTargetAsync( (res) -> {
            callback.complete(res);
        }, (rej) -> {
            callback.error(new Exception(rej));
        });
    }


    // audio binds

    @Async
    public static native void createAudioContext(int sampleRate, int id);

    private static void createAudioContext(int sampleRate,int id, AsyncCallback<Void> callback) {
        WebBinds.createAudioContextAsync(sampleRate, id, (res) -> {
            callback.complete(res);
        }, (rej) -> {
            callback.error(new Exception(rej));
        });
    }

    @Async
    public static native void createAudioBuffer(int ctxId, int id, JSArray<Float32Array> f32channelData, int lengthInSamples, int sampleRate);

    private static void createAudioBuffer(int ctxId,int id, JSArray<Float32Array> f32channelData, int lengthInSamples, int sampleRate, AsyncCallback<Void> callback) {
        WebBinds.createAudioBufferAsync(ctxId, id, f32channelData, lengthInSamples, sampleRate, (res) -> {
            callback.complete(res);
        }, (rej) -> {
            callback.error(new Exception(rej));
        });
    }

    @Async
    public static native void createAudioSource(int ctxId, int id);

    private static void createAudioSource(int ctxId,int id, AsyncCallback<Void> callback) {
        WebBinds.createAudioSourceAsync(ctxId, id, (res) -> {
            callback.complete(res);
        }, (rej) -> {
            callback.error(new Exception(rej));
        });
    }

    @Async
    public static native Float getAudioPlaybackRate(int ctxId, int srcId);

    private static void getAudioPlaybackRate(int ctxId, int srcId, AsyncCallback<Float> callback) {
        WebBinds.getAudioPlaybackRateAsync(ctxId, srcId, (res) -> {
            callback.complete(res);
        }, (rej) -> {
            callback.error(new Exception(rej));
        });
    }
    

    @Async
    public static native void setAudioBuffer(int ctxId, int srcId, int bufId);

    private static void setAudioBuffer(int ctxId, int srcId, int bufId, AsyncCallback<Void> callback) {
        WebBinds.setAudioBufferAsync(ctxId, srcId, bufId, (res) -> {
            callback.complete(res);
        }, (rej) -> {
            callback.error(new Exception(rej));
        });
    }

    @Async
    public static native void playAudioSource(int ctxId, int srcId);    

    private static void playAudioSource(int ctxId, int srcId, AsyncCallback<Void> callback) {
        WebBinds.playAudioSourceAsync(ctxId, srcId, (res) -> {
            callback.complete(res);
        }, (rej) -> {
            callback.error(new Exception(rej));
        });
    }

    @Async
    public static native void pauseAudioSource(int ctxId, int srcId);
    private static void pauseAudioSource(int ctxId, int srcId, AsyncCallback<Void> callback) {
        WebBinds.pauseAudioSourceAsync(ctxId, srcId, (res) -> {
            callback.complete(res);
        }, (rej) -> {
            callback.error(new Exception(rej));
        });
    }

    @Async
    public static native void stopAudioSource(int ctxId, int srcId);
    private static void stopAudioSource(int ctxId, int srcId, AsyncCallback<Void> callback) {
        WebBinds.stopAudioSourceAsync(ctxId, srcId, (res) -> {
            callback.complete(res);
        }, (rej) -> {
            callback.error(new Exception(rej));
        });
    }

    @Async
    public static native String getBaseURL();
    private static void getBaseURL(AsyncCallback<String> callback) {
        WebBinds.getBaseURLAsync( (res) -> {
            callback.complete(res.toString());
        }, (rej) -> {
            callback.error(new Exception(rej));
        });
    }


    @Async
    public static native void connectNip07Backend();
    private static void connectNip07Backend(AsyncCallback<String> callback) {
        WebBinds.connectNip07BackendAsync( (res) -> {
            callback.complete(res.toString());
        }, (rej) -> {
            callback.error(new Exception(rej));
        }); 
    }

    @Async
    public static native void connectWebRTCBackend();
    private static void connectWebRTCBackend(AsyncCallback<String> callback) {
        WebBinds.connectWebRTCBackendAsync( (res) -> {
            callback.complete(res.toString());
        }, (rej) -> {
            callback.error(new Exception(rej));
        }); 
    }


    @Async
    public static native void connectClipboardBackend();
    private static void connectClipboardBackend(AsyncCallback<String> callback) {
        WebBinds.connectClipboardBackendAsync( (res) -> {
            callback.complete(res.toString());
        }, (rej) -> {
            callback.error(new Exception(rej));
        });
    }
}
