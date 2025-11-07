package com.saxion.proj.tfms.routing.service.computation.helper.constants;

import java.util.Arrays;
import java.util.List;

public class Patterns {

    public static class ShiftBlock {
        public final int workMinutes;
        public final int breakMinutes;
        public final int trafficBufferPerHour; // traffic delay per hour in minutes

        public ShiftBlock(int workMinutes, int breakMinutes, int trafficBufferPerHour) {
            this.workMinutes = workMinutes;
            this.breakMinutes = breakMinutes;
            this.trafficBufferPerHour = trafficBufferPerHour;
        }
    }

    // Constant shift pattern
    public static final List<ShiftBlock> PATTERN = Arrays.asList(
            new ShiftBlock(150, 30, 10),  // 2h30 + 30 min break, 10 min traffic/hour
            new ShiftBlock(150, 30, 10),  // 2h30 + 30 min break, 10 min traffic/hour
            new ShiftBlock(60, 0, 10)     // 1h return leg, 10 min traffic/hour
    );
}
