
package org.ngengine.web.context;
import com.jme3.asset.AssetManager;
import com.jme3.input.JoyInput;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.TouchInput;
import com.jme3.material.Material;
import com.jme3.opencl.Context;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.opengl.GLRenderer;
import com.jme3.scene.Geometry;
import com.jme3.system.*;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.FrameBuffer.FrameBufferTarget;
import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.ui.Picture;

import org.ngengine.web.input.WebKeyInput;
import org.ngengine.web.input.WebMouseInput;
import org.ngengine.web.input.WebTouchInput;
import org.ngengine.web.rendering.WebGL;
import org.ngengine.web.rendering.WebGLOptions;
import org.ngengine.web.rendering.WebGLWrapper;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.teavm.interop.Async;
import org.teavm.interop.AsyncCallback;
import org.teavm.jso.JSBody;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.xml.Document;
import org.teavm.jso.function.JSConsumer;
 

public class WebContext implements JmeContext, Runnable {
   

    protected static final Logger logger = Logger.getLogger(WebContext.class.getName());

    protected static final String THREAD_NAME = "jME3 Web Main";

    protected AtomicBoolean created = new AtomicBoolean(false);
    protected AtomicBoolean needClose = new AtomicBoolean(false);
    protected final Object createdLock = new Object();

    protected int frameRate;
    protected AppSettings settings = new AppSettings(true);
    protected Timer timer;
    protected SystemListener listener;
    protected Renderer renderer;
    protected WebCanvasElement canvas;
    protected RenderManager renderManager;
    protected AssetManager assetManager;

    protected Material blitMat;
    protected Geometry blitGeom;
    
    protected FrameBuffer auxiliaryFrameBuffer;
    protected boolean auxiliaryFrameBufferRefreshed = false;
    protected boolean needAuxiliaryFrameBuffer = false;
    
    protected long timeThen;
    protected long timeLate;
    
    @Override
    public Type getType() {
        return Type.Display;
    }

    /**
     * Accesses the listener that receives events related to this context.
     *
     * @return the pre-existing instance
     */
    @Override
    public SystemListener getSystemListener() {
        return listener;
    }

    @Override
    public void setSystemListener(SystemListener listener) {
        this.listener = listener;
    }


