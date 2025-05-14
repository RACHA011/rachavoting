package com.racha.rachavoting.payload.candidate;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CandidateAdminDTO extends CandidatePrivateDTO {
    private Set<Long> authorityIds;
}
