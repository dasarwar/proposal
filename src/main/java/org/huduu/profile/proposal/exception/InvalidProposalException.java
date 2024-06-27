package org.huduu.profile.proposal.exception;

public class InvalidProposalException extends RuntimeException {
    public InvalidProposalException(String msg) {
        super(msg);
    }

    public InvalidProposalException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
