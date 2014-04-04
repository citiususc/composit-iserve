package es.usc.citius.composit.iserve;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import es.usc.citius.composit.core.composition.InputDiscoverer;
import es.usc.citius.composit.core.composition.optimization.BackwardMinimizationOptimizer;
import es.usc.citius.composit.core.composition.optimization.FunctionalDominanceOptimizer;
import es.usc.citius.composit.core.composition.optimization.NetworkOptimizer;
import es.usc.citius.composit.core.matcher.SetMatchFunction;
import es.usc.citius.composit.core.matcher.graph.AbstractMatchGraph;
import es.usc.citius.composit.iserve.discovery.iServeOperationDiscovererAdapter;
import es.usc.citius.composit.iserve.match.iServeMatchGraph;
import es.usc.citius.composit.iserve.match.iServeSetMatchFunction;
import uk.ac.open.kmi.iserve.api.iServeEngineModule;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;

import java.net.URI;

/**
 * CompositIserveModule
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 01/04/2014
 */
public class CompositIserveModule extends AbstractModule {

    @Override
    protected void configure() {
        // Configure iServeEngine
        install(new iServeEngineModule());

        // bind the Operation Translator
        bind(OperationTranslator.class).to(iServeIndexedOperationTranslator.class);

        // bind the Input Discoverer
        bind(new TypeLiteral<InputDiscoverer<URI>>(){}).to(iServeOperationDiscovererAdapter.class);

        // bind setMatchFunction
        bind(new TypeLiteral<SetMatchFunction<URI, ? extends LogicConceptMatchType>>(){}).to(iServeSetMatchFunction.class);

        // bind AbstractMatchGraph
        bind(new TypeLiteral<AbstractMatchGraph<URI, ? extends LogicConceptMatchType>>(){}).to(iServeMatchGraph.class);

        // Bind optimisers
        MapBinder<String, NetworkOptimizer<URI, ? extends LogicConceptMatchType>> mapbinder =
                MapBinder.newMapBinder(
                        binder(),
                        new TypeLiteral<String>(){},
                        new TypeLiteral<NetworkOptimizer<URI, ? extends LogicConceptMatchType>>() {});

        // For now hard-code the optimisations bindings. Should have a plugin method later.
        mapbinder.addBinding(BackwardMinimizationOptimizer.class.getName()).
                to(new TypeLiteral<BackwardMinimizationOptimizer<URI, LogicConceptMatchType>>() {});

        mapbinder.addBinding(FunctionalDominanceOptimizer.class.getName()).
                to(new TypeLiteral<FunctionalDominanceOptimizer<URI, LogicConceptMatchType>>() {});

        // bind the implementation of the engine
//        bind(CompositIserveEngine.class).to(CompositIserveEngineImpl.class);
        install(new FactoryModuleBuilder()
                .implement(CompositIserveEngine.class, CompositIserveEngineImpl.class)
                .build(CompositIserveEngineFactory.class));
    }
}
