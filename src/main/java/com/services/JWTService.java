package com.services;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;

@Service
public class JWTService {

	private SecretKey Key; // Khóa bí mật dùng để ký và xác minh JWT
	private static final long EXPIRATION_TIME = 86400000; // Thời gian hết hạn của token được đặt là 24 giờ (tính bằng milliseconds)

	public JWTService() {
		// Chuỗi bí mật được dùng để tạo khóa cho thuật toán HmacSHA256
		String secreteString = "843567893696976453275974432697R634976R738467TR678T34865R6834R8763T478378637664538745673865783678548735687R3";
		
		// Giải mã chuỗi base64 thành mảng byte và tạo khóa bí mật cho HmacSHA256
		byte[] keyBytes = Base64.getDecoder().decode(secreteString.getBytes(StandardCharsets.UTF_8));
		this.Key = new SecretKeySpec(keyBytes, "HmacSHA256");
	}

	// Phương thức để tạo ra token JWT với tên người dùng đã cung cấp
	public String generateToken(String usename) {
		return Jwts.builder()
				.subject(usename) // Đặt chủ thể (tên người dùng) vào token
				.issuedAt(new Date(System.currentTimeMillis())) // Thời gian phát hành token là thời gian hiện tại
				.expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Thời gian hết hạn sau 24 giờ
				.signWith(Key) // Ký token bằng khóa bí mật
				.compact(); // Nén token thành chuỗi
	}

	// Phương thức để lấy tên người dùng từ token bằng cách truy xuất claim "subject"
	public String extractUsername(String token) {
		return extractClaims(token, Claims::getSubject); // Lấy trường "subject" (tên người dùng) từ token
	}

	// Phương thức chung để lấy ra các claim cụ thể từ token dựa trên hàm đã truyền vào
	private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {
		// Phân tích và xác thực JWT bằng khóa bí mật, sau đó áp dụng hàm để lấy claim cụ thể
		return claimsTFunction.apply(Jwts.parser().verifyWith(Key).build().parseSignedClaims(token).getPayload());
	}

	// Xác thực token bằng cách kiểm tra tên người dùng đã lấy từ token với tên người dùng đã cung cấp và đảm bảo token chưa hết hạn
	public boolean isTokenValid(String token, String username) {
		final String usernameReadFromToken = extractUsername(token);
		return (usernameReadFromToken.equals(username) && !isTokenExpired(token));
	}

	// Kiểm tra tính hợp lệ của chữ ký token (đảm bảo token không bị giả mạo)
	public boolean isSignature(String token) {
		if (token == null) {
			return false; // Token null không hợp lệ
		}
		try {
			extractUsername(token); // Thử lấy tên người dùng; nếu thành công thì chữ ký hợp lệ
			return true;
		} catch (SignatureException e) {
			return false; // Nếu có lỗi SignatureException, token không hợp lệ
		}
	}

	// Kiểm tra token có hết hạn chưa bằng cách so sánh thời gian hết hạn với thời gian hiện tại
	public boolean isTokenExpired(String token) {
		System.out.println("Chạy vào đây " + token);
		try {
			// Kiểm tra nếu ngày hết hạn của token trước thời gian hiện tại
			extractClaims(token, Claims::getExpiration).before(new Date());
			return false; // Nếu chưa hết hạn, trả về false
		} catch (ExpiredJwtException e) {
			return true; // Nếu có lỗi ExpiredJwtException, token đã hết hạn
		}
	}
}

