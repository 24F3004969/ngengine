package org.ngengine.export;

import java.io.IOException;
import java.util.Map;

import org.ngengine.nostr4j.NostrRelay;
import org.ngengine.nostr4j.NostrRelayInfo;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;

public class NostrRelayInfoSavableWrapper implements SavableWrapper<NostrRelayInfo>{ 
    private NostrRelayInfo info;
    protected NostrRelayInfoSavableWrapper(){

    }

    public NostrRelayInfoSavableWrapper(NostrRelayInfo info){
        this.info = info;
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule out = ex.getCapsule(this);
        out.write( info !=null ? info.getRelayUrl() : null ,"url",  null);
        if(info!=null){
            ExportUtils.writeMap(info.get(), out);
        }
    
    }


    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule in = im.getCapsule(this);
        String url = in.readString("url", null);
        if(url!=null){
            Map<String, Object> map = ExportUtils.readMap(in);
            info = new NostrRelayInfo(url, map);
        }
    }


    public NostrRelayInfo get() {
        return info;
    }


    
}
