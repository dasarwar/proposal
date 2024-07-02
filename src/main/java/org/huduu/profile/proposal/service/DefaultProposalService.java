package org.huduu.profile.proposal.service;

import org.apache.commons.lang3.StringUtils;
import org.huduu.profile.common.Constants;
import org.huduu.profile.exception.InvalidProfileException;
import org.huduu.profile.exception.UnAuthorizedDataAccessException;
import org.huduu.profile.model.HuduProfile;
import org.huduu.profile.model.Proposal;
import org.huduu.profile.proposal.domain.ProposalEntity;
import org.huduu.profile.proposal.domain.ProposalHistoryEntity;
import org.huduu.profile.proposal.exception.IllegalProposalStateException;
import org.huduu.profile.proposal.exception.InvalidProposalException;
import org.huduu.profile.proposal.model.ProposalState;
import org.huduu.profile.proposal.repository.ProposalRepository;
import org.huduu.profile.proposal.service.api.ProposalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.huduu.profile.common.ProfileStatus.PUBLISHED;

@Service
public class DefaultProposalService implements ProposalService {

    @Autowired
    private ProfileClientService profileClientService;

    @Autowired
    private ProposalRepository proposalRepository;

    private static final String STATE_PREFIX_DECLINE = "DECLINE";

    @Override
    public Proposal create(Proposal proposal, int limit, String userId, boolean isAdmin, HttpHeaders httpHeaders) {
        List<HuduProfile> profiles = profileClientService.getProfiles(
                List.of(proposal.getProposedByProfile(),proposal.getProposedForProfile()), httpHeaders);

        validateProposal(proposal, profiles, limit, userId, isAdmin);

        ProposalEntity proposalEntity = mapToProposalEntity(proposal);
        proposalEntity.setProposalHistory(Set.of(getProposalHistoryEntity(proposalEntity)));
        ProposalEntity savedEntity = proposalRepository.save(proposalEntity);
        return mapToProposal(savedEntity);
    }

    @Override
    public Proposal updateState(Proposal proposal, String userId, boolean isAdmin, HttpHeaders httpHeaders) {
        //load existing proposal
        ProposalEntity currentProposal = proposalRepository.findByProposedByProfileAndProposedForProfile(
                proposal.getProposedByProfile(), proposal.getProposedForProfile()).get(0);
        if(currentProposal == null) {
            throw new InvalidProfileException(Constants.INVALID_PROPOSAL);
        }
        // load profiles
        List<HuduProfile> profiles = profileClientService.getProfiles(
                List.of(proposal.getProposedByProfile(),proposal.getProposedForProfile()), httpHeaders);

        // validate proposal
        validateUpdateProposal(proposal, profiles, currentProposal, userId, isAdmin);

        currentProposal.setProposalState(proposal.getProposalState());
        currentProposal.setModifiedBy(userId);
        currentProposal.setModifiedDate(LocalDateTime.now());
        currentProposal.getProposalHistory().add(getProposalHistoryEntity(currentProposal));
        ProposalEntity savedEntity = proposalRepository.save(currentProposal);
        return mapToProposal(savedEntity);
    }

    private void validateUpdateProposal(Proposal proposal, List<HuduProfile> profiles, ProposalEntity currentProposal, String login, boolean isAdmin) {
        ProposalState currentState = ProposalState.fromCode(currentProposal.getProposalState());
        List<ProposalState> possibleNextStates = currentState.nextStates();
        if(!possibleNextStates.contains(ProposalState.fromCode(proposal.getProposalState()))) {
            throw new IllegalProposalStateException("Proposal State not allowed");
        }

        if(proposal.getProposalState().endsWith("PP")) {
            HuduProfile proposingProfile = getProfile(profiles, proposal.getProposedByProfile());
            if(!proposingProfile.getUserId().equals(login) && !isAdmin) {
                throw new UnAuthorizedDataAccessException("User not allowed to modify state");
            }
        } else if(proposal.getProposalState().endsWith("PF")) {
            HuduProfile proposedForProfile = getProfile(profiles, proposal.getProposedForProfile());
            if(!proposedForProfile.getUserId().equals(login) && !isAdmin) {
                throw new UnAuthorizedDataAccessException("User not allowed to modify state");
            }
        }
    }

    private Proposal mapToProposal(ProposalEntity entity) {
        return Proposal.builder()
                .proposalState(entity.getProposalState())
                .proposedForProfile(entity.getProposedForProfile())
                .proposedByProfile(entity.getProposedByProfile())
                .displayName(entity.getDisplayName())
                .reasonForRejection(entity.getReasonForRejection())
                .createDate(entity.getCreateDate())
                .createdBy(entity.getCreatedBy())
                .modifiedDate(entity.getModifiedDate())
                .modifiedBy(entity.getModifiedBy())
                .build();
    }

    private ProposalEntity mapToProposalEntity(Proposal proposal) {
        return new ProposalEntity()
                .proposalState(proposal.getProposalState())
                .proposedForProfile(proposal.getProposedForProfile())
                .proposedByProfile(proposal.getProposedByProfile())
                .displayName(proposal.getDisplayName())
                .reasonForRejection(proposal.getReasonForRejection())
                .createDate(proposal.getCreateDate() != null ? proposal.getCreateDate() : LocalDateTime.now())
                .modifiedDate(LocalDateTime.now())
                .createdBy(proposal.getCreatedBy())
                .modifiedBy(proposal.getModifiedBy());
    }

