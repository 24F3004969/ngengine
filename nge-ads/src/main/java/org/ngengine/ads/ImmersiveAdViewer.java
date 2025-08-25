package org.ngengine.ads;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public interface ImmersiveAdViewer {
 


    void showInfo(ImmersiveAdSpace space, String description,String callToAction);
    
    void beginSelection();
    void select(ImmersiveAdSpace space);
    ImmersiveAdSpace endSelection();

    void beginUpdate();
    boolean isVisible(ImmersiveAdSpace space);
    void endUpdate();

}
