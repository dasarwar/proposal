package org.huduu.profile.proposal.service;

import org.huduu.profile.common.Constants;
import org.huduu.profile.exception.InvalidProfileException;
import org.huduu.profile.exception.UnAuthorizedDataAccessException;
import org.huduu.profile.model.HuduProfile;
import org.huduu.profile.model.Proposal;
import org.huduu.profile.proposal.domain.ProposalEntity;
import org.huduu.profile.proposal.exception.IllegalProposalStateException;
import org.huduu.profile.proposal.exception.InvalidProposalException;
import org.huduu.profile.proposal.repository.ProposalRepository;
import org.huduu.profile.proposal.service.api.ProposalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.huduu.profile.common.Gender.FEMALE;
import static org.huduu.profile.common.Gender.MALE;
import static org.huduu.profile.common.ProfileStatus.PUBLISHED;
import static org.huduu.profile.proposal.model.ProposalState.ACCEPT_PHOTO_REQ_PF;
import static org.huduu.profile.proposal.model.ProposalState.REQUEST_PHOTO_PP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class DefaultProposalServiceTest {

    @Autowired
    private ProposalService proposalService;

    @MockBean
    private ProfileClientService profileClientService;

    @MockBean
    private ProposalRepository proposalRepository;

    @Test
    void testCreateProposal() {
        String proposedBy = "proposed-by";
        String proposedFor = "proposed-for";
        Proposal givenProposal = givenProposal(proposedBy, proposedFor, REQUEST_PHOTO_PP.code());
        ProposalEntity givenProposalEntity = givenProposalEntity(proposedBy, proposedFor,REQUEST_PHOTO_PP.code());
        HttpHeaders httpHeaders = new HttpHeaders();

        HuduProfile proposedByProfile = givenProfile(proposedBy, "user-id1", MALE.code(), PUBLISHED.getStatus(), "picture1" );
        HuduProfile proposedForProfile = givenProfile(proposedFor, "user-id2", FEMALE.code(), PUBLISHED.getStatus(), "picture2" );

        given(profileClientService.getProfiles(anyList(),any(HttpHeaders.class) )).willReturn(List.of(proposedByProfile,proposedForProfile));
        given(proposalRepository.save(any(ProposalEntity.class))).willReturn(givenProposalEntity);

        Proposal savedProposal = proposalService.create(givenProposal, 5, "user-id1", false, httpHeaders);

        assertNotNull(savedProposal);
        assertEquals(givenProposalEntity.getProposedByProfile(), savedProposal.getProposedByProfile());
        assertEquals(givenProposalEntity.getProposedForProfile(), savedProposal.getProposedForProfile());
        assertEquals(givenProposalEntity.getProposalState(), savedProposal.getProposalState());
    }

    @Test
    void testCreateProposalValidation_profileIdsNotValid() {
        String proposedBy = "proposed-by";
        String proposedFor = "proposed-for";
        Proposal givenProposal = givenProposal(proposedBy, proposedFor, REQUEST_PHOTO_PP.code());
        ProposalEntity givenProposalEntity = givenProposalEntity(proposedBy, proposedFor,REQUEST_PHOTO_PP.code());
        HttpHeaders httpHeaders = new HttpHeaders();

        // given profiles not found
        given(profileClientService.getProfiles(anyList(),any(HttpHeaders.class) )).willReturn(new ArrayList<HuduProfile>());
        given(proposalRepository.save(any(ProposalEntity.class))).willReturn(givenProposalEntity);

        Exception exception = assertThrows(InvalidProfileException.class, () -> proposalService.create(givenProposal, 5, "user-id1",false , httpHeaders));
        assertEquals(Constants.PROFILES_NOT_VALID, exception.getMessage());
    }


    @Test
    void testCreateProposalValidation_userCanOnlyProposeForTheirOwnProfile() {
        String proposedBy = "proposed-by";
        String proposedFor = "proposed-for";
        Proposal givenProposal = givenProposal(proposedBy, proposedFor, REQUEST_PHOTO_PP.code());
        ProposalEntity givenProposalEntity = givenProposalEntity(proposedBy, proposedFor,REQUEST_PHOTO_PP.code());
        HttpHeaders httpHeaders = new HttpHeaders();

        HuduProfile proposedByProfile = givenProfile(proposedBy, "user-id1", MALE.code(), PUBLISHED.getStatus(), "picture1" );
        HuduProfile proposedForProfile = givenProfile(proposedFor, "user-id2", FEMALE.code(), PUBLISHED.getStatus(), "picture2" );

        given(profileClientService.getProfiles(anyList(),any(HttpHeaders.class))).willReturn(List.of(proposedByProfile,proposedForProfile));
        given(proposalRepository.save(any(ProposalEntity.class))).willReturn(givenProposalEntity);

        Exception exception = assertThrows(UnAuthorizedDataAccessException.class, () -> proposalService.create(givenProposal, 5, "not-same-as-profile-user", false, httpHeaders));
        assertEquals(Constants.UNAUTHORIZED, exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({"Created, picture1, Published, picture2", "Published, '' , Published, picture2", "Published, picture1, Created, picture2", "Published, picture1, Published,''"})
    void testCreateProposalValidation_incompleteProfileCannotMakeProposal(
            String proposingProfileStatus, String proposingProfilePicture, String proposedForProfileStatus, String proposedForProfilePicture) {
        String proposedBy = "proposed-by";
        String proposedFor = "proposed-for";
        Proposal givenProposal = givenProposal(proposedBy, proposedFor, REQUEST_PHOTO_PP.code());
        ProposalEntity givenProposalEntity = givenProposalEntity(proposedBy, proposedFor,REQUEST_PHOTO_PP.code());
        HttpHeaders httpHeaders = new HttpHeaders();

        HuduProfile proposedByProfile = givenProfile(proposedBy, "user-id1", MALE.code(), proposingProfileStatus, proposingProfilePicture );
        HuduProfile proposedForProfile = givenProfile(proposedFor, "user-id2", FEMALE.code(), proposedForProfileStatus, proposedForProfilePicture);

        given(profileClientService.getProfiles(anyList(),any(HttpHeaders.class) )).willReturn(List.of(proposedByProfile,proposedForProfile));
        given(proposalRepository.save(any(ProposalEntity.class))).willReturn(givenProposalEntity);

        Exception exception = assertThrows(InvalidProposalException.class, () -> proposalService.create(givenProposal, 5, "user-id1",false, httpHeaders));
        assertEquals(Constants.INVALID_PROPOSAL, exception.getMessage());
    }

    @Test
    void testCreateProposalValidation_proposingAndProposedProfilesCanToBeSameGender() {
        String proposedBy = "proposed-by";
        String proposedFor = "proposed-for";
        Proposal givenProposal = givenProposal(proposedBy, proposedFor, REQUEST_PHOTO_PP.code());
        ProposalEntity givenProposalEntity = givenProposalEntity(proposedBy, proposedFor,REQUEST_PHOTO_PP.code());
        HttpHeaders httpHeaders = new HttpHeaders();

        HuduProfile proposedByProfile = givenProfile(proposedBy, "user-id1", FEMALE.code(), PUBLISHED.getStatus(), "proposingProfilePicture" );
        HuduProfile proposedForProfile = givenProfile(proposedFor, "user-id2", FEMALE.code(), PUBLISHED.getStatus(), "proposedForProfilePicture");

        given(profileClientService.getProfiles(anyList(),any(HttpHeaders.class) )).willReturn(List.of(proposedByProfile,proposedForProfile));
        given(proposalRepository.save(any(ProposalEntity.class))).willReturn(givenProposalEntity);

        Exception exception = assertThrows(InvalidProposalException.class, () -> proposalService.create(givenProposal, 5,"user-id1",false, httpHeaders));
        assertEquals(Constants.INVALID_PROPOSAL, exception.getMessage());
    }

    @Test
    void testCreateProposalValidation_duplicateProposalNotAllowed() {
        String proposedBy = "proposed-by";
        String proposedFor = "proposed-for";
        Proposal givenProposal = givenProposal(proposedBy, proposedFor, REQUEST_PHOTO_PP.code());
        ProposalEntity givenProposalEntity = givenProposalEntity(proposedBy, proposedFor,REQUEST_PHOTO_PP.code());
        HttpHeaders httpHeaders = new HttpHeaders();

        HuduProfile proposedByProfile = givenProfile(proposedBy, "user-id1", MALE.code(), PUBLISHED.getStatus(), "proposingProfilePicture" );
        HuduProfile proposedForProfile = givenProfile(proposedFor, "user-id2", FEMALE.code(), PUBLISHED.getStatus(), "proposedForProfilePicture");

        given(profileClientService.getProfiles(anyList(),any(HttpHeaders.class))).willReturn(List.of(proposedByProfile,proposedForProfile));
        given(proposalRepository.save(any(ProposalEntity.class))).willReturn(givenProposalEntity);
        given(proposalRepository.findByProposedByProfile(anyString())).willReturn(List.of(givenProposalEntity));

        Exception exception = assertThrows(InvalidProposalException.class, () -> proposalService.create(givenProposal, 5,"user-id1",false, httpHeaders));
        assertEquals(Constants.DUPLICATE_PROPOSAL_ATTEMPT, exception.getMessage());
    }

    @Test
    public void testUpdateState_invalidState() {
        // given
        String proposedBy = "proposed-by";
        String proposedFor = "proposed-for";
        ProposalEntity currentProposal = givenProposalEntity(proposedBy, proposedFor, REQUEST_PHOTO_PP.code());
        Proposal proposal = givenProposal(proposedBy,proposedFor, "FAKE-STATUS");
        HuduProfile proposedByProfile = givenProfile(proposedBy, "proposed-by-user",
                "M", PUBLISHED.getStatus(),"picture1");
        HuduProfile proposedForProfile = givenProfile(proposedFor, "proposed-for-user",
                "F", PUBLISHED.getStatus(),"picture2");

        given(proposalRepository.findByProposedByProfileAndProposedForProfile(anyString(), anyString())).willReturn(List.of(currentProposal));
        given(profileClientService.getProfiles(anyList(), any(HttpHeaders.class))).willReturn(List.of(proposedByProfile, proposedForProfile));

        assertThrows(IllegalProposalStateException.class, () -> proposalService.updateState(proposal, "proposed-for-user",false, new HttpHeaders()));
    }

    @Test
    public void testUpdateState_invalidUser() {
        // given
        String proposedBy = "proposed-by";
        String proposedFor = "proposed-for";
        ProposalEntity currentProposal = givenProposalEntity(proposedBy, proposedFor, REQUEST_PHOTO_PP.code());
        Proposal proposal = givenProposal(proposedBy,proposedFor, ACCEPT_PHOTO_REQ_PF.code());
        HuduProfile proposedByProfile = givenProfile(proposedBy, "proposed-by-user",
                "M", PUBLISHED.getStatus(),"picture1");
        HuduProfile proposedForProfile = givenProfile(proposedFor, "proposed-for-user",
                "F", PUBLISHED.getStatus(),"picture2");

        given(proposalRepository.findByProposedByProfileAndProposedForProfile(anyString(), anyString())).willReturn(List.of(currentProposal));
        given(profileClientService.getProfiles(anyList(), any(HttpHeaders.class))).willReturn(List.of(proposedByProfile, proposedForProfile));

        assertThrows(UnAuthorizedDataAccessException.class, () -> proposalService.updateState(proposal, "proposed-by-user",false, new HttpHeaders()));
    }

    @Test
    public void testUpdateState() {
        // given
        String proposedBy = "proposed-by";
        String proposedFor = "proposed-for";
        ProposalEntity currentProposal = givenProposalEntity(proposedBy, proposedFor, REQUEST_PHOTO_PP.code());
        Proposal proposal = givenProposal(proposedBy,proposedFor, ACCEPT_PHOTO_REQ_PF.code());
        HuduProfile proposedByProfile = givenProfile(proposedBy, "proposed-by-user",
                "M", PUBLISHED.getStatus(),"picture1");
        HuduProfile proposedForProfile = givenProfile(proposedFor, "proposed-for-user",
                "F", PUBLISHED.getStatus(),"picture2");

        given(proposalRepository.findByProposedByProfileAndProposedForProfile(anyString(), anyString())).willReturn(List.of(currentProposal));
        given(profileClientService.getProfiles(anyList(), any(HttpHeaders.class))).willReturn(List.of(proposedByProfile, proposedForProfile));
        ArgumentCaptor<ProposalEntity> captor = ArgumentCaptor.forClass(ProposalEntity.class);

        given(proposalRepository.save(captor.capture())).willReturn(currentProposal);


        Proposal savedProposal = proposalService.updateState(proposal, "proposed-for-user",false, new HttpHeaders());
        assertEquals(ACCEPT_PHOTO_REQ_PF.code(), captor.getValue().getProposalState());
        assertEquals("proposed-for-user", captor.getValue().getModifiedBy());
    }

    private Proposal givenProposal(String proposalBy, String proposalFor, String status) {
        return Proposal.builder()
                .proposedByProfile(proposalBy)
                .proposedForProfile(proposalFor)
                .proposalState(status)
                .build();
    }

    private ProposalEntity givenProposalEntity(String proposalBy, String proposalFor, String status) {
        return new ProposalEntity()
                .proposedByProfile(proposalBy)
                .proposedForProfile(proposalFor)
                .proposalHistory(new HashSet<>())
                .proposalState(status);
    }

    private HuduProfile givenProfile(String profileId, String userId, String gender, String status, String picture) {
        return  HuduProfile.builder().profileId(profileId)
                .userId(userId)
                .gender(gender)
                .status(status)
                .profilePicture1(picture).build();
    }

}
