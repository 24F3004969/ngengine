package org.ngengine.web.filesystem;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.ngengine.web.WebBindsAsync;
import org.ngengine.web.WebDecodedImage;
import org.ngengine.web.WebHdrDecodedImage;
import org.teavm.jso.typedarrays.Float32Array;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.TextureKey;
import com.jme3.math.FastMath;
import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.texture.image.ColorSpace;
import com.jme3.texture.plugins.SVGTextureKey;
import com.jme3.util.BufferUtils;

/**
 * Decode images in the browser using browser decoders when possible.
 */
public class WebImageLoader implements AssetLoader {

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

            String filename = assetInfo.getKey().getName();
            int w;
            int h;
            ByteBuffer bbf;
            Format format;
            ColorSpace colorSpace;

            if(filename.endsWith(".hdr")){
                format = Format.RGB16F;
                colorSpace = ColorSpace.Linear;
                WebHdrDecodedImage decoded = WebBindsAsync.decodeHdrImage(baos.toByteArray(), assetInfo.getKey().getName());
                w = decoded.getWidth();
                h = decoded.getHeight();
                Float32Array data = decoded.getData();
                int length = data.getLength();
                bbf = BufferUtils.createByteBuffer(length * (format.getBitsPerPixel() / 8));
                for (int i = 0; i < length; i++) {
                    bbf.putShort(FastMath.convertFloatToHalf(data.get(i)));
                }
                bbf.flip();              
            } else {
                format = Format.RGBA8;
                colorSpace = ColorSpace.sRGB;
                WebDecodedImage decoded = WebBindsAsync.decodeImage(baos.toByteArray(), assetInfo.getKey().getName(), width, height);
                w = decoded.getWidth();
                h = decoded.getHeight();
                byte[] data = decoded.getData();
                if(flipY) flipImage(data, w, h, Format.RGBA8.getBitsPerPixel());
                bbf = BufferUtils.createByteBuffer(data);       
            }
                        
            Image img = new Image(format, w, h, bbf, null, colorSpace);
            return img;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e);
        }
    }
    


}