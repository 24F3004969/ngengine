package org.ngengine.components.jme3;

import java.util.Collections;
import java.util.List;

import org.ngengine.ViewPortManager;

import com.jme3.app.Application;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;

public class Jme3ViewPortManager implements ViewPortManager{
    private final Application app;
    private final List<ViewPort> sceneViewPortsRO;
    
    public Jme3ViewPortManager(Application app) {
        this.app = app;
        this.sceneViewPortsRO = Collections.unmodifiableList(
            app.getRenderManager().getMainViews()
        );
    }

    @Override
    public ViewPort getMainSceneViewPort() {
        return app.getViewPort();
    }

    @Override
    public ViewPort getGuiViewPort() {
        return app.getGuiViewPort();
    }

    @Override
    public List<ViewPort> getSceneViewPorts() {
        return sceneViewPortsRO;
    }

    @Override
    public ViewPort createNewSceneViewPort(String name, Camera cam) {
        return app.getRenderManager().createMainView(name, cam);
    }

    @Override
    public FilterPostProcessor getFilterPostProcessor(ViewPort vp) {       
        FilterPostProcessor fpp = Utils.getFilterPostProcessor(
                app.getContext().getSettings(),
                app.getAssetManager(),
                app.getViewPort()
        );
        return fpp;
    }
    
}
