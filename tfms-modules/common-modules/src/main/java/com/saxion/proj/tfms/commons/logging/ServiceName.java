package com.saxion.proj.tfms.commons.logging;

import lombok.Getter;

/**
 * Enum representing different service names for logging purposes.
 * Each enum constant corresponds to a specific service/module in the system.
 */
@Getter
public enum ServiceName {
    USER_SERVICE("user-service"),
    ORDER_SERVICE("order-service"),
    SECURITY_SERVICE("security-service"),
    NOTIFICATION_SERVICE("notification-service"),
    DRIVER_SERVICE("driver-service"),
    COMMON_SERVICE("common-service"),
    PACKAGE_SERVICE("package-service"),
    PARCEL_SERVICE("parcel-service");

    private final String serviceName;

    ServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return serviceName;
    }
}
