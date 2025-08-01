package com.gmail.uprial.masochisticsurvival.common;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

public class RandomUtils {
    public final static double MAX_PERCENT = 100.0D;

    private final static Random RANDOM = new Random();

    public static <T> T getSetItem(final Set<T> set) {
        return  (new ArrayList<>(set)).get(RANDOM.nextInt(set.size()));
    }

    public static boolean PASS(final double percentage) {
        return (percentage >= MAX_PERCENT) || (RANDOM.nextDouble() * MAX_PERCENT) < (percentage);
    }
}