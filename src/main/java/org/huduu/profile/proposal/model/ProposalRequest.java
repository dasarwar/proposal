package org.huduu.profile.proposal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProposalRequest {
    @NotBlank
    private String proposalByProfile;
    @NotBlank
    private String proposalForProfile;

    private String proposalState;
}
