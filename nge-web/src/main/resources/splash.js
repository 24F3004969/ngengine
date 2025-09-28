

async function ready(splashEl){
    const infoLast = splashEl.querySelector(".lastIndexed");
    const titleEl = splashEl.querySelector("h2");
    const progressBar = splashEl.querySelector("progress");
    const button = splashEl.querySelector("button#play");

    if(titleEl) titleEl.textContent = "Ready";
    if(infoLast) infoLast.textContent = "";
    if(progressBar) {
        progressBar.value = 100;
        progressBar.max = 100;
    }
    if(button) {
        button.textContent = "Play";
    }
    if('serviceWorker' in navigator && navigator.serviceWorker.controller) {
        navigator.serviceWorker.controller.postMessage({ type: "stop-preload" });
    }
}

async function main(){
    const splashEl = document.querySelector("#ngeSplash");
    if(!splashEl){
        console.warn("No splash element found");
        ready(splashEl);
        return;
    }

    const infoFiles = splashEl.querySelector(".files");
    const infoSizes = splashEl.querySelector(".size");
    const infoLast = splashEl.querySelector(".lastIndexed");
    const progressBar = splashEl.querySelector("progress");
    const button = splashEl.querySelector("button#play");
    button.addEventListener("click", (e) => {
        ready(splashEl);
        splashEl.remove();
    });
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.register('/sw.js').then(reg => {
            if (!navigator.serviceWorker.controller) {
                window.location.reload();
            } else {
                navigator.serviceWorker.controller.postMessage({ type: "start-preload" });
            }
        });
        navigator.serviceWorker.addEventListener("message", (event) => {
            if (
                !event.source ||
                !event.source instanceof ServiceWorker ||
                event.source.scriptURL !== location.origin + '/sw.js' ||
                event.origin !== location.origin                
            )  return;

            if (event.data.type === "preload-progress") {
                if(infoFiles) {
                    const v = event.data.done + "/" + event.data.total;
                    infoFiles.textContent = v;
                }

                const progress = event.data.total > 0 ? (event.data.done / event.data.total) : 1;
                if(progressBar) {
                    progressBar.value = progress * 100;
                }

                if(infoSizes) {
                    const totalGB = (event.data.totalBytes / (1024*1024*1024)).toFixed(2) + "GB";
                    const doneGB = (event.data.doneBytes / (1024*1024*1024)).toFixed(2) + "GB";
                    const v = doneGB + "/" + totalGB;
                    infoSizes.textContent = v;
                }

                if (infoLast && event.data.last) {
                    infoLast.textContent = "Loading " + event.data.last+"...";
                }
                
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





window.addEventListener('load', main);