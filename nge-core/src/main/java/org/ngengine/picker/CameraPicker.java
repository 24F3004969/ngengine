package org.ngengine.picker;



import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;

import jakarta.annotation.Nullable;

public class CameraPicker {
    private static final ThreadLocal<CollisionResults> resultsTl = ThreadLocal.withInitial(CollisionResults::new);
    private static final ThreadLocal<Ray> rayTl = ThreadLocal.withInitial(Ray::new);
    public static final CameraPickerBehavior CLOSER_VISIBLE_PICKER = new CloserVisibleCameraPickerBehavior();
        
    /**
     * Picks the closest visible object in the scene using the {@link #CLOSER_VISIBLE_PICKER} behavior.
     * @param rootNode the root node to pick against
     * @param cam the camera to pick from
     * @return the collision result, or null if nothing was picked
     */
    @Nullable
    public static CollisionResult pick(Collidable rootNode, Camera cam){
        return pick(rootNode, cam, CLOSER_VISIBLE_PICKER);
    }

    /**
     * Picks an object in the scene using the specified picker behavior.
     * @param rootNode the root node to pick against
     * @param cam the camera to pick from
     * @param picker the picker behavior to use
     * @return the collision result, or null if nothing was picked
     */     
    @Nullable
    public static CollisionResult pick(Collidable rootNode, Camera cam, CameraPickerBehavior picker){
        CollisionResults results = resultsTl.get();
        results.clear();
        Ray ray = rayTl.get();
        ray.setOrigin(cam.getLocation());
        ray.setDirection(cam.getDirection());
        ray =  picker.tweakRay(rootNode, cam, ray);
        rootNode.collideWith(ray, results);
        CollisionResult result = picker.select(rootNode, cam, results);
        return result;
    }


    
}
