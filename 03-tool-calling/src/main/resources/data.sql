-- Past bugs, seeded so findSimilarBugs returns real matches.
INSERT INTO bugs (component, summary, severity, resolution) VALUES
('analytics', 'Export button on analytics page does not trigger a download', 'HIGH', 'Fixed in v2.3.1 — missing content-disposition header on the export endpoint'),
('analytics', 'CSV export produces empty file for date ranges over 90 days', 'MEDIUM', 'Fixed in v2.4.0 — query timeout raised, pagination added'),
('auth', 'Login fails intermittently with 500 after password reset', 'HIGH', 'Fixed in v2.2.0 — race condition in session invalidation'),
('auth', 'SSO redirect loop for users in multiple org groups', 'CRITICAL', 'Fixed in v2.5.2 — group resolution made deterministic'),
('billing', 'Invoice PDF shows wrong currency symbol for EU customers', 'LOW', 'Fixed in v2.1.0 — locale-aware formatting'),
('search', 'Search returns stale results after bulk import', 'MEDIUM', 'Open — index refresh scheduled, workaround is manual reindex'),
('ui', 'Dark mode toggle resets on page refresh', 'LOW', 'Open — preference not persisted to user profile');

-- Current service status, seeded so getServiceStatus returns real states.
INSERT INTO service_status (component, status, detail) VALUES
('analytics', 'DEGRADED', 'Export pipeline running slow due to elevated load; downloads may take up to 60s'),
('auth', 'OPERATIONAL', 'All systems normal'),
('billing', 'OPERATIONAL', 'All systems normal'),
('search', 'OPERATIONAL', 'All systems normal'),
('ui', 'OPERATIONAL', 'All systems normal');
