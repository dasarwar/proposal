package org.huduu.profile.proposal.controller;

import org.apache.commons.lang3.StringUtils;
import org.huduu.profile.common.Constants;
import org.huduu.profile.exception.UnAuthorizedDataAccessException;
import org.huduu.profile.model.Proposal;
import org.huduu.profile.model.ResponseStatus;
import org.huduu.profile.proposal.model.ProposalRequest;
import org.huduu.profile.proposal.model.ProposalResponse;
import org.huduu.profile.proposal.model.ProposalWriteResponse;
import org.huduu.profile.proposal.service.api.ProposalService;
import org.huduu.profile.security.KeycloakRoleConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.List;

import static org.huduu.profile.proposal.model.ProposalState.REQUEST_PHOTO_PP;

@RestController
@RequestMapping("/api")
public class ProposalController {


    @Value("${proposal.activeProposalLimit:10}")
    private int ACTIVE_PROPOSAL_LIMIT;

    @Autowired
    private ProposalService proposalService;

    @Autowired
    private KeycloakRoleConverter roleFinder;

    public ProposalController(ProposalService proposalService) {
        this.proposalService = proposalService;
    }

    @GetMapping(path = "/proposals")
    public ResponseEntity<ProposalResponse> getUserProposals(@RequestHeader(value = "operator-id") String operatorId,
                                                            @RequestHeader(value = "interaction-id") String interactionId,
                                                            @RequestHeader(value = "profileId") String profileId,
                                                            @AuthenticationPrincipal Jwt jwt) {
        HttpHeaders headers = addRequiredHeaders(jwt,operatorId,interactionId);

        if(proposalService.isUserProfile(profileId, roleFinder.getUserName(jwt), roleFinder.isAdmin(jwt), headers)) {
            throw new UnAuthorizedDataAccessException(Constants.UNAUTHORIZED);
        }

        List<Proposal> proposedByProposals = proposalService.getActiveProposedBy(profileId);
        List<Proposal> proposedForProposals = proposalService.getProposedFor(profileId);
        ResponseStatus responseStatus = new ResponseStatus(HttpStatus.OK.value() + "", Constants.PROPOSALS_REQUEST_SUCCESS);
        ProposalResponse response = new ProposalResponse();
        response.setResponseStatus(responseStatus);
        response.setProposedByProposals(proposedByProposals);
        response.setProposedByProposals(proposedForProposals);
        return new ResponseEntity<ProposalResponse>(response, HttpStatus.OK);
    }

    @PostMapping(path = "/proposals")
    public ResponseEntity<ProposalWriteResponse> createProposal(@RequestHeader(value = "operator-id") String operatorId,
                                                                @RequestHeader(value = "interaction-id") String interactionId,
                                                                @RequestBody @Valid ProposalRequest request,
                                                                @AuthenticationPrincipal Jwt jwt) {

        String userId = roleFinder.getUserName(jwt);

        Proposal proposal = buildProposal(request, userId);

        HttpHeaders headers = addRequiredHeaders(jwt, operatorId, interactionId);

        Proposal newProposal = proposalService.create(proposal, ACTIVE_PROPOSAL_LIMIT, userId, roleFinder.isAdmin(jwt), headers);

        ResponseStatus responseStatus = new ResponseStatus(HttpStatus.CREATED.value() + "", Constants.PROPOSAL_CREATED);
        ProposalWriteResponse response = ProposalWriteResponse.builder()
                .proposal(newProposal).responseStatus(responseStatus).build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping(path = "/proposals")
    public ResponseEntity<ProposalWriteResponse> updateProposal(@RequestHeader(value = "operator-id") String operatorId,
                         @RequestHeader(value = "interaction-id") String interactionId,
                         @RequestBody @Valid ProposalRequest request,
                         @AuthenticationPrincipal Jwt jwt) {

        String userId = roleFinder.getUserName(jwt);

        Proposal proposal = buildProposal(request, userId);

        HttpHeaders headers = addRequiredHeaders(jwt, operatorId, interactionId);

        //Proposal newProposal = proposalService.create(proposal, ACTIVE_PROPOSAL_LIMIT, userId, roleFinder.isAdmin(jwt), headers);
        Proposal updatedProposal = proposalService.updateState(proposal, userId, roleFinder.isAdmin(jwt) , headers);

        ResponseStatus responseStatus = new ResponseStatus(HttpStatus.OK.value() + "", Constants.PROPOSAL_UPDATED);
        ProposalWriteResponse response = ProposalWriteResponse.builder()
                .proposal(updatedProposal).responseStatus(responseStatus).build();
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    private static Proposal buildProposal(ProposalRequest request, String userId) {
        String proposalState = request.getProposalState();
        if(StringUtils.isEmpty(proposalState)) {
            proposalState = REQUEST_PHOTO_PP.code();
        }
        Proposal proposal = Proposal.builder().proposedByProfile(request.getProposalByProfile())
                .proposedForProfile(request.getProposalForProfile())
                .proposalState(proposalState)
                .modifiedBy(userId)
                .build();
        if(proposalState.equals(REQUEST_PHOTO_PP.code())) {
            proposal.setDisplayName(request.getProposalByProfile() + "-" + request.getProposalForProfile());
            proposal.setCreatedBy(userId);
        }
        return proposal;
    }

    private HttpHeaders addRequiredHeaders(Jwt jwt, String operatorId, String interactionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + jwt.getTokenValue());
        headers.add("operator-id", operatorId);
        headers.add("interaction-id", interactionId);
        return headers;
    }
}
