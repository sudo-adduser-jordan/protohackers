package server.d2;

import server.ServerLogFormatter;
import server.ServerLogOptions;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Logger;

public class ServerRunnable implements Runnable
{
    private static final Logger log = ServerLogFormatter.getLogger(Server.class);
    private static final ServerLogOptions logger = new ServerLogOptions(log);

    private static final int REQUEST_LENGTH = 9;
    private static final int RESPONSE_LENGTH = 4;
    private static final char INSERT_CHAR = 'I';
    private static final char QUERY_CHAR = 'Q';
    private Socket socket;

    public ServerRunnable(Socket socket)
    {
        this.socket = socket;
    };

    public void run()
    {
        try // to accept client connection
        {
            BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
            BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream());
            SessionMemoryCache sessionMemoryCache = new SessionMemoryCache();

            while (!socket.isClosed())
            {
                processRequest(input, output, sessionMemoryCache);
            }
        }
        catch (IOException e)
        {
            logger.warning("Connection error with client: " + socket.getInetAddress());
            e.printStackTrace();
        }
    }

    private void processRequest(BufferedInputStream input, BufferedOutputStream output,
            SessionMemoryCache sessionMemoryCache) throws IOException
    {
        try // to process request
        {
            byte[] message = input.readNBytes(REQUEST_LENGTH);
            // if (message.length != REQUEST_LENGTH) {
            if (message == null) throw new IllegalArgumentException("Message is null.");
            
            logger.debug(bytesToHex(message));
            if (message.length != REQUEST_LENGTH) throw new IllegalArgumentException("Message must be exactly 9 bytes");

            Request request = messageToRequest(message);
            switch (request.getMessageType())
            {
            case INSERT -> sessionMemoryCache.addPrice(request.getFirstValue(), request.getSecondValue());
            case QUERY -> {
                output.write(messageToResponse(sessionMemoryCache.getPrice(request.getFirstValue())));
                output.flush();
            }
            }
            // } else{
            // log.debug("throw away bad request length");
            // }
        }
        catch (Exception e)
        {
            socket.close();
            logger.error("Error processing request: " + e.toString());
            logger.error("Client disconnected: " + socket.getInetAddress());
        }

    }

    // Hexadecimal: 00 00 13 f3
    // Decoded: 5107 // intToBigEndianBytes
    public static byte[] messageToResponse(Integer value)
    {
        ByteBuffer buffer = ByteBuffer.allocate(RESPONSE_LENGTH).order(ByteOrder.BIG_ENDIAN);
        if (value == null)
            buffer.putInt(0);
        else
            buffer.putInt(value);
        byte[] message = buffer.array();
        logger.debug(bytesToHex(message));
        return message;
    }

    // Method to convert byte array to hex string for logging
    public static String bytesToHex(byte[] bytes)
    {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes)
        {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString().trim();
    }

    // Byte: | 0 | 1 2 3 4 | 5 6 7 8 |
    // Type: |char | int32 | int32 | // @Builder pattern
    private static Request messageToRequest(byte[] message)
    {
        Request.RequestBuilder builder = Request.builder();

        byte messageTypeByte = message[0];
        switch (messageTypeByte)
        {
        case INSERT_CHAR:
            builder.MessageType(MessageTypes.INSERT);
            break;
        case QUERY_CHAR:
            builder.MessageType(MessageTypes.QUERY);
            break;
        default:
            logger.error("Bad request.");
            break;
        }

        int firstValue = ByteBuffer.wrap(message, 1, 4).order(ByteOrder.BIG_ENDIAN).getInt();
        int secondValue = ByteBuffer.wrap(message, 5, 4).order(ByteOrder.BIG_ENDIAN).getInt();

        // System.out.println(messageTypeByte);
        logger.info("Request message: " + (char) messageTypeByte + " " + firstValue + "\t" + secondValue);

        return builder.FirstValue(firstValue).SecondValue(secondValue).build();
    }

}