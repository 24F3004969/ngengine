import Binds from "./WebBindsHub.js";

function isWorker() {
    return (typeof WorkerGlobalScope !== 'undefined' && self instanceof WorkerGlobalScope);
}

let counter = 0;

function newId(){
    counter++;
    let id = counter;
    if(isWorker())id="r"+id;
    else id="w"+id;
    return id;
}




class ProxiedRTCDataChannel {
    constructor(label, id, connId, parentP){
        this.connId = connId;
        this.create = id == null;
        this.id = id || newId();
        this.label = label;        
        this.readyState = "connecting";
        this.parentP = parentP;
        this.onopen = ()=>{};
        this.onclose = ()=>{};
        this.onmessage = (msg)=>{};
        this.onerror = (err)=>{};       
    }

    async init(){
        if(this.b)return this.b;
        return this.b = new Promise(async (res,rej)=>{
            try{
                Binds.addEventListener("rtcOnDataChannelStateChange", (channelId, state) => {
                    if (channelId !== this.id) return;
                    this.readyState = state;
                });
                Binds.addEventListener("rtcOnDataChannelOpen", (channelId) => {
                    if (channelId !== this.id) return;
                    if (this.onopen) this.onopen();
                });
                Binds.addEventListener("rtcOnDataChannelClose", (channelId) => {
                    if (channelId !== this.id) return;
                     if (this.onclose) this.onclose();
                });
                Binds.addEventListener("rtcOnDataChannelMessage", (channelId, data) => {
                    if (channelId !== this.id) return;
                    if (this.onmessage) this.onmessage(data);
                });
                Binds.addEventListener("rtcOnDataChannelError", (channelId, error) => {
                    if (channelId !== this.id) return;
                    console.log(error);
                    if (this.onerror) this.onerror({
                        error: error
                    });
                });
                if(this.create){
                    await Binds.fireEvent("rtcCreateDataChannel", this.connId, this.id, this.label);
                }
                res();
            }catch(e){
                rej(e);
            }   
        });
    }

    async enqueue(r) {
        await this.parentP;
        if(!this.q) this.q = this.init();
        await this.q;
        return r();
    }
    set binaryType(v){
        this.enqueue( ()=>{
            return Binds.fireEvent("rtcSetDataChannelBinaryType", this.connId, this.id, v);
        });
    }

    send(buffer){
        return this.enqueue( ()=>{   
            return Binds.fireEvent("rtcSendDataChannelMessage", this.connId, this.id, buffer);
        });
    }

    close(){
        return this.enqueue( ()=>{
            return Binds.fireEvent("rtcCloseDataChannel", this.connId, this.id);
        });
    }   

}

class ProxiedRTCPeerConnection {
    
    constructor(conf){
        this.conf=conf;
        this.id = newId();
        this.connectionState="new";
        this.iceConnectionState="new";
        this.onicecandidate = ()=>{};
        this.onconnectionstatechange = ()=>{};
        this.oniceconnectionstatechange = ()=>{};
        this.ondatachannel = ()=>{};
      

    }

    async init(){
        if(this.b)return this.b;
        this.b = new Promise(async (res, rej) => {
            try{
                Binds.addEventListener("rtcOnConnectionStateChange", (connId, state) => {
                    if(connId !== this.id) return;
                    this.connectionState = state;
                    if(this.onconnectionstatechange) this.onconnectionstatechange();
                }); 
                Binds.addEventListener("rtcOnIceConnectionStateChange", (connId, state) => {
                    if(connId !== this.id) return;
                    this.iceConnectionState = state;
                    if(this.oniceconnectionstatechange) this.oniceconnectionstatechange();
                });
                Binds.addEventListener("rtcOnIceCandidate", (connId, candidate) => {
                    if(connId !== this.id) return;
                    if(this.onicecandidate) this.onicecandidate({
                        candidate: new ProxiedRTCIceCandidate(candidate)
                    });
                });
                Binds.addEventListener("rtcOnDataChannel", (connId, channelId, label, readyState) => {
                    if(connId !== this.id) return;
                    const channel = new ProxiedRTCDataChannel(label, channelId, connId, this.init);
                    channel.readyState = readyState;
                    if(this.ondatachannel) this.ondatachannel({
                        channel: channel
                    });
                });
                await Binds.fireEvent("rtcCreatePeerConnection", this.id, this.conf);
                res();
            }catch(e){
                rej(e);
            }
        });
        return this.b;
    }
    async enqueue(r) {
        if (!this.q) this.q = this.init();
        await this.q;
        return r();
    }
    setLocalDescription(obj){
        return this.enqueue(()=>{
            return Binds.fireEvent("rtcSetLocalDescription", this.id, obj);
        });       
    }

    setRemoteDescription(obj){

        return this.enqueue(()=>{
            return Binds.fireEvent("rtcSetRemoteDescription", this.id, obj);
        });
    
    }

    createOffer(options){
        return this.enqueue(()=>{
            return Binds.fireEvent("rtcCreateOffer", this.id, options);
        });
    }
    
    createAnswer(options){  
        return this.enqueue(()=>{
            return Binds.fireEvent("rtcCreateAnswer", this.id, options);
        });
    }
     

    createDataChannel(label){
        const dataChannel = new ProxiedRTCDataChannel(label, null, this.id, this.q);
        return dataChannel;
    }

    addIceCandidate(candidate){
        return this.enqueue(()=>{
            return Binds.fireEvent("rtcAddIceCandidate", this.id, {
                candidate: candidate.options.candidate,
                sdpMid: candidate.options.sdpMid,
                sdpMLineIndex: candidate.options.sdpMLineIndex
            });
        });
    }

