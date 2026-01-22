package org.ngengine;

import org.ngengine.components.Component;
import org.ngengine.components.ComponentManager;

public class ComponentRef {
    private final ComponentManager mng;
    private final Component component;
    ComponentRef(ComponentManager mng, Component component){
        this.mng = mng;
        this.component = component;
    }
    public ComponentRef enable(){
        this.mng.enableComponent((Component)get());
        return this;
    }

    public ComponentRef disable(){
        this.mng.disableComponent((Component)get());
        return this;
    }
    public <T extends Component> T get(){
        return (T)this.component;
    }

    public boolean isEmpty(){
        return this.component==null;
    }   

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((component == null) ? 0 : component.hashCode());
        result = prime * result + ((mng == null) ? 0 : mng.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ComponentRef other = (ComponentRef) obj;
        if (component == null) {
            if (other.component != null) return false;
        } else if (!component.equals(other.component)) return false;
        if (mng == null) {
            if (other.mng != null) return false;
        } else if (!mng.equals(other.mng)) return false;
        return true;    
    }
}
