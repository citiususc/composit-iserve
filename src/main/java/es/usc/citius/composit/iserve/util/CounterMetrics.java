package es.usc.citius.composit.iserve.util;




public interface CounterMetrics {
    int counter(String name);
    int increment(String name);
    int incrementBy(String name, int count);
    void reset();
}
