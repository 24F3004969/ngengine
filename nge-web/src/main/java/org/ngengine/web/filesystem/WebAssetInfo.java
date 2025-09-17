package org.ngengine.web.filesystem;

import java.io.InputStream;
import java.nio.ByteBuffer;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.util.BufferInputStream;
import com.jme3.util.BufferUtils;

public class WebAssetInfo extends AssetInfo{
    private ByteBuffer data;
    public WebAssetInfo(AssetManager manager, AssetKey key, byte[] data) {
        this(manager,key,BufferUtils.createByteBuffer(data));
    }

    public WebAssetInfo(AssetManager manager, AssetKey key, ByteBuffer data) {
        super(manager, key);
        this.data = data;
    }

    @Override
    public InputStream openStream() {
        return new BufferInputStream(data.slice());
    }
    
}
