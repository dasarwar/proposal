package org.huduu.profile.proposal.exception;

public class IllegalProposalStateException extends RuntimeException {
    public IllegalProposalStateException(String msg) {
        super(msg);
    }

    public IllegalProposalStateException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
