package beetech.app.core.utils;

import static org.epctagcoder.parse.SGTIN.ParseSGTIN.Builder;

import org.epctagcoder.option.SGTIN.SGTINExtensionDigit;
import org.epctagcoder.option.SGTIN.SGTINFilterValue;
import org.epctagcoder.option.SGTIN.SGTINTagSize;
import org.epctagcoder.parse.SGTIN.ParseSGTIN;
import org.epctagcoder.result.SGTIN;




public class SGTIN96Converter {

    // Convert GTIN and Serial Number to SGTIN-96
    public static String toSGTIN96(String gtin, String serialNumber) throws Exception {
        if (!validGtin(gtin)) {
            throw new IllegalArgumentException("GTIN is invalid");
        }
        int start = gtin.length() ==14?1:0;
        String companyPrefix = gtin.substring(start, start + 7);
        String productCode = gtin.substring(start + 7, start + 12);
        ParseSGTIN parseSGTIN = Builder().withCompanyPrefix(companyPrefix)
                .withExtensionDigit(SGTINExtensionDigit.EXTENSION_0)
                .withItemReference(productCode)
                .withSerial(serialNumber)
                .withTagSize(SGTINTagSize.BITS_96)
                .withFilterValue(SGTINFilterValue.ALL_OTHERS_0)
                .build();
        SGTIN sgtin = parseSGTIN.getSGTIN();
        String sgtin96 = sgtin.getRfidTag();
        return sgtin96;
    }

    private static boolean validGtin(String gtin) {
        if (gtin == null || (gtin.length() != 12 && gtin.length() != 13 && gtin.length() != 14)) {
            return false;
        }
        int sum = 0;
        boolean alternate = false; // Iterate over the GTIN digits from right to left
        for (int i = gtin.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(gtin.substring(i, i + 1));
            if (alternate) {
                n *= 3;
            }
            sum += n;
            alternate = !alternate;
        } // Check if the sum is a multiple of 10 return (sum % 10 == 0);
        return sum % 10 == 0;
    }
    // Convert SGTIN-96 to GTIN and Serial Number
    public static String[] fromSGTIN96(String sgtin96) throws Exception {
        SGTIN sgtin = Builder().withRFIDTag(sgtin96).build().getSGTIN();
        String gtin = sgtin.getExtensionDigit() + sgtin.getCompanyPrefix() + sgtin.getItemReference() + sgtin.getCheckDigit();
        return new String[]{gtin, sgtin.getSerial()};
    }

    public static void main(String[] args) throws Exception {
        // Example usage
        String gtin = "12345678901234";
        String serialNumber = "123456789";

        String sgtin96 = toSGTIN96(gtin, serialNumber);
        System.out.println("SGTIN-96: " + sgtin96);

        String[] result = fromSGTIN96(sgtin96);
        System.out.println("GTIN: " + result[0]);
        System.out.println("Serial Number: " + result[1]);
    }
}

