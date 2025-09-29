import Binds from './WebBindsHub.js';

function s() {
    return ((typeof window !== 'undefined' && window) ||
        (typeof globalThis !== 'undefined' && globalThis) ||
        (typeof global !== 'undefined' && global) ||
        (typeof self !== 'undefined' && self));
}

async function inject(){
    if(s().nostr) return;
    try{
        if(await Binds.fireEvent("nip07IsAvailable")){
            s().nostr = {
                getPublicKey: async () => {
                    return await Binds.fireEvent("nip07GetPublicKey");
                },
                signEvent: async (event) => {
                    return await Binds.fireEvent("nip07SignEvent", event);
                },
                nip04: {
                    encrypt: async (pubkey, plaintext) => {
                        return await Binds.fireEvent("nip07Nip04Encrypt", pubkey, plaintext);
                    },
                    decrypt: async (pubkey, ciphertext) => {
                        return await Binds.fireEvent("nip07Nip04Decrypt", pubkey, ciphertext);
                    }
                },
                nip44: {
                    encrypt: async (pubkey, plaintext) => {
                        return await Binds.fireEvent("nip07Nip44Encrypt", pubkey, plaintext);
                    },
                    decrypt: async (pubkey, ciphertext) => {
                        return await Binds.fireEvent("nip07Nip44Decrypt", pubkey, ciphertext);
                    }
                }
            };
        } else {
            console.warn("NIP-07 extension not available");
        }
    } catch(e){
        console.warn("Error injecting NIP-07 extension: "+e);
    }

}

function bind(){
    Binds.addEventListener("nip07IsAvailable", async ()=>{
        return (typeof s().nostr !== 'undefined');
    });
    Binds.addEventListener("nip07GetPublicKey", async ()=>{
        if(!s().nostr) throw new Error("Nostr extension not available");
        return await s().nostr.getPublicKey();
    });
    Binds.addEventListener("nip07SignEvent", async (event)=>{
        if(!s().nostr) throw new Error("Nostr extension not available");
        return await s().nostr.signEvent(event);
    });
    Binds.addEventListener("nip07Nip04Encrypt", async (pubkey, plaintext)=>{
        if(!s().nostr || !s().nostr.nip04) throw new Error("Nostr extension or nip04 not available");
        return await s().nostr.nip04.encrypt(pubkey, plaintext);
    });
    Binds.addEventListener("nip07Nip04Decrypt", async (pubkey, ciphertext)=>{
        if(!s().nostr || !s().nostr.nip04) throw new Error("Nostr extension or nip04 not available");
        return await s().nostr.nip04.decrypt(pubkey, ciphertext);
    });
    Binds.addEventListener("nip07Nip44Encrypt", async (pubkey, plaintext)=>{
        if(!s().nostr || !s().nostr.nip44) throw new Error("Nostr extension or nip44 not available");
        return await s().nostr.nip44.encrypt(pubkey, plaintext);
    });
    Binds.addEventListener("nip07Nip44Decrypt", async (pubkey, ciphertext)=>{
        if(!s().nostr || !s().nostr.nip44) throw new Error("Nostr extension or nip44 not available");
        return await s().nostr.nip44.decrypt(pubkey, ciphertext);
    }); 

}




export default { inject, bind};