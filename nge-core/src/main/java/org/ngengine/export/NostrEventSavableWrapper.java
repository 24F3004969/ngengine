package org.ngengine.export;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

import org.ngengine.nostr4j.event.NostrEvent;
import org.ngengine.nostr4j.event.SignedNostrEvent;
import org.ngengine.nostr4j.event.UnsignedNostrEvent;
import org.ngengine.nostr4j.event.NostrEvent.TagValue;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;

public class NostrEventSavableWrapper implements SavableWrapper<NostrEvent> {
    private static final Logger log = Logger.getLogger(NostrEventSavableWrapper.class.getName());
    private NostrEvent event;

    protected NostrEventSavableWrapper(){

    }

    public NostrEventSavableWrapper(NostrEvent event){
        this.event = event;
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule out = ex.getCapsule(this);
        if(event instanceof UnsignedNostrEvent){
            out.write((byte)1, "type", (byte)-1);
            UnsignedNostrEvent uevent = (UnsignedNostrEvent) event;
            out.write(uevent.getKind(), "kind", 0);
            out.write(uevent.getContent(), "content", null);            
            Collection<String> tagKeys = uevent.listTagKeys();
            out.write(tagKeys.toArray(new String[0]), "tagKeys", null);
            for(String k : tagKeys){
                Collection<TagValue> v = uevent.getTag(k);
                out.write(v.size(), "tagValCount_"+k, 0);
                for(TagValue tv : v){
                    out.write(tv.getAll().toArray(new String[0]), "tagValue_"+k, null);
                }
            }
        } else if (event instanceof SignedNostrEvent){
            out.write((byte)2, "type", (byte)-1);
            SignedNostrEvent sevent = (SignedNostrEvent) event;
            Map<String, Object> map = sevent.toMap();
            ExportUtils.writeMap(map, out);
        } else {
            throw new IOException("Unknown NostrEvent type: "+ event.getClass().getName());
        }

    }

    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule in = im.getCapsule(this);
        int type = in.readByte("type", (byte)-1);
        if(type==1){            
            UnsignedNostrEvent uevent = new UnsignedNostrEvent();
            uevent.withKind( in.readInt("kind", 0) );
            uevent.withContent( in.readString("content", null) );
            String[] tagKeys = in.readStringArray("tagKeys", null);
            if(tagKeys!=null){
                for(String k : tagKeys){
                    int tagValCount = in.readInt("tagValCount_"+k, 0);
                    for(int i=0;i<tagValCount;i++){
                        String[] tagValue = in.readStringArray("tagValue_"+k, null);
                        if(tagValue!=null){
                            uevent.withTag(k, tagValue);
                        }
                    }
                }
            }
        } else if(type==2){
            Map<String, Object> map = ExportUtils.readMap(in);
            event = new SignedNostrEvent(map);
        } else {
            throw new IOException("Unknown NostrEvent type: "+ type);
        }
    
    }

    @Override
    public NostrEvent get() {
        return event;
    }
    
}
