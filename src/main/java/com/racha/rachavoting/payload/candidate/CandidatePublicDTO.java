
package com.racha.rachavoting.payload.candidate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidatePublicDTO {
    private Long id;
    private String fullName;
    private String profilePicture;
    private String state;
    private Long partyId;
    private String partyName;
    private String manifestoUrl;
}
