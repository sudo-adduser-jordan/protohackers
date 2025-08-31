package server;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
    private final StringBuilder messageBuffer; // used to pass exceptions etc
    private final SessionMemoryCache sessionMemoryCache;
    private final JsonMapper jsonMapper;

    public ChannelContext(SocketChannel channel) {
        this.channel = channel;
        this.sessionMemoryCache = new SessionMemoryCache();
        this.readBuffer = ByteBuffer.allocate(42069);
        this.writeBuffer = ByteBuffer.allocate(42069);
        this.messageBuffer = new StringBuilder();
        this.jsonMapper = JsonMapper.builder()
                                    .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                                    .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
                                    .build();
    }
}