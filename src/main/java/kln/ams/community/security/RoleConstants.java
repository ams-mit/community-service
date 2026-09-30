package kln.ams.community.security;

import java.util.Set;

public final class RoleConstants {

    private RoleConstants() {}

    public static final String SYSTEM_ADMINISTRATOR = "SYSTEM_ADMINISTRATOR";
    public static final String APARTMENT_MANAGER = "APARTMENT_MANAGER";
    public static final String SECURITY_OFFICER = "SECURITY_OFFICER";
    public static final String OWNER = "OWNER";
    public static final String TENANT_RESIDENT = "TENANT_RESIDENT";
    public static final String FINANCE_OFFICER = "FINANCE_OFFICER";
    public static final String MAINTENANCE_COORDINATOR = "MAINTENANCE_COORDINATOR";
    public static final String TECHNICIAN = "TECHNICIAN";
    public static final String SERVICE_STAFF = "SERVICE_STAFF";

    public static final Set<String> ALLOWED_INTERNAL_SERVICES = Set.of(
            "operations-service",
            "billing-payment-service",
            "utility-charge-service",
            "lease-occupancy-service",
            "resident-management-service",
            "property-unit-service"
    );
}
