package server.d1;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data // {"method":"isPrime","number":123} 
@JsonPropertyOrder({"method", "number"})
public class RequestJSON
{
    private String method;
    private double number;

    public RequestJSON(
            @JsonProperty(value = "method", required = true)
            String method,
            @JsonProperty(value = "number", required = true)
            double number)
    {
        this.method = method;
        this.number = number;
    }
}
