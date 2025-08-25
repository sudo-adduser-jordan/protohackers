package server;

import lombok.AllArgsConstructor;
import lombok.Data;

// {"method":"isPrime","prime":false} response

@Data
@AllArgsConstructor
public class ResponseJSON {
    private String method;
    private boolean isPrime;    
}
