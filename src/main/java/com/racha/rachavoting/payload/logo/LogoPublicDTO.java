package com.racha.rachavoting.payload.logo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoPublicDTO {
    private String Name;

    private String contentType;

    private byte[] data;
}
