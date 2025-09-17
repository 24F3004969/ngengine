package org.ngengine.export;

import java.io.IOException;

import org.ngengine.nostr4j.keypair.NostrKeyPair;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;

public class NostrKeyPairSavableWrapper implements SavableWrapper<NostrKeyPair>{
    private NostrKeyPair keyPair;
    
    protected NostrKeyPairSavableWrapper(){
    }
    
    public NostrKeyPairSavableWrapper(NostrKeyPair keyPair){
        this.keyPair = keyPair;
    }



    @Override
    public NostrKeyPair get() {
        return keyPair;
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule out = ex.getCapsule(this);
        out.write( keyPair !=null ? new NostrPrivateKeySavableWrapper(keyPair.getPrivateKey()) : null ,"privateKey",  null);
        out.write( keyPair !=null ? new NostrPublicKeySavableWrapper(keyPair.getPublicKey()) : null ,"publicKey",  null);
        
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule in = im.getCapsule(this);
        NostrPrivateKeySavableWrapper pkeyWrap = (NostrPrivateKeySavableWrapper) in.readSavable("privateKey", null);
        NostrPublicKeySavableWrapper pubkeyWrap = (NostrPublicKeySavableWrapper) in.readSavable("publicKey", null);
        if(pkeyWrap!=null && pubkeyWrap!=null){
            keyPair = new NostrKeyPair(pkeyWrap.get(), pubkeyWrap.get());       
        }
    }
    
}
