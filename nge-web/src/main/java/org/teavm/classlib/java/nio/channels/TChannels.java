package org.teavm.classlib.java.nio.channels;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Objects;

public class TChannels {
    private static class ReadableByteChannelImpl implements ReadableByteChannel {
        private final InputStream in;
        private boolean open;

        public ReadableByteChannelImpl(InputStream in) {
            this.in = in;
            this.open = true;
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() throws IOException {
            try {
                in.close();
            } finally {
                open = false;
            }
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            int remaining = dst.remaining();
            if (remaining == 0) {
                return 0;
            }
            byte[] buffer = new byte[remaining];
            int bytesRead = in.read(buffer);
            if (bytesRead == -1) {
                return -1;  
            }
            dst.put(buffer, 0, bytesRead);
            return bytesRead;
        }
    }

    public static ReadableByteChannel newChannel(InputStream in) {
        Objects.requireNonNull(in, "in");
        return new ReadableByteChannelImpl(in);
    }

}
