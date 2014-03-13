package es.usc.citius.composit.iserve.util;


public class DummyMetrics implements CounterMetrics {

    @Override
    public int counter(String name) {
        return 0;
    }

    @Override
    public int increment(String name) {
        return 0;
    }

    @Override
    public int incrementBy(String name, int count) {
        return 0;
    }

    @Override
    public void reset() {
        // Do nothing
    }

    @Override
    public String toString() {
        return "Metrics Disabled. Use Metrics.enable() to activate metrics";
    }
}
