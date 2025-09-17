package org.ngengine.export;

import java.io.IOException;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;

public class NumberSavableWrapper implements SavableWrapper<Number>{
    private Number number;
    
    protected NumberSavableWrapper(){

    }

    public NumberSavableWrapper(Number number){
        this.number = number;
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule out = ex.getCapsule(this);
        if(number instanceof Integer){
            out.write(0,"t", 0);
            out.write(number.intValue(), "v", 0);
        } else if (number instanceof Long){
            out.write(1,"t", 0);
            out.write(number.longValue(), "v", 0L);
        } else if (number instanceof Float){
            out.write(2,"t", 0);
            out.write(number.floatValue(), "v", 0f);
        } else if (number instanceof Short){
            out.write(3,"t", 0);
            out.write(number.shortValue(), "v", (short)0);
        } else if (number instanceof Byte){
            out.write(4,"t", 0);
            out.write(number.byteValue(), "v", (byte)0);
        } else {
            out.write(4,"t", 0);
            out.write(number.doubleValue(), "v", 0d);
        }
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule in = im.getCapsule(this);
        int t = in.readInt("t", 0);
        switch(t){
            case 0:
                number = Integer.valueOf(in.readInt("v", 0));
                break;
            case 1:
                number = Long.valueOf(in.readLong("v", 0L));
                break;
            case 2:
                number = Float.valueOf(in.readFloat("v", 0f));
                break;
            case 3:
                number = Short.valueOf(in.readShort("v", (short)0));
                break;
            case 4:
                number = Byte.valueOf(in.readByte("v", (byte)0));
                break;
            case 5:
            default:
                number = Double.valueOf(in.readDouble("v", 0d));
                break;  
        }
    }

    @Override
    public Number get() {
        return number;
    }
    
}
