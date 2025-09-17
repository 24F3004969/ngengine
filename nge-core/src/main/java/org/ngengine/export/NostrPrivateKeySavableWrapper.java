package org.ngengine.export;

import java.io.IOException;

import org.ngengine.nostr4j.keypair.NostrPrivateKey;
import org.ngengine.nostr4j.keypair.NostrPrivateKey.KeySecurity;
import org.ngengine.nostr4j.keypair.NostrPublicKey;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;

public class NostrPrivateKeySavableWrapper implements SavableWrapper<NostrPrivateKey>{
    private String key;
    private KeySecurity keySecurity;

    protected NostrPrivateKeySavableWrapper(){
    }

    public NostrPrivateKeySavableWrapper(NostrPrivateKey key){
        this(key, null);
    }
    
    public NostrPrivateKeySavableWrapper(NostrPrivateKey key, String password){
        if(password!=null){
            try{
                this.key = key.asNcryptsec(password).await();
            } catch(Exception e){
                throw new RuntimeException("Failed to encrypt private key for export", e);
            }
        } else {
            this.key = key.asBech32();
        }
        keySecurity = key.getKeySecurity();
        assert password == null || this.key.startsWith("ncryptsec");
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule out = ex.getCapsule(this);
        out.write( key ,"key",  null);
        out.write( keySecurity !=null ? keySecurity.name() : null ,"keySecurity",  null);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule in = im.getCapsule(this);
        this.key = in.readString("key", null);
        this.keySecurity = KeySecurity.valueOf(in.readString("keySecurity", KeySecurity.UNKNOWN.name()));
    }

    @Override
    public NostrPrivateKey get() {
        if(key.startsWith("ncryptsec")){
            throw new UnsupportedOperationException("This privatekey is encrypted, please use the get(passphrase) method instead");
        } else {
            NostrPrivateKey pkey = NostrPrivateKey.fromBech32(key);
            pkey.setKeySecurity(keySecurity);
            return pkey;
        }
    }
 
    
    public NostrPrivateKey get(String passphrase) {
        if(key.startsWith("ncryptsec")){
            try{
                return NostrPrivateKey.fromNcryptsec(key, passphrase).await();
            } catch(Exception e){
                throw new RuntimeException("Failed to decrypt private key", e);
            }
        } else {
            NostrPrivateKey pkey  = NostrPrivateKey.fromBech32(key);
            pkey.setKeySecurity(keySecurity);
            return pkey;
        }
    }
}
