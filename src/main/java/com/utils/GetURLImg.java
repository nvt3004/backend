package com.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetURLImg {
	private static final Logger logger = LoggerFactory.getLogger(GetURLImg.class);

	public static String getURLImg(HttpServletRequest request, String photoName) {
		if (photoName == null || photoName.isEmpty()) {
			logger.error("Photo name is missing");
			throw new IllegalArgumentException("Photo name cannot be null or empty");
		}

		String scheme = request.getScheme(); // http hoặc https
		String serverName = request.getServerName(); // localhost hoặc domain
		int serverPort = request.getServerPort(); // 8080 hoặc cổng khác
		String baseUrl;

		// Kiểm tra nếu là cổng mặc định thì không cần thêm vào URL
		if ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443)) {
			baseUrl = scheme + "://" + serverName;
		} else {
			baseUrl = scheme + "://" + serverName + ":" + serverPort;
		}

		String url = baseUrl + "/api/getImage/" + photoName;
		logger.info("Generated URL: {}", url);

		return url;
	}
}
