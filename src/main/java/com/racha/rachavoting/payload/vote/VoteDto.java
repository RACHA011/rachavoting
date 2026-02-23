package com.racha.rachavoting.payload.vote;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteDto {

    @NotBlank
    private String idNo;

    @NotBlank
    private String regNo;

    @NotBlank
    private String secretKey;
}
