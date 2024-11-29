package com.utils;

import java.math.BigDecimal;

public class NumberToWordsConverterUtil {
	private static final String[] ONES = { "", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín" };
	private static final String[] TENS = { "", "mười", "hai mươi", "ba mươi", "bốn mươi", "năm mươi", "sáu mươi",
			"bảy mươi", "tám mươi", "chín mươi" };
	private static final String[] HUNDREDS = { "", "một trăm", "hai trăm", "ba trăm", "bốn trăm", "năm trăm",
			"sáu trăm", "bảy trăm", "tám trăm", "chín trăm" };

	public static String convert(BigDecimal number) {
	    if (number == null || number.compareTo(BigDecimal.ZERO) == 0) {
	        return "Không đồng";
	    }

	    BigDecimal[] parts = number.stripTrailingZeros().divideAndRemainder(BigDecimal.ONE);
	    long integerPart = parts[0].longValue(); // Phần nguyên
	    int decimalPart = parts[1].movePointRight(parts[1].scale()).intValue(); // Phần thập phân chính xác

	    String result = numberToWords(integerPart) + " đồng";
	    if (decimalPart > 0) {
	        result += " và " + numberToWords(decimalPart) + " xu";
	    }

	    return capitalizeFirstLetter(result.trim());
	}

	// Viết hoa chữ cái đầu tiên
	private static String capitalizeFirstLetter(String input) {
	    if (input == null || input.isEmpty()) {
	        return input;
	    }
	    return input.substring(0, 1).toUpperCase() + input.substring(1);
	}


	private static String numberToWords(long num) {
		if (num == 0)
			return "";
		StringBuilder result = new StringBuilder();

		if (num >= 1_000_000_000) {
			result.append(numberToWords(num / 1_000_000_000)).append(" tỷ ");
			num %= 1_000_000_000;
		}

		if (num >= 1_000_000) {
			result.append(numberToWords(num / 1_000_000)).append(" triệu ");
			num %= 1_000_000;
		}

		if (num >= 1_000) {
			result.append(numberToWords(num / 1_000)).append(" nghìn ");
			num %= 1_000;
		}

		if (num > 0) {
			result.append(convertGroupToWords((int) num));
		}

		return result.toString().trim();
	}

	public static String convertGroupToWords(int num) {
	    StringBuilder result = new StringBuilder();

	    int h = num / 100;          // Hàng trăm
	    int t = (num % 100) / 10;   // Hàng chục
	    int o = num % 10;           // Hàng đơn vị

	    // Hàng trăm
	    if (h > 0) {
	        result.append(HUNDREDS[h]).append(" ");
	    }

	    // Hàng chục
	    if (t > 1) {
	        result.append(TENS[t]).append(" ");
	    } else if (t == 1) {
	        result.append("mười ");
	    } else if (t == 0 && o > 0) { // Thêm từ "lẻ" khi hàng chục = 0 và hàng đơn vị > 0
	        result.append("lẻ ");
	    }

	    // Hàng đơn vị
	    if (o > 0) {
	        if (o == 1 && t > 1) {
	            result.append("mốt");
	        } else if (o == 5 && t > 0) {
	            result.append("lăm");
	        } else {
	            result.append(ONES[o]);
	        }
	    }

	    return result.toString().trim();
	}


}
