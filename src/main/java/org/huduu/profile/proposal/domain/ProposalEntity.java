package org.huduu.profile.proposal.domain;

import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Entity(name = "proposal")
@Table (name="proposal", schema = "proposaldb")
@Data
public class ProposalEntity implements Serializable {

    private static final long serialVersionUID = -8194838891217370271L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name= "display_name")
    private String displayName;

    @Column(name = "proposed_by_profile", nullable = false, length = 30)
    private String proposedByProfile;

    @Column(name = "proposed_for_profile", nullable = false, length = 30)
    private String proposedForProfile;

    @Column(name="proposal_state", nullable = false, length = 20)
    private String proposalState;

    @Column(name = "reason_for_rejection", length = 200)
    private String reasonForRejection;

    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProposalHistoryEntity> proposalHistory;

    @CreatedDate
    private LocalDateTime createDate;

    @LastModifiedDate
    private LocalDateTime modifiedDate;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String modifiedBy;

    public ProposalEntity displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public ProposalEntity proposedByProfile(String proposedByProfile) {
        this.proposedByProfile = proposedByProfile;
        return this;
    }

    public ProposalEntity proposedForProfile(String proposedForProfile) {
        this.proposedForProfile = proposedForProfile;
        return this;
    }

    public ProposalEntity proposalState(String proposalState) {
        this.proposalState = proposalState;
        return this;
    }

    public ProposalEntity reasonForRejection(String reasonForRejection) {
        this.reasonForRejection = reasonForRejection;
        return this;
    }

    public ProposalEntity proposalHistory(Set<ProposalHistoryEntity> proposalHistory) {
        this.proposalHistory = proposalHistory;
        return this;
    }

    public ProposalEntity createDate(LocalDateTime createDate) {
        this.createDate = createDate;
        return this;
    }

    public ProposalEntity modifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
        return this;
    }

    public ProposalEntity createdBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public ProposalEntity modifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
        return this;
    }
}
