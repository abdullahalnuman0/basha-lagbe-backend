package com.massseat.app.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FcmTokenRequest {

    @NotBlank
    @Size(max = 512)
    private String token;
}
