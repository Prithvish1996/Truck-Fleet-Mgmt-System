package com.saxion.proj.tfms.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter printWriter;

    @Mock
    private ScheduledExecutorService scheduledExecutorService;

    private RateLimitingFilter rateLimitingFilter;

    private static final String CLIENT_IP = "192.168.1.100";
    private static final String FORWARDED_IP = "10.0.0.1";

    @BeforeEach
    void setUp() {
    }

    @Test
    void testConstructor_ShouldInitializeScheduledExecutor() {
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newScheduledThreadPool(1))
                    .thenReturn(scheduledExecutorService);

            rateLimitingFilter = new RateLimitingFilter();

            executorsMock.verify(() -> Executors.newScheduledThreadPool(1));
            verify(scheduledExecutorService).scheduleAtFixedRate(
                    any(Runnable.class), eq(1L), eq(1L), eq(TimeUnit.MINUTES)
            );
        }
    }

    @Test
    void testDoFilterInternal_WithinRateLimit_ShouldContinueChain() throws ServletException, IOException {
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newScheduledThreadPool(1))
                    .thenReturn(scheduledExecutorService);
            rateLimitingFilter = new RateLimitingFilter();

            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn(CLIENT_IP);

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(429);
        }
    }

    @Test
    void testDoFilterInternal_ExceedsRateLimit_ShouldReturn429() throws ServletException, IOException {
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newScheduledThreadPool(1))
                    .thenReturn(scheduledExecutorService);
            rateLimitingFilter = new RateLimitingFilter();

            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn(CLIENT_IP);
            when(response.getWriter()).thenReturn(printWriter);

            for (int i = 0; i <= 60; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            verify(response).setStatus(429);
            verify(response).setContentType("application/json");
            verify(printWriter).write("{\"error\":\"Too many requests\",\"message\":\"Please try again later\"}");
            verify(filterChain, times(60)).doFilter(request, response);
        }
    }

    @Test
    void testDoFilterInternal_ExactlyAtRateLimit_ShouldContinueChain() throws ServletException, IOException {
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newScheduledThreadPool(1))
                    .thenReturn(scheduledExecutorService);
            rateLimitingFilter = new RateLimitingFilter();

            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn(CLIENT_IP);

            for (int i = 0; i < 60; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            verify(filterChain, times(60)).doFilter(request, response);
            verify(response, never()).setStatus(429);
        }
    }

    @Test
    void testDoFilterInternal_OneOverRateLimit_ShouldReturn429() throws ServletException, IOException {
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newScheduledThreadPool(1))
                    .thenReturn(scheduledExecutorService);
            rateLimitingFilter = new RateLimitingFilter();

            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn(CLIENT_IP);
            when(response.getWriter()).thenReturn(printWriter);

            for (int i = 0; i < 60; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            verify(response).setStatus(429);
            verify(response).setContentType("application/json");
            verify(printWriter).write("{\"error\":\"Too many requests\",\"message\":\"Please try again later\"}");
            verify(filterChain, times(60)).doFilter(request, response);
        }
    }

    @Test
    void testGetClientIP_WithXForwardedForHeader_ShouldReturnForwardedIP() throws ServletException, IOException {
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newScheduledThreadPool(1))
                    .thenReturn(scheduledExecutorService);
            rateLimitingFilter = new RateLimitingFilter();

            when(request.getHeader("X-Forwarded-For")).thenReturn(FORWARDED_IP);

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(request).getHeader("X-Forwarded-For");
            verify(request, never()).getRemoteAddr();
        }
    }

    @Test
    void testGetClientIP_WithXForwardedForNull_ShouldReturnRemoteAddr() throws ServletException, IOException {
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newScheduledThreadPool(1))
                    .thenReturn(scheduledExecutorService);
            rateLimitingFilter = new RateLimitingFilter();

            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn(CLIENT_IP);

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(request).getHeader("X-Forwarded-For");
            verify(request).getRemoteAddr();
        }
    }

    @Test
    void testGetClientIP_WithMultipleForwardedIPs_ShouldReturnFirstIP() throws ServletException, IOException {
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newScheduledThreadPool(1))
                    .thenReturn(scheduledExecutorService);
            rateLimitingFilter = new RateLimitingFilter();

            String multipleIPs = "10.0.0.1, 192.168.1.1, 172.16.0.1";
            when(request.getHeader("X-Forwarded-For")).thenReturn(multipleIPs);

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(request).getHeader("X-Forwarded-For");
            verify(request, never()).getRemoteAddr();
        }
    }

    @Test
    void testGetClientIP_WithForwardedIPWithSpaces_ShouldReturnTrimmedIP() throws ServletException, IOException {
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newScheduledThreadPool(1))
                    .thenReturn(scheduledExecutorService);
            rateLimitingFilter = new RateLimitingFilter();

            String ipWithSpaces = "  10.0.0.1  , 192.168.1.1";
            when(request.getHeader("X-Forwarded-For")).thenReturn(ipWithSpaces);

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    void testDoFilterInternal_DifferentIPs_ShouldTrackSeparately() throws ServletException, IOException {
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newScheduledThreadPool(1))
                    .thenReturn(scheduledExecutorService);
            rateLimitingFilter = new RateLimitingFilter();

            HttpServletRequest request1 = mock(HttpServletRequest.class);
            HttpServletRequest request2 = mock(HttpServletRequest.class);

            when(request1.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request1.getRemoteAddr()).thenReturn("192.168.1.1");
            when(request2.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request2.getRemoteAddr()).thenReturn("192.168.1.2");

            for (int i = 0; i < 60; i++) {
                rateLimitingFilter.doFilterInternal(request1, response, filterChain);
                rateLimitingFilter.doFilterInternal(request2, response, filterChain);
            }

            verify(filterChain, times(120)).doFilter(any(), eq(response));
            verify(response, never()).setStatus(429);
        }
    }

    @Test
    void testDoFilterInternal_SameIPExceedsLimit_ShouldBlock() throws ServletException, IOException {
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newScheduledThreadPool(1))
                    .thenReturn(scheduledExecutorService);
            rateLimitingFilter = new RateLimitingFilter();

            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn(CLIENT_IP);
            when(response.getWriter()).thenReturn(printWriter);

            for (int i = 0; i < 60; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            for (int i = 0; i < 5; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            verify(response, times(5)).setStatus(429);
            verify(response, times(5)).setContentType("application/json");
            verify(printWriter, times(5)).write("{\"error\":\"Too many requests\",\"message\":\"Please try again later\"}");
        }
    }

    @Test
    void testDoFilterInternal_ConcurrentRequests_ShouldHandleCorrectly() throws ServletException, IOException {
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newScheduledThreadPool(1))
                    .thenReturn(scheduledExecutorService);
            rateLimitingFilter = new RateLimitingFilter();

            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn(CLIENT_IP);
            when(response.getWriter()).thenReturn(printWriter);

            Thread[] threads = new Thread[10];
            for (int i = 0; i < 10; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 10; j++) {
                        try {
                            rateLimitingFilter.doFilterInternal(request, response, filterChain);
                        } catch (Exception e) {
                            fail("Exception in concurrent test: " + e.getMessage());
                        }
                    }
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    fail("Thread interrupted");
                }
            }

            verify(response, atLeastOnce()).setStatus(429);
        }
    }

    @Test
    void testDoFilterInternal_XForwardedForEmpty_ShouldSplitEmptyString() throws ServletException, IOException {
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newScheduledThreadPool(1))
                    .thenReturn(scheduledExecutorService);
            rateLimitingFilter = new RateLimitingFilter();

            when(request.getHeader("X-Forwarded-For")).thenReturn("");

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(request, never()).getRemoteAddr();
        }
    }

    @Test
    void testDoFilterInternal_XForwardedForSingleIP_ShouldReturnThatIP() throws ServletException, IOException {
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newScheduledThreadPool(1))
                    .thenReturn(scheduledExecutorService);
            rateLimitingFilter = new RateLimitingFilter();

            when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1");

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(request, never()).getRemoteAddr();
        }
    }

    @Test
    void testDoFilterInternal_RateLimitBoundary_ShouldHandleEdgeCases() throws ServletException, IOException {
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newScheduledThreadPool(1))
                    .thenReturn(scheduledExecutorService);
            rateLimitingFilter = new RateLimitingFilter();

            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn(CLIENT_IP);
            when(response.getWriter()).thenReturn(printWriter);

            for (int i = 1; i <= 59; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
                verify(response, never()).setStatus(429);
            }

            rateLimitingFilter.doFilterInternal(request, response, filterChain);
            verify(response, never()).setStatus(429);

            rateLimitingFilter.doFilterInternal(request, response, filterChain);
            verify(response).setStatus(429);
        }
    }

    @Test
    void testGetClientIP_XForwardedForConditions() throws ServletException, IOException {
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newScheduledThreadPool(1))
                    .thenReturn(scheduledExecutorService);
            rateLimitingFilter = new RateLimitingFilter();

            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn(CLIENT_IP);

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            verify(request).getHeader("X-Forwarded-For");
            verify(request).getRemoteAddr();
        }
    }

    @Test
    void testGetClientIP_XForwardedForNotNull() throws ServletException, IOException {
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newScheduledThreadPool(1))
                    .thenReturn(scheduledExecutorService);
            rateLimitingFilter = new RateLimitingFilter();

            when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1");

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            verify(request).getHeader("X-Forwarded-For");
            verify(request, never()).getRemoteAddr();
        }
    }

    @Test
    void testRateLimitCondition_LessThanLimit() throws ServletException, IOException {
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newScheduledThreadPool(1))
                    .thenReturn(scheduledExecutorService);
            rateLimitingFilter = new RateLimitingFilter();

            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn(CLIENT_IP);

            for (int i = 0; i < 30; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            verify(filterChain, times(30)).doFilter(request, response);
            verify(response, never()).setStatus(429);
        }
    }

    @Test
    void testRateLimitCondition_EqualToLimit() throws ServletException, IOException {
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newScheduledThreadPool(1))
                    .thenReturn(scheduledExecutorService);
            rateLimitingFilter = new RateLimitingFilter();

            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn(CLIENT_IP);

            for (int i = 0; i < 60; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            verify(filterChain, times(60)).doFilter(request, response);
            verify(response, never()).setStatus(429);
        }
    }

    @Test
    void testRateLimitCondition_GreaterThanLimit() throws ServletException, IOException {
        try (MockedStatic<Executors> executorsMock = mockStatic(Executors.class)) {
            executorsMock.when(() -> Executors.newScheduledThreadPool(1))
                    .thenReturn(scheduledExecutorService);
            rateLimitingFilter = new RateLimitingFilter();

            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn(CLIENT_IP);
            when(response.getWriter()).thenReturn(printWriter);

            for (int i = 0; i < 61; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            verify(response).setStatus(429);
            verify(filterChain, times(60)).doFilter(request, response);
        }
    }
}