    private ProposalHistoryEntity getProposalHistoryEntity(ProposalEntity proposal) {
        ProposalHistoryEntity historyEntity = new ProposalHistoryEntity();
        historyEntity.setProposal(proposal);
        historyEntity.setProposalState(proposal.getProposalState());
        historyEntity.setFeedback(proposal.getReasonForRejection());
        historyEntity.setCreateBy(proposal.getCreatedBy());
        historyEntity.setCreateDate(LocalDateTime.now());
        return historyEntity;
    }

    private void validateProposal(Proposal proposal, List<HuduProfile> profiles, int limit,
                                  String logonId, boolean isAdmin) {
        if(profiles.size() != 2) {
            throw new InvalidProfileException(Constants.PROFILES_NOT_VALID);
        }
        HuduProfile proposingProfile = getProfile(profiles, proposal.getProposedByProfile());
        HuduProfile proposedForProfile = getProfile(profiles, proposal.getProposedForProfile());
        if(!isAuthorized(proposingProfile, logonId, isAdmin)) {
            throw new UnAuthorizedDataAccessException(Constants.UNAUTHORIZED);
        }

        if(!isProposalReady(proposingProfile) || !isProposalReady(proposedForProfile)) {
            throw new InvalidProposalException(Constants.INVALID_PROPOSAL);
        }

        if(proposingProfile.getGender().equalsIgnoreCase(proposedForProfile.getGender())) {
            throw new InvalidProposalException(Constants.INVALID_PROPOSAL);
        }

        //
        List<ProposalEntity> proposalEntitiesForProposingProfile = getProposedByProposalEntities(
                proposingProfile.getProfileId());

        List<ProposalEntity> proposalEntitiesForProposedProfile = getProposedByProposalEntities(
                proposedForProfile.getProfileId());
        // there is already a proposal
        if(proposalEntitiesForProposingProfile.stream().anyMatch(proposalEntity ->
                proposalEntity.getProposedForProfile().equals(proposal.getProposedForProfile()))) {
            throw new InvalidProposalException(Constants.DUPLICATE_PROPOSAL_ATTEMPT);

        }

        if(proposalEntitiesForProposedProfile.stream().anyMatch(proposalEntity ->
                proposalEntity.getProposedForProfile().equals(proposal.getProposedByProfile()))) {
            throw new InvalidProposalException(Constants.DUPLICATE_PROPOSAL_ATTEMPT);

        }

        if(getActiveProposedByProposalEntities(proposalEntitiesForProposingProfile).size() >= limit) {
            throw new InvalidProfileException(Constants.PROPOSAL_LIMIT_ERROR);
        }

    }

    private boolean isAuthorized(HuduProfile profile, String logon, boolean isAdmin) {
        if(!isAdmin && !logon.equals(profile.getUserId())) {
            return false;
        }
        return true;
    }

    private static boolean isProposalReady(HuduProfile profile) {
        if(profile.getStatus() != null && PUBLISHED.getStatus().equals(profile.getStatus()) && profile.getProfilePicture1() != null
                && !StringUtils.isEmpty(profile.getProfilePicture1().trim())) {
            return true;
        }
        return false;
    }

    private HuduProfile getProfile(List<HuduProfile> profileEntities, String profileId) {
        for(HuduProfile entity : profileEntities) {
            if(profileId.equals(entity.getProfileId())) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public List<Proposal> getProposedBy(String profileId) {
        return proposalRepository.findByProposedByProfile(profileId).stream()
                .map(this::mapToProposal).collect(Collectors.toList());
    }

    @Override
    public List<Proposal> getProposedFor(String profileId) {
        return proposalRepository.findByProposedForProfile(profileId).stream()
                .map(entity -> mapToProposal(entity)).collect(Collectors.toList());
    }

    @Override
    public List<Proposal> getActiveProposedBy(String profileId) {
        return proposalRepository.findByProposedByProfile(profileId).stream()
                .filter(entity -> !entity.getProposalState().startsWith(STATE_PREFIX_DECLINE))
                .map(this::mapToProposal).collect(Collectors.toList());
    }

    @Override
    public List<Proposal> getActiveProposedFor(String profileId) {
        return proposalRepository.findByProposedForProfile(profileId).stream()
                .filter(entity -> !entity.getProposalState().startsWith(STATE_PREFIX_DECLINE))
                .map(this::mapToProposal).collect(Collectors.toList());
    }

    private List<ProposalEntity> getProposedByProposalEntities(String profileId) {
        return proposalRepository.findByProposedByProfile(profileId);
    }

    private List<ProposalEntity> getActiveProposedByProposalEntities(List<ProposalEntity> allProposalEntities) {
        return allProposalEntities.stream()
                .filter(proposalEntity ->
                        !proposalEntity.getProposalState().startsWith(STATE_PREFIX_DECLINE)).collect(Collectors.toList());
    }

    @Override
    public List<Proposal> getProposalsByState(String proposalState) {
        return proposalRepository.findByProposalState(proposalState).stream()
                .map(this::mapToProposal).collect(Collectors.toList());
    }

    @Override
    public boolean isUserProfile(String profileId, String userName, boolean admin, HttpHeaders httpHeaders) {
        List<HuduProfile> profiles = profileClientService.getProfiles(List.of(profileId), httpHeaders);

        if (!admin && (profiles == null || profiles.isEmpty() || !userName.equals(profiles.get(0).getUserId()))) {
            return false;
        }

        return true;
    }

}
