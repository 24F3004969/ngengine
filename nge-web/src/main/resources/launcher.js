import Binds from "./org/ngengine/web/WebBindsHub.js";
import AudioRenderer from "./org/ngengine/web/AudioRenderer.js";
import ImageLoader from "./org/ngengine/web/ImageLoader.js";
import Nip07 from "./org/ngengine/web/Nip07.js";

const USE_OFFSCREEN_CANVAS = true;
const RUN_IN_WORKER = true;
let fullscreen = false;
let pointerLock = false;
let loadingAnimationTimer = null;
let loadingAnimation = null;

function animLoop(){
    window.requestAnimationFrame(()=>{
        Binds.fireEvent("render");
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
    Nip07.bind();


    Binds.addEventListener("ping",()=>{
        if(loadingAnimation){
            loadingAnimation.remove();
            loadingAnimation = null;
        }

        if(loadingAnimationTimer) clearTimeout(loadingAnimationTimer);

        loadingAnimationTimer = setTimeout(()=>{
            renderLoadingAnimation();
        }, 2000);
    })

    Binds.addEventListener("getRenderTarget", ()=>{
        return renderTarget;
    });

    Binds.addEventListener("toggleFullscreen", (v)=>{
        fullscreen = v;
    })


 
    Binds.addEventListener("setPageTitle", (title)=>{
        if (typeof title !== 'string') return;
        const g = window;
        if (g && g.document && g.document.title !== undefined) {
            g.document.title = title;
        }
        if (g) {
            g.name = title;
        }
    });

    Binds.addEventListener("togglePointerLock", (v)=>{
        pointerLock = v;
    });


    Binds.addEventListener("getBaseURL", ()=>{
        return window.location.href;
    });

    canvas.addEventListener("click", (e) => {
        if (fullscreen) {
            canvas.requestFullscreen();
        } else if(document.fullscreenElement === canvas){
            document.exitFullscreen();
        }
        if (pointerLock) {
            canvas.requestPointerLock();
        } else if(document.pointerLockElement === canvas){
            document.exitPointerLock();
        }
    })


    // Binds.addEventListener("exitPointerLock", ()=>{
    //     if(document.pointerLockElement === canvas) document.exitPointerLock();
    // });

    let PIXELS_PER_LINE = null;
    const prepareEvent = (inputEvent) => {
        const event = {};
        const keys = [
            "type", "clientX", "clientY", "screenX", "screenY", "button", "buttons",
            "ctrlKey", "shiftKey", "altKey", "metaKey", "deltaY", "deltaMode",
            "key", "code", "pointerId", "pointerType", "movementX", "movementY",
            "touches", "changedTouches", "targetTouches"
        ];
        const obj = {};
        for(const key of keys) {
            if (inputEvent[key] !== undefined) event[key] = inputEvent[key];
        }

        if (event.clientX !== undefined) {
            const rect = canvas.getBoundingClientRect();    
            const scaleX = canvas.width / rect.width;    
            event.clientX = Math.round((event.clientX - rect.left) * scaleX);
        }
        if (event.clientY !== undefined) {
            const rect = canvas.getBoundingClientRect();
            const scaleY = canvas.height / rect.height;
            event.clientY = Math.round(canvas.height - ((event.clientY - rect.top) * scaleY));
        }
        if(event.deltaY !== undefined && event.deltaMode !== undefined) {
            const g = window;
            const doc = g && g.document;
            let deltaValue = event.deltaY;
            let deltaMode = event.deltaMode; // 0=pixel, 1=line, 2=page
        
            let pixelsPerLine = PIXELS_PER_LINE;
            if (!pixelsPerLine) {
                if (!doc || !doc.body) {
                    pixelsPerLine = 16; // fallback when no DOM
                } else {
                    const el = doc.createElement("span");
                    el.style.cssText = "position:absolute;visibility:hidden;font-size:16px;line-height:1.2;margin:0;padding:0;border:0;";
                    el.textContent = "X";
                    doc.body.appendChild(el);
                    const cs = g.getComputedStyle ? g.getComputedStyle(el) : null;
                    const lh = cs && cs.lineHeight && cs.lineHeight.endsWith("px")
                        ? parseFloat(cs.lineHeight)
                        : (el.offsetHeight || 16);
                    doc.body.removeChild(el);
                    pixelsPerLine = lh || 16;
                }
                PIXELS_PER_LINE = pixelsPerLine;
            }
        
            if (deltaMode === 0) {
                event.deltaY = deltaValue; // pixels
            } else if (deltaMode === 1) {
                event.deltaY = deltaValue * pixelsPerLine; // lines -> pixels
            } else {
                // pages -> pixels
                const viewportHeight = doc && doc.documentElement
                    ? Math.max(doc.documentElement.clientHeight, g.innerHeight || 0)
                    : 800;
                const estimatedLinesPerPage = Math.max(1, Math.floor(viewportHeight / pixelsPerLine));
                event.deltaY = deltaValue * pixelsPerLine * estimatedLinesPerPage;
            }        
        }
        return event;
    }



    window.document.addEventListener('mousemove', (event) => {
        // event.preventDefault();
        event = prepareEvent(event);
        Binds.fireEvent("mousemove", event);
    }, false);
    window.document.addEventListener('wheel', (event) => {
        // event.preventDefault();
        event = prepareEvent(event);
        Binds.fireEvent("wheel", event);
    }, false);
    window.document.addEventListener('mousedown', (event) => {
        // event.preventDefault();
        event = prepareEvent(event);
        Binds.fireEvent("mousedown", event);
    }, false);
    window.document.addEventListener('mouseup', (event) => {
        // event.preventDefault();
        event = prepareEvent(event);
        Binds.fireEvent("mouseup", event);
    }, false);
    window.document.addEventListener('touchstart', (event) => {
        // event.preventDefault();
        event = prepareEvent(event);
        Binds.fireEvent("touchstart", event);
    }, true);
    window.document.addEventListener('touchmove', (event) => {
        // event.preventDefault();
        event = prepareEvent(event);
        Binds.fireEvent("touchmove", event);
    }, true);
    window.document.addEventListener('touchcancel', (event) => {
        // event.preventDefault();
        event = prepareEvent(event);
        Binds.fireEvent("touchcancel", event);
    }, true);
    window.document.addEventListener('touchend', (event) => {
        // event.preventDefault();
        event = prepareEvent(event);
        Binds.fireEvent("touchend", event);
    }, true); 
    window.document.addEventListener('keydown', (event) => {
        // event.preventDefault();
        event = prepareEvent(event);
        Binds.fireEvent("keydown", event);
    }, false);
    window.document.addEventListener('keyup', (event) => {
        // event.preventDefault();
        event = prepareEvent(event);
        Binds.fireEvent("keyup", event);
    }, false);
    window.document.addEventListener('keypress', (event) => {
        // event.preventDefault();
        event = prepareEvent(event);
        Binds.fireEvent("keypress", event);
    }, false);
    window.document.addEventListener('pointerlockchange', (event) => {
        event = prepareEvent(event);
        Binds.fireEvent("pointerlockchange", event);
    }, false);
    window.document.addEventListener('fullscreenchange', (event) => {
        event = prepareEvent(event);
        Binds.fireEvent("fullscreenchange", event);
    }, false);

}

window.addEventListener('load',  () => {
 
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
    resize();

    // Bind client actions
    const renderTarget = USE_OFFSCREEN_CANVAS ? canvas.transferControlToOffscreen() : canvas;
    bind(canvas, renderTarget);

 
    // Start anim loop trigger
    animLoop();
    

    let loading = false;
    button.addEventListener('click', async () => {
        if(loading)return;
        loading = true;

        canvas.style.visibility = 'visible';
        console.log("Starting nge...");
        renderLoadingAnimation();
        if (RUN_IN_WORKER){
             Binds.addEventListener("ready", ()=>{
                console.log("NGE worker is ready");
                Binds.fireEvent("main", []);            
            });
            const worker = new Worker("./worker.js", { type: 'module' });
            Binds.registerWorker(worker);
           
        } else {
            const { main } = await import("./webapp.js");
            Binds.addEventListener("ready", ()=>{
                main([]);
            });
           
            Binds.fireEvent("ready");
        }

    });

});

