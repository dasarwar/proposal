package org.huduu.profile.proposal.service.api;

import org.huduu.profile.model.Proposal;
import org.springframework.http.HttpHeaders;

import java.util.List;

public interface ProposalService {
    Proposal create(Proposal proposal, int limit, String userId, boolean isAdmin, HttpHeaders httpHeaders);

    Proposal updateState(Proposal existingProposal, String userId, boolean isAdmin, HttpHeaders httpHeaders);

    List<Proposal> getProposedBy(String profileId);

    List<Proposal> getProposedFor(String profileId);

    List<Proposal> getActiveProposedBy(String profileId);

    List<Proposal> getActiveProposedFor(String profileId);

    List<Proposal> getProposalsByState(String proposalState);

    boolean isUserProfile(String profileId, String userName, boolean admin, HttpHeaders httpHeaders);
}
