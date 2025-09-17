package org.ngengine.export;

import java.io.IOException;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;

public class StringSavableWrapper implements SavableWrapper<String>{
    private String str;
    protected StringSavableWrapper(){

    }

    public StringSavableWrapper(String str){
        this.str = str;
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule out = ex.getCapsule(this);
        out.write( str ,"str",  null);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule in = im.getCapsule(this);
        str = in.readString("str", null);
    }

    @Override
    public String get() {
        return str;
    }
    
}
