//package com.services;
//
//import java.io.File;
//import java.io.IOException;
//
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import com.assemblyai.api.AssemblyAI;
//import com.assemblyai.api.resources.transcripts.types.Transcript;
//import com.assemblyai.api.resources.transcripts.types.TranscriptStatus;
//
//@Service
//public class TranscriptService {
//
//	AssemblyAI client = AssemblyAI.builder().apiKey("abe44e5a3103481d831431dfc5243bf1").build();
//
//	public String transcribe(MultipartFile file) {
//		try {
//
//			File tempFile = File.createTempFile("audio", ".mp3");
//			file.transferTo(tempFile);
//
//			Transcript transcript = client.transcripts().transcribe(tempFile.getAbsolutePath());
//
//			if (transcript.getStatus().equals(TranscriptStatus.ERROR)) {
//				System.err.println(transcript.getError().get());
//				return null;
//			}
//
//			return transcript.getText().get();
//		} catch (IOException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
//}
