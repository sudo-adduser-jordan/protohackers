package server.d2;

import server.ServerLogFormatter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

enum MessageTypes
{
    INSERT, QUERY
}

public class ServerRunnable implements Runnable
{
    private static final Logger logger = ServerLogFormatter.getLogger(Server.class);
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
        try // process client requests
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
            Request request = messageToRequest(message);

            switch (request.getMessageType())
            {
            case INSERT -> sessionMemoryCache.addPrice(request.getFirstValue(), request.getSecondValue());
            case QUERY -> output.write(messageToResponse(sessionMemoryCache.getPrice(request.getFirstValue())));
            }

        }
        catch (Exception e)
        {
            socket.close();
            logger.severe("Client disconnected: " + socket.getInetAddress());
        }

    }

    private static byte[] messageToResponse(int value)
    {
        byte[] message = new byte[RESPONSE_LENGTH];

        for (int i = 0; i < RESPONSE_LENGTH; i++)
        {
            message[i] = (byte) ((value >>> 8 * (3 - i)) & 0b11111111);
        }

        return message;
    }

    private static Request messageToRequest(byte[] message)
    {
        if (message.length != 9)
        {
            throw new IllegalArgumentException("Message must be exactly 9 bytes");
        }

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
            logger.severe("Bad request.");
        }

        int firstValue = 0;
        for (int i = 1; i <= 4; i++)
        {
            firstValue = (firstValue << 8) | (message[i] & 0xFF);
        }

        int secondValue = 0;
        for (int i = 5; i <= 8; i++)
        {
            secondValue = (secondValue << 8) | (message[i] & 0xFF);
        }
        return builder.FirstValue(firstValue).SecondValue(secondValue).build();
    }

}