package com.racha.rachavoting.mapper;

import org.springframework.stereotype.Component;

import com.racha.rachavoting.model.Logo;
import com.racha.rachavoting.payload.logo.LogoPublicDTO;

@Component
public class LogoMapper {

    public LogoPublicDTO topublicDto(Logo logo) {
        if (logo == null) {
            return null;
        }
        return LogoPublicDTO.builder()
                .Name(logo.getName())
                .contentType(logo.getContentType())
                .data(logo.getData())
                .build();

    }
}
