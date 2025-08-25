package server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// {"method":"isPrime","number":123} request

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestJSON {
    private String method;    
    private int number;
}
