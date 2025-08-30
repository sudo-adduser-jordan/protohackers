package server.d1;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data // {"method":"isPrime","prime":false}
@AllArgsConstructor
public class ResponseJSON
{
    private String method;
    private boolean isPrime;
}
