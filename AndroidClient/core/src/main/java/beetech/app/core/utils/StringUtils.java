package beetech.app.core.utils;

public class  StringUtils {
    public static String leftPad(String input, int length, String padStr) {

        if(input == null || padStr == null){
            return null;
        }

        if(input.length() >= length){
            return input;
        }

        int padLength = length - input.length();

        StringBuilder paddedString = new StringBuilder();
        paddedString.append(padStr.repeat(padLength));
        paddedString.append(input);

        return paddedString.toString();
    }
}
