package com.astavet.shop.controller.v1;

import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GuestIdentity {
    public UUID cartId(HttpSession session) {
        synchronized (session) {
            UUID id = (UUID) session.getAttribute("guestCartId");
            if (id == null) {
                id = UUID.randomUUID();
                session.setAttribute("guestCartId", id);
            }
            return id;
        }
    }
}
