package com.saxion.proj.tfms.routing.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration for aspect-oriented programming support.
 * Enables proxy-based AOP for cross-cutting concerns like logging.
 */
@Configuration
@EnableAspectJAutoProxy
public class AopConfiguration {
}
