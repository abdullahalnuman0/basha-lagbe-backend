package com.massseat.app.service;

import jakarta.validation.constraints.Email;

public interface EmailService {

    void sendOtp(String to, String code,String purpose);

}
