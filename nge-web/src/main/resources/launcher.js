import Binds from "./org/ngengine/web/WebBindsHub.js";
import AudioRenderer from "./org/ngengine/web/AudioRenderer.js";
import ImageLoader from "./org/ngengine/web/ImageLoader.js";
import Nip07Proxy from "./org/ngengine/web/Nip07Proxy.js";
import WindowHooks from "./org/ngengine/web/Window.js";
import WebRTCProxy from "./org/ngengine/web/WebRTCProxy.js";
import ClipboardProxy from "./org/ngengine/web/ClipboardProxy.js";




let loadingAnimationTimer = null;
let loadingAnimation = null;
let render = true;
let fullscreen = false;

function animLoop(){
    window.requestAnimationFrame(()=>{
        if(render){
            Binds.fireEvent("render");
        }
        animLoop();
    });
}


function renderLoadingAnimation(){
    if(loadingAnimation) return;
    const el = document.createElement("div");
    el.setAttribute("id", "ngeLoading");
    el.innerHTML = '<span class="ngeLoader"></span>';  
    document.body.appendChild(el);
    loadingAnimation = el;
}


function bind(canvas, renderTarget){
    AudioRenderer.bind();
    ImageLoader.bind();
    Nip07Proxy.bind();
    WebRTCProxy.bind();
    WindowHooks.bind(canvas, renderTarget);
    WindowHooks.bindListeners(canvas, renderTarget);  
    ClipboardProxy.bind();  
    Binds.addEventListener("ping",()=>{
        if(loadingAnimation){
            loadingAnimation.remove();
            loadingAnimation = null;
        }

        if(loadingAnimationTimer) clearTimeout(loadingAnimationTimer);

        loadingAnimationTimer = setTimeout(()=>{
            renderLoadingAnimation();
        }, 1500);
    });
}


function showFullscreenButton(config, canvas, show){
    if(!config.showFullScreenButton) return;
    if(fullscreen === show) return;
    fullscreen = show;
    if(show){
        const el = document.createElement("div");
        el.setAttribute("id", "ngeFullscreenButton");
        el.innerHTML = '<span class="ngeFullscreenIcon"></span>';  
        document.body.appendChild(el);
        el.addEventListener("click", (e) => {
            if (fullscreen) {
                canvas.requestFullscreen();
            }  
        });
    }else if(!show && button){
        const button = document.querySelector("#ngeFullscreenButton");
        if(button){
            button.remove();
        }
    }   
}

function tweakConfig(config){
    if(!config) config = {};
    if(typeof config.is_capacitor === "undefined"){
        config.is_capacitor = typeof Capacitor !== "undefined" && Capacitor.getPlatform
    };
    if(typeof config.run_in_worker === "undefined"){
        config.run_in_worker = !config.is_capacitor;
    }
    if(typeof config.use_offscreen_canvas === "undefined"){
        config.use_offscreen_canvas = config.run_in_worker;
    }
    if(typeof config.canvasSelector === "undefined"){
        config.canvasSelector = 'canvas#nge';
    }
    if(typeof config.showFullScreenButton === "undefined"){
        config.showFullScreenButton = !config.is_capacitor;
    }
    return config;
}

export default async function launch(config){
    config = tweakConfig(config);
    console.log("Launch with config",config)

    const canvas = document.querySelector(config.canvasSelector);
    Binds.addEventListener("toggleFullscreen", (v) => {
        showFullscreenButton(config, canvas,v);
    });

    // make canvas always full screen
    let resizeTimeout = null;
    function resize() {
        if(resizeTimeout){
            clearTimeout(resizeTimeout);
        }
        resizeTimeout = setTimeout(()=>{
            let r = 1;
            canvas.style.width = window.innerWidth + 'px';
            canvas.style.height = window.innerHeight + 'px';
            const width = window.innerWidth * r;
            const height = window.innerHeight * r;
            Binds.fireEvent("resizeRenderTarget", width, height);
            resizeTimeout = null;
        },100);     
    }
    window.addEventListener('resize', resize);
    canvas.addEventListener('resize', resize);

    // listen to loss of webgl context
    canvas.addEventListener("webglcontextlost", (event) => {
        event.preventDefault();
        console.warn("WebGL context lost");
        render = false;
        Binds.fireEvent("webglcontextlost");
    });

    canvas.addEventListener("webglcontextrestored", (event) => {
        console.info("WebGL context restored");
        render = true;
        Binds.fireEvent("webglcontextrestored");
    });

    // Bind client actions
    const renderTarget = config.use_offscreen_canvas ? canvas.transferControlToOffscreen() : canvas;
    bind(canvas, renderTarget);


    // Start anim loop trigger
    animLoop();

    // resize canvas the first time the backend comes alive
    let firstPing = true;
    Binds.addEventListener("ping", () => {
        if (!firstPing) return;
        resize();
        firstPing = false;
    })


       
    canvas.style.visibility = 'visible';
    console.log("Starting nge...");
    renderLoadingAnimation();
    if (config.run_in_worker) {
        Binds.addEventListener("ready", () => {
            console.log("NGE worker is ready");
            Binds.fireEvent("main", []).then(() => {
                resize();
            })
        });
        const worker = new Worker("./worker.js", { type: 'module' });
        Binds.registerWorker(worker);

    } else {
        const { main } = await import("./webapp.js");
        Binds.addEventListener("ready", () => {
            main([]);
            resize();
        });
        Binds.fireEvent("ready");
    }
    

}