package com.services;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.assemblyai.api.AssemblyAI;
import com.assemblyai.api.resources.transcripts.types.Transcript;
import com.assemblyai.api.resources.transcripts.types.TranscriptLanguageCode;
import com.assemblyai.api.resources.transcripts.types.TranscriptStatus;
import com.assemblyai.api.resources.transcripts.types.TranscriptOptionalParams; // Import TranscriptOptionalParams

@Service
public class TranscriptService {

	private static final String API_KEY = "abe44e5a3103481d831431dfc5243bf1";

	public String Transcript(MultipartFile file) throws IOException {

		AssemblyAI client = AssemblyAI.builder().apiKey(API_KEY).build();

		TranscriptOptionalParams params = TranscriptOptionalParams.builder().languageCode(TranscriptLanguageCode.VI)
				.build();
		Transcript transcript = client.transcripts().transcribe(file.getInputStream(), params);

		if (transcript.getStatus().equals(TranscriptStatus.ERROR)) {
			System.err.println(transcript.getError().get());
			System.exit(1);
		}

		return transcript.getText().get();
	}
}
