importScripts("./zip-core.js");

// Force in-thread codecs in Service Worker (no sub-workers allowed)
if (globalThis.zip && typeof globalThis.zip.configure === "function") {
  globalThis.zip.configure({ useWebWorkers: false });
}

const USE_CACHE = true;
const INDEX_URL = "./resources.index.txt";
const PRELOAD_IGNORE_URL = "./preload-ignore.txt";
const CACHE_BASENAME = "ngeapp";
const CACHE_VERSION = "v1";
const PREFETCH_WORKERS = 5;
const HASHES_STORE = "__hashes__";
const BUNDLE_KEY = "bundle.zip";

const NON_CACHEABLE = [
    INDEX_URL,
    PRELOAD_IGNORE_URL,
    BUNDLE_KEY
];

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
let bundleOpenPromise = null;
let bundleBlob = null;
let bundleBlobReader = null;
let bundleZipReader = null;
let bundleUpdatePromise = null;
let bundleEntryMap = null;
let COUNT_PROMISE = null;

function closeBundleReaders() {
  try { bundleZipReader && bundleZipReader.close && bundleZipReader.close(); } catch(_) {}
  bundleZipReader = null;
  bundleBlobReader = null;
  bundleBlob = null;
  bundleEntries = undefined;
  bundleEntryMap = null;
}

async function getZip(){
   return globalThis.zip;
}

// Normalize a URL pathname to scope-relative path used by resources.index.txt
function toScopeRelative(pathname) {
    const base = new URL(self.registration.scope).pathname; // exact scope path
    let p = pathname || "/";
    if (p.startsWith(base)) p = p.slice(base.length);
    if (p.startsWith("/")) p = p.slice(1);
    return decodeURIComponent(p);
}

