const C='atlas-v2';
const F=['./','./index.html','./manifest.webmanifest','./icons/icon-192.png','./icons/icon-512.png','./config.js'];
self.addEventListener('install',e=>e.waitUntil(caches.open(C).then(c=>c.addAll(F)).then(()=>self.skipWaiting())));
self.addEventListener('activate',e=>e.waitUntil(caches.keys().then(keys=>Promise.all(keys.filter(k=>k!==C).map(k=>caches.delete(k)))).then(()=>self.clients.claim())));
self.addEventListener('fetch',e=>{if(e.request.method!=='GET')return;e.respondWith(caches.match(e.request).then(c=>c||fetch(e.request).then(r=>{const x=r.clone();caches.open(C).then(cache=>cache.put(e.request,x)).catch(()=>{});return r}).catch(()=>caches.match('./index.html'))))});
