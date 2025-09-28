const USE_CACHE = false;
const INDEX_URL = "./resources.index.txt";
const PRELOAD_IGNORE_URL = "./preload-ignore.txt";
const CACHE_BASENAME = "ngeapp";
const CACHE_VERSION = "v1";
const PREFETCH_WORKERS = 5;
const HASHES_STORE = "__hashes__";


let CACHE = null;
let HASHES = null;
let INDEX = null;
let PREFETCH = true;
let NUM_PREFETCHED = 0;
let NUM_ENTRIES_TO_PRELOAD = null;
let LAST_FETCHED = "";
let BYTES_TO_PRELOAD = 0;
let BYTES_PREFETCHED = 0;
let PRELOAD_IGNORE_LIST  = null;

async function getCache() {
    if(CACHE) return CACHE;
    const origin = self.location.origin;
    const scope = self.registration.scope;
    const input = origin + scope;

    const encoder = new TextEncoder();
    const data = encoder.encode(input);
    const digest = await crypto.subtle.digest("SHA-256", data);

    let hex = "";
    const bytes = new Uint8Array(digest);
    for (let i = 0; i < bytes.length; i++) {
        hex += bytes[i].toString(16).padStart(2, "0");
    }

    const shortHash = hex.substring(0, 16);
    const scopedName = `${CACHE_BASENAME}-${shortHash}-${CACHE_VERSION}`;

    CACHE = await caches.open(scopedName);
    if(!CACHE) throw new Error("Failed to open cache");
    return CACHE;
}


async function getStoredHashes() {
    if (HASHES) return HASHES;
    try {
        const cache = await getCache();
        const resp = await cache.match(HASHES_STORE);
        if (!resp) {
            HASHES = {};
        }
        else HASHES = await resp.json();
        if(!HASHES) throw new Error("No hashes");
    } catch (e) {
        console.warn(e);
        HASHES = {};
    }
    return HASHES;
}

async function storeHash(url, hash) {
    const hashes = await getStoredHashes();
    hashes[url] = hash;    
    try{
        const cache = await getCache();
        await cache.put(
            HASHES_STORE,
            new Response(JSON.stringify(hashes), {
                headers: { "Content-Type": "application/json" }
            })
        );
    } catch(e) {
        console.warn("Failed to store hashes", e);
    }
}

async function canPreload(path){
    if(!PRELOAD_IGNORE_LIST){
        try{
            const resp = await fetch(PRELOAD_IGNORE_URL);
            if(!resp.ok) throw new Error("Failed to fetch preload ignore list: " + resp.status);
            const text = await resp.text();
            const lines = text.trim().split("\n");
            const entries = new Set();
            for (let i = 0; i < lines.length; i++) {
                const line = lines[i].trim();
                if(line) entries.add(line);
            }
            PRELOAD_IGNORE_LIST = entries;
        } catch(e){
            console.warn("Failed to fetch preload ignore list", e);
            PRELOAD_IGNORE_LIST = new Set();
        }
    }
    if (path.startsWith("/")) path = path.substring(1);
    
    for(const ignore of PRELOAD_IGNORE_LIST){
        if(path.startsWith(ignore)) return false;
    }
    return true;

}

async function getIndex() {
    if(INDEX) return INDEX;
    try {
        const resp = await fetch(INDEX_URL);
        if(!resp.ok) throw new Error("Failed to fetch index: " + resp.status);
        const text = await resp.text();
        const lines = text.trim().split("\n");
        const entries = {};
        for (let i = 0; i < lines.length; i++) {
            const parts = lines[i].split(/\s+/);
            if (parts.length < 3) continue;
            const hash = parts[0];
            const size = parseInt(parts[1]);
            const path = parts.slice(2).join(" ");
            entries[path] = { hash, size, path };
        }
        INDEX = entries;
    } catch(e) {
        console.warn("Failed to fetch index", e);
        INDEX = {};
    }
    return INDEX;
   
}

async function getIndexEntry(urlOrPath){
    let path = decodeURIComponent(new URL(urlOrPath).pathname);
    if(path.startsWith("/")) path = path.substring(1);
    const index = await getIndex();
    return index[path];
}

async function countPreloadEntries(){
    if(NUM_ENTRIES_TO_PRELOAD) return [NUM_ENTRIES_TO_PRELOAD, BYTES_TO_PRELOAD];
    const index = await getIndex();
    NUM_ENTRIES_TO_PRELOAD = 0;
    for(const path in index){
        if(await canPreload(path)){
            NUM_ENTRIES_TO_PRELOAD++;
            BYTES_TO_PRELOAD += index[path].size;
        }
    }
    return [NUM_ENTRIES_TO_PRELOAD, BYTES_TO_PRELOAD];
}



 
async function getFromCache(url){
    if(!USE_CACHE) return null;
    // check if cached
    const hashes = await getStoredHashes();
    const cachedHash = hashes[url];
    if (!cachedHash) {
        return null;
    }
    
    // check if still valid
    const entry = await getIndexEntry(url);
    const hash = entry?.hash;
    if (hash !== cachedHash) {
        return null;
    }

    // return from cache
    try{
        const cache = await getCache();
        const resp = await cache.match(url);
        if (!resp || !resp.ok) return null;
        return resp;
    } catch(e) {
        console.warn("Failed to get from cache", e);
        return null;
    }
}

