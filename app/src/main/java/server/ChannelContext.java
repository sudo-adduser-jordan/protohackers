package server;

import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Getter
@Setter
public class ChannelContext
{
    private final SocketChannel channel;
    private final ByteBuffer readBuffer;
    private final ByteBuffer writeBuffer;
    private final StringBuilder messageBuffer; // for assembling partial messages

    public ChannelContext(SocketChannel channel) {
        this.channel = channel;
        this.readBuffer = ByteBuffer.allocate(1024);
        this.writeBuffer = ByteBuffer.allocate(1024);
        this.messageBuffer = new StringBuilder();
    }
}