package org.ngengine.ads;

import java.time.Duration;
import org.ngengine.components.ComponentManager;
import org.ngengine.gui.win.NWindowManagerComponent;
import org.ngengine.gui.win.NToast;
import org.ngengine.gui.win.NToast.ToastType;
import org.ngengine.picker.CameraPicker;
import org.ngengine.picker.CameraPickerBehavior;
import org.ngengine.picker.CloserVisibleCameraPickerBehavior;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResult;
import com.jme3.math.Ray;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Camera.FrustumIntersect;

public class ImmersiveAdCameraView implements ImmersiveAdViewer  {

    private final Camera cam;
    private float maxDistance = 2100.0f;
    private ComponentManager mng;    
    private NToast currentToast;
    private CameraPickerBehavior pickerBehavior;
    private ImmersiveAdSpace lastSpace;
    private String calloutPrefix = "Press action to";

    public ImmersiveAdCameraView(ComponentManager mng, Camera cam){
        this.cam = cam;    
        this.mng = mng;
    }

    public void setMaxDistance(float maxDistance) {
        this.maxDistance = maxDistance;
        this.pickerBehavior = null;
    }

    public void setCalloutPrefix(String calloutPrefix) {
        this.calloutPrefix = calloutPrefix;
    }


    @Override
    public boolean isVisible(ImmersiveAdSpace space){
        FrustumIntersect frustumState = cam.contains(space.getBounds());
        if(frustumState==FrustumIntersect.Outside) return false;
        return true;              
    }


    @Override
    public void showInfo(ImmersiveAdSpace space, String description, String callToAction) {
        NWindowManagerComponent windowManager = mng.getComponent(NWindowManagerComponent.class);
        if (windowManager != null  && lastSpace!=space) {
            if (currentToast != null) currentToast.removeFromParent();
            if(space!=null){
                windowManager.showToast(ToastType.INFO,
                    description + "\n    " + calloutPrefix + " " + callToAction, Duration.ofMinutes(1),
                    (toast, err) -> {
                        toast.addActionListener(id->{
                            space.openLink();
                        });
                        currentToast = toast;
                    });
            }
            
        }
        lastSpace = space;        
    }

    @Override
    public void beginUpdate() {

    }

    @Override
    public void endUpdate() {
     
    }


    private ImmersiveAdSpace bestSpace;
    private float bestDistance = Float.MAX_VALUE;


    @Override
    public void beginSelection() {
        bestSpace = null;
        bestDistance = Float.MAX_VALUE;
        
    }


    

    @Override
    public void select(ImmersiveAdSpace space) {
        if(pickerBehavior==null){
            pickerBehavior = new CloserVisibleCameraPickerBehavior(){
                @Override
                public Ray tweakRay(Collidable rootNode, Camera cam, Ray ray) {
                    ray.setLimit(maxDistance);
                    return ray;
                }
            };
        }

        CollisionResult res = CameraPicker.pick(space.getBounds(), cam, pickerBehavior);
        if(res!=null&&(bestSpace==null||res.getDistance()<bestDistance)){
            bestDistance = res.getDistance();
            bestSpace = space;
        }
    }

    @Override
    public ImmersiveAdSpace endSelection() {
        return bestSpace;
    }

  
}
