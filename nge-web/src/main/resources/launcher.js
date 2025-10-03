import Binds from "./org/ngengine/web/WebBindsHub.js";
import AudioRenderer from "./org/ngengine/web/AudioRenderer.js";
import ImageLoader from "./org/ngengine/web/ImageLoader.js";
import Nip07Proxy from "./org/ngengine/web/Nip07Proxy.js";
import WindowHooks from "./org/ngengine/web/Window.js";
import WebRTCProxy from "./org/ngengine/web/WebRTCProxy.js";
import ClipboardProxy from "./org/ngengine/web/ClipboardProxy.js";


const IS_CAPACITOR = typeof Capacitor !== "undefined" && Capacitor.getPlatform
const USE_OFFSCREEN_CANVAS = !IS_CAPACITOR;
const RUN_IN_WORKER = !IS_CAPACITOR;

let loadingAnimationTimer = null;
let loadingAnimation = null;
let render = true;

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

async function main(){

    const canvas = document.querySelector('canvas#nge');
    const splashEl = document.querySelector("#ngeSplash");
    const button = splashEl.querySelector("button#play");



    // make canvas always full screen
    function resize() {
        canvas.style.width = window.innerWidth + 'px';
        canvas.style.height = window.innerHeight + 'px';
        const width = window.innerWidth * devicePixelRatio;
        const height = window.innerHeight * devicePixelRatio;
        Binds.fireEvent("resizeRenderTarget", width, height);
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
    const renderTarget = USE_OFFSCREEN_CANVAS ? canvas.transferControlToOffscreen() : canvas;
    bind(canvas, renderTarget);


    // Start anim loop trigger
    animLoop();

    // resize canvas the first time the backend 
    // comes alive
    let firstPing = true;
    Binds.addEventListener("ping", () => {
        if (!firstPing) return;
        resize();
        firstPing = false;
    })

    let loading = false;
    button.addEventListener('click', async () => {
        if (loading) return;
        loading = true;

        canvas.style.visibility = 'visible';
        console.log("Starting nge...");
        renderLoadingAnimation();
        if (RUN_IN_WORKER) {
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

    });
}

if (document.readyState === 'complete') {
    main();
}else{
    window.addEventListener('load',  () => {
        main();
    });

}