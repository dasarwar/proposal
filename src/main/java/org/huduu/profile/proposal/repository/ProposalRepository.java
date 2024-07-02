package org.huduu.profile.proposal.repository;



import org.huduu.profile.proposal.domain.ProposalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProposalRepository extends JpaRepository<ProposalEntity, Long> {
    List<ProposalEntity> findByProposedForProfile(String proposedForProfile);

    List<ProposalEntity> findByProposedByProfile(String proposedByProfile);

    List<ProposalEntity> findByProposedByProfileAndProposedForProfile(
            String proposedByProfile, String proposedForProfile);

    List<ProposalEntity> findByProposalState(String proposalState);


}
