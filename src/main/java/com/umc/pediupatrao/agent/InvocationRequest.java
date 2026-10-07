/**
 * Invocation request DTO for agent invocation.
 */
package com.umc.pediupatrao.agent;

import java.util.List;
import java.util.Map;

/**
 * Request object for invoking agents.
 */
public class InvocationRequest {
    
    private List<String> agents;
    private String prompt;
    private Map<String, Object> parameters;
    private int timeoutMs;

    public List<String> getAgents() {
        return agents;
    }

    public void setAgents(List<String> agents) {
        this.agents = agents;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
}