package org.ngengine.export;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.ngengine.nostr4j.nip46.Nip46AppMetadata;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;

public class Nip46MetadataSavableWrapper implements SavableWrapper<Nip46AppMetadata> {
    private Nip46AppMetadata metadata;

    protected Nip46MetadataSavableWrapper(){

    }   

    public Nip46MetadataSavableWrapper(Nip46AppMetadata metadata){
        this.metadata = Objects.requireNonNull(metadata);
    }


    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule out = ex.getCapsule(this);
        out.write(metadata.getName(), "name", null);
        out.write(metadata.getUrl(), "url", null);
        out.write(metadata.getImage(), "image", null);
        out.write(metadata.getPerms().toArray(new String[0]), "perms", null);
        
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule in = im.getCapsule(this);
        String name = in.readString("name", null);
        String url = in.readString("url", null);
        String image = in.readString("image", null);
        String[] perms = in.readStringArray("perms", null);
        metadata = new Nip46AppMetadata(name, url, image, Arrays.asList(perms));
    }

    @Override
    public Nip46AppMetadata get() {
        return metadata;
    }

   
}
