package protohackers;

import lombok.Getter;

import java.io.*;
import java.net.Socket;

@Getter
public class Connection implements Closeable
{
    Socket socket;
    PrintWriter writer;
    BufferedReader reader;
    ServerLogOptions logger;

    public Connection(Socket socket) throws IOException
    {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.logger = new ServerLogOptions(ServerLogFormatter.getLogger(Connection.class));
    }

    @Override
    public void close()
    {
        try (Writer writer = this.writer;
             Reader reader = this.reader;
             Socket socket = this.socket)
        {
//            logger.debug("Client resources closed | " + socket.getInetAddress());
//            logger.debug("Client resources closed\t\t | " + reader.toString());
//            logger.debug("Client resources closed\t\t | " + writer.toString());

        }
        catch (IOException e)
        {
            logger.error(e.getMessage());
        }
    }
}