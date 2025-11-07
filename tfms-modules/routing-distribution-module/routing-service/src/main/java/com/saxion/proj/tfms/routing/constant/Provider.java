package com.saxion.proj.tfms.routing.constant;

import lombok.Getter;

@Getter
public enum Provider {
    TOMTOM("TomTom API"),
    GOOGLE_MAPS("Google Maps API"),
    DIJKSTRA("Internal Dijkstra Algorithm"),
    A_STAR("Internal A* Algorithm");

    private final String description;

    Provider(String description) {
        this.description = description;
    }
}
