package com.saxion.proj.tfms.service;

import com.saxion.proj.tfms.config.ProductionConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 * Service to handle application startup logic and provide deployment mode information.
 */
@Service
public class BuildService {

    @Autowired
    private ProductionConfiguration productionConfiguration;
    
    @Autowired
    private Environment environment;
    
    @Value("${server.port:8080}")
    private int serverPort;
    
    @Value("${app.mode:Unknown}")
    private String appMode;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String[] activeProfiles = environment.getActiveProfiles();
        String profile = activeProfiles.length > 0 ? activeProfiles[0] : "default";
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🚀 TFMS - Truck Fleet Management System");
        System.out.println("=".repeat(60));
        System.out.println("📊 Active Profile: " + profile.toUpperCase());
        System.out.println("⚙️  Production Mode: " + (productionConfiguration.isEnable() ? "ENABLED" : "DISABLED"));
        System.out.println("🌐 Server Port: " + serverPort);
        
        if (productionConfiguration.isEnable()) {
            // Production mode - check if React build files exist
            ClassPathResource staticIndex = new ClassPathResource("static/index.html");
            
            if (staticIndex.exists()) {
                System.out.println("✅ Production Mode - Serving React app from /static");
                System.out.println("📱 Application URL: http://localhost:" + serverPort);
                System.out.println("🔗 API Endpoints: http://localhost:" + serverPort + "/api");
            } else {
                System.out.println("⚠️  Production mode enabled but React build not found!");
                System.out.println("💡 Run: ./mvnw clean package -Pprod to build frontend");
            }
        } else {
            // Development mode
            System.out.println("🔧 Development Mode - Frontend runs separately");
            System.out.println("🔗 Backend API: http://localhost:" + serverPort + "/api");
            System.out.println("📱 Frontend: http://localhost:3000 (run separately)");
            System.out.println("🗄️  H2 Console: http://localhost:" + serverPort + "/h2-console");
        }
        
        System.out.println("=".repeat(60));
        System.out.println("✅ Application started successfully!");
        System.out.println("=".repeat(60) + "\n");
    }
}
