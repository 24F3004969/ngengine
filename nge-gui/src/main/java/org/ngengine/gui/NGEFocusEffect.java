package org.ngengine.gui;

import java.util.WeakHashMap;

import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.anim.Animation;
import com.simsilica.lemur.effect.AbstractEffect;
import com.simsilica.lemur.effect.EffectInfo;
import com.simsilica.lemur.input.InputMapper.ViewState;

public class NGEFocusEffect extends AbstractEffect<Panel> {
    private final boolean onFocus;
    private final static ThreadLocal<WeakHashMap<Panel,State>> states = ThreadLocal.withInitial(WeakHashMap::new);
    private final Material highlightMat;
    private final static class State{
        Geometry overlay;
        boolean removed = false;
    }

    protected State getState(Panel target){
        WeakHashMap<Panel,State> map = states.get();
        return map.compute(target,(k,v)->{
            if(v==null||v.removed){
                v = new State();
            }
            return v;
        });
    }

    protected void clearState(State state){
        if(state.overlay != null) {
            state.overlay.removeFromParent();
            state.removed = true;
            state.overlay = null;
        }
    }

    public NGEFocusEffect(boolean onFocus, Material highlightMat){ 
        super("focus");
        this.onFocus = onFocus;
        this.highlightMat = highlightMat;
    }   

    @Override
    public Animation create(Panel target, EffectInfo existing) {
        return new Animation(){
     
            @Override
            public boolean animate(double tpf) {
                if(onFocus){
                    Node overlay = getOverlay(target);
                    if(overlay==null) return true;
                    State state = getState(target);
                    update(state, overlay, target);
                } else {
                    State state = getState(target);
                    clearState(state);
                }
                return true;
            }

            @Override
            public void cancel() {
              
            }
        };
    }

    protected Node getOverlay(Spatial target){
        ViewState vs = GuiGlobals.getInstance().getInputMapper().get(target);
        if (vs==null) return null;
        ViewPort vp = vs.getViewPort();
        Node overlayScene = (Node) vp.getScenes().get(0);
        return overlayScene;
    }


    protected void update(State state, Node overlayScene, Spatial target){
 

        if(state.overlay == null){
            Quad quad = new Quad(1,1);
            Geometry geo = new Geometry("focusOverlay", quad);
            geo.setMaterial(highlightMat);
            overlayScene.attachChild(geo);
            state.overlay = geo;            
        }           
        
        if(state.overlay!=null){
            BoundingBox box = (BoundingBox) target.getWorldBound();
            state.overlay.setLocalScale(box.getXExtent()*2, box.getYExtent()*2, 1);                
            state.overlay.setLocalTranslation(
                box.getCenter().x - box.getXExtent(),
                box.getCenter().y - box.getYExtent(),
                box.getCenter().z + 0.1f
            );
        }
        
    }



  
}
