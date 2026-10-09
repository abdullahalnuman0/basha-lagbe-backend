package com.massseat.app.dto.chat;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenChatRequest {

    /**
     * Room id.
     */
    @NotNull
    private Long contextId;
}
