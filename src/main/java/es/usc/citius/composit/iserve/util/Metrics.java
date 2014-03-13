package es.usc.citius.composit.iserve.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class Metrics {

    private static final class LazySingleton {
        private static final CounterMetrics DEFAULT_IMPL = new SimpleMetrics();
        private static final CounterMetrics DUMMY_IMPL = new DummyMetrics();
    }

    private Map<String, AtomicInteger> counters;
    private static boolean disabled = false;

    private Metrics(){
        counters = new HashMap<String, AtomicInteger>();
    }

    public static CounterMetrics get(){
        if (disabled){
            return LazySingleton.DUMMY_IMPL;
        }
        return LazySingleton.DEFAULT_IMPL;
    }

    public static void enable(){
        disabled = false;
    }

    public static void disable(){
        disabled = true;
    }


}
