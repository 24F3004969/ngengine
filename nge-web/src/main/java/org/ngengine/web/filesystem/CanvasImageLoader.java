package org.ngengine.web.filesystem;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.ngengine.web.WebBindsAsync;
import org.ngengine.web.WebDecodedImage;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.TextureKey;
import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.texture.plugins.SVGTextureKey;
import com.jme3.util.BufferUtils;

/**
 * Use an HTML Canvas to decode images
 */
public class CanvasImageLoader implements AssetLoader {

  private void flipImage(byte[] img, int width, int height, int bpp) {
        int scSz = (width * bpp) / 8;
        byte[] sln = new byte[scSz];
        int y2 = 0;
        for (int y1 = 0; y1 < height / 2; y1++) {
            y2 = height - y1 - 1;
            System.arraycopy(img, y1 * scSz, sln, 0, scSz);
            System.arraycopy(img, y2 * scSz, img, y1 * scSz, scSz);
            System.arraycopy(sln, 0, img, y2 * scSz, scSz);
        }
    }
    @Override
    public Object load(AssetInfo assetInfo) throws IOException {
        try{
            InputStream is = new BufferedInputStream(assetInfo.openStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024*1024*16];
            int len;
            while ((len = is.read(buffer)) > -1 ) {
                baos.write(buffer, 0, len);
            }

            int width = -1;
            int height = -1;
            boolean flipY = false;
            AssetKey<?> key = assetInfo.getKey();

            if(key !=null && key instanceof TextureKey){
                TextureKey tkey = (TextureKey) key;
                flipY = tkey.isFlipY();
            }

            
            if (key !=null && key instanceof SVGTextureKey) {
                SVGTextureKey svgKey = (SVGTextureKey) key;
                width = svgKey.getWidth();
                height = svgKey.getHeight();
            }

            WebDecodedImage decoded = WebBindsAsync.decodeImage(baos.toByteArray(), assetInfo.getKey().getName(), width, height);
            int w = decoded.getWidth();
            int h = decoded.getHeight();
            byte data[] = decoded.getData();

            if(flipY) flipImage(data, w, h, Format.RGBA8.getBitsPerPixel());

            ByteBuffer bbf = BufferUtils.createByteBuffer(data.length);
            bbf.put(data);
            bbf.flip();
                        
            Image img = new Image(Format.RGBA8, w, h, bbf, null, com.jme3.texture.image.ColorSpace.sRGB);
            return img;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e);
        }
    }
    


}