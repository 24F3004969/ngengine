const CONFIG_PATH = "ngeapp.json";

let updateComplete = false;

// update the splash screen progress bar
function updateProgress(
    splashEl,
    lastFile,
    done,
    total,
    doneBytes,
    totalBytes,
    status,
    buttonLabel,
    complete
){
    if(updateComplete)return;
    if(complete)updateComplete = true;
    const infoLast = splashEl.querySelector(".lastIndexed");
    const titleEl = splashEl.querySelector("h2");
    const progressBar = splashEl.querySelector("progress");
    const button = splashEl.querySelector("button#play");
    const infoFiles = splashEl.querySelector(".files");
    const infoSizes = splashEl.querySelector(".size");

    if(titleEl) titleEl.textContent = status;
    if(infoLast) {
        const maxLen = 32;
        let lastFileShort = lastFile;
        if(lastFile.length > maxLen) {
            const extIndex = lastFile.lastIndexOf('.');
            const ext = extIndex >=0 ? lastFile.substring(extIndex) : '';
            const name = extIndex >=0 ? lastFile.substring(0, extIndex) : lastFile;
            if(name.length > maxLen - ext.length - 3) {
                lastFileShort = name.substring(0, (maxLen - ext.length - 3)/2) + "..." + name.substring(name.length - (maxLen - ext.length - 3)/2) + ext;
            }
        }
        infoLast.textContent = lastFileShort;
    }

    if(done !=null && total != null) {
        if(infoFiles){
            const v = done + "/" + total;
            infoFiles.textContent = v;
        }
    } else{
        if(infoFiles){
            infoFiles.textContent = " ";
        }
        if(progressBar) {
            progressBar.value = 100;
            progressBar.max = 100;
        }
    }

    if(doneBytes !=null && totalBytes != null && infoSizes) {
        const totalGB = (totalBytes / (1024*1024*1024)).toFixed(2) + "GB";
        const doneGB = (doneBytes / (1024*1024*1024)).toFixed(2) + "GB";
        const v = doneGB + "/" + totalGB;
        infoSizes.textContent = v;
    } else if(infoSizes){
        infoSizes.textContent = " ";
    }
    if (progressBar) {
        let progress = 1.0;
        if (doneBytes != null && totalBytes != null && totalBytes > 0) {
            progress = doneBytes / totalBytes;
        } else if (done != null && total != null && total > 0) {
            progress = done / total;
        } 
        progressBar.value = progress * 100;
        progressBar.max = 100;
    }
                
    if(button && buttonLabel) { 
        button.textContent = buttonLabel;
        button.disabled = false;
    } else {
        button.textContent = "Loading...";
        button.disabled = true;
    }
}


// toggle the splash screen to ready state (=everything loaded)
async function ready(splashEl){
    updateProgress(splashEl, "", null, null, null, null, "Ready", "Play", true);
    if('serviceWorker' in navigator && navigator.serviceWorker.controller) {
        navigator.serviceWorker.controller.postMessage({ type: "stop-preload" });
    }    
}

// load the config file
async function loadConfig(){
    const url = CONFIG_PATH;
    const config = fetch(url).then(r=> r.json()).catch(e=>{
        console.warn("Failed to load config", e);
        return {};
    });
    if (config.bundle && !'serviceWorker' in navigator) {
        alert("This application requires a browser with Service Worker support.");
        throw new Error("Service workers required");
    }
    return config;
}


// start the  launcher
async function startPreloader(splashEl){
    if ('serviceWorker' in navigator) {
        const baseURL = new URL('./', window.location.href);
        const serviceWorkerPath = new URL('sw.js', baseURL).pathname;
        const serviceWorkerScope = baseURL.pathname;
        navigator.serviceWorker.addEventListener("message", (event) => {
            if (
                !event.source ||
                !event.source instanceof ServiceWorker ||
                event.origin !== location.origin                
            ) return;
            

            if (event.data.type === "preload-progress") {
                const canSkip = event.data.canSkip;
                const status = event.data.status || "Loading...";
                updateProgress(
                    splashEl,
                    event.data.last || "",
                    event.data.done,
                    event.data.total,
                    event.data.doneBytes,
                    event.data.totalBytes,
                    status,
                    canSkip ? "Skip and Play" : null
                );
                
                if (event.data.done >= event.data.total) {
                    ready(splashEl);
                }
            }
        });

     
        navigator.serviceWorker.addEventListener('controllerchange', () => {
            console.log('New service worker controller available');
            if (navigator.serviceWorker.controller) {
                navigator.serviceWorker.controller.postMessage({ 
                    type: "start-preload", 
                    config: CONFIG_PATH 
                });
            }
        });
        
        navigator.serviceWorker.register(serviceWorkerPath+"?t="+Date.now(),{
            scope: serviceWorkerScope
        }).then(reg => {
            if (!navigator.serviceWorker.controller) {
                console.log("No active service worker - reloading");
                window.location.reload();
            } 
        }).catch(e => {
            console.error("Service worker registration failed", e);
            alert("Service worker registration failed");
        });
       
        console.log("Service worker registered");
    } else {
        console.warn("Service workers are not supported.");
        ready(splashEl);
    }

    
}

// launch the web app
async function launchWebApp(splashEl,config){
    splashEl.remove();
    await import("./launcher.js").then(m=>{
        m.default(config);
    }).catch(e=>{
        console.error("Failed to launch application", e);
        alert("Failed to launch application: " + e);
    });
}

 
async function main(){
    const splashEl = document.querySelector("#ngeSplash");
    if(!splashEl){
        console.warn("No splash element found");
        ready(splashEl);
        return;
    }
    const config = await loadConfig();

    const button = splashEl.querySelector("button#play");
    button.addEventListener("click", (e) => {
        ready(splashEl);
        launchWebApp(splashEl, config);
    });

    updateProgress(splashEl, "", null, null, null, null, "Starting...", null);

    await startPreloader(splashEl);
    
}





window.addEventListener('load', main);