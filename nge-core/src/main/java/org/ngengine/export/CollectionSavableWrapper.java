package org.ngengine.export;

import java.util.Collection;

    @SuppressWarnings("unchecked")

public class CollectionSavableWrapper<T> implements SavableWrapper<Collection<T>> {
    private Collection<T> collection;
    
    public CollectionSavableWrapper() {
        this.collection = null;
    }
    
    public CollectionSavableWrapper(Collection<T> collection) {
        this.collection = collection;
    }
    
    @Override
    public Collection<T> get() {
        return collection;
    }

    @Override
    public void write(com.jme3.export.JmeExporter ex) throws java.io.IOException {
        com.jme3.export.OutputCapsule out = ex.getCapsule(this);
        ExportUtils.writeCollection((Collection<Object>)(Object)collection, out);
    }

    @Override
    public void read(com.jme3.export.JmeImporter im) throws java.io.IOException {
        com.jme3.export.InputCapsule in = im.getCapsule(this);
        collection = (Collection<T>)(Object)ExportUtils.readCollection(in);
    }
 
     
    
}
