package com.saxion.proj.tfms.commons.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Common Swagger annotations for TFMS API endpoints
 */
public class SwaggerAnnotations {

    /**
     * Standard API operation with authentication
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful"),
            @ApiResponse(responseCode = "400", description = "Bad request", 
                    content = @Content(schema = @Schema(implementation = com.saxion.proj.tfms.commons.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", 
                    content = @Content(schema = @Schema(implementation = com.saxion.proj.tfms.commons.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", 
                    content = @Content(schema = @Schema(implementation = com.saxion.proj.tfms.commons.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", 
                    content = @Content(schema = @Schema(implementation = com.saxion.proj.tfms.commons.dto.ApiResponse.class)))
    })
    public @interface StandardApiOperation {
        @org.springframework.core.annotation.AliasFor(annotation = Operation.class, attribute = "summary")
        String summary() default "";
        
        @org.springframework.core.annotation.AliasFor(annotation = Operation.class, attribute = "description")
        String description() default "";
    }

    /**
     * Public API operation (no authentication required)
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful"),
            @ApiResponse(responseCode = "400", description = "Bad request", 
                    content = @Content(schema = @Schema(implementation = com.saxion.proj.tfms.commons.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", 
                    content = @Content(schema = @Schema(implementation = com.saxion.proj.tfms.commons.dto.ApiResponse.class)))
    })
    public @interface PublicApiOperation {
        @org.springframework.core.annotation.AliasFor(annotation = Operation.class, attribute = "summary")
        String summary() default "";
        
        @org.springframework.core.annotation.AliasFor(annotation = Operation.class, attribute = "description")
        String description() default "";
    }
}
