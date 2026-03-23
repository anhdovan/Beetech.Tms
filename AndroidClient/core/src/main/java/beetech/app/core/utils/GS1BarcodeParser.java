package beetech.app.core.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GS1BarcodeParser {

    public static Map<String, String> parseGS1Barcode(String barcode) {
        Map<String, String> result = new HashMap<>();

        // Corrected regular expression with exact serial number length
        Pattern combinedPattern = Pattern.compile("01(\\d{14})10((?:(?!21\\d{10}).)*)(?=21\\d{10})21(\\d{10})");

        // Extract data using the corrected pattern
        Matcher matcher = combinedPattern.matcher(barcode);
        if (matcher.find()) {
            result.put("gtin", matcher.group(1));
            result.put("batch", matcher.group(2));
            result.put("serial", matcher.group(3));
        }

        return result;
    }

    public static boolean codesEqual(String code, String gtin) {
        if(code.length()!=14) code = "0" + code;
        if(gtin.length()!=14) gtin = "0" + gtin;
        return code.equals(gtin);
    }
}