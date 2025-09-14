# Common GitLab CI Variables and Configuration
# This file documents all available variables that can be used in service-specific CI files

## Global Variables (Inherited from parent .gitlab-ci.yml)

### Maven Configuration
# MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository"
# MAVEN_CLI_OPTS: "--batch-mode --errors --fail-at-end --show-version"

### Docker Configuration  
# DOCKER_DRIVER: "overlay2"
# DOCKER_IMAGE: "docker:20.10.16"
# DOCKER_HOST: "tcp://docker:2375"  # Inherited globally - DO NOT override in child jobs
# DOCKER_TLS_CERTDIR: ""

### Environment Configuration
# SPRING_PROFILES_ACTIVE: "local"

### Branch Configuration
# TARGET_BRANCH_1: "TFMS_dev_v1.0.0"
# TARGET_BRANCH_2: "TFMS_v1.0.0_dev"

### Common Paths
# DEPLOYMENT_PATH: "deployment/docker"
# DOCKER_COMPOSE_FILE: "docker-compose.yml"

### Project Structure Paths
# PLATFORM_SERVICES_PATH: "platform"
# MICROSERVICES_PATH: "microservices"
# SHARED_MODULES_PATH: "shared"

### Service-Specific Variables (Analytics Example)
# ANALYTICS_SERVICE_PATH: "platform/monitoring/analytics-service"
# ANALYTICS_SERVICE_NAME: "analytics-service"
# ANALYTICS_SERVICE_PORT: "8090"

## Templates for New Services

### For Platform Services:
# SERVICE_PATH: "platform/{category}/{service-name}"
# SERVICE_NAME: "{service-name}"
# SERVICE_PORT: "{port-number}"

### For Core Microservices:
# SERVICE_PATH: "microservices/core-services/{service-name}"
# SERVICE_NAME: "{service-name}"
# SERVICE_PORT: "{port-number}"

### For Business Services:
# SERVICE_PATH: "microservices/business-services/{service-name}"
# SERVICE_NAME: "{service-name}"
# SERVICE_PORT: "{port-number}"

### For Communication Services:
# SERVICE_PATH: "microservices/communication-services/{service-name}"
# SERVICE_NAME: "{service-name}"
# SERVICE_PORT: "{port-number}"

### For Infrastructure Services:
# SERVICE_PATH: "microservices/infrastructure-services/{service-name}"
# SERVICE_NAME: "{service-name}"
# SERVICE_PORT: "{port-number}"

## Common Rules Templates

### Standard Change Rules (YAML Anchor):
# rules: &service_change_rules
#   - if: '$CI_COMMIT_BRANCH == $TARGET_BRANCH_1'
#     changes:
#       - "${SERVICE_PATH}/**/*"
#   - if: '$CI_COMMIT_BRANCH == $TARGET_BRANCH_2'
#     changes:
#       - "${SERVICE_PATH}/**/*"
#   - if: '$CI_PIPELINE_SOURCE == "merge_request_event"'
#     changes:
#       - "${SERVICE_PATH}/**/*"

### Manual Deployment Rules:
# rules:
#   - if: '$CI_COMMIT_BRANCH == $TARGET_BRANCH_1'
#     changes:
#       - "${SERVICE_PATH}/**/*"
#     when: manual
#   - if: '$CI_COMMIT_BRANCH == $TARGET_BRANCH_2'
#     changes:
#       - "${SERVICE_PATH}/**/*"
#     when: manual

## Benefits of This Approach

1. **Centralized Configuration**: All common paths and settings in one place
2. **Easy Maintenance**: Change a path once, affects all services
3. **Consistency**: All services use the same patterns
4. **Reusability**: YAML anchors reduce repetition
5. **Scalability**: Easy to add new services with same pattern
6. **Documentation**: Clear understanding of available variables

## Common Issues and Solutions

### DOCKER_HOST Variable Circular Reference
**Problem**: Setting `DOCKER_HOST: $DOCKER_HOST` in job variables creates a circular reference
**Error**: `dial tcp: lookup $DOCKER_HOST: no such host`
**Solution**: Remove `DOCKER_HOST` from job-level variables - it's inherited from the global variables
**Fixed in**: All service CI templates as of latest update
