package protohackers.server.d1;

public class JSONRequests
{
    // Valid JSON string
    public static final String validJSON = """
{"method":"isPrime","number":123}""";

    public static final String validJSONResponse = """
{"method":"isPrime","prime":false}""";

    // Invalid JSON strings
    public static final String invalidJSONMissingColon = """
{"method":"isPrime","number"123}""";

    public static final String invalidJSONTrailingComma = """
{"method":"isPrime","number":123,}""";

    public static final String invalidJSONUnclosedBrace = """
{"method":"isPrime","number":123""";

    public static final String invalidJSONExtraComma = """
{"method":"isPrime","number":123,}""";

    public static final String invalidJSONMissingQuotes = """
{method:"isPrime","number":123}""";

    public static final String invalidJSONWrongQuotes = """
{'method':'isPrime','number':123}""";

    public static final String invalidJSONEmptyObject = """
{}""";

    public static final String invalidJSONEmptyArray = """
[]""";

    public static final String invalidJSONNumberAsString = """
{"method":"isPrime","number":"123"}""";

    public static final String invalidJSONExtraField = """
{"method":"isPrime","number":123,"extra":"field"}""";

    public static final String invalidJSONMissingMethod = """
{"number":123}""";

    public static final String invalidJSONMissingNumber = """
{"method":"isPrime"}""";

    public static final String invalidJSONIncorrectType = """
{"method":"isPrime","number":"notANumber"}""";

    public static final String invalidJSONArrayInsteadOfObject = """
["method","isPrime","number",123]""";

    public static final String invalidJSONNestedMalformed = """
{"method":"isPrime","parameters":{"number":123}""";

    public static String[] getInvalidJSONRequests()
    {
        return new String[]{invalidJSONMissingColon,
                invalidJSONTrailingComma,
//                invalidJSONUnclosedBrace,
                invalidJSONExtraComma, invalidJSONMissingQuotes, invalidJSONWrongQuotes, invalidJSONEmptyObject,
                invalidJSONEmptyArray,
                invalidJSONNumberAsString,
//                 invalidJSONExtraField,
                invalidJSONMissingMethod, invalidJSONMissingNumber, invalidJSONIncorrectType, invalidJSONArrayInsteadOfObject, invalidJSONNestedMalformed};
    }

}