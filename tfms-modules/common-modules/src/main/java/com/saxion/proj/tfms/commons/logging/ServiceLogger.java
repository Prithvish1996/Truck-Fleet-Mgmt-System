package com.saxion.proj.tfms.commons.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

public class ServiceLogger {
    private final Logger logger;

    private ServiceLogger(Class<?> clazz) {
        this.logger = LogManager.getLogger(clazz);
    }

    public static ServiceLogger getLogger(Class<?> clazz) {
        return new ServiceLogger(clazz);
    }

    private void setContext(ServiceName serviceName, String operation) {
        // Only update service if provided (otherwise use global context)
        if (serviceName != null) {
            ThreadContext.put("service", serviceName.getServiceName());
        }
        // Set operation context if provided
        if (operation != null) {
            ThreadContext.put("operation", operation);
        }
    }

    private void clearContext() {
        // Only clear operation context, keep service context
        ThreadContext.remove("operation");
    }

    public void debug(ServiceName service, String message, Object... params) {
        setContext(service, null);
        logger.debug(message, params);
        clearContext();
    }

    public void info(ServiceName service, String message, Object... params) {
        setContext(service, null);
        logger.info(message, params);
        clearContext();
    }

    public void warn(ServiceName service, String message, Object... params) {
        setContext(service, null);
        logger.warn(message, params);
        clearContext();
    }

    public void error(ServiceName service, String message, Object... params) {
        setContext(service, null);
        logger.error(message, params);
        clearContext();
    }

    public void error(ServiceName service, String message, Throwable throwable) {
        setContext(service, null);
        logger.error(message, throwable);
        clearContext();
    }

  
    public void debugOp(ServiceName service, String operation, String message, Object... params) {
        setContext(service, operation);
        logger.debug(message, params);
        clearContext();
    }

    public void infoOp(ServiceName service, String operation, String message, Object... params) {
        setContext(service, operation);
        logger.info(message, params);
        clearContext();
    }

    public void warnOp(ServiceName service, String operation, String message, Object... params) {
        setContext(service, operation);
        logger.warn(message, params);
        clearContext();
    }

    public void errorOp(ServiceName service, String operation, String message, Object... params) {
        setContext(service, operation);
        logger.error(message, params);
        clearContext();
    }

    public void errorOp(ServiceName service, String operation, String message, Throwable throwable) {
        setContext(service, operation);
        logger.error(message, throwable);
        clearContext();
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    //Check if INFO level is enabled.
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    //Check if INFO level is enabled.
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    //Check if INFO level is enabled.
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

}
