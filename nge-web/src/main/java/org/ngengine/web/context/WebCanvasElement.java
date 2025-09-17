package org.ngengine.web.context;
import org.ngengine.web.WebBinds;
import org.teavm.jso.JSBody;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.TextRectangle;

public abstract class WebCanvasElement extends HTMLCanvasElement {
    public abstract void requestPointerLock(PointerLockOptions options);

    @JSBody(params = { }, script = "document.exitPointerLock();")
    public abstract void exitPointerLock();

    @JSBody(params = { }, script = "return !!document.pointerLockElement;")
    public abstract boolean isPointerLocked();


    public float getPixelDeltaScrollY(float deltaValue, int deltaMode){
        return WebBinds.getPixelDeltaScroll(deltaValue, deltaMode);
    }


public int getRelativePosX(int clientX) {
    TextRectangle rect = this.getBoundingClientRect();    
    // scale factor between CSS pixels and canvas internal resolution
    double scaleX = this.getWidth() / rect.getWidth();    
    return (int)Math.round((clientX - rect.getLeft()) * scaleX);
}

public int getRelativePosY(int clientY) {
    TextRectangle rect = this.getBoundingClientRect();
    double scaleY = this.getHeight() / rect.getHeight();
    return (int)Math.round(this.getHeight() - ((clientY - rect.getTop()) * scaleY));
}
    public abstract void requestFullscreen();


    @JSBody(params = { }, script = "return !!document.fullscreenElement;")
    public abstract boolean isFullscreen();

    @JSBody(params = { }, script = "document.exitFullscreen();")
    public abstract void exitFullscreen();

    public void canvasFitParent(){
        WebBinds.canvasFitParent(this);
    }
    

}
