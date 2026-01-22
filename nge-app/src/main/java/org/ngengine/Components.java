package org.ngengine;

import org.ngengine.components.Component;
import org.ngengine.components.ComponentManager;
import org.ngengine.components.ComponentManagerProvider;
import org.ngengine.components.ComponentManager.ComponentDependency;
import org.ngengine.components.jme3.ComponentManagerControl;

import com.jme3.scene.Spatial;

public class Components {
    private static final ComponentRef EMPTY = new ComponentRef(null, null);
    public static ComponentDependency[] dependencies(Object... deps){
        if(deps==null) return null;
        ComponentDependency[] arr = new ComponentDependency[deps.length];
        for(int i=0; i<deps.length; i++){
            if(deps[i] instanceof ComponentDependency){
                arr[i] = (ComponentDependency)deps[i];
            }else{
                arr[i] = new ComponentDependency(deps[i]);
            }
        }
        return arr;
    }

    public static ComponentRef mount(Spatial sp, Component component, Object... dependencies){
        dependencies = dependencies(dependencies);
        ComponentManagerControl c = sp.getControl(ComponentManagerControl.class);
        if(c==null){
            c = new ComponentManagerControl();
            sp.addControl(c);
        }
        c.addComponent(component, dependencies);
        return new ComponentRef(c, component);
    }

    public static ComponentRef mount(NGEApplication app, Component component, Object... dependencies){
        dependencies = dependencies(dependencies);
        ComponentManager c = app.getComponentManager();
        c.addComponent(component, dependencies);
        return new ComponentRef(c, component);
    }

    public static ComponentRef mount(ComponentManager cm, Component component, Object... dependencies){
        dependencies = dependencies(dependencies);
        cm.addComponent(component, dependencies);
        return new ComponentRef(cm, component);
    }

    public static ComponentRef mount(ComponentManagerProvider cmp, Component component, Object... dependencies){
        dependencies = dependencies(dependencies);
        ComponentManager cm = cmp.getComponentManager();
        cm.addComponent(component, dependencies);
        return new ComponentRef(cm, component);
    }

    
    public static void unmount(Spatial sp, ComponentRef cref){
        unmount(sp, (Component)cref.get());
    }

    public static void unmount(NGEApplication app, ComponentRef cref){
        unmount(app, (Component)cref.get());
    }

    public static void unmount(ComponentManager cm, ComponentRef cref){
        unmount(cm, (Component)cref.get());
    }

    public static void unmount(ComponentManagerProvider cmp, ComponentRef cref){
        unmount(cmp.getComponentManager(), (Component)cref.get());
    }

    public static void unmount(Spatial sp, Component component){
        ComponentManagerControl c = sp.getControl(ComponentManagerControl.class);
        if(c!=null){
            c.removeComponent(component);
        }
    }

    public static void unmount(NGEApplication app, Component component){
        ComponentManager c = app.getComponentManager();
        c.removeComponent(component);
    }

    public static void unmount(ComponentManager cm, Component component){
        cm.removeComponent(component);
    }

    public static void unmount(ComponentManagerProvider cmp, Component component){
        ComponentManager cm = cmp.getComponentManager();
        cm.removeComponent(component);
    }

    public static boolean has(Spatial sp, Class<? extends Component> cls) {
        ComponentManagerControl c = sp.getControl(ComponentManagerControl.class);
        if(c!=null){
            return c.hasComponent(cls);
        }
        return false;
    }

    public static boolean has(Spatial sp, Component cc) {
        ComponentManagerControl c = sp.getControl(ComponentManagerControl.class);
        if(c!=null){
            return c.hasComponent(cc);
        }
        return false;
    }

    public static boolean has(Spatial sp, String id) {
        ComponentManagerControl c = sp.getControl(ComponentManagerControl.class);
        if(c!=null){
            return c.hasComponent(id);
        }
        return false;
    }


    public static boolean has(NGEApplication app, Class<? extends Component> cls) {
        ComponentManager c = app.getComponentManager();
        if(c!=null){
            return c.hasComponent(cls);
        }
        return false;
    }

    public static boolean has(NGEApplication app, Component cc) {
        ComponentManager c = app.getComponentManager();
        if(c!=null){
            return c.hasComponent(cc);
        }
        return false;
    }


    public static boolean has(NGEApplication app, String id) {
        ComponentManager c = app.getComponentManager();
        if(c!=null){
            return c.hasComponent(id);
        }
        return false;
    }

    public static boolean has(ComponentManager cm, Class<? extends Component> cls) {
        return cm.hasComponent(cls);
    }

    public static boolean has(ComponentManager cm, Component cc) {
        return cm.hasComponent(cc);
    }

    public static boolean has(ComponentManager cm, String id) {
        return cm.hasComponent(id);
    }

    public static boolean has(ComponentManagerProvider cmp, Class<? extends Component> cls) {
        ComponentManager cm = cmp.getComponentManager();
        return cm.hasComponent(cls);
    }

    public static boolean has(ComponentManagerProvider cmp, Component cc) {
        ComponentManager cm = cmp.getComponentManager();
        return cm.hasComponent(cc);
    }

    public static boolean has(ComponentManagerProvider cmp, String id) {
        ComponentManager cm = cmp.getComponentManager();
        return cm.hasComponent(id);
    }

    

    public static ComponentRef get(Spatial sp, Class<? extends Component> cls) {
        ComponentManagerControl c = sp.getControl(ComponentManagerControl.class);
        if(c!=null){
            Component comp = c.getComponent(cls);
            if(comp!=null){
                return new ComponentRef(c, comp);
            }
        }
        return EMPTY;
    }

