package org.huduu.profile.proposal.service;

import org.huduu.profile.model.HuduProfile;
import org.huduu.profile.proposal.model.MatchingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;


@Service
public class ProfileClientService {
    // TODO: implement
    @Autowired
    private RestTemplate restTemplate;

    @Value("${matchingServiceUrl}")
    private String matchingServiceUrl;

    public HuduProfile getProfile(String profileId, HttpStatus headers) {
        // TODO: implement
        return null;
    }

    public List<HuduProfile> getProfiles(List<String> profileIds, HttpHeaders httpHeaders) {
        HttpEntity entity = new HttpEntity<>(profileIds, httpHeaders);
        String url = matchingServiceUrl + "/get-profiles";
        ResponseEntity<MatchingResponse> responseEntity =  restTemplate.exchange(url, HttpMethod.POST, entity, MatchingResponse.class);
        List<HuduProfile> profiles = new ArrayList<>();
        if(responseEntity.getBody() != null && responseEntity.getBody().getMatchedProfiles() != null) {
            profiles = responseEntity.getBody().getMatchedProfiles();
        }
        return profiles;
    }
}
