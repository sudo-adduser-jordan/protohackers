package server;

import lombok.Data;

@Data // {"method":"isPrime","number":123} 
public class RequestJSON {
    private String method;    
    private int number;
}
