//package protohackers.server.d1;
//
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.MapperFeature;
//import com.fasterxml.jackson.databind.json.JsonMapper;
//import protohackers.ServerLogFormatter;
//import protohackers.ServerLogOptions;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.math.BigInteger;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.Objects;
//import java.util.logging.Logger;
//
//public class Server
//{
//    private static final Logger log = ServerLogFormatter.getLogger(Server.class);
//    private static final ServerLogOptions logger = new ServerLogOptions(log);
//    private static final int PORT = 42069;
////    private static final String HOST = "localhost";
//
//    public static void main(String[] args)
//    {
//        new Server().start(PORT);
//    }
//
//    public void start(int port)
//    {
//        try (ServerSocket serverSocket = new ServerSocket(port))
//        {
//            logger.info("Server is listening on port " + port);
//
//            while (!serverSocket.isClosed())
//            {
//                try (Socket clientSocket = serverSocket.accept();
//                     BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//                     PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
//                ) // to accept client socket
//                {
//                        logger.info("Client connected: " + clientSocket.getInetAddress());
//
//                        String requestString = reader.readLine();
//
//                        logger.info("Received:\t" + requestString);
////
//                        JsonMapper jsonMapper = JsonMapper.builder()
//                                                          .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
//                                                          .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//                                                          .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
//                                                          .build();
//                        RequestJSON requestJSON = stringToRequestJSON(requestString, jsonMapper);
//
//                        if (requestJSON == null) {
//                            logger.info("requestJSON: null");
//
//                            writer.println(requestString);
//                            logger.info("Sent:\t\t" + requestString);
//
//                            clientSocket.close();
//                            logger.error("Client disconnected: " + clientSocket.getInetAddress());
//                            return;
//                        }
//                            boolean isPrimeResult = isPrime(requestJSON.getNumber());
//                            ResponseJSON responseJSON = new ResponseJSON("isPrime", isPrimeResult);
//                            String responseString = jsonMapper.writeValueAsString(responseJSON);
//
//                                writer.println(responseString);
//                                logger.info("Sent:\t\t" + responseString);
////                                clientSocket.close();
//                }
//                catch (IOException e)
//                {
//                    logger.info("Client connection error: " + e.getMessage());
//                }
//            }
//        }
//        catch (IOException e)
//        {
//            logger.info("Server exception: " + e.getMessage());
//        }
//    }
//
//    public  static boolean isPrime(double number)
//    {
//        if (number != Math.floor(number) || number <= 1) return false;
//        BigInteger bigInt = BigInteger.valueOf((long) number);
//        return bigInt.isProbablePrime(100);
//    }
//
//    private static RequestJSON stringToRequestJSON(String requestString, JsonMapper jsonMapper) {
//        try {
//            RequestJSON requestJSON = jsonMapper.readValue(requestString, RequestJSON.class);
//            if (!Objects.equals(requestJSON.getMethod(), "isPrime")) {
//                return null;
//            }
//            return requestJSON;
//        } catch (JsonProcessingException e) {
//            log.warning("JSON processing exception");
//            return null;
//        }
//    }
//
//
//
//}
