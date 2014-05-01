package es.usc.citius.composit.iserve;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import es.usc.citius.composit.core.composition.InputDiscoverer;
import es.usc.citius.composit.core.composition.network.ServiceMatchNetwork;
import es.usc.citius.composit.core.composition.optimization.BackwardMinimizationOptimizer;
import es.usc.citius.composit.core.composition.optimization.FunctionalDominanceOptimizer;
import es.usc.citius.composit.core.composition.optimization.NetworkOptimizer;
import es.usc.citius.composit.core.matcher.SetMatchFunction;
import es.usc.citius.composit.core.matcher.graph.MatchGraph;
import es.usc.citius.composit.iserve.discovery.iServeOperationDiscovererAdapter;
import es.usc.citius.composit.iserve.match.iServeMatchGraph;
import es.usc.citius.composit.iserve.match.iServeSetMatchFunction;
import es.usc.citius.composit.iserve.util.WSCImportUtils;
import es.usc.citius.composit.wsc08.data.WSCTest;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.api.iServeEngine;
import uk.ac.open.kmi.iserve.api.iServeEngineModule;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;

import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * CompositIserveEngineTest
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 02/04/2014
 */
@RunWith(JukitoRunner.class)
public class CompositIserveEngineTest {

    private static final Logger log = LoggerFactory.getLogger(CompositIserveEngineTest.class);
    private static final String WSC_01_ONTOLOGY_URL = "http://localhost:15000/wsc/ontology/ontology.owl";
    private static final String WSC_01_ONTOLOGY_NS = WSC_01_ONTOLOGY_URL + "#";

    private static WSCTest test = WSCTest.TESTSET_2008_01;

    /**
     * JukitoModule.
     */
    public static class InnerModule extends JukitoModule {


        @Override
        protected void configureTest() {
            // Configure iServeEngine
            install(new iServeEngineModule());


            // bind the Operation Translator
            // bindMany(ServiceManager.class, ServiceManagerSparql.class, ServiceManagerIndexRdf.class);
//            bind(OperationTranslator.class).to(iServeIndexedOperationTranslator.class);
            bind(OperationTranslator.class).to(iServeLazyOperationTranslator.class);

            // bind the Input Discoverer
            // bindMany(ServiceManager.class, ServiceManagerSparql.class, ServiceManagerIndexRdf.class);
            bind(new TypeLiteral<InputDiscoverer<URI>>(){}).to(iServeOperationDiscovererAdapter.class);

            // bind setMatchFunction
            bind(new TypeLiteral<SetMatchFunction<URI, LogicConceptMatchType>>(){}).to(iServeSetMatchFunction.class);

            // bind MatchGraph
            bind(new TypeLiteral<MatchGraph<URI, LogicConceptMatchType>>(){}).to(iServeMatchGraph.class);

            // Bind optimisers
            MapBinder<String, NetworkOptimizer<URI, LogicConceptMatchType>> mapbinder =
                    MapBinder.newMapBinder(
                            binder(),
                            new TypeLiteral<String>(){},
                            new TypeLiteral<NetworkOptimizer<URI, LogicConceptMatchType>>() {});

            // For now hard-code the optimisations bindings. Should have a plugin method later.
            mapbinder.addBinding(BackwardMinimizationOptimizer.class.getName()).
                    to(new TypeLiteral<BackwardMinimizationOptimizer<URI, LogicConceptMatchType>>() {});

            mapbinder.addBinding(FunctionalDominanceOptimizer.class.getName()).
                    to(new TypeLiteral<FunctionalDominanceOptimizer<URI, LogicConceptMatchType>>() {});

            // bind the implementation of the engine
            install(new FactoryModuleBuilder()
                    .implement(CompositIserveEngine.class, CompositIserveEngineImpl.class)
                    .build(CompositIserveEngineFactory.class));

        }
    }

    @BeforeClass
    public static void oneOfSetup() throws Exception {
        Injector injector = Guice.createInjector(new iServeEngineModule());
        iServeEngine iserve = injector.getInstance(iServeEngine.class);

        // Clear registry
        iserve.getRegistryManager().clearRegistry();

        // Import data
        WSCImportUtils.importDataset(iserve, new URL(WSC_01_ONTOLOGY_URL), test, false);
    }

    @Test
    public void testCompose(CompositIserveEngineFactory factory, Map<String, Provider<NetworkOptimizer<? extends URI, ? extends LogicConceptMatchType>>> optimisationsProviders) throws Exception {

        //FIXME: No optimisers there!! (maybe not providers?)

        log.info("Testing composition...");

        ImmutableSet.Builder<URI> inputsBuilder = new ImmutableSet.Builder<URI>();
        inputsBuilder.add(new URI(WSC_01_ONTOLOGY_NS + "con1233457844")).
                add(new URI(WSC_01_ONTOLOGY_NS + "con1849951292")).
                add(new URI(WSC_01_ONTOLOGY_NS + "con864995873"));

        ImmutableSet.Builder<URI> outputsBuilder = new ImmutableSet.Builder<URI>();
        outputsBuilder.add(new URI(WSC_01_ONTOLOGY_NS + "con1220759822")).
                add(new URI(WSC_01_ONTOLOGY_NS + "con2119691623"));

        List<NetworkOptimizer<URI, LogicConceptMatchType>> optimizers =
                new LinkedList<NetworkOptimizer<URI, LogicConceptMatchType>>();

        for ( Map.Entry<String, Provider<NetworkOptimizer<? extends URI, ? extends LogicConceptMatchType>>> providerEntry : optimisationsProviders.entrySet() ) {
            optimizers.add((NetworkOptimizer<URI, LogicConceptMatchType>) providerEntry.getValue().get());
        }

        CompositIserveEngine compositionEngine = factory.create(optimizers, 0);

        ServiceMatchNetwork<URI, LogicConceptMatchType> result = compositionEngine.compose(inputsBuilder.build(), outputsBuilder.build());
        log.info("Composition Result: \n {}", result.getLeveledList());

    }
}
