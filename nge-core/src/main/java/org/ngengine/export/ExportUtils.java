package org.ngengine.export;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.scene.UserData;

@SuppressWarnings("unchecked")
public class ExportUtils {

 

    public static void writeMap(Map<String,?> map, OutputCapsule out) throws IOException {
        UserData data = new UserData(UserData.getObjectType(map), map);
        out.write(data, "data", null);        
    }

    public static <T> Map<String,T> readMap(InputCapsule in) throws IOException {
        UserData data = (UserData) in.readSavable("data", null);
        if(data == null){
            return null;
        }
        return (Map<String,T>) data.getValue();
    }

    public static void writeCollection(Collection<Object> col, OutputCapsule out) throws IOException {
        ArrayList<Object> s = new ArrayList<>(col);
        UserData data = new UserData(UserData.getObjectType(s), s);
        out.write(data, "data", null);        
    }

    public static Collection<Object> readCollection(InputCapsule in) throws IOException {
        UserData data = (UserData) in.readSavable("data", null);
        if(data == null){
            return null;
        }
        return (Collection<Object>) data.getValue();
    }

    public static void writeArray(Object[] arr, OutputCapsule out) throws IOException {
        ArrayList<Object> s = new ArrayList<>();
        for(Object o : arr){
            s.add(o);
        }
        UserData data = new UserData(UserData.getObjectType(s), s);
        out.write(data, "data", null);        
    }

    public static Object[] readArray(InputCapsule in) throws IOException {
        UserData data = (UserData) in.readSavable("data", null);
        if(data == null){
            return null;
        }
        Collection<Object> col = (Collection<Object>) data.getValue();
        return col.toArray(new Object[0]);
    }
    
}
