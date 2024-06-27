package org.huduu.profile.proposal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.huduu.profile.common.Constants;
import org.huduu.profile.exception.InvalidProfileException;
import org.huduu.profile.model.Proposal;
import org.huduu.profile.proposal.model.ProposalRequest;
import org.huduu.profile.proposal.service.api.ProposalService;
import org.huduu.profile.security.KeycloakRoleConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.huduu.profile.common.Constants.PROFILES_NOT_VALID;
import static org.huduu.profile.proposal.model.ProposalState.REQUEST_PHOTO_PP;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProposalControllerTest {
    @Autowired
    private ProposalController proposalController;

    @MockBean
    private ProposalService proposalService;

    @MockBean
    private KeycloakRoleConverter roleFinder;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCreateProposal() throws Exception{
        given(proposalService.create(any(Proposal.class), anyInt(),anyString(),anyBoolean(), any(HttpHeaders.class)))
                .willReturn(givenProposal("profile-id1", "profile-id2", REQUEST_PHOTO_PP.code()));
        given(roleFinder.getUserName(any(Jwt.class))).willReturn("user-id");
        given(roleFinder.isAdmin(any(Jwt.class))).willReturn(false);

        mockMvc.perform(post("/api/proposals")
                        .with(jwt().jwt(builder -> builder
                                .claim("email", "test@test.com")
                                .claim("custom_claim", "value42")
                                .claim("realm_access", Map.of("roles", List.of("ROLE_candidate")))
                                .claim("preferred_username", "user-id")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                       // .header("profileId", "profile-id")
                        .header("operator-id", "1")
                        .header("interaction-id", "111")
                        .content(asJsonString(givenProposalRequest("profile-id", "profile-id-2"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responseStatus.code").value("201"))
                .andExpect(jsonPath("$.proposal.proposedByProfile").value("profile-id1"))
                .andExpect(jsonPath("$.proposal.proposedForProfile").value("profile-id2"))
                .andExpect(jsonPath("$.proposal.proposalState").value(REQUEST_PHOTO_PP.code()))
                .andReturn();
    }

    @Test
    void testCreateProposalReturnsBadRequestForMissingHeader() throws Exception{

        mockMvc.perform(post("/api/proposals")
                        .with(jwt().jwt(builder -> builder
                                .claim("email", "test@test.com")
                                .claim("custom_claim", "value42")
                                .claim("realm_access", Map.of("roles", List.of("ROLE_candidate")))
                                .claim("preferred_username", "user-id")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("operator-id", "1")
                        .content(asJsonString(givenProposalRequest("profile-id", "profile-id-2"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseStatus.code").value("400"))
                .andExpect(jsonPath("$.responseStatus.message").value(Constants.REQUEST_NOT_VALID))
                .andReturn();
    }

    @Test
    void testCreateProposalBadRequestForInvalidProfiles() throws Exception {
        given(roleFinder.getUserName(any(Jwt.class))).willReturn("user-id");
        given(roleFinder.isAdmin(any(Jwt.class))).willReturn(false);
        given(proposalService.create(any(Proposal.class), anyInt(),anyString(),anyBoolean(), any(HttpHeaders.class))).willThrow(new InvalidProfileException(PROFILES_NOT_VALID));

        mockMvc.perform(post("/api/proposals")
                        .with(jwt().jwt(builder -> builder
                                .claim("email", "test@test.com")
                                .claim("custom_claim", "value42")
                                .claim("realm_access", Map.of("roles", List.of("ROLE_candidate")))
                                .claim("preferred_username", "user-id")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        //.header("profileId", "profile-id")
                        .header("operator-id", "1")
                        .header("interaction-id", "111")
                        .content(asJsonString(givenProposalRequest("profile-id", "profile-id-2"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseStatus.code").value("400"))
                .andExpect(jsonPath("$.responseStatus.message").value(Constants.PROFILES_NOT_VALID))
                .andReturn();
    }

    private ProposalRequest givenProposalRequest(String proposalBy, String proposalFor) {
        return ProposalRequest.builder()
                .proposalByProfile(proposalBy)
                .proposalForProfile(proposalFor)
                .build();
    }

    private Proposal givenProposal(String proposalBy, String proposalFor, String status) {
        return Proposal.builder()
                .proposedByProfile(proposalBy)
                .proposedForProfile(proposalFor)
                .proposalState(status)
                .build();
    }

    private String asJsonString(Object profile) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try {
            return objectMapper.writeValueAsString(profile);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
