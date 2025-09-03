package protohackers;

import lombok.*;
import lombok.extern.slf4j.*;

import java.io.*;
import java.net.*;
import java.nio.*;

@Slf4j
@Getter
public class Connection implements Closeable
{
    Socket socket;
    PrintWriter writer;
    BufferedReader reader;
    ByteBuffer byteBuffer;


    public Connection(Socket socket) throws IOException
    {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.byteBuffer = ByteBuffer.allocate(1024);
    }

    @Override
    public void close()
    {
        try (Writer writer = this.writer;
             Reader reader = this.reader;
             Socket socket = this.socket;)
        {
            byteBuffer.clear();
//            log.debug("Client resources closed | " + socket.getInetAddress());
//            log.debug("Client resources closed\t\t | " + reader.toString());
//            log.debug("Client resources closed\t\t | " + writer.toString());

        }
        catch (IOException e)
        {
//            log.error(e.getMessage());
        }
    }
}