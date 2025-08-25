package org.ngengine.picker;

import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;

import jakarta.annotation.Nullable;

/**
 * Defines how the picking is done.
 */
public interface CameraPickerBehavior {
    /**
     * Selects one of the collision results.
     * @param rootNode the root node that was picked against
     * @param cam the camera that was used for picking
     * @param results the collision results
     * @return the selected collision result, or null if none is suitable
     */
    @Nullable
    public CollisionResult select(Collidable rootNode, Camera cam, CollisionResults results);

    /**
     * Tweaks the ray before it is used for picking.
     * @param rootNode the root node that will be picked against
     * @param cam the camera that is used for picking
     * @param ray the ray to tweak
     * @return the tweaked ray (usually the same instance as the input ray)
     */
    public Ray tweakRay(Collidable rootNode, Camera cam, Ray ray);
}