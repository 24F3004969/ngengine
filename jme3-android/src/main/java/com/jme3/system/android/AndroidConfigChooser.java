package com.jme3.system.android;

import android.opengl.GLSurfaceView;
import android.os.Build;

import com.jme3.system.AppSettings;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * AndroidConfigChooser is used to determine the best suited EGL Config
 *
 * @author iwgeric, Riccardo Balbo with gpt-5
 */
public final class AndroidConfigChooser implements GLSurfaceView.EGLConfigChooser {
    private static final Logger log = Logger.getLogger(AndroidConfigChooser.class.getName());

    // EGL attributes
    private static final int EGL_RED_SIZE        = 0x3024;
    private static final int EGL_GREEN_SIZE      = 0x3023;
    private static final int EGL_BLUE_SIZE       = 0x3022;
    private static final int EGL_ALPHA_SIZE      = 0x3021;
    private static final int EGL_DEPTH_SIZE      = 0x3025;
    private static final int EGL_STENCIL_SIZE    = 0x3026;
    private static final int EGL_SAMPLE_BUFFERS  = 0x3032;
    private static final int EGL_SAMPLES         = 0x3031;
    private static final int EGL_RENDERABLE_TYPE = 0x3040;
    private static final int EGL_NONE            = 0x3038;

    // Renderable type bits
    private static final int EGL_OPENGL_ES2_BIT      = 0x0004;
    private static final int EGL_OPENGL_ES3_BIT_KHR  = 0x0040;

    private final AppSettings settings;

    public AndroidConfigChooser(AppSettings settings) {
        this.settings = settings;
    }

    @Override
    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        // Desired values from settings
        boolean gamma = settings.isGammaCorrection();
        int samplesReq = Math.max(0, settings.getSamples());
        int depthReq = Math.max(0, settings.getDepthBits());
        int stencilReq = Math.max(0, settings.getStencilBits());
        int alphaReq = Math.max(0, settings.getAlphaBits());

        // Prefer 8-8-8 when gamma correction is requested
        int bpp = settings.getBitsPerPixel();
        int rReq, gReq, bReq;
        if (gamma) {
            rReq = gReq = bReq = 8;
            if (bpp < 24) settings.setBitsPerPixel(24);
        } else if (bpp >= 24) {
            rReq = gReq = bReq = 8;
        } else {
            // 16bpp path
            rReq = 5; gReq = 6; bReq = 5;
            if (bpp != 16) settings.setBitsPerPixel(16);
        }

        // Try ES3 first (API 18+) then ES2
        int desiredRenderable = (Build.VERSION.SDK_INT >= 18) ? EGL_OPENGL_ES3_BIT_KHR : EGL_OPENGL_ES2_BIT;

        EGLConfig cfg = findBestConfig(egl, display, desiredRenderable, rReq, gReq, bReq, alphaReq, depthReq, stencilReq, samplesReq);
        if (cfg == null && desiredRenderable == EGL_OPENGL_ES3_BIT_KHR) {
            log.info("Falling back to EGL_OPENGL_ES2_BIT config");
            cfg = findBestConfig(egl, display, EGL_OPENGL_ES2_BIT, rReq, gReq, bReq, alphaReq, depthReq, stencilReq, samplesReq);
        }
        if (cfg == null) {
            // Last-ditch fallback: accept anything with depth >= 16
            log.info("Falling back to minimal config (depth >= 16)");
            cfg = findBestConfig(egl, display, EGL_OPENGL_ES2_BIT, 0, 0, 0, 0, 16, 0, 0);
        }
        if (cfg == null) {
            throw new IllegalStateException("No suitable EGLConfig found");
        }

