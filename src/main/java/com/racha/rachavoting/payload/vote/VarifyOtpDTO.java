package com.racha.rachavoting.payload.vote;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VarifyOtpDTO {
    @Size(min = 6, max = 6, message = "OTP must be exactly 6 characters")
    private String otp;
}
