package server;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data // {"method":"isPrime","number":123} 
public class RequestJSON {
    private String method;    
    private int number;

    public RequestJSON(
        @JsonProperty(value= "method", required = true)String method,
        @JsonProperty(value= "number",required = true)int number
    ) {
        this.method = method;
        this.number = number;
    }

}
