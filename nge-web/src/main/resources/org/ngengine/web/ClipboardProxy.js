import Binds from './WebBindsHub.js';

function s() {
    return ((typeof window !== 'undefined' && window) ||
        (typeof globalThis !== 'undefined' && globalThis) ||
        (typeof global !== 'undefined' && global) ||
        (typeof self !== 'undefined' && self));
}

async function inject(){
    if(typeof navigator !== 'undefined' && typeof navigator.clipboard !== 'undefined') return;
    const g  = s();
    g.ngeClipboard = {
        writeText: async (text) => {
            return await Binds.fireEvent("clipboardWriteText", text);
        },
        readText: async () => {
            return await Binds.fireEvent("clipboardReadText");
        }
    };

}

function bind(){
    Binds.addEventListener("clipboardWriteText", async (text)=>{
        if(typeof navigator !== 'undefined' && typeof navigator.clipboard !== 'undefined'){
            return await navigator.clipboard.writeText(text);
        }
        throw new Error("Clipboard API not available");
    });
    Binds.addEventListener("clipboardReadText", async ()=>{
        if(typeof navigator !== 'undefined' && typeof navigator.clipboard !== 'undefined'){
            return await navigator.clipboard.readText();
        }
        throw new Error("Clipboard API not available");
    });
}




export default { inject, bind};