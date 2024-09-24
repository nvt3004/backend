package com.responsedto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyResponseDTO {
    private int replyId;
    private UserResponseDTO userReply;
    private String content;
    private Date replyDate;
}
