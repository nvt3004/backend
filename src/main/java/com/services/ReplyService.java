package com.services;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.Feedback;
import com.entities.Reply;
import com.repositories.ReplyJPA;


@Service
public class ReplyService {
	@Autowired
	ReplyJPA replyJPA;
	
	
	public Reply createReply(Reply reply) {	
		return replyJPA.save(reply);
	}
	
	public boolean isReplied(Feedback feedback) {
		if(feedback.getReplies() == null) {
			return false;
		}
		
		if(feedback.getReplies().size()>0) {
			return true;
		}
		
		
		return false;
	}
}
