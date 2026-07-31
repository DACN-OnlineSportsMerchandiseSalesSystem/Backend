package com.javaweb.security;

import java.util.Set;

public final class AppRoles {
    public static final String CUSTOMER = "CUSTOMER";
    public static final String ADMIN = "ADMIN";
    public static final String IT_ADMIN = "IT_ADMIN";

    private static final Set<String> ALLOWED_ROLES = Set.of(CUSTOMER, ADMIN, IT_ADMIN);

    private AppRoles() {
    }

    public static boolean isAllowed(String roleName) {
        return roleName != null && ALLOWED_ROLES.contains(roleName.trim().toUpperCase(java.util.Locale.ROOT));
    }
}
