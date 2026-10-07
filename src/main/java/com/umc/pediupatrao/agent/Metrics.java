/**
 * Metrics for agent invocation.
 */
package com.umc.pediupatrao.agent;

import java.time.Duration;

/**
 * Execution metrics for an agent.
 */
public class Metrics {

    private long executionTimeMs;
    private long tokensUsed;
    private int modelRank;  // Optional: model version/rank
    private double confidence;  // Optional: result confidence

    public Metrics() {
    }

    public Metrics(long executionTimeMs, long tokensUsed) {
        this.executionTimeMs = executionTimeMs;
        this.tokensUsed = tokensUsed;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public long getTokensUsed() {
        return tokensUsed;
    }

    public void setTokensUsed(long tokensUsed) {
        this.tokensUsed = tokensUsed;
    }

    public int getModelRank() {
        return modelRank;
    }

    public void setModelRank(int modelRank) {
        this.modelRank = modelRank;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}