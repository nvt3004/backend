package com.utils;

import java.text.Normalizer;

public class RemoveDiacritics {
	public String removeDiacritics(String input) {
		return Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}
}
