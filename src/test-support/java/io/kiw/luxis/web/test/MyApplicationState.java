package io.kiw.luxis.web.test;

public class MyApplicationState {
    private long longValue = 55;
    private volatile long pendingCorrelationId = -1;
    private volatile int pendingValue;

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(final int newLongValue) {
        this.longValue = newLongValue;
    }

    public long getPendingCorrelationId() {
        return pendingCorrelationId;
    }

    public void setPendingCorrelationId(final long pendingCorrelationId) {
        this.pendingCorrelationId = pendingCorrelationId;
    }

    public int getPendingValue() {
        return pendingValue;
    }

    public void setPendingValue(final int pendingValue) {
        this.pendingValue = pendingValue;
    }
}
