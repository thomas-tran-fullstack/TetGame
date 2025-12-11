type SignalPayload = {
  type: 'offer'|'answer'|'candidate'|'announce';
  target?: string;
  data?: any;
  sender?: string;
};

class WebRTCService {
  private pcs: Map<string, RTCPeerConnection> = new Map();
  private localStream: MediaStream | null = null;
  private onRemoteStream: (peerId: string, stream: MediaStream) => void = () => {};
  private config: RTCConfiguration = { iceServers: [{ urls: 'stun:stun.l.google.com:19302' }] };

  setRemoteStreamHandler(fn: (peerId: string, stream: MediaStream) => void) {
    this.onRemoteStream = fn;
  }

  async initLocalAudio() {
    if (this.localStream) return this.localStream;
    try {
      const s = await navigator.mediaDevices.getUserMedia({ audio: true, video: false });
      this.localStream = s;
      return s;
    } catch (err) {
      console.error('Failed to get user media', err);
      throw err;
    }
  }

  stopLocalAudio() {
    if (!this.localStream) return;
    this.localStream.getTracks().forEach(t => t.stop());
    this.localStream = null;
  }

  private createPc(peerId: string) {
    if (this.pcs.has(peerId)) return this.pcs.get(peerId)!;
    const pc = new RTCPeerConnection(this.config);

    pc.onicecandidate = (e) => {
      if (e.candidate) {
        // send candidate via websocket (external caller should wrap send)
        // handler will be in component/service that owns websocket
      }
    };

    pc.ontrack = (e) => {
      const stream = e.streams && e.streams[0];
      if (stream) this.onRemoteStream(peerId, stream);
    };

    this.pcs.set(peerId, pc);
    return pc;
  }

  async startVoice(roomId: string, myId: string, peers: string[], sendSignal: (p: SignalPayload) => void) {
    await this.initLocalAudio();
    if (!this.localStream) throw new Error('No local stream');

    for (const peer of peers) {
      if (peer === myId) continue;
      const pc = this.createPc(peer);
      // add tracks
      this.localStream.getTracks().forEach(track => pc.addTrack(track, this.localStream!));

      // onicecandidate send
      pc.onicecandidate = (e) => {
        if (e.candidate) {
          sendSignal({ type: 'candidate', target: peer, data: e.candidate });
        }
      };

      const offer = await pc.createOffer();
      await pc.setLocalDescription(offer);
      sendSignal({ type: 'offer', target: peer, data: offer });
    }
  }

  async handleSignal(msg: SignalPayload, myId: string, sendSignal: (p: SignalPayload) => void) {
    const sender = msg.sender;
    if (!sender) return;
    if (msg.type === 'offer') {
      // create pc and answer
      const pc = this.createPc(sender);
      this.localStream?.getTracks().forEach(t => pc.addTrack(t, this.localStream!));
      await pc.setRemoteDescription(new RTCSessionDescription(msg.data));
      const answer = await pc.createAnswer();
      await pc.setLocalDescription(answer);
      sendSignal({ type: 'answer', target: sender, data: answer });

      pc.onicecandidate = (e) => {
        if (e.candidate) sendSignal({ type: 'candidate', target: sender, data: e.candidate });
      };
    } else if (msg.type === 'answer') {
      const pc = this.pcs.get(sender);
      if (pc) {
        await pc.setRemoteDescription(new RTCSessionDescription(msg.data));
      }
    } else if (msg.type === 'candidate') {
      const pc = this.pcs.get(sender);
      if (pc) {
        try {
          await pc.addIceCandidate(new RTCIceCandidate(msg.data));
        } catch (err) {
          console.warn('ICE add failed', err);
        }
      }
    }
  }

  stopAll() {
    this.pcs.forEach(pc => {
      try { pc.close(); } catch(e){}
    });
    this.pcs.clear();
    this.stopLocalAudio();
  }
}

export default new WebRTCService();
