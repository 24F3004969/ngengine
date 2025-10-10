importScripts("./zip-core.js");
const USE_CACHE = true;
const INDEX_URL = "./resources.index.txt";
const PRELOAD_IGNORE_URL = "./preload-ignore.txt";
const CACHE_BASENAME = "ngeapp";
const CACHE_VERSION = "v1";
const PREFETCH_WORKERS = 5;
const HASHES_STORE = "__hashes__";
const BUNDLE_KEY = "bundle.zip";


let CACHE = null;
let HASHES = null;
let INDEX = null;
let PREFETCH = true;
let NUM_PREFETCHED = 0;
let NUM_ENTRIES_TO_PRELOAD = null;
let LAST_FETCHED = "";
let BYTES_TO_PRELOAD = 0;
let BYTES_PREFETCHED = 0;
let PRELOAD_IGNORE_LIST = null;



let bundledLoaded = false;
let bundleEntries = undefined;


async function updateBundle(url, hash) {
    updateProgress(
        "",
        0,
        1,
        null,
        null,
        "Downloading bundled resources...",
        false
    );

    const cache = await getCache();

    // check if already up to date
    const hashes = await getStoredHashes();
    const storedHash = hashes[BUNDLE_KEY];
    if (hash && storedHash === hash) {
        console.log("Bundle already up-to-date");
        return;
    }

    // fetch with progress
    const resp = await fetch(url);
    if (!resp.ok || !resp.body) throw new Error("Failed to fetch bundle");

    const totalBytes = Number(resp.headers.get("content-length")) || null;
    const reader = resp.body.getReader();
    const chunks = [];
    let received = 0;

    while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        chunks.push(value);
        received += value.length;

        updateProgress(
            "",
            0,
            1,
            received,
            totalBytes,
            "Downloading bundled resources...",
            false
        );
    }

    const blob = new Blob(chunks);

    // store bundle
    console.log("Storing bundle in cache...");
    await cache.put(BUNDLE_KEY, new Response(blob));

    // store hash (generate if not provided)
    if (hash) await storeHash(BUNDLE_KEY, hash);
    bundledLoaded = true;
    console.log("Bundle updated");
}

async function getZip(){
   return globalThis.zip;
}

async function loadBundledResources() {
    if (!bundledLoaded) return [];
    if (bundleEntries) return bundleEntries;

    const Zip = await getZip();

    const cache = await getCache();
    const resp = await cache.match(BUNDLE_KEY);
    if (!resp) {
        console.error("No bundle in cache");
        // bundle not available
        bundleEntries = [];
        return [];
    }


    const blob = await resp.blob();
    const blobReader = new Zip.BlobReader(blob);
    const zipReader = new Zip.ZipReader(blobReader);
    bundleEntries = await zipReader.getEntries();
    return bundleEntries;
}


async function getContent(url) {
    const Zip = await getZip();
    const entries = await loadBundledResources();
    let path;
    if (entries) {
        path = new URL(url, self.location).pathname;
        const parentPath = new URL("./", self.location).pathname;
        if (path.startsWith(parentPath)) path = path.substring(parentPath.length);
        if (path.startsWith("/")) path = path.substring(1);
        path = decodeURIComponent(path);
        const entry = entries.find(e => e.filename === path);
        if (entry) {
            const blob = await entry.getData(new Zip.BlobWriter());
            return new Response(blob, {
                headers: { "Content-Type": guessMime(path) }
            });
        }
    } else {
        console.log((path || url), "not found in bundle", "bundleEntries =", entries);
    }

    return fetch(url);
}


function guessMime(path) {
    if (path.endsWith(".png")) return "image/png";
    if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
    if (path.endsWith(".svg")) return "image/svg+xml";
    if (path.endsWith(".js")) return "application/javascript";
    if (path.endsWith(".css")) return "text/css";
    if (path.endsWith(".html")) return "text/html";
    return "application/octet-stream";
}


async function loadConfig(url) {
    return fetch(url).then(r => r.json()).catch(e => {
        console.warn("Failed to load config", e);
        return {};
    });
}

async function getCache() {
    if (CACHE) return CACHE;
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
    if (!CACHE) throw new Error("Failed to open cache");
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
        if (!HASHES) throw new Error("No hashes");
    } catch (e) {
        console.warn(e);
        HASHES = {};
    }
    return HASHES;
}

async function storeHash(url, hash) {
    const hashes = await getStoredHashes();
    hashes[url] = hash;
    try {
        const cache = await getCache();
        await cache.put(
            HASHES_STORE,
            new Response(JSON.stringify(hashes), {
                headers: { "Content-Type": "application/json" }
            })
        );
    } catch (e) {
        console.warn("Failed to store hashes", e);
    }
}

async function canPreload(path) {
    if (!PRELOAD_IGNORE_LIST) {
        try {
            const resp = await getContent(PRELOAD_IGNORE_URL);
            if (!resp.ok) throw new Error("Failed to fetch preload ignore list: " + resp.status);
            const text = await resp.text();
            const lines = text.trim().split("\n");
            const entries = new Set();
            for (let i = 0; i < lines.length; i++) {
                const line = lines[i].trim();
                if (line) entries.add(line);
            }
            PRELOAD_IGNORE_LIST = entries;
        } catch (e) {
            console.warn("Failed to fetch preload ignore list", e);
            PRELOAD_IGNORE_LIST = new Set();
        }
    }
    if (path.startsWith("/")) path = path.substring(1);

    for (const ignore of PRELOAD_IGNORE_LIST) {
        if (path.startsWith(ignore)) return false;
    }
    return true;

}