    public static ComponentRef get(NGEApplication app, Class<? extends Component> cls) {
        ComponentManager c = app.getComponentManager();
        if(c!=null){
            Component comp = c.getComponent(cls);
            if(comp!=null){
                return new ComponentRef(c, comp);
            }
        }
        return EMPTY;
    }

    public static ComponentRef get(Spatial sp, String id) {
        ComponentManagerControl c = sp.getControl(ComponentManagerControl.class);
        if(c!=null){
            Component comp = (Component)c.getComponentById(id);
            if(comp!=null){
                return new ComponentRef(c, comp);
            }
        }
        return EMPTY;
    }

    public static ComponentRef get(NGEApplication app, String id) {
        ComponentManager c = app.getComponentManager();
        if(c!=null){
            Component comp = (Component)c.getComponentById(id);
            if(comp!=null){
                return new ComponentRef(c, comp);
            }
        }
        return EMPTY;
    } 

    public static ComponentRef get(ComponentManager cm, String id) {
        Component comp = (Component)cm.getComponentById(id);
        if(comp!=null){
            return new ComponentRef(cm, comp);
        }
        return EMPTY;
    }

    public static ComponentRef get(ComponentManager cm, Class<? extends Component> cls) {
        Component comp = cm.getComponent(cls);
        if(comp!=null){
            return new ComponentRef(cm, comp);
        }
        return EMPTY;
    }

    public static ComponentRef get(ComponentManager cm, Component component) {
        if(cm.hasComponent(component)){
            return new ComponentRef(cm, component);
        }
        return EMPTY;
    }

    public static ComponentRef get(ComponentManagerProvider cmp, String id) {
        ComponentManager cm = cmp.getComponentManager();
        Component comp = (Component)cm.getComponentById(id);
        if(comp!=null){
            return new ComponentRef(cm, comp);
        }
        return EMPTY;
    }

    public static ComponentRef get(ComponentManagerProvider cmp, Class<? extends Component> cls) {
        ComponentManager cm = cmp.getComponentManager();
        Component comp = cm.getComponent(cls);
        if(comp!=null){
            return new ComponentRef(cm, comp);
        }
        return EMPTY;
    }

    public static ComponentRef get(ComponentManagerProvider cmp, Component component) {
        ComponentManager cm = cmp.getComponentManager();
        if(cm.hasComponent(component)){
            return new ComponentRef(cm, component);
        }
        return EMPTY;
    }



    // 

    public static ComponentRef getRecursive(Spatial sp, Class<? extends Component> cls) {
        ComponentManager c = sp.getControl(ComponentManagerControl.class);
        while(c!=null){
            Component comp = c.getComponent(cls);
            if(comp!=null){
                return new ComponentRef(c, comp);
            } 
            c = c.getParent();
        }
        return EMPTY;
    }

    public static ComponentRef getRecursive(NGEApplication app, Class<? extends Component> cls) {
        ComponentManager c = app.getComponentManager();
        while(c!=null){
            Component comp = c.getComponent(cls);
            if(comp!=null){
                return new ComponentRef(c, comp);
            }
            c = c.getParent();
        }
        return EMPTY;
    }

    public static ComponentRef getRecursive(Spatial sp, String id) {
        ComponentManager c = sp.getControl(ComponentManagerControl.class);
        while(c!=null){
            Component comp = (Component)c.getComponentById(id);
            if(comp!=null){
                return new ComponentRef(c, comp);
            }
            c = c.getParent();
        }
        return EMPTY;
    }

    public static ComponentRef getRecursive(NGEApplication app, String id) {
        ComponentManager c = app.getComponentManager();
        while(c!=null){
            Component comp = (Component)c.getComponentById(id);
            if(comp!=null){
                return new ComponentRef(c, comp);
            }
            c = c.getParent();
        }
        return EMPTY;
    } 

    public static ComponentRef getRecursive(ComponentManager cm, String id) {
        while(cm!=null){
            Component comp = (Component)cm.getComponentById(id);
            if(comp!=null){
                return new ComponentRef(cm, comp);
            }
            cm = cm.getParent();
        }
        return EMPTY;
    }

    public static ComponentRef getRecursive(ComponentManager cm, Class<? extends Component> cls) {
        while(cm!=null){
            Component comp = cm.getComponent(cls);
            if(comp!=null){
                return new ComponentRef(cm, comp);
            }
            cm = cm.getParent();
        }
        return EMPTY;
    }

    public static ComponentRef getRecursive(ComponentManager cm, Component component) {
        while(cm!=null){
            if(cm.hasComponent(component)){
                return new ComponentRef(cm, component);
            }
            cm = cm.getParent();
        }
        return EMPTY;
    }

    public static ComponentRef getRecursive(ComponentManagerProvider cmp, String id) {
        ComponentManager cm = cmp.getComponentManager();
        while(cm!=null){
            Component comp = (Component)cm.getComponentById(id);
            if(comp!=null){
                return new ComponentRef(cm, comp);
            }
            cm = cm.getParent();
        }
        return EMPTY;
    }

    public static ComponentRef getRecursive(ComponentManagerProvider cmp, Class<? extends Component> cls) {
        ComponentManager cm = cmp.getComponentManager();
        while(cm!=null){
            Component comp = cm.getComponent(cls);
            if(comp!=null){
                return new ComponentRef(cm, comp);
            }
            cm = cm.getParent();
        }
        return EMPTY;
    }

    public static ComponentRef getRecursive(ComponentManagerProvider cmp, Component component) {
        ComponentManager cm = cmp.getComponentManager();
        while(cm!=null){
            if(cm.hasComponent(component)){
                return new ComponentRef(cm, component);
            }
            cm = cm.getParent();
        }
        return EMPTY;
    }
    

    
}
