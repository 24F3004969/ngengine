package org.ngengine.web;

import org.teavm.interop.Async;
import org.teavm.interop.AsyncCallback;

public class WebBindsAsync {
    @Async
    public static native WebDecodedImage decodeImage(byte[] data, String filename, int targetWidth, int targetHeight);

    private static void decodeImage( byte[] data, String filename, int targetWidth, int targetHeight, AsyncCallback<WebDecodedImage> callback) {
        WebBinds.decodeImageAsync(data, filename, targetWidth, targetHeight, (res) -> {
            callback.complete(res);
        }, (rej) -> {
            callback.error(rej);
        });
    }


    @Async
    public static native String loadScript(String script);

    private static void loadScript(String script, AsyncCallback<String> callback) {
        WebBinds.loadScriptAsync(script, (res) -> {
            callback.complete(res);
        }, (rej) -> {
            callback.error(rej);
        });
    }
}
