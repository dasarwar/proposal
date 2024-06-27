package org.huduu.profile.proposal.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.huduu.profile.model.Proposal;
import org.huduu.profile.model.ResponseStatus;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProposalResponse {
    private ResponseStatus responseStatus;
    private List<Proposal> proposedByProposals;
    private List<Proposal> proposedForProposals;
}
