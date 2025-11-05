package com.saxion.proj.tfms.commons.constants;

public enum StatusEnum {
    PENDING, // When a parcel is created and pending schedule
    SCHEDULED, // When a parcel is scheduled to be delivered
    DELIVERED, // When a parcel is delivered to the customer
    RETURNED, // When a parcel is returned
    ASSIGNED, // When a truck is assigned to driver
    COMPLETED // When a drive complete his assignment
}
