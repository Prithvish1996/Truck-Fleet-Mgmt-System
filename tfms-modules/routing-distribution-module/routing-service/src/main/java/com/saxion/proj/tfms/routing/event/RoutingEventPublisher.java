package com.saxion.proj.tfms.routing.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publisher for routing domain events.
 * Decouples routing service from downstream consumers.
 */
@Component
public class RoutingEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public RoutingEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publish routing optimization completed event.
     */
    public void publishOptimizationCompleted(RoutingOptimizationCompletedEvent event) {
        eventPublisher.publishEvent(event);
    }
}
