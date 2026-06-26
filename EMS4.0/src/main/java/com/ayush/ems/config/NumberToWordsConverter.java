package com.ayush.ems.config;

import java.math.BigDecimal;

public class NumberToWordsConverter {

    private static final String[] units = {
        "", "One", "Two", "Three", "Four", "Five",
        "Six", "Seven", "Eight", "Nine", "Ten", "Eleven",
        "Twelve", "Thirteen", "Fourteen", "Fifteen",
        "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] tens = {
        "", "", "Twenty", "Thirty", "Forty", "Fifty",
        "Sixty", "Seventy", "Eighty", "Ninety"
    };

    public static String convert(BigDecimal amount) {
        long rupees = amount.longValue();
        int paise = amount.remainder(BigDecimal.ONE).movePointRight(2).intValue();

        String rupeesInWords = convertToWords(rupees);
        String paiseInWords = (paise > 0) ? " and " + convertToWords(paise) + " Paise" : "";

        return rupeesInWords + " Rupees" + paiseInWords;
    }

    private static String convertToWords(long number) {
        if (number == 0) return "Zero";

        StringBuilder words = new StringBuilder();

        if ((number / 10000000) > 0) {
            words.append(convertToWords(number / 10000000)).append(" Crore ");
            number %= 10000000;
        }
        if ((number / 100000) > 0) {
            words.append(convertToWords(number / 100000)).append(" Lakh ");
            number %= 100000;
        }
        if ((number / 1000) > 0) {
            words.append(convertToWords(number / 1000)).append(" Thousand ");
            number %= 1000;
        }
        if ((number / 100) > 0) {
            words.append(convertToWords(number / 100)).append(" Hundred ");
            number %= 100;
        }
        if (number > 0) {
            if (words.length() > 0) words.append("and ");
            if (number < 20) {
                words.append(units[(int) number]);
            } else {
                words.append(tens[(int) number / 10]);
                if ((number % 10) > 0) {
                    words.append("-").append(units[(int) number % 10]);
                }
            }
        }

        return words.toString().trim();
    }
}
