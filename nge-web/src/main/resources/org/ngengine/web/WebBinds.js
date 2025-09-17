 

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

function s() {
    return ((typeof window !== 'undefined' && window) ||
        (typeof globalThis !== 'undefined' && globalThis) ||
        (typeof global !== 'undefined' && global) ||
        (typeof self !== 'undefined' && self));
}

function mimeFromFilename(name) {
    if (!name || typeof name !== 'string') return 'image/*';
    const ext = name.split('.').pop().toLowerCase();
    switch (ext) {
        case 'png': return 'image/png';
        case 'jpg':
        case 'jpeg': return 'image/jpeg';
        case 'webp': return 'image/webp';
        case 'gif': return 'image/gif';
        case 'bmp': return 'image/bmp';
        case 'avif': return 'image/avif';
        case 'svg': return 'image/svg+xml';
        default: return 'image/*';
    }
}


async function _decodeImageAsync (data, filename, scaleW, scaleH) { /*
    returns { data: Uint8Array, width: number, height: number }
*/
    const g = ((typeof window !== 'undefined' && window) || globalThis);
    const URL_ = g.URL || g.webkitURL;

    // const doc = g.document;
    let u8in = new Uint8Array(data);
    let svgText;

    if (filename && filename.toLowerCase().endsWith('.svg')){
        svgText = new TextDecoder().decode(u8in);
        svgText = svgText.replace(/currentColor/g, "#ffffff");
        u8in = new TextEncoder().encode(svgText);
    }


    const blob = new Blob([u8in], { type: mimeFromFilename(filename) });
    const url = URL_.createObjectURL(blob);
    let canvas = null;
    try {
        const img = new Image();
        const p = new Promise((res, rej) => { img.onload = res; img.onerror = () => rej(new Error('Image decode error')); });
        img.src = url;
        await p;

        // Intrinsic dimensions
        let w = (img.naturalWidth || img.width) | 0;
        let h = (img.naturalHeight || img.height) | 0;

        // Fallback for SVG without explicit size
        if ((w === 0 || h === 0) && svgText) {
            try {
                const m = svgText.match(/viewBox\s*=\s*"([\d.\s]+)"/i);
                if (m) {
                    const parts = m[1].trim().split(/\s+/).map(Number);
                    if (parts.length === 4) {
                        const vw = parts[2];
                        const vh = parts[3];
                        if (vw > 0 && vh > 0) {
                            const maxDim = 256;
                            const scale = vw > vh ? maxDim / vw : maxDim / vh;
                            w = Math.max(1, (vw * scale) | 0);
                            h = Math.max(1, (vh * scale) | 0);
                        }
                    }
                }
                if (w === 0 || h === 0) { w = 256; h = 256; }
            } catch (_) {
                w = 256; h = 256;
            }
        }

        // Apply requested scaling (maintain aspect if only one provided)
        let dw = (typeof scaleW === 'number' && scaleW > 0) ? scaleW | 0 : 0;
        let dh = (typeof scaleH === 'number' && scaleH > 0) ? scaleH | 0 : 0;
        if (dw && !dh) {
            dh = Math.max(1, Math.round(h * (dw / w)));
        } else if (dh && !dw) {
            dw = Math.max(1, Math.round(w * (dh / h)));
        }
        if (!dw) dw = w;
        if (!dh) dh = h;

        // canvas = doc.createElement('canvas');
        canvas = new g.OffscreenCanvas(dw, dh);
        // canvas.style.cssText = 'position:absolute;top:-10000px;left:-10000px;visibility:hidden;';
        // doc.body.appendChild(canvas); // some browsers require canvas to be in DOM for certain operations
        canvas.width = dw;
        canvas.height = dh;

        const ctx = canvas.getContext('2d',{
                alpha: true,
                premultipliedAlpha: false   ,
                colorSpace: 'srgb', 
                willReadFrequently: true 
        });
        ctx.clearRect(0, 0, dw, dh);
        ctx.fillStyle = 'rgba(0,0,0,0)';
        ctx.fillRect(0, 0, dw, dh);
        ctx.imageSmoothingEnabled = true;
        ctx.imageSmoothingQuality = 'high';

        if (svgText) {
            img.width = dw;
            img.height = dh;
            ctx.drawImage(img, 0, 0, dw, dh);
            const tempData = ctx.getImageData(0, 0, dw, dh);
            ctx.putImageData(tempData, 0, 0);
        } else {
            ctx.drawImage(img, 0, 0, dw, dh);
        }
        ctx.fillRect(0, 0, dw, dh);

        const imageData = ctx.getImageData(0, 0, dw, dh, {
            colorSpace: 'srgb'
        });
        const pixels = imageData.data;  
       

        return {
            data: new Uint8Array(pixels.buffer, pixels.byteOffset, pixels.length),
            width: dw,
            height: dh
        };
    } finally {
        try { URL_.revokeObjectURL(url); } catch(e) {}
 
        
    }
}

export const decodeImageAsync = (data /*byte[]*/, filename /*str*/ , targetWidth /*int*/, targetHeight /*int*/, res, rej) => { /* {
        data: Uint8Array,
        width: number,
        height: number
    
    }*/
    _decodeImageAsync(_u(data), filename, targetWidth, targetHeight ).then(res).catch(rej);  

}

export const helloBinds = () => {
    console.log("nge.js is loaded!");
    const g = s();
    if (g) {
        console.log("User Agent: " + (g.navigator ? g.navigator.userAgent : "unknown"));
        console.log("Platform: " + (g.navigator ? g.navigator.platform : "unknown"));
    }
}

let PIXELS_PER_LINE = null;
export const getPixelDeltaScroll = (deltaValue, deltaMode) => {
    const g = s();
    const doc = g && g.document;

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
        return deltaValue; // pixels
    } else if (deltaMode === 1) {
        return deltaValue * pixelsPerLine; // lines -> pixels
    } else {
        // pages -> pixels
        const viewportHeight = doc && doc.documentElement
            ? Math.max(doc.documentElement.clientHeight, g.innerHeight || 0)
            : 800;
        const estimatedLinesPerPage = Math.max(1, Math.floor(viewportHeight / pixelsPerLine));
        return deltaValue * pixelsPerLine * estimatedLinesPerPage;
    }
}


export const canvasFitParent =  (canvas) => {
    const parent = canvas.parentElement;
    canvas.width = parent.clientWidth;
    canvas.height = parent.clientHeight;
    canvas.setAttribute("width", parent.clientWidth);
    canvas.setAttribute("height", parent.clientHeight);
}



export const loadScriptAsync = async (script, res, rej) => {
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
        rej(e);
    }
};


export const setPageTitle = (title) => {
    if (typeof title !== 'string') return;
    const g = s();
    if (g && g.document && g.document.title !== undefined) {
        g.document.title = title;
    }
}