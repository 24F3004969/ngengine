package com.jme3.texture.plugins;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoader;
import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.texture.image.ColorSpace;
import com.jme3.util.BufferUtils;

/**
 * @author Riccardo Balbo
 */
public class SVGLoader implements AssetLoader {

    @Override
    public Object load(AssetInfo assetInfo) throws IOException {
        AssetKey<?> key = assetInfo.getKey();
        int width = 256;
        int height = 256;
        boolean flipY = false;

        if (key instanceof SVGTextureKey) {
            SVGTextureKey svgKey = (SVGTextureKey) key;
            width = svgKey.getWidth();
            height = svgKey.getHeight();
            flipY = svgKey.isFlipY();
        }

        try (InputStream in = assetInfo.openStream()) {
            // Parse SVG using AndroidSVG
            SVG svg = SVG.getFromInputStream(in);
            if (svg == null) {
                throw new IOException("Failed to parse SVG data.");
            }

            // Get SVG dimensions
            float svgWidth = svg.getDocumentWidth();
            float svgHeight = svg.getDocumentHeight();

            // Use viewbox if document size is not specified
            if (svgWidth <= 0 || svgHeight <= 0) {
                RectF viewBox = svg.getDocumentViewBox();
                if (viewBox == null) {
                    throw new IOException("Invalid SVG dimensions and no viewBox specified");
                }
                svgWidth = viewBox.width();
                svgHeight = viewBox.height();
            }

            if (svgWidth <= 0 || svgHeight <= 0) {
                throw new IOException("Invalid SVG dimensions: " + svgWidth + "x" + svgHeight);
            }

            // Calculate scaling to fit the requested size while preserving aspect ratio
            float scaleX = (float) width / svgWidth;
            float scaleY = (float) height / svgHeight;
            float scale = Math.min(scaleX, scaleY);

            float scaledW = svgWidth * scale;
            float scaledH = svgHeight * scale;
            float tx = (width - scaledW) / 2.0f;
            float ty = (height - scaledH) / 2.0f;

            // Create a bitmap to render the SVG
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            // Fill with transparent background
            canvas.drawARGB(0, 0, 0, 0);

            // Apply transformation to center and scale the SVG
            canvas.translate(tx, ty);
            canvas.scale(scale, scale);

            // Render the SVG
            svg.renderToCanvas(canvas);

            // Allocate direct buffer for jME3 image data
            ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

            // Read pixels as ARGB ints
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            // Write RGBA into the direct buffer, optionally flipping vertically
            writeARGBToRGBA(pixels, buffer, width, height, flipY);

            // Create jME3 Image
            Image image = new Image(Format.RGBA8, width, height, buffer, ColorSpace.sRGB);

            // Clean up Android resources
            bitmap.recycle();

            return image;

        } catch (SVGParseException e) {
            throw new IOException("Error parsing SVG: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IOException("Error loading SVG: " + e.getMessage(), e);
        }
    }

    /**
     * Write ARGB int[] pixels to a direct ByteBuffer as RGBA bytes.
     * Optionally flips vertically for OpenGL-style textures.
     */
    private void writeARGBToRGBA(int[] pixels, ByteBuffer buffer, int width, int height, boolean flipY) {
        buffer.clear();
        for (int y = 0; y < height; y++) {
            int srcY = flipY ? (height - 1 - y) : y;
            int rowOff = srcY * width;
            for (int x = 0; x < width; x++) {
                int c = pixels[rowOff + x];       // 0xAARRGGBB (premultiplied alpha on Android bitmaps)
                byte r = (byte) ((c >> 16) & 0xFF);
                byte g = (byte) ((c >> 8)  & 0xFF);
                byte b = (byte) ( c        & 0xFF);
                byte a = (byte) ((c >>> 24) & 0xFF);
                buffer.put(r).put(g).put(b).put(a);
            }
        }
    }
}