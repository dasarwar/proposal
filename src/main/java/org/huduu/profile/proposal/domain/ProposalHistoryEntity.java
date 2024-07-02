package org.huduu.profile.proposal.domain;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
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