    close(){
        return this.enqueue(()=>{
            return Binds.fireEvent("rtcClosePeerConnection", this.id);
        });
    }
}

class ProxiedRTCIceCandidate {
    constructor(options){
        this.options = options;
        this.candidate = options.candidate;
        this.sdpMid = options.sdpMid;
        this.sdpMLineIndex = options.sdpMLineIndex;
    }
}



async function inject(){
    if(!isWorker())return;
    self.RTCPeerConnection = ProxiedRTCPeerConnection;
    self.RTCIceCandidate = ProxiedRTCIceCandidate;
    self.RTCDataChannel = ProxiedRTCDataChannel;

}




function bind(){
 
    const resources = {};

    const waitForResource = (id,timeout = 60000) => {
        return new Promise((res,rej)=>{
            const start = Date.now();
            const retry = () => {
                if(resources[id]){
                    res(resources[id]);
                }else if(Date.now() - start > timeout){
                    rej("Timeout waiting for resource: " + id);
                }else{
                    setTimeout(retry,10);
                }
            };
            retry();
        });
    };

    const bindChannel = (channelId, channel) => {
        channel.onopen = async (ev) => {
                    Binds.fireEvent("rtcOnDataChannelStateChange", channelId, channel.readyState);
        };
        channel.onclose = async  (ev) => {
                    Binds.fireEvent("rtcOnDataChannelStateChange", channelId, channel.readyState);
        };
        channel.onerror = async (ev) => {
            console.log("Error",ev)
            Binds.fireEvent("rtcOnDataChannelError", channelId, ev.message);
        };
        channel.onmessage = async (ev) => {
            Binds.fireEvent("rtcOnDataChannelMessage", channelId, {
                data:ev.data
            });
        };
    }


    Binds.addEventListener("rtcCreatePeerConnection", (id, conf) => {
        const conn = new RTCPeerConnection(conf);
        conn.onconnectionstatechange = (ev) => {
            Binds.fireEvent("rtcOnConnectionStateChange", id, conn.connectionState);
        };

        conn.oniceconnectionstatechange = (ev) => {
            Binds.fireEvent("rtcOnIceConnectionStateChange", id, conn.iceConnectionState);
        };
        conn.onicecandidate = (ev) => {
            if(ev.candidate){
                Binds.fireEvent("rtcOnIceCandidate", id, {
                    candidate: ev.candidate.candidate,
                    sdpMid: ev.candidate.sdpMid,
                    sdpMLineIndex: ev.candidate.sdpMLineIndex
                });
            } 
        };
        conn.ondatachannel = async (ev) => {
            const channel = ev.channel;
            const channelId = newId();
            resources[channelId] = channel;
            bindChannel(channelId, channel);
            Binds.fireEvent("rtcOnDataChannel", id, channelId, channel.label, channel.readyState);
        };
        resources[id] = conn;        
    });

    Binds.addEventListener("rtcSetLocalDescription", async (id, desc) => {
        const conn = await waitForResource(id);
        if(!conn) return Promise.reject("PeerConnection not found: " + id);
        const  p = await conn.setLocalDescription(new RTCSessionDescription(desc));
        return p;
    });

    Binds.addEventListener("rtcSetRemoteDescription", async (id, desc) => {
        const conn = await waitForResource(id);
        if(!conn) return Promise.reject("PeerConnection not found: " + id);
        const p = await conn.setRemoteDescription(new RTCSessionDescription(desc));
        return p;
    });

    Binds.addEventListener("rtcCreateOffer", async (id, options) => {
        const conn = await waitForResource(id);
        if(!conn) return Promise.reject("PeerConnection not found: " + id);
        return conn.createOffer(options).then( (offer) => {
            return {
                type: offer.type,
                sdp: offer.sdp
            };
        });
    });

    Binds.addEventListener("rtcCreateAnswer", async (id, options) => {
        const conn = await waitForResource(id);
        if(!conn) return Promise.reject("PeerConnection not found: " + id);
        return conn.createAnswer(options).then( (answer) => {
            return {
                type: answer.type,
                sdp: answer.sdp
            };
        });
    });

    Binds.addEventListener("rtcCreateDataChannel", async (connId, channelId, label) => {
        const conn = await waitForResource(connId);
        if(!conn) return;
        const channel = await conn.createDataChannel(label);
        bindChannel(channelId, channel);
        resources[channelId] = channel;
    });

    Binds.addEventListener("rtcSetDataChannelBinaryType", async (connId, channelId, binaryType) => {
        const conn = await waitForResource(connId);
        if(!conn) return;
        const channel = await waitForResource(channelId);
        if(!channel) return;
        channel.binaryType = binaryType;
    });

    Binds.addEventListener("rtcSendDataChannelMessage", async (connId, channelId, data) => {
        const conn =  await waitForResource(connId);
        if(!conn) return;
        const channel = await waitForResource(channelId);
        if(!channel) return;
        channel.send(data);
    });

    Binds.addEventListener("rtcCloseDataChannel", async (connId, channelId) => {
        const conn = await waitForResource(connId);
        if(!conn) return;
        const channel = await waitForResource(channelId);
        if(!channel) return;
        channel.close();
        delete resources[channelId];
    });

    Binds.addEventListener("rtcAddIceCandidate", async (connId, candidate) => {
        const conn =  await waitForResource(connId);
        if(!conn) return;
        if(candidate){
            conn.addIceCandidate(new RTCIceCandidate(candidate));
        }  
    });

    Binds.addEventListener("rtcClosePeerConnection",async (id) => {
        const conn = await waitForResource(id);
        if(!conn) return;
        conn.close();
        delete resources[id];
    });


    

}
export default { inject, bind};