package org.huduu.profile.proposal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.huduu.profile.model.HuduProfile;
import org.huduu.profile.model.ResponseStatus;
import org.huduu.profile.model.SummaryProfile;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchingResponse {
    private ResponseStatus responseStatus;
    private List<HuduProfile> matchedProfiles;
}
