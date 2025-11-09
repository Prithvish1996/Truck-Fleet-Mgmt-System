package com.saxion.proj.tfms.commons.constants;

public enum StatusEnum {
    PENDING, // When a parcel is created and pending schedule
    SCHEDULED, // When a parcel is scheduled to be delivered
    DELIVERED, // When a parcel is delivered to the customer
    RETURNED, // When a parcel is returned
    PLANNED, // When a route is generated
    ASSIGNED, // When a route or truck is assigned to driver
    COMPLETED // When a drive complete his assignment
}