async function storeInCache(url, response, hash) {
    try{
        const cache = await getCache();
        await cache.put(url, response);
        await storeHash(url, hash);
    } catch(e) {
        console.warn("Failed to store in cache", e);
    }
}

async function createHash(response) {
    const data = await response.arrayBuffer();
    const digest = await crypto.subtle.digest("SHA-256", data);
    let hex = "";
    const bytes = new Uint8Array(digest);
    for (let i = 0; i < bytes.length; i++) {
        hex += bytes[i].toString(16).padStart(2, "0");
    }
    return hex;
}

async function fetchAndCache(request, awaitCaching = false){
    const resp = await fetch(request);
    if (resp.ok) {
        const cachable = resp.clone();
        LAST_FETCHED = new URL(request.url).pathname;
        const c = createHash(resp.clone()).then(hash => {
            storeInCache(request.url, cachable, hash).catch(e => {
                console.warn("Failed to store in cache", e);
            }).then(r=>{
                // console.log("Cached ", request.url);
            });
        }).catch(e => {
            console.warn("Failed to create hash", e);
        });
        if (awaitCaching) await c;
    }
    return resp;
}


// intercept fetch requests
// return cached value if available and up to date
// otherwise fetch and cache
self.addEventListener("fetch", (event) => {
    event.respondWith((async () => {
      
        const cached = await getFromCache(event.request.url);
        if(cached) return cached;

        try {
            return await fetchAndCache(event.request);           
        } catch (err) {
            return new Response("Offline", { status: 503 });
        }
    })());
});


async function prefetchResources() {
    if(!PREFETCH) return;
    const index = await getIndex();
 
    const entries = Object.values(index);
    if (entries.length === 0) return;

    // Sort largest → smallest
    entries.sort((a, b) => b.size - a.size);

    let i = 0;

    async function worker() {
        while (i < entries.length) {
            const entry = entries[i++];
            if(!await canPreload(entry.path))continue;
            try {
                if (!PREFETCH) break;
                const url = new URL(entry.path, self.location).href;
                const cached = await getFromCache(url);
                if (!cached){
                    const req = new Request(url);
                    const resp = await fetchAndCache(req, true);
                    if (!resp.ok) throw new Error("Failed to fetch: " + resp.status);
                } else {
                }
                
            } catch (e) {
                console.warn(e);
            } finally {
                BYTES_PREFETCHED += entry.size;
                NUM_PREFETCHED++;
                try{
                    const [maxEntries, maxBytes]= await countPreloadEntries();
                    if (NUM_PREFETCHED >= maxEntries) NUM_PREFETCHED = maxEntries;
                    if (BYTES_PREFETCHED >= maxBytes) BYTES_PREFETCHED = maxBytes;
                } catch(e){
                    console.warn(e);
                }
                
                updateClient();
            }
        }
    }

    const workers = [];
    for (let i = 0; i < PREFETCH_WORKERS; i++) {
        workers.push(worker());
    }
    await Promise.all(workers);
}


async function stopPrefetch(){
    PREFETCH = false;
    console.log("Stopping prefetch as requested by client");

}

let updateTask = null;
async function updateClient(){
    if (!updateTask) {
        updateTask = setTimeout(async ()=>{
            try {
                updateTask = null
                const clients = await self.clients.matchAll({ includeUncontrolled: true, type: 'window' });
                const [maxEntries, maxBytes]= await countPreloadEntries();
                for (const client of clients) {
                    client.postMessage({
                        type: "preload-progress",
                        total: maxEntries,
                        done: NUM_PREFETCHED,
                        last: LAST_FETCHED,
                        totalBytes: maxBytes,
                        doneBytes: BYTES_PREFETCHED
                    });
                }
                
            } catch (e) {
                console.warn(e);
            }
        },200);
    }
}

async function startPrefetch(){
    PREFETCH = true;
    NUM_PREFETCHED = 0;
    BYTES_PREFETCHED = 0;
    LAST_FETCHED = "";

    const cache = await getCache();
    const index = await getIndex();
    const requests = await cache.keys();
    for (const request of requests) {
        const url = request.url;
        // Skip the hashes store
        if (url.endsWith(HASHES_STORE)) continue;
        // If not in index, delete from cache
        let relPath = url.replace(self.location.origin, "");
        if (relPath.startsWith("/")) relPath = relPath.substring(1);
        relPath = decodeURIComponent(relPath);
        if (!index[relPath]) {
            await cache.delete(request);
            console.log("Clearing stale cache for:", relPath);
        }
    }

    await prefetchResources();
    await updateClient(); // final update
}


self.addEventListener("message", async (event) => {
    try{
        if (
            event.data &&
            event.origin === location.origin &&
            event.source &&            
            event.source instanceof WindowClient
        ) {
            if (event.data.type === "stop-preload"){
                await stopPrefetch();
            } else if (event.data.type === "start-preload"){
                await startPrefetch();
            } else {
                console.warn("Unknown message type", event.data.type);
            }
        }   
    } catch(e){
        console.warn("Failed to process message", e);
    }
});


// starts prefetching
self.addEventListener("install", (event) => {
    event.waitUntil((async () => {
        self.skipWaiting();
    })());
});



self.addEventListener("activate", (event) => {
    event.waitUntil((async () => {
        await self.clients.claim();
    })());
});
