package org.ngengine.export;

import java.util.HashMap;
import java.util.Map;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;

public class MapSavableWrapper<T> implements SavableWrapper<Map<String, T>> {
    private Map<String, T> map;
    
    public MapSavableWrapper() {
        this.map = new HashMap<>();
    }
    
    public MapSavableWrapper(Map<String, T> map) {
        this.map = map;
    }
    
    @Override
    public Map<String, T> get() {
        return map;
    }

    @Override
    public void write(JmeExporter ex) throws java.io.IOException {
        OutputCapsule out = ex.getCapsule(this);
        ExportUtils.writeMap(map, out);
    }

    @Override
    public void read(JmeImporter im) throws java.io.IOException {
        InputCapsule in = im.getCapsule(this);
        map = ExportUtils.readMap(in);
    }
    
}
