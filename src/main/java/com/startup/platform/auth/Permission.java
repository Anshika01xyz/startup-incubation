package com.startup.platform.auth;

/**
 * Fine-grained permissions checked inside services.
 * Role -> Permission mapping lives in AuthorizationServiceImpl .
 * Module owners may request new permissions through a short interface-change note.
 */
public enum Permission {
    // ---- Startup module (Anshika) ----
    STARTUP_CREATE, STARTUP_EDIT_OWN, STARTUP_VIEW_ALL, STARTUP_MANAGE_TEAM, STARTUP_UPLOAD_DOCUMENT,

    // ---- Incubation module (Shaffy) ----
    INCUBATOR_MANAGE, PROGRAM_MANAGE,
    APPLICATION_SUBMIT, APPLICATION_VIEW_OWN, APPLICATION_VIEW_ALL, APPLICATION_REVIEW, APPLICATION_DECIDE,
    EVALUATION_SUBMIT, EVALUATION_VIEW,
    MENTOR_MANAGE, MENTOR_ASSIGN,
    MILESTONE_CREATE, MILESTONE_UPDATE, MILESTONE_VIEW,
    EVALUATION_SUMMARY_VIEW,   // read-only readiness score for investors (approved startups only)

    // ---- Investor module (Aastha) ----
    INVESTOR_PROFILE_MANAGE, INVESTOR_VIEW_MATCHES, INVESTOR_VIEW_APPROVED_STARTUPS,
    FUNDING_REQUEST_CREATE, FUNDING_REQUEST_VIEW, FUNDING_REQUEST_REVIEW, INVESTMENT_RECORD,

    // ---- Shared ----
    USER_MANAGE, ACTIVITY_LOG_VIEW, REPORT_EXPORT
}
