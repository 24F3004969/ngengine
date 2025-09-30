package org.ngengine.network.protocol.messages;

import org.ngengine.network.protocol.NetworkSafe;

import com.jme3.network.AbstractMessage;

@NetworkSafe

public class ClassRegistrationAckMessage extends AbstractMessage {
    private long classId;
    public ClassRegistrationAckMessage(long id) {
        this.classId = id;
    }

    public ClassRegistrationAckMessage() {
    }

    public long getClassId() {
        return classId;
    }

    @Override
    public String toString() {
        return "ClassRegistrationAckMessage{" + "classId=" + classId + '}'; 
    }

}
