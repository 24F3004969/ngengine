package org.ngengine;

import java.util.List;


import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;

/**
 * Get and manage ViewPorts in the application.
 */
public interface ViewPortManager {
    /**
     * Get the main scene ViewPort.
     * @return the main scene ViewPort
     */
    public ViewPort getMainSceneViewPort();

    /**
     * Get the GUI ViewPort.
     * @return the GUI ViewPort
     */
    public ViewPort getGuiViewPort();

    /**
     * Get all scene ViewPorts (read-only).
     * @return the list of scene ViewPorts
     */
    public List<ViewPort> getSceneViewPorts();
    
    /**
     * Create a new scene ViewPort with the given name and camera.
     * @param name the name of the ViewPort
     * @param cam the camera to use
     * @return the created ViewPort
     */
    public ViewPort createNewSceneViewPort(String name, Camera cam);
    

    /**
     * Get and create if needed a FilterPostProcessor for the given ViewPort.
     * @param vp the ViewPort
     * @return the FilterPostProcessor
     */
    public  FilterPostProcessor getFilterPostProcessor(ViewPort vp);

    /**
     * Get the first scene node of the ViewPort. This is usually the root node of the scene graph.
     *
     * @param vp
     *            the ViewPort instance
     * @return the root node of the scene graph
     * @see ViewPort#getScenes()
     */
    public default Node getRootNode(ViewPort vp){
        return (Node) vp.getScenes().get(0);
    }

}
