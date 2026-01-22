package org.ngengine.components.runners;

import org.ngengine.components.Component;
import org.ngengine.components.ComponentManager;

public interface ComponentRunner {
    
    default void finalize(ComponentManager componentManager, Component component){}
}
