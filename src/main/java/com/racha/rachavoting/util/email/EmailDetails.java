package com.racha.rachavoting.util.email;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
// @AllArgsConstructor
public class EmailDetails {
    private String recipient;
    private String msgBody;
    private String subject;

    private boolean isHtml;
    private String plainTextContent; 

    public EmailDetails(String recipient, String msgBody, String subject) {
        this.recipient = recipient;
        this.msgBody = msgBody;
        this.subject = subject;
    }
}