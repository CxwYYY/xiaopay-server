package com.xiao.xiaopay.domain.agent.model;

/**
 * One-time agent binding code status.
 */
public final class AgentBindCodeStatus {
    public static final String PENDING = "PENDING";
    public static final String CLAIMED = "CLAIMED";
    public static final String EXPIRED = "EXPIRED";
    public static final String CANCELED = "CANCELED";

    private AgentBindCodeStatus() {
    }
}
