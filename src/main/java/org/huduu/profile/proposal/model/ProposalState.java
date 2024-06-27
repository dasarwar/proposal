package org.huduu.profile.proposal.model;

import java.util.ArrayList;
import java.util.List;

public enum ProposalState {
    REQUEST_PHOTO_PP("RPH_PP", "Request to view pictures"),
    ACCEPT_PHOTO_REQ_PF("ACCEPT_RPH_PF","Requested profile accept agrees to share pictures"),
    DECLINE_PHOTO_REQ_PF("DECLINE_RPH_PF","Requested profile declines to share pictures"),
    REQUEST_INITIAL_MEETING_PP("RIM_PP", "Request for initial meeting"),
    DECLINE_PHOTO_PP("DECLINE_RPH_PP","Requesting profile declines to proceed further"),
    ACCEPT_INITIAL_MEETING_PF("IM_AGREED", "Waiting to schedule initial meeting"),
    DECLINE_INITIAL_MEETING_PF("DECLINE_RIM_PF","Requested profile declines initial meeting"),
    INITIAL_MEETING_COMPLETE("IM_COMPLETE", "Initial meeting competed"),
    DECLINE_AFTER_IM_PP("DECLINE_AIM_PP", "Requesting profile declines to proceed after initial meeting"),
    DECLINE_AFTER_IM_PF("DECLINE_AIM_PF","Requested profile declines to continue after initial meeting"),
    REQUEST_FOLLOWUP_MEETING_PP("RFM_PP","Requesting profile requests follow up meeting"),
    REQUEST_FOLLOWUP_MEETING_PF("RFM_PF", "Requested profile requests follow up meeting"),
    FOLLOWUP_MEETING_COMPLETE("FM_COMPLETE", "Follow up meeting complete"),
    ACCEPT_FOLLOWUP_MEETING_PF("FM_AGREED","Waiting to schedule followup meeting"),
    DECLINE_FOLLOWUP_MEETING_PF("DECLINE_RFM_PF",""),
    ACCEPT_FOLLOWUP_MEETING_PP("FM_AGREED",""),
    DECLINE_FOLLOWUP_MEETING_PP("DECLINE_RFM_PP",""),
    DECLINE_AFTER_FOLLOWUP_PP("DECLINE_AFU_PP",""),
    DECLINE_AFTER_FOLLOWUP_PF("DECLINE_AFU_PP",""),
    FACE2FACE_MEETING_AGREED("F2F_AGREED",""),
    FACE2FACE_MEETING_COMPLETE("F2F_COMPLETE",""),
    PROPOSE_MARRIAGE_PP("MRP_PP",""),
    PROPOSE_MARRIAGE_PF("MRP_PF",""),
    ACCEPT_MARRIAGE_PROPOSAL("ACCEPT_MRP",""),
    DECLINE_TO_CONTINUE_PP("DECLINE_CONTINUE_PP", "Ending the proposal"),
    DECLINE_TO_CONTINUE_PF("DECLINE_CONTINUE_PF", "Ending the proposal"),
    MARRIED("MARRIED", "Married"),
    DECLINE_MARRIAGE_PROPOSAL("DECLINE_MRP","");

