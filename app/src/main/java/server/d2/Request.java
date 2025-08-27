package server.d2;

import lombok.Builder;
import lombok.Getter;

enum MessageTypes
{
    INSERT, QUERY
}

@Getter
@Builder
class Request {
    private final MessageTypes MessageType;
    private final int FirstValue; // 32
    private final int SecondValue; // 32

    @Override // review format
    public String toString() {
        return "%s, %d, %d".formatted(MessageType.toString(), FirstValue, SecondValue);
    }
}