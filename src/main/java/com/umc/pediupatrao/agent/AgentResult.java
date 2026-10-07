/**
 * Result DTO for agent invocation.
 */
package com.umc.pediupatrao.agent;

import java.time.Instant;

/**
 * Result returned by an agent invocation.
 */
public class AgentResult {

    private String agent;
    private String status;
    private String output;
    private Metrics metrics;
    private Instant timestamp;

    public AgentResult() {
    }

    public AgentResult(String agent, String status, String output, Metrics metrics, Instant timestamp) {
        this.agent = agent;
        this.status = status;
        this.output = output;
        this.metrics = metrics;
        this.timestamp = timestamp;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    public long getExecutionTimeMs() {
        if (metrics != null) {
            return metrics.getExecutionTimeMs();
        }
        return 0;
    }
    
    public String getErrorMessage() {
        if ("failed".equals(status) && output != null) {
            return output;
        }
        return null;
    }
}