    private final String code;
    private final String description;
    ProposalState(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String code() {
        return this.code;
    }

    public String description() {
        return this.description;
    }

    public static ProposalState fromCode(String code) {
        for (ProposalState ps : ProposalState.values()) {
            if (ps.code.equalsIgnoreCase(code)) {
                return ps;
            }
        }
        return null;
    }

    public List<ProposalState> nextStates() {
        List<ProposalState> nextStates = new ArrayList<>();
        switch (this) {
            case REQUEST_PHOTO_PP:
                nextStates.add(ACCEPT_PHOTO_REQ_PF);
                nextStates.add(DECLINE_PHOTO_REQ_PF);
                break;
            case ACCEPT_PHOTO_REQ_PF:
                nextStates.add(REQUEST_INITIAL_MEETING_PP);
                nextStates.add(DECLINE_PHOTO_PP);
                break;
            case REQUEST_INITIAL_MEETING_PP:
                nextStates.add(ACCEPT_INITIAL_MEETING_PF);
                nextStates.add(DECLINE_INITIAL_MEETING_PF);
                break;
            case ACCEPT_INITIAL_MEETING_PF:
                nextStates.add(INITIAL_MEETING_COMPLETE);
                nextStates.add(DECLINE_AFTER_IM_PP);
                nextStates.add(DECLINE_AFTER_IM_PF);
                nextStates.add(REQUEST_FOLLOWUP_MEETING_PP);
                nextStates.add(REQUEST_FOLLOWUP_MEETING_PF);
                break;
            case INITIAL_MEETING_COMPLETE:
                nextStates.add(DECLINE_AFTER_IM_PP);
                nextStates.add(DECLINE_AFTER_IM_PF);
                nextStates.add(REQUEST_FOLLOWUP_MEETING_PP);
                nextStates.add(REQUEST_FOLLOWUP_MEETING_PF);
                break;
            case DECLINE_AFTER_IM_PP:
            case DECLINE_AFTER_IM_PF:
            case DECLINE_PHOTO_REQ_PF:
            case DECLINE_FOLLOWUP_MEETING_PF:
            case DECLINE_INITIAL_MEETING_PF:
            case DECLINE_PHOTO_PP:
            case DECLINE_FOLLOWUP_MEETING_PP:
            case DECLINE_AFTER_FOLLOWUP_PP:
            case DECLINE_AFTER_FOLLOWUP_PF:
            case DECLINE_TO_CONTINUE_PP:
            case DECLINE_TO_CONTINUE_PF:
            case DECLINE_MARRIAGE_PROPOSAL:
            case MARRIED:
                break;
            case REQUEST_FOLLOWUP_MEETING_PP:
                nextStates.add(ACCEPT_FOLLOWUP_MEETING_PF);
                nextStates.add(DECLINE_FOLLOWUP_MEETING_PF);
                break;
            case REQUEST_FOLLOWUP_MEETING_PF:
                nextStates.add(ACCEPT_FOLLOWUP_MEETING_PP);
                nextStates.add(DECLINE_FOLLOWUP_MEETING_PP);
                break;
            case FOLLOWUP_MEETING_COMPLETE:
                nextStates.add(REQUEST_FOLLOWUP_MEETING_PP);
                nextStates.add(REQUEST_FOLLOWUP_MEETING_PF);
                nextStates.add(FACE2FACE_MEETING_AGREED);
                nextStates.add(DECLINE_AFTER_FOLLOWUP_PP);
                nextStates.add(DECLINE_AFTER_FOLLOWUP_PF);
                break;
            case ACCEPT_FOLLOWUP_MEETING_PF:
            case ACCEPT_FOLLOWUP_MEETING_PP:
                nextStates.add(FOLLOWUP_MEETING_COMPLETE);
                nextStates.add(FACE2FACE_MEETING_AGREED);
                nextStates.add(DECLINE_TO_CONTINUE_PF);
                nextStates.add(DECLINE_TO_CONTINUE_PP);
                break;
            case FACE2FACE_MEETING_AGREED:
                nextStates.add(FACE2FACE_MEETING_COMPLETE);
                nextStates.add(PROPOSE_MARRIAGE_PP);
                nextStates.add(PROPOSE_MARRIAGE_PF);
                nextStates.add(DECLINE_TO_CONTINUE_PF);
                nextStates.add(DECLINE_TO_CONTINUE_PP);
                break;
            case FACE2FACE_MEETING_COMPLETE:
                nextStates.add(PROPOSE_MARRIAGE_PP);
                nextStates.add(PROPOSE_MARRIAGE_PF);
                nextStates.add(DECLINE_TO_CONTINUE_PF);
                nextStates.add(DECLINE_TO_CONTINUE_PP);
                break;
            case PROPOSE_MARRIAGE_PP:
            case PROPOSE_MARRIAGE_PF:
                nextStates.add(ACCEPT_MARRIAGE_PROPOSAL);
                nextStates.add(DECLINE_MARRIAGE_PROPOSAL);
                nextStates.add(DECLINE_TO_CONTINUE_PF);
                nextStates.add(DECLINE_TO_CONTINUE_PP);
                break;
            case ACCEPT_MARRIAGE_PROPOSAL:
                nextStates.add(MARRIED);
                nextStates.add(DECLINE_TO_CONTINUE_PP);
                nextStates.add(DECLINE_TO_CONTINUE_PF);
                break;
            default:
                nextStates.add(DECLINE_TO_CONTINUE_PF);
                nextStates.add(DECLINE_TO_CONTINUE_PP);
                break;

        }
        return nextStates;
    }

}