async function getIndex() {
    if (INDEX) return INDEX;
    try {
        const resp = await getContent(INDEX_URL);
        if (!resp.ok) throw new Error("Failed to fetch index: " + resp.status);
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
    } catch (e) {
        console.warn("Failed to fetch index", e);
        INDEX = {};
    }
    return INDEX;

}

async function getIndexEntry(urlOrPath) {
    let path = decodeURIComponent(new URL(urlOrPath).pathname);
    if (path.startsWith("/")) path = path.substring(1);
    const index = await getIndex();
    return index[path];
}

async function countPreloadEntries() {
    if (NUM_ENTRIES_TO_PRELOAD) return [NUM_ENTRIES_TO_PRELOAD, BYTES_TO_PRELOAD];
    const index = await getIndex();
    NUM_ENTRIES_TO_PRELOAD = 0;
    for (const path in index) {
        if (await canPreload(path)) {
            NUM_ENTRIES_TO_PRELOAD++;
            BYTES_TO_PRELOAD += index[path].size;
        }
    }
    return [NUM_ENTRIES_TO_PRELOAD, BYTES_TO_PRELOAD];
}




async function getFromCache(url) {
    if (!USE_CACHE) return null;
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
    try {
        const cache = await getCache();
        const resp = await cache.match(url);
        if (!resp || !resp.ok) return null;
        return resp;
    } catch (e) {
        console.warn("Failed to get from cache", e);
        return null;
    }
}

async function storeInCache(url, response, hash) {
    try {
        const cache = await getCache();
        await cache.put(url, response);
        await storeHash(url, hash);
    } catch (e) {
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

async function fetchAndCache(request, awaitCaching = false) {
    const resp = await getContent(request.url);
    if (resp.ok) {
        const cachable = resp.clone();
        LAST_FETCHED = new URL(request.url).pathname;
        const c = createHash(resp.clone()).then(hash => {
            storeInCache(request.url, cachable, hash).catch(e => {
                console.warn("Failed to store in cache", e);
            }).then(r => {
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
        if (cached) return cached;

        try {
            return await fetchAndCache(event.request);
        } catch (err) {
            return new Response("Offline", { status: 503 });
        }
    })());
});


async function prefetchResources() {
    if (!PREFETCH) return;
    console.log("Starting prefetch...");
    const index = await getIndex();

    const entries = Object.values(index);
    if (entries.length === 0) return;

    // Sort largest → smallest
    entries.sort((a, b) => b.size - a.size);

    let i = 0;

    async function worker() {
        while (i < entries.length) {
            const entry = entries[i++];
            if (!await canPreload(entry.path)) continue;
            try {
                if (!PREFETCH) break;
                const url = new URL(entry.path, self.location).href;
                const cached = await getFromCache(url);
                if (!cached) {
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
                try {
                    const [maxEntries, maxBytes] = await countPreloadEntries();
                    if (NUM_PREFETCHED >= maxEntries) NUM_PREFETCHED = maxEntries;
                    if (BYTES_PREFETCHED >= maxBytes) BYTES_PREFETCHED = maxBytes;
                } catch (e) {
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


async function stopPreload() {
    PREFETCH = false;
    console.log("Stopping preload as requested by client");

}

async function updateProgress(
    lastFile,
    done,
    total,
    doneBytes,
    totalBytes,
    status,
    canSkip
) {
    const clients = await self.clients.matchAll({ includeUncontrolled: true, type: 'window' });
    for (const client of clients) {
        client.postMessage({
            type: "preload-progress",
            total: total,
            done: done,
            last: lastFile,
            totalBytes: totalBytes,
            doneBytes: doneBytes,
            status: status,
            canSkip: canSkip
        });
    }

}
let updateTask = null;
async function updateClient() {
    if (!updateTask) {
        updateTask = setTimeout(async () => {
            try {
                updateTask = null
                const clients = await self.clients.matchAll({ includeUncontrolled: true, type: 'window' });
                const [maxEntries, maxBytes] = await countPreloadEntries();
                // for (const client of clients) {
                //     client.postMessage({
                //         type: "preload-progress",
                //         total: maxEntries,
                //         done: NUM_PREFETCHED,
                //         last: LAST_FETCHED,
                //         totalBytes: maxBytes,
                //         doneBytes: BYTES_PREFETCHED
                //     });
                // }
                updateProgress(
                    LAST_FETCHED,
                    NUM_PREFETCHED,
                    maxEntries,
                    BYTES_PREFETCHED,
                    maxBytes,
                    "Loading...",
                    true
                );

            } catch (e) {
                console.warn(e);
            }
        }, 200);
    }
}

async function startPreload(configUrl) {
    PREFETCH = true;
    NUM_PREFETCHED = 0;
    BYTES_PREFETCHED = 0;
    LAST_FETCHED = "";

    const config = await loadConfig(configUrl);
    if (config.bundle) {
        console.log("Download app bundle");
        await updateBundle(config.bundle, config.bundleHash);
    }

    const cache = await getCache();
    const index = await getIndex();
    const requests = await cache.keys();
    for (const request of requests) {
        const url = request.url;
        // Skip system entries
        if (!url.trim() || url.endsWith(BUNDLE_KEY) || url.endsWith(HASHES_STORE)) continue;
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
    try {
        if (
            event.data &&
            event.origin === location.origin &&
            event.source &&
            event.source instanceof WindowClient
        ) {
            if (event.data.type === "stop-preload") {
                await stopPreload();
            } else if (event.data.type === "start-preload") {
                await startPreload(event.data.config);
            } else {
                console.warn("Unknown message type", event.data.type);
            }
        }
    } catch (e) {
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
