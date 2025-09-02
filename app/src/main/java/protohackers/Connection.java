package protohackers;

import lombok.Getter;

import java.io.*;
import java.net.Socket;


@Getter
public class Connection implements Closeable
{
    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    public Connection(Socket socket) throws IOException
    {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void close() throws IOException
    {
        reader.close();
        writer.close();
        socket.close();
    }
}
