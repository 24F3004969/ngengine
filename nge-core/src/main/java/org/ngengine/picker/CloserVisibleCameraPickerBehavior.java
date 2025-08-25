package org.ngengine.picker;

import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;

/**
 * A picker behavior that selects the closest visible object.
 */
public class CloserVisibleCameraPickerBehavior implements CameraPickerBehavior {

   


    @Override
    public CollisionResult select(Collidable rootNode, Camera cam, CollisionResults results) {
        for(int i=0; i<results.size(); i++){
            CollisionResult r = results.getCollision(i);
            if(r.getDistance()<=0) continue;
            Geometry geo = r.getGeometry();
            if(geo!=null&&geo.getCullHint()==CullHint.Always) continue;
            return r;
        }
        return null;
    }

    @Override
    public Ray tweakRay(Collidable rootNode, Camera cam, Ray ray) {
        ray.setLimit(cam.getFrustumFar());
        return ray;
    }
}