// Build normalized non-cacheable list (scope-relative)
const NON_CACHEABLE_REL = (() => {
    const keys = NON_CACHEABLE.concat([HASHES_STORE]);
    const out = [];
    for (const k of keys) {
        try {
            const p = new URL(k, self.location).pathname;
            out.push(toScopeRelative(p));
        } catch {
            out.push(String(k).replace(/^\.\//, ""));
        }
    }
    return out;
})();

function isNonCacheablePathname(pathname) {
    const rel = toScopeRelative(pathname);
    return NON_CACHEABLE_REL.includes(rel);
}

async function updateBundle(url, hash) {
    updateProgress("", 0, 1, null, null, "Downloading bundled resources...", false);

    const cache = await getCache();
    const hashes = await getStoredHashes();
    const storedHash = hashes[BUNDLE_KEY];
    if (hash && storedHash === hash) {
        console.log("Bundle already up-to-date");
        bundledLoaded = true;
        return;
    }

    // Probe server for size and range support
    let totalBytes = null;
    let acceptRanges = false;
    try {
        const head = await fetch(url, { method: "HEAD", cache: "no-store" });
        totalBytes = Number(head.headers.get("content-length")) || null;
        acceptRanges = /\bbytes\b/i.test(head.headers.get("accept-ranges") || "");
    } catch (_) {
        // ignore, fallback below
    }

    if (acceptRanges && totalBytes && totalBytes > 0) {
        // Resumable ranged download streamed into Cache
        const CHUNK = 2 * 1024 * 1024; // 2 MiB
        let offset = 0;
        let received = 0;

        const stream = new ReadableStream({
            async pull(controller) {
                if (offset >= totalBytes) {
                    controller.close();
                    return;
                }
                const end = Math.min(offset + CHUNK, totalBytes);
                let attempt = 0;
                for (;;) {
                    let reader = null;
                    try {
                        const resp = await fetch(url, {
                            headers: { Range: `bytes=${offset}-${end - 1}` },
                            cache: "no-store"
                        });
                        if (resp.status !== 206 || !resp.body) {
                            throw new Error(`Range fetch failed: ${resp.status}`);
                        }
                        reader = resp.body.getReader();
                        while (true) {
                            const { done, value } = await reader.read();
                            if (value && value.length) {
                                controller.enqueue(value);
                                received += value.length;
                                updateProgress("", 0, 1, received, totalBytes, "Downloading bundled resources...", false);
                            }
                            if (done) break;
                        }
                        offset = end;
                        break; // chunk done
                    } catch (e) {
                        attempt++;
                        try { reader && reader.cancel && reader.cancel(); } catch(_) {}
                        if (attempt > 4) {
                            controller.error(e);
                            return;
                        }
                        await new Promise(r => setTimeout(r, attempt * 500));
                    }
                }
            }
        });

        // Put streamed response into cache 
        await cache.put(BUNDLE_KEY, new Response(stream, {
            headers: { "Content-Type": "application/zip" }
        }));

        if (received !== totalBytes) {
            // Extra guard if server lied about size
            await cache.delete(BUNDLE_KEY);
            throw new Error(`Bundle size mismatch: got ${received} of ${totalBytes}`);
        }
    } else {
        // Fallback: single GET 
        const resp = await fetch(url, { cache: "no-store" });
        if (!resp.ok || !resp.body) throw new Error(`Failed to fetch bundle: ${resp.status}`);

        const totalHdr = Number(resp.headers.get("content-length")) || null;
        const reader = resp.body.getReader();

        let received = 0;
        const stream = new ReadableStream({
            async pull(controller) {
                try {
                    const { done, value } = await reader.read();
                    if (value && value.length) {
                        controller.enqueue(value);
                        received += value.length;
                        updateProgress("", 0, 1, received, totalHdr, "Downloading bundled resources...", false);
                    }
                    if (done) controller.close();
                } catch (e) {
                    controller.error(e);
                }
            },
            cancel() { try { reader.releaseLock(); } catch(_) {} }
        });

        await cache.put(BUNDLE_KEY, new Response(stream, {
            headers: { "Content-Type": "application/zip" }
        }));

        if (totalHdr != null && received !== totalHdr) {
            await cache.delete(BUNDLE_KEY);
            throw new Error(`Bundle truncated: got ${received} of ${totalHdr} bytes`);
        }
    }

    // Validate ZIP from cache  
    const stored = await cache.match(BUNDLE_KEY);
    if (!stored) throw new Error("Bundle missing from cache after download");
    const blob = await stored.blob();

    const Zip = await getZip();
    try {
        const zipReader = new Zip.ZipReader(new Zip.BlobReader(blob));
        const entries = await zipReader.getEntries();
        await zipReader.close();
        if (!Array.isArray(entries) || entries.length === 0) {
            throw new Error("Invalid/empty ZIP");
        }
    } catch (e) {
        console.error("Bundle validation failed:", e);
        await cache.delete(BUNDLE_KEY);
        throw e;
    }
    
    closeBundleReaders();

    if (hash) await storeHash(BUNDLE_KEY, hash);
    bundledLoaded = true;
    console.log("Bundle updated");
}

async function loadBundledResources() {
    if (bundleEntries) return bundleEntries;
    if (bundleOpenPromise) return bundleOpenPromise;

    // If an update is in flight, await it to avoid opening a half-written ZIP
    if (bundleUpdatePromise) {
        try { await bundleUpdatePromise; } catch (_) { /* ignored here; will fall back */ }
    }

    bundleOpenPromise = (async () => {
        const Zip = await getZip();
        const cache = await getCache();
        const resp = await cache.match(BUNDLE_KEY);
        if (!resp) {
            bundleEntries = [];
            bundleEntryMap = Object.create(null);
            return bundleEntries;
        }
        bundledLoaded = true;

        bundleBlob = await resp.blob();
        try {
            bundleBlobReader = new Zip.BlobReader(bundleBlob);
            bundleZipReader = new Zip.ZipReader(bundleBlobReader);
            bundleEntries = await bundleZipReader.getEntries();
            bundleEntryMap = Object.create(null);
            for (const e of bundleEntries) {
                bundleEntryMap[e.filename] = e;
            }
        } catch (e) {
            console.error("Failed to open bundle:", e);
            closeBundleReaders();
            bundleEntries = [];
            bundleEntryMap = Object.create(null);
        }
        return bundleEntries;
    })();

    try {
        return await bundleOpenPromise;
    } finally {
        bundleOpenPromise = null;
    }
}

async function getContent(url) {
    const Zip = await getZip();
    const entries = await loadBundledResources();
    let path;
    if (entries && entries.length) {
        path = toScopeRelative(new URL(url, self.location).pathname);

        const entry = bundleEntryMap ? bundleEntryMap[path] : undefined;
        if (entry) {
            const blob = await entry.getData(new Zip.BlobWriter());
            return new Response(blob, { headers: { "Content-Type": guessMime(path) } });
        }
    }
    return fetch(url, { cache: "no-cache" });
}

function guessMime(path) {
    if (path.endsWith(".png")) return "image/png";
    if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
    if (path.endsWith(".webp")) return "image/webp";
    if (path.endsWith(".gif")) return "image/gif";
    if (path.endsWith(".svg")) return "image/svg+xml";
    if (path.endsWith(".js") || path.endsWith(".mjs")) return "application/javascript";
    if (path.endsWith(".css")) return "text/css";
    if (path.endsWith(".html") || path.endsWith(".htm")) return "text/html";
    if (path.endsWith(".json")) return "application/json";
    if (path.endsWith(".wasm")) return "application/wasm";
    if (path.endsWith(".mp3")) return "audio/mpeg";
    if (path.endsWith(".ogg")) return "audio/ogg";
    if (path.endsWith(".mp4")) return "video/mp4";
    if (path.endsWith(".ttf")) return "font/ttf";
    if (path.endsWith(".otf")) return "font/otf";
    if (path.endsWith(".woff")) return "font/woff";
    if (path.endsWith(".woff2")) return "font/woff2";
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
    let pathname;
    try {
        pathname = new URL(urlOrPath, self.location.origin).pathname;
    } catch (_) {
        pathname = String(urlOrPath || "/");
    }
    const rel = toScopeRelative(pathname);
    const index = await getIndex();
    return index[rel];
}

async function countPreloadEntries() {
    if (NUM_ENTRIES_TO_PRELOAD != null && BYTES_TO_PRELOAD != null && NUM_ENTRIES_TO_PRELOAD !== 0) {
        return [NUM_ENTRIES_TO_PRELOAD, BYTES_TO_PRELOAD];
    }
    if (COUNT_PROMISE) return COUNT_PROMISE;

    COUNT_PROMISE = (async () => {
        const index = await getIndex();
        let count = 0;
        let bytes = 0;
        for (const path in index) {
            if (await canPreload(path)) {
                count++;
                bytes += index[path].size;
            }
        }
        NUM_ENTRIES_TO_PRELOAD = count;
        BYTES_TO_PRELOAD = bytes;
        return [count, bytes];
    })();

    try {
        return await COUNT_PROMISE;
    } finally {
        COUNT_PROMISE = null;
    }
}

async function getFromCache(url) {
    if (!USE_CACHE) return null;

    // skip non-cacheable by pathname (normalized)
    const pathname = new URL(url, self.location.origin).pathname;
    if (isNonCacheablePathname(pathname)) return null;

    // check if cached
    const hashes = await getStoredHashes();
    const cachedHash = hashes[url];
    if (!cachedHash) {
        return null;
    }

    // check if still valid (compare with index hash)
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

async function fetchAndCache(request, awaitCaching = false) {
    const pathname = new URL(request.url, self.location.origin).pathname;
    if (isNonCacheablePathname(pathname)) {
        return await fetch(request, { cache: "no-cache" });
    }

    // Serve from bundle if present; else network
    const resp = await getContent(request.url);
    if (resp.ok) {
        const cachable = resp.clone();
        LAST_FETCHED = new URL(request.url).pathname;

        // Use hash from index (no SHA-256 generation)
        const entry = await getIndexEntry(request.url);
        let c = Promise.resolve();
        if (entry && entry.hash) {
            c = storeInCache(request.url, cachable, entry.hash)
                .catch(e => console.warn("Failed to store in cache", e));
        }
        if (awaitCaching) await c;
    }
    return resp;
}

// intercept fetch requests
// return cached value if available and up to date
// otherwise fetch and cache
self.addEventListener("fetch", (event) => {
    event.respondWith((async () => {
        const req = event.request;
        const url = new URL(req.url);
        if (url.origin !== self.location.origin || (req.method !== "GET" && req.method !== "HEAD")) {
            return fetch(req);
        }

        const cached = await getFromCache(req.url);
        if (cached) return cached;

        try {
            return await fetchAndCache(req);
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

            }
        }
    }

    const workers = [];
    for (let w = 0; w < PREFETCH_WORKERS; w++) {
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

let updating = false;
let updateTask = null;
async function updateClient(start) {
    const update = async (force) =>{
        try {
            if(!force&&!updating) return;
            const [maxEntries, maxBytes] = await countPreloadEntries();
            if(!force&&!updating) return;
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
    };
    
    if (start) {
        if(updating) return;
        updating = true;
        const updateLoop = async () => {
            updateTask = new Promise((res,rej)=>{
                update().then(res).catch(rej);
            });
            await updateTask;
            if(!updating) return;
            setTimeout(()=>{
                if (updating) {
                    updateLoop().catch((e)=>{
                        console.warn("Update loop failed", e);
                    });
                }
            },200);
        };
        updateLoop();              
    } else {
        if (!updating) return;
        updating = false;
        if (updateTask) await updateTask;
        await update(true);

    }
}

async function startPreload(configUrl) {
    PREFETCH = true;
    NUM_PREFETCHED = 0;
    BYTES_PREFETCHED = 0;
    LAST_FETCHED = "";
    NUM_ENTRIES_TO_PRELOAD = null;
    BYTES_TO_PRELOAD = 0;
    PRELOAD_IGNORE_LIST = null;
    COUNT_PROMISE = null;

    const config = await loadConfig(configUrl);
    if (config.bundle) {
        console.log("Download app bundle");
        bundleUpdatePromise = updateBundle(config.bundle, config.bundleHash)
            .catch(e => { throw e; })
            .finally(() => { bundleUpdatePromise = null; });
        await bundleUpdatePromise;
    }

    const cache = await getCache();
    const index = await getIndex();
    const requests = await cache.keys();
    for (const request of requests) {
        const url = request.url;
        const pathname = new URL(url).pathname;

        // Skip system entries (normalized)
        if (isNonCacheablePathname(pathname)) continue;

        // If not in index, delete from cache
        const relPath = toScopeRelative(pathname);
        if (!index[relPath]) {
            await cache.delete(request);
            console.log("Clearing stale cache for:", relPath);
        }
    }

    await updateClient(true);
    await prefetchResources();
    await updateClient(false); // final update
}

self.addEventListener("message", (event) => {
    event.waitUntil((async () => {
        try {
            const data = event.data || {};
            const src = event.source;
            const isWindow = src && (src.type === "window");
            if (isWindow && data.type === "stop-preload") {
                await stopPreload();
            } else if (isWindow && data.type === "start-preload") {
                await startPreload(data.config);
            } else {
                console.warn("Unknown message to service worker:", data);
            }
        } catch (e) {
            console.warn("Failed to process message", e);
        }
    })());
});

// starts prefetching
self.addEventListener("install", (event) => {
    event.waitUntil(self.skipWaiting());
});

self.addEventListener("activate", (event) => {
    event.waitUntil((async () => {
        await self.clients.claim();

        try {
            const cache = await getCache();
            const resp = await cache.match(BUNDLE_KEY);
            if (resp) {
                bundledLoaded = true;
                // Warm up entries (do not block activation)
                loadBundledResources().catch(() => {});
            }
        } catch (e) {
            // ignore
        }
    })());
});