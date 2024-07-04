package org.huduu.profile.proposal.domain;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "proposal_history", schema = "proposaldb")
@Getter
@Setter
public class ProposalHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="proposal_id", foreignKey = @ForeignKey(name="PROPOSAL_HISTORY_PROPOSAL_ID_FK"))
    @NotNull
    private ProposalEntity proposal;

    @Column(name = "proposal_state", length = 20)
    private String proposalState;

    @Column(name = "feedback", length = 200)
    private String feedback;

    @CreatedDate
    private LocalDateTime createDate;

    @CreatedBy
    private String createBy;
}

