package org.ngengine.web.patches;

 
public abstract class FieldPatch {
    public abstract Object get(Object obj)
        throws IllegalArgumentException, IllegalAccessException;


    public int getInt(Object obj)
        throws IllegalArgumentException, IllegalAccessException {
        Number n = (Number)get(obj);
        return n.intValue();
    }
}
