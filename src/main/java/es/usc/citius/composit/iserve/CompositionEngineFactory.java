package es.usc.citius.composit.iserve;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import es.usc.citius.composit.core.composition.optimization.NetworkOptimizer;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;

import java.net.URI;
import java.util.List;

/**
 * CompositionEngineFactory is a Factory for creating CompositEngines
 * Takes care of Guice wiring on behalf of applications
 *
 * TODO: Rename when ComposIT is refactored
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 02/04/2014
 */
public class CompositionEngineFactory {

    public static CompositIserveEngine createEngine() {
        Injector injector = Guice.createInjector(new CompositIserveModule());
        return injector.getInstance(CompositIserveEngineFactory.class).
                create(ImmutableList.<NetworkOptimizer<URI, LogicConceptMatchType>>of(), 0);
    }

    public static CompositIserveEngine createEngine(List<NetworkOptimizer<URI, LogicConceptMatchType>> optimisations, Integer cacheSize) {
        Injector injector = Guice.createInjector(new CompositIserveModule());
        return injector.getInstance(CompositIserveEngineFactory.class).create(optimisations, cacheSize);
    }

}
