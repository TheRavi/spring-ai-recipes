package com.triager.service;

import com.triager.model.PastBug;
import com.triager.model.ServiceStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BugRepository {

    private final JdbcClient jdbcClient;

    public BugRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /**
     * Finds past bugs whose component or summary loosely matches the query term.
     * Uses a LIKE match on both columns — crude, but enough to demonstrate a real
     * database lookup driven by an LLM tool call.
     */
    public List<PastBug> findSimilar(String term) {
        String like = "%" + term.toLowerCase() + "%";
        return jdbcClient.sql("""
                SELECT id, component, summary, severity, resolution
                FROM bugs
                WHERE LOWER(component) LIKE :like
                   OR LOWER(summary) LIKE :like
                ORDER BY id
                """)
            .param("like", like)
            .query(PastBug.class)
            .list();
    }

    public Optional<ServiceStatus> findStatus(String component) {
        return jdbcClient.sql("""
                SELECT component, status, detail
                FROM service_status
                WHERE LOWER(component) = LOWER(:component)
                """)
            .param("component", component)
            .query(ServiceStatus.class)
            .optional();
    }
}
