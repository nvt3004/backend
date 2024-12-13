package com.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class FormarCurrencyUtil {
	public static String formatCurrency(BigDecimal amount) {
		Locale locale = new Locale("vi", "VN");
		NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
		String formattedAmount = currencyFormatter.format(amount);
		return formattedAmount.replace("₫", "VND");
	}

	  public static String formatDiscount(String discount) {
	        if (discount == null || discount.isEmpty()) {
	            return "0 VND";
	        }

	        if (discount.contains("%")) {
	            return discount;
	        }
	        String numericValueString = discount.replaceAll("[^\\d.-]", "");

	        try {
	            double numericValue = Double.parseDouble(numericValueString);

	            NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
	            numberFormat.setMaximumFractionDigits(0);
	            numberFormat.setGroupingUsed(true);
	            
	            return numberFormat.format(numericValue) + " VND";
	        } catch (NumberFormatException e) {
	            return "0 VND";
	        }
	    }
}
