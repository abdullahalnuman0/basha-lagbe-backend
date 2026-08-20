package com.massseat.app.dto.auth;

import com.massseat.app.entity.enums.Gender;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank
    @Size(max = 100)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String phone;

    @NotBlank
    @Size(min = 6, max = 72)
    private String password;

    private Gender gender;

    @Size(max = 100)
    private String division;

    @Size(max = 100)
    private String district;

    @Size(max = 150)
    private String area;

    @Size(max = 150)
    private String institution;

    @AssertTrue(message = "You must accept the Terms & Conditions and Privacy Policy")
    private boolean termsAccepted;

}
