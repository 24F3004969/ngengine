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
    constructor(label, id){
        this.id = id || newId();
        this.label = label;        
        this.readyState = "connecting";
        this.b = Promise.resolve();
        this.onopen = ()=>{};
        this.onclose = ()=>{};
        this.onmessage = (msg)=>{};
        this.onerror = (err)=>{};
        Binds.addEventListener("rtcOnDataChannelStateChange", (channelId, state) => {
            if(channelId !== this.id) return;
            this.readyState = state;
        });
        Binds.addEventListener("rtcOnDataChannelOpen", (channelId) => {
            if(channelId !== this.id) return;
            this.readyState = "open";
            if(this.onopen) this.onopen();
        });
        Binds.addEventListener("rtcOnDataChannelClose", (channelId) => {
            if(channelId !== this.id) return;
            this.readyState = "closed";
            if(this.onclose) this.onclose();
        });
        Binds.addEventListener("rtcOnDataChannelMessage", (channelId, data) => {
            if(channelId !== this.id) return;
            if(this.onmessage) this.onmessage(data);
        });
        Binds.addEventListener("rtcOnDataChannelError", (channelId, error) => {
            if(channelId !== this.id) return;
            if(this.onerror) this.onerror({
                error: error
            });
        });
    }

    set binaryType(v){
        this.b.then(()=>{
          Binds.fireEvent("rtcSetDataChannelBinaryType", this.id, v);
        });
    }

    send(buffer){
        this.b.then(()=>{   
            Binds.fireEvent("rtcSendDataChannelMessage", this.id, buffer);
        });
    }

    close(){
        this.b.then(()=>{
            Binds.fireEvent("rtcCloseDataChannel", this.id);
        });
    }   

}

class ProxiedRTCPeerConnection {
    
    constructor(conf){
        this.id = newId();
        this.connectionState="new";
        this.iceConnectionState="new";
        this.onicecandidate = ()=>{};
        this.onconnectionstatechange = ()=>{};
        this.oniceconnectionstatechange = ()=>{};
        this.ondatachannel = ()=>{};
        this.b = Binds.fireEvent("rtcCreatePeerConnection", this.id, conf);
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
            const channel = new ProxiedRTCDataChannel(label, channelId);
            channel.readyState = readyState;
            if(this.ondatachannel) this.ondatachannel({
                channel: channel
            });
        });

    }

    setLocalDescription(obj){
        return this.b = this.b.then(()=>{
            return Binds.fireEvent("rtcSetLocalDescription", this.id, obj);
        });
    }

    setRemoteDescription(obj){
        return this.b = this.b.then(()=>{
            return Binds.fireEvent("rtcSetRemoteDescription", this.id, obj);
        });
    }

    createOffer(options){
        return this.b = this.b.then(()=>{
            return Binds.fireEvent("rtcCreateOffer", this.id, options);
        });
    }
    
    createAnswer(options){  
        return this.b = this.b.then(()=>{
            return Binds.fireEvent("rtcCreateAnswer", this.id, options);
        });
    }
     

    createDataChannel(label){
        const dataChannel = new ProxiedRTCDataChannel(label);
        dataChannel.b = this.b.then(()=>{
            Binds.fireEvent("rtcCreateDataChannel", this.id, dataChannel.id, dataChannel.label);
        })
        return dataChannel;
    }

    addIceCandidate(candidate){
        this.b.then(()=>{
            Binds.fireEvent("rtcAddIceCandidate", this.id, {
                candidate: candidate.options.candidate,
                sdpMid: candidate.options.sdpMid,
                sdpMLineIndex: candidate.options.sdpMLineIndex
            });
        });
    }

    close(){
        this.b.then(()=>{
            Binds.fireEvent("rtcClosePeerConnection", this.id);
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
    const bindChannel = (channelId, channel) => {
        channel.onopen = (ev) => {
            Binds.fireEvent("rtcOnDataChannelStateChange", channelId, channel.readyState);
        };
        channel.onclose = (ev) => {
            Binds.fireEvent("rtcOnDataChannelStateChange", channelId, channel.readyState);
        };
        channel.onerror = (ev) => {
            Binds.fireEvent("rtcOnDataChannelError", channelId, ev.message);
        };
        channel.onmessage = (ev) => {
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
        conn.ondatachannel = (ev) => {
            const channel = ev.channel;
            const channelId = newId();
            resources[channelId] = channel;
            Binds.fireEvent("rtcOnDataChannel", id, channelId, channel.label, channel.readyState);
            bindChannel(channelId, channel);
        };
        resources[id] = conn;
        return Promise.resolve();
    });
    Binds.addEventListener("rtcSetLocalDescription", (id, desc) => {
        const conn = resources[id];
        if(!conn) return Promise.reject("PeerConnection not found: " + id);
        console.log(desc)
        return conn.setLocalDescription(new RTCSessionDescription(desc));
    });

    Binds.addEventListener("rtcSetRemoteDescription", (id, desc) => {
        const conn = resources[id];
        if(!conn) return Promise.reject("PeerConnection not found: " + id);
        return conn.setRemoteDescription(new RTCSessionDescription(desc));
    });

    Binds.addEventListener("rtcCreateOffer", (id, options) => {
        const conn = resources[id];
        if(!conn) return Promise.reject("PeerConnection not found: " + id);
        return conn.createOffer(options).then( (offer) => {
            return {
                type: offer.type,
                sdp: offer.sdp
            };
        });
    });

    Binds.addEventListener("rtcCreateAnswer", (id, options) => {
        const conn = resources[id];
        if(!conn) return Promise.reject("PeerConnection not found: " + id);
        return conn.createAnswer(options).then( (answer) => {
            return {
                type: answer.type,
                sdp: answer.sdp
            };
        });
    });

    Binds.addEventListener("rtcCreateDataChannel", (connId, channelId, label) => {
        const conn = resources[connId];
        if(!conn) return;
        const channel = conn.createDataChannel(label);
        bindChannel(channelId, channel);
        resources[channelId] = channel;
    });

    Binds.addEventListener("rtcSetDataChannelBinaryType", (channelId, binaryType) => {
        const channel = resources[channelId];
        if(!channel) return;
        channel.binaryType = binaryType;
    });

    Binds.addEventListener("rtcSendDataChannelMessage", (channelId, data) => {
        const channel = resources[channelId];
        if(!channel) return;
        channel.send(data);
    });

    Binds.addEventListener("rtcCloseDataChannel", (channelId) => {
        const channel = resources[channelId];
        if(!channel) return;
        channel.close();
        delete resources[channelId];
    });

    Binds.addEventListener("rtcAddIceCandidate", (connId, candidate) => {
        const conn = resources[connId];
        if(!conn) return;
        if(candidate){
            conn.addIceCandidate(new RTCIceCandidate(candidate));
        }  
    });

    Binds.addEventListener("rtcClosePeerConnection", (id) => {
        const conn = resources[id];
        if(!conn) return;
        conn.close();
        delete resources[id];
    });



}
export default { inject, bind};