package org.ngengine.export;

import java.io.IOException;

import org.ngengine.nostr4j.keypair.NostrPublicKey;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;

public class NostrPublicKeySavableWrapper implements SavableWrapper<NostrPublicKey>{
    private NostrPublicKey key;
    protected NostrPublicKeySavableWrapper(){

    }

    public NostrPublicKeySavableWrapper(NostrPublicKey key){
        this.key = key;
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule out = ex.getCapsule(this);
        out.write( key !=null ? key.asBech32() : null ,"key",  null);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule in = im.getCapsule(this);
        String keyStr = in.readString("key", null);
        if(keyStr!=null){
            key = NostrPublicKey.fromBech32(keyStr);
        }
    }

    @Override
    public NostrPublicKey get() {
        return key;
    }
    
}
