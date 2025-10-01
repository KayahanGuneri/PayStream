package com.paystream.transferservice.domain;

/** Enforces allowed transitions for the transfer lifecycle. */
public final class TransferStateMachine {
    private TransferStateMachine() {}

    /** Throws IllegalStateException if transition is not allowed. */
    public static void enforce(TransferStatus from, TransferStatus to) {
        switch (from) {
            case PENDING -> {
                if (!(to == TransferStatus.IN_PROGRESS)) illegal(from, to);
            }
            case IN_PROGRESS -> {
                if (!(to == TransferStatus.COMPLETED || to == TransferStatus.FAILED)) illegal(from, to);
            }
            case COMPLETED, FAILED, REVERSED -> {
                // terminal states: no outbound transitions allowed in Week-5
                illegal(from, to);
            }
        }
    }

    private static void illegal(TransferStatus from, TransferStatus to) {
        throw new IllegalStateException("Illegal transfer state transition: " + from + " -> " + to);
    }
}
