package com.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadService {

	private final String URL_DOMAIN = "http://localhost:8080";

	public String getUrlImage(String fileName) {
		if(fileName == null) {
			return null;
		}
		
		return String.format("%s/images/%s", URL_DOMAIN, fileName);
	}
	
	   public String saveBufferedImage(BufferedImage image, String folder) {
	        Path root = Paths.get(String.format("static/%s", folder));
	        String fileName = String.valueOf(new Date().getTime()) + ".png";

	        try {
	            Files.createDirectories(root);
	            File outputFile = root.resolve(fileName).toFile();
	            ImageIO.write(image, "png", outputFile);

	            return fileName;
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	        return null;
	    }

	public String save(MultipartFile file, String forder) {
		Path root = Paths.get(String.format("static/%s", forder));
		String fileName = String.valueOf(new Date().getTime()) + ".jpg";

		try {
			Files.createDirectories(root);
			Files.copy(file.getInputStream(), root.resolve(fileName));

			return fileName;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public byte[] dowload(String fileName) throws IOException {
		Path root = Paths.get(String.format("static/images/%s", fileName));

		return Files.readAllBytes(root);
	}

	public void delete(String fileName, String forder) {
		Path root = Paths.get("static/" + forder);
		Path filePath = root.resolve(fileName);

		try {
			if (Files.exists(filePath)) {
				Files.delete(filePath);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isEmptyFile(List<MultipartFile> files) {

		for (MultipartFile file : files) {
			boolean isEmptyFile = file.isEmpty();

			if (isEmptyFile) {
				return true;
			}
		}

		return false;
	}

	public byte[] converImageBase64ToImage(String base64) {
		return Base64.decodeBase64(base64);
	}

	public String save(String base64, String folder) {
		if(base64 == null) return null;

		byte[] bytes = converImageBase64ToImage(base64);
		Path root = Paths.get(String.format("static/%s", folder));
		String fileName = String.valueOf(new Date().getTime()) + ".jpg";

		try {
			Files.createDirectories(root);

			Path filePath = root.resolve(fileName);
			Files.write(filePath, bytes);

			return fileName;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void delete2(String fileName, String folder) {
		// Tạo đường dẫn đến thư mục ảnh
		Path root = Paths.get("static/" + folder);
		Path filePath = root.resolve(fileName); // Đường dẫn của file cần xóa
	
		try {
			// Kiểm tra nếu tệp tồn tại, thực hiện xóa
			if (Files.exists(filePath)) {
				Files.delete(filePath);
				System.out.println("Đã xóa file: " + fileName);
			} else {
				System.out.println("File không tồn tại: " + fileName);
			}
		} catch (IOException e) {
			e.printStackTrace();
			// Log lỗi nếu có vấn đề trong quá trình xóa
		}
	}


}
