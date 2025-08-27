    package server.d1;

    import java.io.BufferedOutputStream;
    import java.io.BufferedReader;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.InputStreamReader;
    import java.io.OutputStream;
    import java.lang.reflect.Field;
    import java.net.Socket;

    import org.junit.jupiter.api.AfterAll;
    import org.junit.jupiter.api.BeforeAll;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;

    import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;

    class TestServer
    {
        private final static int TEST_PORT = 6968;
        private static Server server;
        private Socket socket0;
        private Socket socket1;
        private Socket socket2;
        private Socket socket3;
        private Socket socket4;

        @BeforeAll
        public static void setup()
        {
            server = new Server();
            Thread serverThread = new Thread(() -> server.startServer(TEST_PORT));
            serverThread.start();
        }

        @BeforeEach
        public void setupConnections()
        {
            try // to connect to server with multiple clients
            {
                socket0 = new Socket("localhost", TEST_PORT);
                socket1 = new Socket("localhost", TEST_PORT);
                socket2 = new Socket("localhost", TEST_PORT);
                socket3 = new Socket("localhost", TEST_PORT);
                socket4 = new Socket("localhost", TEST_PORT);
            }
            catch (IOException e)
            {
                fail("Failed to connect to server: " + e.getMessage());
            }
        }

        // @Test
        // public void testJSONRequests() throws IOException
        // {
        //     Socket[] sockets =
        //     { socket0, socket1, socket2, socket3, socket4 };
            
        //     for (Socket socket : sockets)
        //     {
        //         JsonMapper jsonMapper = JsonMapper.builder()
        //     .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
        //     .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        //     .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
        //     .build();
             
        //         InputStream inputStream = socket.getInputStream();
        //         OutputStream outputStream = socket.getOutputStream();
        //         BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
        //         BufferedOutputStream output = new BufferedOutputStream(outputStream);

        //         output.write(JSONRequests.validJSON.getBytes());
        //         output.flush();
        //         assertTrue(isValidJson(jsonMapper, input.readLine()));

        //         for (String jsonRequest: JSONRequests.getInvalidJSONRequests()) {
        //             output.write(jsonRequest.getBytes());
        //             output.flush();
        //             // System.out.println(isValidJson(jsonMapper, input.readLine()));
        //             assertFalse(isValidJson(jsonMapper, input.readLine()));
        //         }

        //         socket.close();
        //     }

        // }

        @AfterAll
        static void tearDown()
        {
            server.stopServer();
        }

        public static boolean isValidJson(JsonMapper jsonMapper, String jsonString)
        {
                System.out.println(jsonString);
            
            if (jsonString == null) {
            return false;
            }


            
            try
            {
                jsonMapper.readTree(jsonString);
                return true; // Valid JSON
            }
            catch (JsonProcessingException e)
            {
                return false; // Invalid JSON
            }
        }
    }