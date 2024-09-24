package com.responsedto;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponseDTO {
	private int feedbackId;
	private String comment;
	private int productId;
	private String productName;
	private Date feedbackDate;
	private int rating;
	private UserResponseDTO user;
	private List<String> images;
	private ReplyResponseDTO reply;
	
}
