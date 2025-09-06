
package com.jme3.web.demo;

import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.audio.AudioListenerState;
import com.jme3.environment.EnvironmentProbeControl;
import com.jme3.input.ChaseCamera;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.BloomFilter.GlowMode;
import com.jme3.post.filters.ContrastAdjustmentFilter;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.post.filters.ToneMapFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeSystem;
import com.jme3.util.SkyFactory;
import com.jme3.util.mikktspace.MikktspaceTangentGenerator;
import com.jme3.web.context.NativeUtils;
import com.jme3.web.filesystem.WebLocator;

/**
 * TestPBRSimple
 */
public class TestPBRSimple extends SimpleApplication {
    private boolean REALTIME_BAKING = false;

    public TestPBRSimple(AppState... initialStates) {
        super(initialStates);
    }
 

    float steps = 8;
    float increment = 1f / steps;
    float progress = 0;

    @Override
    public void simpleInitApp() {
// LOCATOR / com.jme3.web.filesystem.WebLocator

//         LOADER com.jme3.audio.plugins.WAVLoader : wav
// LOADER com.jme3.material.plugins.J3MLoader : j3m
// LOADER com.jme3.material.plugins.J3MLoader : j3md
// LOADER com.jme3.material.plugins.ShaderNodeDefinitionLoader : j3sn
// LOADER com.jme3.font.plugins.BitmapFontLoader : fnt
// LOADER com.jme3.texture.plugins.DDSLoader : dds
// LOADER com.jme3.texture.plugins.HDRLoader : hdr
// LOADER com.jme3.texture.plugins.TGALoader : tga
// LOADER com.jme3.export.binary.BinaryLoader : j3o
// LOADER com.jme3.export.binary.BinaryLoader : j3f
// LOADER com.jme3.scene.plugins.OBJLoader : obj
// LOADER com.jme3.scene.plugins.MTLLoader : mtl
// LOADER com.jme3.shader.plugins.GLSLLoader : vert, frag, geom, tsctrl, tseval, glsl, glsllib
// LOADER com.jme3.scene.plugins.gltf.GltfLoader : gltf
// LOADER com.jme3.scene.plugins.gltf.BinLoader : bin
// LOADER com.jme3.scene.plugins.gltf.GlbLoader : glb
// LOADER com.jme3.web.filesystem.StbImageLoader : png, jpg, jpeg
// LOADER com.jme3.audio.plugins.OGGLoader : ogg

        // assetManager.registerLocator("/", WebLocator.class);
        // assetManager.registerLoader(com.jme3.audio.plugins.WAVLoader.class, "wav");
        // assetManager.registerLoader(com.jme3.material.plugins.J3MLoader.class, "j3m", "j3md");
        // assetManager.registerLoader(com.jme3.material.plugins.ShaderNodeDefinitionLoader.class, "j3sn");
        // assetManager.registerLoader(com.jme3.font.plugins.BitmapFontLoader.class, "fnt");
        // assetManager.registerLoader(com.jme3.texture.plugins.DDSLoader.class, "dds");
        // assetManager.registerLoader(com.jme3.texture.plugins.HDRLoader.class, "hdr");
        // assetManager.registerLoader(com.jme3.texture.plugins.TGALoader.class, "tga");
        // assetManager.registerLoader(com.jme3.export.binary.BinaryLoader.class, "j3o", "j3f");
        // assetManager.registerLoader(com.jme3.scene.plugins.OBJLoader.class, "obj");
        // assetManager.registerLoader(com.jme3.scene.plugins.MTLLoader.class, "mtl");
        // assetManager.registerLoader(com.jme3.shader.plugins.GLSLLoader.class, "vert", "frag", "geom", "tsctrl", "tseval", "glsl", "glsllib");
        // assetManager.registerLoader(com.jme3.scene.plugins.gltf.GltfLoader.class, "gltf");
        // assetManager.registerLoader(com.jme3.scene.plugins.gltf.BinLoader.class, "bin");
        // assetManager.registerLoader(com.jme3.scene.plugins.gltf.GlbLoader.class, "glb");
        // assetManager.registerLoader(com.jme3.web.filesystem.StbImageLoader.class, "png", "jpg", "jpeg");
        assetManager.registerLoader(com.jme3.audio.plugins.OGGLoader.class, "ogg"); 

        NativeUtils.setProgress((this.progress += increment), "Loading tank geometry");

        Geometry model = (Geometry) assetManager.loadModel("Models/Tank/tank.j3o");

        NativeUtils.setProgress((this.progress += increment), "Generating tank tangents");
        MikktspaceTangentGenerator.generate(model);

        NativeUtils.setProgress((this.progress += increment), "Loading tank material");
        Material pbrMat = assetManager.loadMaterial("Models/Tank/tank.j3m");
        model.setMaterial(pbrMat);
        rootNode.attachChild(model);

        NativeUtils.setProgress((this.progress += increment), "Loading camera");
        ChaseCamera chaseCam = new ChaseCamera(cam, model, inputManager);
        chaseCam.setDragToRotate(true);
        chaseCam.setMinVerticalRotation(-FastMath.HALF_PI);
        chaseCam.setMaxDistance(1000);
        chaseCam.setSmoothMotion(true);
        chaseCam.setRotationSensitivity(10);
        chaseCam.setZoomSensitivity(5);
        flyCam.setEnabled(false);

        NativeUtils.setProgress((this.progress += increment), "Loading skybox");
        Spatial sky = SkyFactory.createSky(assetManager, "Textures/Sky/Path.hdr", SkyFactory.EnvMapType.EquirectMap);
        rootNode.attachChild(sky);

        // Create baker control
        EnvironmentProbeControl envProbe = new EnvironmentProbeControl(assetManager, 256);
        rootNode.addControl(envProbe);

        // Tag the sky, only the tagged spatials will be rendered in the env map
        envProbe.tag(sky);

        NativeUtils.setProgress((this.progress += increment), "Loading filters");
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        int numSamples = context.getSettings().getSamples();
        if (numSamples > 0) {
            fpp.setNumSamples(numSamples);
        }

        
        fpp.addFilter(new ToneMapFilter(Vector3f.UNIT_XYZ.mult(4.0f)));
        fpp.addFilter(new ContrastAdjustmentFilter(1f / 2.2f));
        fpp.addFilter(new FXAAFilter());
        viewPort.addProcessor(fpp);
    }

    float lastBake = 0;
    int frames = 0;

    @Override
    public void simpleUpdate(float tpf) {

        if (frames < 6) {
            if (frames == 0) {
                NativeUtils.setProgress((this.progress += increment), "Preparing environment");
            } else if (frames == 4) {
                NativeUtils.setProgress(1, "Ready");
            }
        }
        frames++;
        if (REALTIME_BAKING) {
            lastBake += tpf;
            if (lastBake > 1.4f) {
                rootNode.getControl(EnvironmentProbeControl.class).rebake();
                lastBake = 0;
            }
        }
    }
}