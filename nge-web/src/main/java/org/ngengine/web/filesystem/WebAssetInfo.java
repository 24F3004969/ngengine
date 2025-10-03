package org.ngengine.web.filesystem;

import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import org.ngengine.platform.NGEPlatform;
import org.ngengine.platform.transport.NGEHttpResponseStream;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoadException;
import com.jme3.asset.AssetManager;
import com.jme3.util.BufferInputStream;

public class WebAssetInfo extends AssetInfo{
    private URL url;
    private ByteBuffer data;

    public WebAssetInfo(AssetManager manager, AssetKey key, URL url, ByteBuffer data) {
        super(manager, key);
        this.url = url;
        this.data = data;
    }

    @Override
    public InputStream openStream() {
        if(data!=null){
            return new BufferInputStream(data.slice());
        }
        try{
            NGEHttpResponseStream req = NGEPlatform.get().httpRequestStream("GET", url.toString(), null, null, null).await();
            return req.body;
         } catch (Exception ex) {
            throw new AssetLoadException("Failed to read URL " + url, ex);
        }
    }
}
