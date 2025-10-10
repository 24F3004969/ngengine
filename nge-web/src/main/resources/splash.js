const CONFIG_PATH = "ngeapp.json";

// update the splash screen progress bar
function updateProgress(
    splashEl,
    lastFile,
    done,
    total,
    doneBytes,
    totalBytes,
    status,
    buttonLabel
){
    const infoLast = splashEl.querySelector(".lastIndexed");
    const titleEl = splashEl.querySelector("h2");
    const progressBar = splashEl.querySelector("progress");
    const button = splashEl.querySelector("button#play");
    const infoFiles = splashEl.querySelector(".files");
    const infoSizes = splashEl.querySelector(".size");

    if(titleEl) titleEl.textContent = status;
    if(infoLast) infoLast.textContent = lastFile;

    if(done !=null && total != null) {
        if(infoFiles){
            const v = done + "/" + total;
            infoFiles.textContent = v;
        }
        if(progressBar) {
            const progress = total > 0 ? (done / total) : 1;
            progressBar.value = progress * 100;
            progressBar.max = 100;
        }
    } else{
        if(infoFiles){
            infoFiles.textContent = "";
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
        infoSizes.textContent = "";
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
    updateProgress(splashEl, "", null, null, null, null, "Ready", "Play");
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
        navigator.serviceWorker.register(serviceWorkerPath,{
            scope: serviceWorkerScope
        }).then(reg => {
            if (!navigator.serviceWorker.controller) {
                window.location.reload();
            } else {
                navigator.serviceWorker.controller.postMessage({ type: "start-preload", config: CONFIG_PATH });
            }
        }).catch(e => {
            console.error("Service worker registration failed", e);
            alert("Service worker registration failed");
        });
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