package org.ngengine.export;

import java.io.IOException;

import org.ngengine.nostr4j.signer.NostrNIP46Signer;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;

public class Nip46SignerSavableWrapper implements SavableWrapper<NostrNIP46Signer> {
    private NostrNIP46Signer signer;

    public Nip46SignerSavableWrapper(){
    }

    public Nip46SignerSavableWrapper(NostrNIP46Signer signer){
        this.signer = signer;
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule out = ex.getCapsule(this);
        Nip46MetadataSavableWrapper metaWrap = new Nip46MetadataSavableWrapper(signer.getMetadata());
        NostrKeyPairSavableWrapper keyWrap = new NostrKeyPairSavableWrapper(signer.getTransportKeyPair());
        out.write(metaWrap, "metadata", null);
        out.write(keyWrap, "keypair", null);

    }

    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule in = im.getCapsule(this);
        Nip46MetadataSavableWrapper metaWrap = (Nip46MetadataSavableWrapper) in.readSavable("metadata", null);
        NostrKeyPairSavableWrapper keyWrap = (NostrKeyPairSavableWrapper) in.readSavable("keypair", null);
        signer = new NostrNIP46Signer(metaWrap.get(),keyWrap.get());
    }

    @Override
    public NostrNIP46Signer get() {
        return signer;
    }
    
}
