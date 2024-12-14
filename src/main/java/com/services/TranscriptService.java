package com.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.assemblyai.api.AssemblyAI;
import com.assemblyai.api.PollingTranscriptsClient;
import com.assemblyai.api.resources.transcripts.TranscriptsClient;
import com.assemblyai.api.resources.transcripts.requests.TranscriptParams;
import com.assemblyai.api.resources.transcripts.types.Transcript;
import com.assemblyai.api.resources.transcripts.types.TranscriptStatus;

@Service
public class TranscriptService {

	private final AssemblyAI client;

	private String apiKey = "abe44e5a3103481d831431dfc5243bf1";

	public TranscriptService() {
		this.client = AssemblyAI.builder().apiKey(apiKey).build();
	}

	public String transcribe(MultipartFile file) throws Exception {
		// Tạo tệp tạm thời từ MultipartFile
		File tempFile = Files.createTempFile("audio", ".mp3").toFile();
		file.transferTo(tempFile);

		// Tải tệp lên AssemblyAI và nhận URL
		String fileUrl = uploadFileToAssemblyAI(tempFile);

		// Tạo transcript parameters
		TranscriptParams transcriptParams = TranscriptParams.builder().audioUrl(fileUrl).build();

		// Gửi yêu cầu transcription
		TranscriptsClient transcriptsClient = client.transcripts();
		Transcript transcript = ((PollingTranscriptsClient) transcriptsClient).transcribe(transcriptParams);

		// Đợi cho đến khi transcript hoàn thành
		while (transcript.getStatus() == TranscriptStatus.QUEUED
				|| transcript.getStatus() == TranscriptStatus.PROCESSING) {
			TimeUnit.SECONDS.sleep(5); // Chờ 5 giây trước khi kiểm tra lại
			transcript = transcriptsClient.get(transcript.getId());
		}

		// Kiểm tra kết quả transcription
		if (transcript.getStatus() == TranscriptStatus.COMPLETED) {
			return transcript.getText().orElse(null);
		} else {
			throw new Exception("Transcript failed with error: " + transcript.getError().orElse("Unknown error"));
		}
	}

	private String uploadFileToAssemblyAI(File file) throws IOException, ParseException {
		String uploadUrl = null;
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpPost uploadRequest = new HttpPost("https://api.assemblyai.com/v2/upload");
			uploadRequest.setHeader("Authorization", "Bearer " + apiKey);

			HttpEntity entity = MultipartEntityBuilder.create().addBinaryBody("file", file).build();

			uploadRequest.setEntity(entity);
			CloseableHttpResponse response = httpClient.execute(uploadRequest);

			if (response.getCode() == 200) {
				String responseBody = EntityUtils.toString(response.getEntity());
				uploadUrl = extractUploadUrl(responseBody);
			} else {
				System.err.println("Tải tệp lên thất bại: " + response.getReasonPhrase());
			}
		}

		return uploadUrl;
	}

	private String extractUploadUrl(String responseBody) {
		int startIndex = responseBody.indexOf("upload_url") + 13;
		int endIndex = responseBody.indexOf("\"", startIndex);
		return responseBody.substring(startIndex, endIndex);
	}
}
