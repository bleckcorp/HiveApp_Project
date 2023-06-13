package com.bctech.hive.service;



import com.bctech.hive.dto.request.EmailDto;

import java.io.IOException;

public interface EmailService {

    void sendEmail(EmailDto emailDto) throws IOException;

}