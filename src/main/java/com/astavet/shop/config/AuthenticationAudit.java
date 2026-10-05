package com.astavet.shop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationAudit {
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationAudit.class);
    @EventListener
    public void success(AuthenticationSuccessEvent event) {
        LOG.atInfo().addKeyValue("actor", event.getAuthentication().getName()).log("Administrator signed in");
    }
    @EventListener
    public void failure(AuthenticationFailureBadCredentialsEvent event) {
        LOG.atWarn().log("Administrator sign-in rejected");
    }
}
