package org.ngengine.export;

import com.jme3.export.Savable;

public interface SavableWrapper<T> extends Savable{
    public T get();
}