    public void sync(int fps) {
        long timeNow;
        long gapTo;
        long savedTimeLate;

        gapTo = timer.getResolution() / fps + timeThen;
        timeNow = timer.getTime();
        savedTimeLate = timeLate;

        try {
            while (gapTo > timeNow + savedTimeLate) {
                Thread.sleep(1);
                timeNow = timer.getTime();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (gapTo < timeNow) {
            timeLate = timeNow - gapTo;
        } else {
            timeLate = 0;
        }

        timeThen = timeNow;
    }


    private void rebuildAuxiliaryFrameBufferIfNeeded(int width, int height) {
        if(!settings.isResizable()){
            width = settings.getWidth();
            height = settings.getHeight();
        }

        int samples = settings.getSamples() <= 0?1:settings.getSamples();
        boolean srgb = settings.isGammaCorrection();
        boolean hasDepth = settings.getDepthBits() > 0;
        boolean hasStencil = settings.getStencilBits() > 0;

        // check if we already have a framebuffer with the desired properties
        if(auxiliaryFrameBuffer!=null
            &&auxiliaryFrameBuffer.getWidth()==width
            &&auxiliaryFrameBuffer.getHeight()==height
        ){
            return;
        }
        
        System.out.println("Rebuild auxiliary framebuffer: "+width+"x"+height+" srgb="+srgb+" samples="+samples);
        FrameBuffer mainFrameBuffer = new FrameBuffer(width, height, samples);
        
        // color target
        // we use an f16 render target to avoid losing precision before the sRGB conversion
        Texture2D colorTex = new Texture2D(new Image(srgb?Format.RGBA16F:Format.RGBA8, width, height, null, ColorSpace.Linear));
        if(samples>1){
            colorTex.getImage().setMultiSamples(samples);
        }
        mainFrameBuffer.addColorTarget(FrameBufferTarget.newTarget(colorTex));


        // depth and stencil targets
        if (hasDepth || hasStencil) {
            if(hasStencil){
                mainFrameBuffer.setDepthTarget(FrameBufferTarget.newTarget(Format.Depth24Stencil8));
            }else{
                mainFrameBuffer.setDepthTarget(FrameBufferTarget.newTarget(Format.Depth));
            }
        }

        
        // cleanup the old framebuffer 
        if (this.auxiliaryFrameBuffer != null) 
            this.auxiliaryFrameBuffer.dispose();
        
        // set the new framebuffer
        this.auxiliaryFrameBuffer = mainFrameBuffer;
        auxiliaryFrameBufferRefreshed = true;
    }

 

    private void doInit() {
       
        
        timer = new NanoTimer();
        
        Window window=Window.current();
        Document doc = window.getDocument();

        logger.fine("Searching viable canvas...");
      
        WebCanvasElement canvas = (WebCanvasElement) doc.querySelector("canvas#jme");
        if (canvas == null) {
            logger.fine("Canvas not found, create a new one.");
            canvas = (WebCanvasElement) doc.createElement("canvas");
            canvas.setId("jme");
            doc.getElementsByTagName("body").get(0).appendChild(canvas);
        }

        canvas.canvasFitParent();

 
        this.canvas = canvas;
        
        if (settings.getWidth() == -1 || settings.getHeight() == -1) {
            canvas.setWidth(canvas.getClientWidth());
            canvas.setHeight(canvas.getClientHeight());
        } else {
            canvas.setWidth(settings.getWidth());
            canvas.setHeight(settings.getHeight());
        }
        
        

        setTitle(settings.getTitle());

        boolean hasAntialias = settings.getSamples() > 1;
        settings.setSamples(1);  // WebGL doesn't support MSAA

        
        this.needAuxiliaryFrameBuffer = settings.isGammaCorrection();
        
        WebGLOptions attrs =  WebGLOptions.create();
        String colorSpace="srgb";
        attrs.setColorSpace(colorSpace);
        attrs.setDrawingColorSpace(colorSpace);
        attrs.setPowerPreference("high-performance");
        attrs.setDepth(settings.getDepthBits()>0);
        attrs.setAlpha(true);
        attrs.setDesynchronized(true);
        attrs.setPremultipliedAlpha(false);
        attrs.setPreserveDrawingBuffer(true);
        attrs.setFailIfMajorPerformanceCaveat(false);
        attrs.setStencil(settings.getStencilBits()>0);
        attrs.setAntialias(hasAntialias);

 
        WebGLWrapper ctx = (WebGLWrapper) canvas.getContext("webgl2", attrs);
        if (ctx == null) {
            throw new RuntimeException("WebGL2 not supported");
        }
        ctx.pixelStorei(WebGLWrapper.UNPACK_COLORSPACE_CONVERSION_WEBGL, WebGLWrapper.NONE);

        logger.fine("Starting WebGL renderer...");


        WebGL gl = new WebGL(ctx);
        renderer = new GLRenderer(gl, gl, gl);

        renderer.initialize();

        gl.setCaps(renderer.getCaps());


        logger.fine("sRGB: "+settings.isGammaCorrection());
        renderer.setMainFrameBufferSrgb(settings.isGammaCorrection()); 
        renderer.setLinearizeSrgbImages(settings.isGammaCorrection());
            
        logger.fine("WebGL renderer started!");

        listener.initialize();
        logger.fine("WebGL created!");

    }

    private void doDestroy() {
          listener.destroy();
        timer = null;
   
        logger.fine("WebGL destroyed.");
    }

    @Override
    public  void onRenderManagerReady(RenderManager rm, AssetManager assetManager) {
        this.assetManager = assetManager;
        this.renderManager = rm;
    }


    private boolean loop() {
        try{
            if(needClose.get()){
                doDestroy();
                return false;
            }

            if (settings.isResizable()) {
                canvas.canvasFitParent();
            }
            
            int w = canvas.getClientWidth();
            int h = canvas.getClientHeight();
            
            if (settings.isResizable()) {
                if (w != settings.getWidth() || h != settings.getHeight()) {
                    settings.setResolution(w, h);        
                    listener.reshape(w, h);
                }
            }

            if (settings.isFullscreen()) {
                if (!canvas.isFullscreen()) {
                    canvas.requestFullscreen();
                }
            } else {
                if (canvas.isFullscreen()) {
                    canvas.exitFullscreen();
                }
                
            }



            if(needAuxiliaryFrameBuffer && w>2 && h>2){
                rebuildAuxiliaryFrameBufferIfNeeded(w, h);
    
                // we render on a auxiliary framebuffer
                // and then we blit it to the main framebuffer applying gamma correction
                // manually

                GLRenderer gl = (GLRenderer) renderer;
                
                gl.setMainFrameBufferOverride(auxiliaryFrameBuffer);
                listener.update();
                gl.setMainFrameBufferOverride(null);

                if(blitMat==null){
                    blitMat = new Material(assetManager, "Common/MatDefs/Post/WebGLBlit.j3md");
                    blitMat.getAdditionalRenderState().setDepthTest(false);
                    blitMat.getAdditionalRenderState().setDepthWrite(false);            
                    System.out.println("Rebuild blit material");
                }

                if(blitGeom==null){
                    blitGeom = new Picture("blit surface");
                    blitGeom.setMaterial(blitMat);        
                    System.out.println("Rebuild blit geometry");    
                }

                if(auxiliaryFrameBufferRefreshed){
                    blitMat.setTexture("Texture", (Texture2D) auxiliaryFrameBuffer.getColorTarget(0).getTexture());
                    if(auxiliaryFrameBuffer.getSamples()<=1){
                        blitMat.clearParam("NumSamples");    
                    } else {
                        blitMat.setInt("NumSamples", auxiliaryFrameBuffer.getSamples());
                    }
                    System.out.println("Update blit material");
                    auxiliaryFrameBufferRefreshed = false;
                }
                    

                gl.setFrameBuffer(null);
                blitGeom.updateGeometricState();
                renderManager.renderGeometry(blitGeom);
            } else {
                listener.update();
            }
           
         } catch(Throwable e){
            logger.log(Level.SEVERE, "Error in WebGL context: "+e.getMessage(), e);
            throw new RuntimeException(e);
        }       
      
        return true;
    }

 
    @Override
    public void run() {            
        doInit();  
        while(true){
            if(!loop())return;
            if (!settings.isVSync()) {
                sync(frameRate>0?frameRate:60);
            } else{
                vsync();
            }
        }             
    }

    @Async
    public static native void vsync();
    private static void vsync( AsyncCallback<Void> callback) {
        vsyncAsync(result -> callback.complete(result));
    }

    @JSBody(params = {"callback" }, script = "return window.requestAnimationFrame(function(t){ callback(null); });")
    public static native void vsyncAsync(JSConsumer<Void> callback);

    @Override
    public void destroy(boolean waitFor) {
        needClose.set(true);
     }

    @Override
    public void create(boolean waitFor) {
        if (created.get()) {
            logger.warning("create() called when WebGL context is already created!");
            return;
        }
        new Thread(this, THREAD_NAME).start();

    }

    @Override
    public void restart() {
    }

    @Override
    public void setAutoFlushFrames(boolean enabled) {
    }

    @Override
    public MouseInput getMouseInput() {
        return new WebMouseInput(canvas);
    }

    @Override
    public KeyInput getKeyInput() {
        return new WebKeyInput(canvas);
    }

    @Override
    public JoyInput getJoyInput() {
        return null;
    }

    @Override
    public TouchInput getTouchInput() {
        return new WebTouchInput(canvas, settings);
    }

    @Override
    public void setTitle(String title) {
        Window.current().setName(title);
        Window.current().getDocument().setTitle(title);        
    }

    public void create() {
        create(false);
    }

    public void destroy() {
        destroy(false);
    }

    protected void waitFor(boolean createdVal) {
        synchronized (createdLock) {
            while (created.get() != createdVal) {
                try {
                    createdLock.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    @Override
    public boolean isCreated() {
        return created.get();
    }

    @Override
    public void setSettings(AppSettings settings) {
        this.settings.copyFrom(settings);
        frameRate = settings.getFrameRate();
    }

    @Override
    public AppSettings getSettings() {
        return settings;
    }

    @Override
    public Renderer getRenderer() {
        return renderer;
    }

    @Override
    public Timer getTimer() {
        return timer;
    }

    @Override
    public boolean isRenderable() {
        return true;  
    }

    @Override
    public Context getOpenCLContext() {
        return null;
    }

    /**
     * Returns the height of the framebuffer.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public int getFramebufferHeight() {
        return auxiliaryFrameBuffer !=null ? auxiliaryFrameBuffer.getHeight() : canvas.getClientHeight();
    }

    /**
     * Returns the width of the framebuffer.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public int getFramebufferWidth() {
        return auxiliaryFrameBuffer !=null ? auxiliaryFrameBuffer.getWidth() : canvas.getClientWidth();
    }

    /**
     * Returns the screen X coordinate of the left edge of the content area.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public int getWindowXPosition() {
        return 0;
    }

    /**
     * Returns the screen Y coordinate of the top edge of the content area.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public int getWindowYPosition() {
        return 0;
    }

    @Override
    public Displays getDisplays() {
     
        Displays displayList = new Displays();
        
        long monitorI = 0;
        int monPos = displayList.addNewMonitor(monitorI);
        displayList.setPrimaryDisplay(monPos);
        int width = this.canvas.getWidth();
        int height = this.canvas.getHeight();
        int rate =  60; // TODO: set real rate
        displayList.setInfo(monPos, "Canvas", width, height, rate);

        return displayList;
    }

    @Override
    public int getPrimaryDisplay() {
        return 0;
    }
}
