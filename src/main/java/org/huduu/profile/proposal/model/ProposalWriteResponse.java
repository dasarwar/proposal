package org.huduu.profile.proposal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.huduu.profile.model.Proposal;
import org.huduu.profile.model.ResponseStatus;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProposalWriteResponse {
    private ResponseStatus responseStatus;
    private Proposal proposal;
}
