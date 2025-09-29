 
import Binds from "./WebBindsHub.js";
import ImageLoader from "./ImageLoader.js";
import Nip07 from "./Nip07.js";
// convert various buffer types to Uint8Array
const _u = (data) => {
    if (data instanceof Uint8Array) {
        return data;
    } else if (data instanceof Int8Array) {
        return new Uint8Array(data.buffer, data.byteOffset, data.byteLength);
    } else if (Array.isArray(data)) {
        return new Uint8Array(data);
    } else if (data instanceof ArrayBuffer) {
        return new Uint8Array(data);
    } else if (data instanceof Uint8ClampedArray) {
        return new Uint8Array(data.buffer, data.byteOffset, data.byteLength);
    } else if (data instanceof DataView) {
        return new Uint8Array(data.buffer, data.byteOffset, data.byteLength);
    } else if (typeof Buffer !== 'undefined' && data instanceof Buffer) {
        return new Uint8Array(data.buffer, data.byteOffset, data.byteLength);
    } else {
        throw new TypeError('Unsupported data type for conversion to Uint8Array');
    }
};

function isWorker() {
    return (typeof WorkerGlobalScope !== 'undefined' && self instanceof WorkerGlobalScope);
}

function s() {
    return ((typeof window !== 'undefined' && window) ||
        (typeof globalThis !== 'undefined' && globalThis) ||
        (typeof global !== 'undefined' && global) ||
        (typeof self !== 'undefined' && self));
}

export const decodeImageAsync = (data /*byte[]*/, filename /*str*/ , targetWidth /*int*/, targetHeight /*int*/, res, rej) => { /* {
        data: Uint8Array,
        width: number,
        height: number
    
    }*/
   if(!isWorker()||!filename.toLowerCase().endsWith('.svg')) {
        ImageLoader.decodeImage(_u(data), filename, targetWidth, targetHeight ).then(res).catch(e=>rej( String(e)));  
   } else{
        Binds.fireEvent("decodeImage", _u(data), filename, targetWidth, targetHeight).then(res).catch(e=>rej( String(e)));
   }
}

export const helloBinds = () => {
    console.log("nge.js is loaded!");
    const g = s();
    if (g) {
        console.log("User Agent: " + (g.navigator ? g.navigator.userAgent : "unknown"));
        console.log("Platform: " + (g.navigator ? g.navigator.platform : "unknown"));
    }
}
 


 





export const loadScriptAsync = async (script, res, rej) => {
    const isWorker = (typeof WorkerGlobalScope !== 'undefined' && self instanceof WorkerGlobalScope);
    if(isWorker) {
        try {
            console.log("Loading script: " + script + " in worker");
            importScripts(script);
            console.log("Script loaded: " + script);
            res();
        } catch (e) {
            const code = await fetch(script).then(r => r.text());
            eval(code);
            console.log("Script loaded via fetch+eval: " + script);
            res();
        }
    } else {
        console.log("Loading script: " + script);
        const g = s();
        const scriptElement = g.document.createElement("script");
        scriptElement.src = script;
        g.document.head.appendChild(scriptElement);
    
        scriptElement.onload = () => {
            console.log("Script loaded: " + script);
            res();
        }
        scriptElement.onerror = (e) => {
            console.error("Failed to load script: " + script, e);
            rej(String(e));
        }
    }
};




export const setPageTitle = (title) => {
    Binds.fireEvent("setPageTitle", title);
}

export const toggleFullscreen = (v) =>{
    Binds.fireEvent("toggleFullscreen", v);
}

export const togglePointerLock = (v) =>{
    Binds.fireEvent("togglePointerLock", v);
}

 
// export const addEventListener = (event, fun) =>{
//     Binds.addEventListener(event, fun);
// }

// export const removeEventListener = (event, fun) =>{
//     Binds.removeEventListener(event, fun);
// }

export const waitNextFrame = (callback) => {
    const l = () => {
        try{
            Binds.removeEventListener("render", l);
        } catch(e){
            console.warn("Error removing render listener in waitNextFrame", e);
        }
        try{
            callback();
        } catch(e){
            console.error("Error in waitNextFrame callback", e);
        }
    }
    Binds.addEventListener("render", l);
}

// export const fireEventAsync = (event, args, res,rej) => {
//     Binds.fireEvent(event, ...args).then(res).catch(e=>rej(String(e)));
// }

export const getRenderTargetAsync = (res, rej) => {
    Binds.fireEvent("getRenderTarget").then(res).catch(e=>rej(String(e)));
}

export const addResizeRenderTargetListener = (fun)=>{
    Binds.addEventListener("resizeRenderTarget", fun);
}

export const addSwapRenderTargetListener = (fun)=>{
    Binds.addEventListener("swapRenderTarget", fun);
}

export const addInputEventListener = (event, fun) => {
    Binds.addEventListener(event, fun);
}

export const removeInputEventListener = (event, fun) => {
    Binds.removeEventListener(event, fun);
}




// audio
export const addAudioEndListener = (fun) => {
    Binds.addEventListener("audioSourceEnded", fun);
};

