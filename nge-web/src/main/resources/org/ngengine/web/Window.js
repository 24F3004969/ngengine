import Binds from "./WebBindsHub.js";
let pointerLock = false;

function bind(canvas, renderTarget){

    Binds.addEventListener("getRenderTarget", ()=>{
        return renderTarget;
    });

 

 
    Binds.addEventListener("setPageTitle", (title)=>{
        if (typeof title !== 'string') return;
        if (window.document) {
            window.document.title = title;
        }
        window.name = title;        
    });

    Binds.addEventListener("togglePointerLock", (v)=>{
        pointerLock = v;
    });

    Binds.addEventListener("getBaseURL", ()=>{
        return window.location.href;
    });

    canvas.addEventListener("click", (e) => {
        if (pointerLock) {
            canvas.requestPointerLock();
        } else if(document.pointerLockElement === canvas){
            document.exitPointerLock();
        }
    });
}

function bindListeners(canvas,renderTarget){
    let PIXELS_PER_LINE = null;
    const prepareEvent = (inputEvent) => {
        const event = {};
        const keys = [
            "type", "clientX", "clientY", "screenX", "screenY", "button", "buttons",
            "ctrlKey", "shiftKey", "altKey", "metaKey", "deltaY", "deltaMode",
            "key", "code", "pointerId", "pointerType", "movementX", "movementY"
        ];
        for(const key of keys) {
            if (inputEvent[key] !== undefined) event[key] = inputEvent[key];
        }

        const makeTouchesClonable = (tx)=>{
            const touches = [];
            for (let i = 0; i < tx.length; i++) {
                const t = tx[i];
                const rect = canvas.getBoundingClientRect();
                const scaleX = canvas.width / rect.width;
                const scaleY = canvas.height / rect.height;
                touches.push({
                    identifier: t.identifier,
                    clientX: Math.round((t.clientX - rect.left) * scaleX),
                    clientY: Math.round(canvas.height - ((t.clientY - rect.top) * scaleY)),
                    screenX: t.screenX,
                    screenY: t.screenY,
                    force: t.force,
                });
            }
            return touches;
        }
        event.touches = inputEvent.touches ? makeTouchesClonable(inputEvent.touches) : [];
        event.targetTouches = inputEvent.targetTouches ? makeTouchesClonable(inputEvent.targetTouches) : [];
        event.changedTouches = inputEvent.changedTouches ? makeTouchesClonable(inputEvent.changedTouches) : [];

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
            const doc = window.document;
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
                    const cs = window.getComputedStyle ? window.getComputedStyle(el) : null;
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
                    ? Math.max(doc.documentElement.clientHeight, window.innerHeight || 0)
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

export default { bind, bindListeners };