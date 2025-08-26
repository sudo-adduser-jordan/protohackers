package server;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data // {"method":"isPrime","number":123} 
public class RequestJSON {
    @JsonProperty(required = true)
    private String method;    
    @JsonProperty(required = true)
    private int number;
}
