package com.massseat.app.dto.chat;

import com.massseat.app.entity.enums.MessageType;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessageRequest {

    @Size(max = 4000)
    private String content;

    private MessageType type;

    @Size(max = 500)
    private String attachmentUrl;
}
