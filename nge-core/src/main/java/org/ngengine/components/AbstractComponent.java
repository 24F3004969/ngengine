package org.ngengine.components;


import org.ngengine.runner.Runner;
import org.ngengine.store.DataStoreProvider;

import com.jme3.util.clone.Cloner;

public abstract class AbstractComponent implements Component, ComponentManagerProvider {
    private ComponentManager mng;
    
    
    public AbstractComponent(){
        
    }

    @Override
    public final void onAttached(ComponentManager mng, Runner runner, DataStoreProvider dataStore) {
        this.mng = mng;
        onAttached();
    }

    public final void onDetached(ComponentManager mng, Runner runner, DataStoreProvider dataStore) {
        this.mng = mng;
        onDetached();
    }


    protected void onDetached(){
        
    }

    protected void onAttached(){
        
    } 

    @Override
    public final void onEnable(ComponentManager mng, Runner runner, DataStoreProvider dataStore,
            boolean firstTime) {
        this.mng = mng;
        onEnable(mng, firstTime);
    }

    @Override
    public final void onDisable(ComponentManager mng, Runner runner, DataStoreProvider dataStore) {
        this.mng = mng;
        onDisable(mng);
    }

    protected abstract void onEnable(ComponentManager mng, boolean firstTime);
    protected abstract void onDisable(ComponentManager mng);

    @Override
    public Component newInstance() {
        // try {
        //     return (Component) this.clone();
        // } catch (CloneNotSupportedException e) {
        //     try {
        //         return getClass().getDeclaredConstructor().newInstance();
        //     } catch ( Exception e1) {
        //         throw new RuntimeException("Cannot create new instance of component "+this.getClass().getName(), e1);
        //     }
        // }        
        Cloner cloner = new Cloner();
        return cloner.clone(this);
    }

    @Override
    public final ComponentManager getComponentManager() {
        return mng;
    }

  
 
}
