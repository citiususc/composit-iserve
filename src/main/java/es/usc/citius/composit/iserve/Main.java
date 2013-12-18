package es.usc.citius.composit.iserve;

import ch.qos.logback.classic.Level;
import es.usc.citius.composit.core.composition.InputDiscoverer;
import es.usc.citius.composit.core.composition.optimization.BackwardMinimizationOptimizer;
import es.usc.citius.composit.core.composition.optimization.FunctionalDominanceOptimizer;
import es.usc.citius.composit.core.composition.search.ComposIT;
import es.usc.citius.composit.core.composition.search.CompositionProblem;
import es.usc.citius.composit.core.matcher.graph.MatchGraph;
import es.usc.citius.composit.core.model.impl.SignatureIO;
import es.usc.citius.composit.iserve.discovery.iServeMatchGraphBasedDiscoverer;
import es.usc.citius.composit.iserve.discovery.iServeOperationDiscovererAdapter;
import es.usc.citius.composit.iserve.match.iServePluginKBMatchGraph;
import es.usc.citius.composit.iserve.util.WSCImportUtils;
import es.usc.citius.composit.wsc08.data.WSCTest;
import uk.ac.open.kmi.iserve.api.iServeEngine;
import uk.ac.open.kmi.iserve.api.iServeEngineFactory;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.discovery.disco.impl.GenericLogicDiscoverer;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;


public class Main {

    private static final int port = 15000;

    public static void main(String[] args) throws Exception {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        // Create a iServe engine
        final iServeEngine iserve = iServeEngineFactory.createEngine();
        URL ontoUrl = new URL("http://localhost:" + port + "/wsc/ontology/ontology.owl");
        // Import data
        WSCImportUtils.importDataset(iserve, ontoUrl, WSCTest.TESTSET_2008_01, false);

        System.out.println("Available matchers: " + iserve.listAvailableMatchers());
        // Create a simple KB-Based MatchGraph that meets the WSC match rules (exact/plugin match)
        final MatchGraph<URI, LogicConceptMatchType> matchGraph = new iServePluginKBMatchGraph(ontoUrl.toURI(),
                iserve.getRegistryManager().getKnowledgeBaseManager());

        final ServiceManager serviceManager = iserve.getRegistryManager().getServiceManager();
        final ConceptMatcher iserveMatcher = iserve.getDefaultConceptMatcher();
        final ConceptMatcher indexedMatcher = iserve.getConceptMatcher("uk.ac.open.kmi.iserve.discovery.disco.impl.SparqlIndexedLogicConceptMatcher");
        final OperationTranslator opManager = new iServeOperationTranslatorImpl(serviceManager);
        final InputDiscoverer<URI> discoverer = new iServeMatchGraphBasedDiscoverer(opManager, serviceManager, matchGraph);
        final InputDiscoverer<URI> iserveDiscoverer = new iServeOperationDiscovererAdapter(
                new GenericLogicDiscoverer(serviceManager, indexedMatcher), opManager);

        CompositionProblem<URI, LogicConceptMatchType> problem = new CompositionProblem<URI, LogicConceptMatchType>() {
            @Override
            public MatchGraph<URI, LogicConceptMatchType> getMatchGraph() {
                return matchGraph;
            }

            @Override
            public InputDiscoverer<URI> getInputDiscoverer() {
                return iserveDiscoverer;
            }
        };
        ComposIT<URI, LogicConceptMatchType> composit = new ComposIT<URI, LogicConceptMatchType>(problem);
        composit.addOptimization(new BackwardMinimizationOptimizer<URI, LogicConceptMatchType>());
        composit.addOptimization(new FunctionalDominanceOptimizer<URI, LogicConceptMatchType>());

        // WSC01 Request
        // inputs: ("con1233457844", "con1849951292", "con864995873")
        // outputs: ("con1220759822", "con2119691623")
        SignatureIO<URI> request = new SignatureIO<URI>(
                Arrays.asList(
                        URI.create(ontoUrl.toString() + "#con1233457844"),
                        URI.create(ontoUrl.toString() + "#con1849951292"),
                        URI.create(ontoUrl.toString() + "#con864995873")
                ),

                Arrays.asList(
                        URI.create(ontoUrl.toString() + "#con1220759822"),
                        URI.create(ontoUrl.toString() + "#con2119691623")
                )
        );

        composit.search(request);

        iserve.shutdown();
    }
}
