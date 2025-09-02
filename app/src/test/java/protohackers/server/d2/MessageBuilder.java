package protohackers.server.d2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MessageBuilder
{
    // Create an insert message (9 bytes)
    public static byte[] createInsertMessage(int timestamp, int price)
    {
        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.put((byte) 'I'); // message type
        buffer.putInt(timestamp);
        buffer.putInt(price);
        return buffer.array();
    }

    // Create a query message (9 bytes)
    public static byte[] createQueryMessage(int minTime, int maxTime)
    {
        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.put((byte) 'Q'); // message type
        buffer.putInt(minTime);
        buffer.putInt(maxTime);
        return buffer.array();
    }

    // Decode server response (4 bytes to int)
    public static int decodeResponse(byte[] response)
    {
        ByteBuffer buffer = ByteBuffer.wrap(response);
        buffer.order(ByteOrder.BIG_ENDIAN); // ensure big-endian
        return buffer.getInt();
    }

}
