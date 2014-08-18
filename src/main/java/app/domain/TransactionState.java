package app.domain;

public enum TransactionState {
    INITIAL,
    PENDING,
    APPLIED,
    DONE,
    CANCELING,
    CANCELED
};