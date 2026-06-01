package com.triager.tools;

import com.triager.model.PastBug;
import com.triager.model.ServiceStatus;
import com.triager.service.BugRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The tools Gemini can call. Each @Tool method becomes something the model can
 * invoke on its own when it decides the question needs it.
 * <p>
 * The descriptions are not documentation for humans — they are the only thing
 * the model sees when deciding which tool to call. Vague descriptions lead to
 * the wrong tool being called, or no tool at all. Treat them as prompt
 * engineering, because that is what they are.
 */
@Component
public class BugTools {

    private final BugRepository repository;

    public BugTools(BugRepository repository) {
        this.repository = repository;
    }

    @Tool(description = """
        Search the history of past bug reports for issues similar to a given
        component name or keyword. Use this when the user asks whether a problem
        has been seen before, how a past issue was resolved, or for prior context
        on a component. Returns matching past bugs with their resolution status.
        """)
    public List<PastBug> findSimilarBugs(
        @ToolParam(description = "A component name (e.g. 'analytics', 'auth') or a keyword from the bug, e.g. 'export'")
        String term
    ) {
        System.out.println(">> Tool called: findSimilarBugs(term=" + term + ")");
        return repository.findSimilar(term);
    }

    @Tool(description = """
        Check the current live operational status of a service component. Use this
        when the user asks whether something is currently broken, degraded, or down
        right now. Returns the present status (OPERATIONAL, DEGRADED, or DOWN) and a
        detail message. This is about the present moment, not historical bugs.
        """)
    public ServiceStatus getServiceStatus(
        @ToolParam(description = "The component to check, e.g. 'analytics', 'auth', 'billing', 'search', 'ui'")
        String component
    ) {
        System.out.println(">> Tool called: getServiceStatus(component=" + component + ")");
        return repository.findStatus(component)
            .orElse(new ServiceStatus(component, "UNKNOWN", "No status record for this component"));
    }
}