        if (log.isLoggable(Level.INFO)) {
            logChosenConfig(egl, display, cfg);
        }
        return cfg;
    }

    private EGLConfig findBestConfig(EGL10 egl, EGLDisplay display,
                                     int renderableBit,
                                     int rReq, int gReq, int bReq, int aReq,
                                     int depthReq, int stencilReq, int samplesReq) {

        // Build base spec for eglChooseConfig
        int[] baseSpec = new int[] {
                EGL_RENDERABLE_TYPE, renderableBit,
                EGL_NONE
        };

        int[] num = new int[1];
        if (!egl.eglChooseConfig(display, baseSpec, null, 0, num) || num[0] == 0) {
            return null;
        }

        EGLConfig[] all = new EGLConfig[num[0]];
        if (!egl.eglChooseConfig(display, baseSpec, all, all.length, num)) {
            return null;
        }

        // Optionally filter for MSAA via attribute hints; many drivers ignore, so we still score below
        EGLConfig[] candidates = all;

        // Evaluate and pick best
        int bestScore = Integer.MIN_VALUE;
        EGLConfig best = null;
        for (EGLConfig c : candidates) {
            int r = getAttrib(egl, display, c, EGL_RED_SIZE);
            int g = getAttrib(egl, display, c, EGL_GREEN_SIZE);
            int b = getAttrib(egl, display, c, EGL_BLUE_SIZE);
            int a = getAttrib(egl, display, c, EGL_ALPHA_SIZE);
            int d = getAttrib(egl, display, c, EGL_DEPTH_SIZE);
            int s = getAttrib(egl, display, c, EGL_STENCIL_SIZE);
            int sb = getAttrib(egl, display, c, EGL_SAMPLE_BUFFERS);
            int ss = getAttrib(egl, display, c, EGL_SAMPLES);

            int score = scoreConfig(rReq, gReq, bReq, aReq, depthReq, stencilReq, samplesReq,
                                    r, g, b, a, d, s, sb, ss, settings.isGammaCorrection());

            if (score > bestScore) {
                bestScore = score;
                best = c;
            }
        }
        return best;
    }

    // Higher score is better
    private int scoreConfig(int rReq, int gReq, int bReq, int aReq,
                            int dReq, int sReq, int msaaReq,
                            int r, int g, int b, int a, int d, int s, int sb, int samples,
                            boolean gamma) {
        int score = 0;

        // Hard rejections
        if (d < Math.min(16, dReq)) return Integer.MIN_VALUE / 2; // ensure at least 16 if requested > 0
        if (s < sReq) return Integer.MIN_VALUE / 2;

        // Prefer 8-8-8 for gamma correction (avoid RGB565)
        if (gamma) {
            if (r < 8 || g < 8 || b < 8) score -= 1000;
        }

        // Color channel closeness (prefer at least requested, penalize below)
        score += channelScore(r, rReq);
        score += channelScore(g, gReq);
        score += channelScore(b, bReq);

        // Alpha: allow overshoot, penalize missing if requested
        if (aReq > 0) {
            score += channelScore(a, aReq);
        } else {
            // Prefer opaque when not requested
            if (a == 0) score += 10;
        }

        // Depth: prefer closer-to-requested, avoid huge overshoot penalty
        score += rangeScore(d, dReq, 8);

        // Stencil
        score += rangeScore(s, sReq, 8);

        // MSAA: require sample buffers when samples requested
        if (msaaReq > 0) {
            if (sb > 0 && samples >= msaaReq) {
                score += 50 + Math.min(samples, 8); // bonus for meeting/over
            } else if (sb > 0 && samples > 0) {
                score += 20; // some MSAA is better than none
            } else {
                score -= 200; // requested but not supported
            }
        } else {
            if (sb == 0 || samples == 0) score += 5; // prefer no MSAA when not requested
        }

        return score;
    }

    private int channelScore(int actual, int desired) {
        if (desired <= 0) return 0;
        if (actual < desired) return -200 - (desired - actual) * 10;
        // small bonus for meeting/exceeding, diminishing
        return 10 - Math.min(5, actual - desired);
    }

    private int rangeScore(int actual, int desired, int softness) {
        if (desired <= 0) return 0;
        if (actual < desired) return -300 - (desired - actual) * 15;
        return Math.max(0, softness - (actual - desired)); // prefer near desired
    }

    private int getAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attrib) {
        int[] out = new int[1];
        if (egl.eglGetConfigAttrib(display, config, attrib, out)) {
            return out[0];
        }
        return 0;
    }

    private void logChosenConfig(EGL10 egl, EGLDisplay display, EGLConfig c) {
        int r = getAttrib(egl, display, c, EGL_RED_SIZE);
        int g = getAttrib(egl, display, c, EGL_GREEN_SIZE);
        int b = getAttrib(egl, display, c, EGL_BLUE_SIZE);
        int a = getAttrib(egl, display, c, EGL_ALPHA_SIZE);
        int d = getAttrib(egl, display, c, EGL_DEPTH_SIZE);
        int s = getAttrib(egl, display, c, EGL_STENCIL_SIZE);
        int sb = getAttrib(egl, display, c, EGL_SAMPLE_BUFFERS);
        int ss = getAttrib(egl, display, c, EGL_SAMPLES);
        log.info(String.format("EGLConfig chosen: R%d G%d B%d A%d D%d S%d MSAA[%d buffers, %d samples]",
                r, g, b, a, d, s, sb, ss));
    }
}