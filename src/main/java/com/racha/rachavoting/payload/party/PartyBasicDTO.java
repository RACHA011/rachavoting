package com.racha.rachavoting.payload.party;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyBasicDTO {
    private Long id;
    private String name;
    private String abbreviation;
    private String leader;
    private String slogan;
    private String logoUrl;
    private List<String> colors;
}