export const createAudioContextAsync = (sampleRate, id, res, rej) => {
    Binds.fireEvent("createAudioContext", sampleRate, id).then(res).catch(e=>rej(String(e)));
};

export const freeAudioContext = (id) => {
    Binds.fireEvent("freeAudioContext", id);
};

export const createAudioBufferAsync = (ctxId, id, f32channelData, lengthInSamples, sampleRate, res, rej) => {
    Binds.fireEvent("createAudioBuffer", ctxId, id, f32channelData, lengthInSamples, sampleRate).then(res).catch(e=>rej(String(e)));
};

export const freeAudioBuffer = (ctxId, bufId) => {
    Binds.fireEvent("freeAudioBuffer", ctxId, bufId);
};

export const createAudioSourceAsync = (ctxId, id,  res, rej) => {
    Binds.fireEvent("createAudioSource", ctxId, id).then(res).catch(e=>rej(String(e)));
};

export const freeAudioSource = (ctxId, srcId) => {
    Binds.fireEvent("freeAudioSource", ctxId, srcId);
};

export const setAudioBufferAsync = (ctxId, srcId, bufId, res, rej) => {
    Binds.fireEvent("setAudioBuffer", ctxId, srcId, bufId).then(res).catch(e=>rej(String(e)));
};

export const setAudioPositional = (ctxId, srcId, v) => {
    Binds.fireEvent("setAudioPositional", ctxId, srcId, v);
};

export const setContextAudioEnv = (ctxId, i8data) => {
    Binds.fireEvent("setContextAudioEnv", ctxId, i8data);
};

export const setAudioPosition = (ctxId, srcId, x, y, z) => {
    Binds.fireEvent("setAudioPosition", ctxId, srcId, x, y, z);
};

export const setAudioVelocity = (ctxId, srcId, x, y, z) => {
    Binds.fireEvent("setAudioVelocity", ctxId, srcId, x, y, z);
};

export const setAudioMaxDistance = (ctxId, srcId, v) => {
    Binds.fireEvent("setAudioMaxDistance", ctxId, srcId, v);
};

export const setAudioRefDistance = (ctxId, srcId, v) => {
    Binds.fireEvent("setAudioRefDistance", ctxId, srcId, v);
};

export const setAudioDirection = (ctxId, srcId, x, y, z) => {
    Binds.fireEvent("setAudioDirection", ctxId, srcId, x, y, z);
};

export const setAudioConeInnerAngle = (ctxId, srcId, v) => {
    Binds.fireEvent("setAudioConeInnerAngle", ctxId, srcId, v);
};

export const setAudioConeOuterAngle = (ctxId, srcId, v) => {
    Binds.fireEvent("setAudioConeOuterAngle", ctxId, srcId, v);
};

export const setAudioConeOuterGain = (ctxId, srcId, v) => {
    Binds.fireEvent("setAudioConeOuterGain", ctxId, srcId, v);
};

export const setAudioLoop = (ctxId, srcId, v) => {
    Binds.fireEvent("setAudioLoop", ctxId, srcId, v);
};

export const setAudioPitch = (ctxId, srcId, v) => {
    Binds.fireEvent("setAudioPitch", ctxId, srcId, v);
};

export const setAudioVolume = (ctxId, srcId, v) => {
    Binds.fireEvent("setAudioVolume", ctxId, srcId, v);
};

export const getAudioPlaybackRateAsync = (ctxId, srcId, res, rej) => {
    Binds.fireEvent("getAudioPlaybackRate", ctxId, srcId).then(res).catch(e=>rej(String(e)));
};

export const playAudioSourceAsync = (ctxId, srcId, res, rej) => {
    Binds.fireEvent("playAudioSource", ctxId, srcId).then(res).catch(e=>rej(String(e)));
};

export const pauseAudioSourceAsync = (ctxId, srcId, res, rej) => {
    Binds.fireEvent("pauseAudioSource", ctxId, srcId).then(res).catch(e=>rej(String(e)));
};

export const stopAudioSourceAsync = (ctxId, srcId, res, rej) => {
    Binds.fireEvent("stopAudioSource", ctxId, srcId).then(res).catch(e=>rej(String(e)));
};

export const setAudioContextListener = (
    ctxId, 
    px, py, pz, 
    dx, dy, dz, 
    ux, uy, uz
) => {
    Binds.fireEvent("setAudioContextListener", 
        ctxId, 
        px, py, pz, 
        0,0,0,
        dx, dy, dz, 
        ux, uy, uz
    );
}

let baseUrl = null;
export const getBaseURLAsync = (res, rej) => {
    if(baseUrl){
        res(baseUrl);
        return;
    }
    Binds.fireEvent("getBaseURL").then( (url) => {
        baseUrl = url;
        res(url);
    }).catch(e=>rej(String(e)));
}

export const connectNip07BackendAsync = (res, rej) => {
    Nip07.inject().then(res).catch(e=>rej(String(e)));
}


export const pingFrontEnd = () => {
    Binds.fireEvent("ping");
}


export const runWithDelay=(f, t) => {
    setTimeout(f, t);
}