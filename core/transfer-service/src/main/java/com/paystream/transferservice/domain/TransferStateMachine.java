package com.paystream.transferservice.domain;

/** Guards legal transitions; keeps business rules in one place (OCP-ready). */
public final class TransferStateMachine {

    /** Returns true if transition is allowed by the business. */
    public static boolean canTransition(TransferStatus from, TransferStatus to) {
        // PENDING → IN_PROGRESS
        if (from == TransferStatus.PENDING && to == TransferStatus.IN_PROGRESS) return true;
        // IN_PROGRESS → COMPLETED|FAILED|REVERSED
        if (from == TransferStatus.IN_PROGRESS &&
                (to == TransferStatus.COMPLETED || to == TransferStatus.FAILED || to == TransferStatus.REVERSED)) return true;
        // All other transitions are illegal
        return false;
    }

    /** Throws an IllegalStateException when transition is not allowed. */
    public static void enforce(TransferStatus from, TransferStatus to) {
        if (!canTransition(from, to)) {
            throw new IllegalStateException("Illegal transition: " + from + " → " + to);
        }
    }

    private TransferStateMachine() { /* utility class */ }
}
