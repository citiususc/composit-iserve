package es.usc.citius.composit.iserve;

import ch.qos.logback.classic.Level;
import es.usc.citius.composit.core.composition.InputDiscoverer;
import es.usc.citius.composit.core.composition.optimization.BackwardMinimizationOptimizer;
import es.usc.citius.composit.core.composition.optimization.FunctionalDominanceOptimizer;
import es.usc.citius.composit.core.composition.search.ComposIT;
import es.usc.citius.composit.core.composition.search.CompositionProblem;
import es.usc.citius.composit.core.knowledge.Concept;
import es.usc.citius.composit.core.matcher.graph.MatchGraph;
import es.usc.citius.composit.core.model.impl.SignatureIO;
import es.usc.citius.composit.iserve.discovery.iServeMatchGraphBasedDiscoverer;
import es.usc.citius.composit.iserve.match.ConceptMatcherMetrics;
import es.usc.citius.composit.iserve.match.iServeMatchGraph;
import es.usc.citius.composit.iserve.util.WSCImportUtils;
import es.usc.citius.composit.wsc08.data.WSCTest;
import uk.ac.open.kmi.iserve.api.iServeEngine;
import uk.ac.open.kmi.iserve.api.iServeEngineFactory;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.iserve.sal.util.metrics.Metrics;

import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;


public class Main {

    private static final int port = 15000;
    private static WSCTest test = WSCTest.TESTSET_2008_01;

    private static SignatureIO<URI> translate(SignatureIO<Concept> signature, URL ontologyUrl){
        Set<URI> inputs = new HashSet<URI>();
        for(Concept input : signature.getInputs()){
            inputs.add(URI.create(ontologyUrl.toString() + "#" + input.getID()));
        }
        Set<URI> outputs = new HashSet<URI>();
        for(Concept output : signature.getOutputs()){
            outputs.add(URI.create(ontologyUrl.toString() + "#" + output.getID()));
        }
        return new SignatureIO<URI>(inputs, outputs);
    }

    public static void main(String[] args) throws Exception {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        // Create a iServe engine
        final iServeEngine iserve = iServeEngineFactory.createEngine();
        URL ontoUrl = new URL("http://localhost:" + port + "/wsc/ontology/ontology.owl");
        // Import data
        WSCImportUtils.importDataset(iserve, ontoUrl, test, false);

        System.out.println("Available matchers: " + iserve.listAvailableMatchers());
        // Create a simple KB-Based MatchGraph that meets the WSC match rules (exact/plugin match)
        //final MatchGraph<URI, LogicConceptMatchType> matchGraph = new iServePluginKBMatchGraph(ontoUrl.toURI(), iserve.getRegistryManager().getKnowledgeBaseManager());
        final ServiceManager serviceManager = iserve.getRegistryManager().getServiceManager();

        //final ConceptMatcher iserveMatcher = iserve.getDefaultConceptMatcher();
        final ConceptMatcher iserveMatcher = iserve.getConceptMatcher("uk.ac.open.kmi.iserve.discovery.disco.impl.SparqlIndexedLogicConceptMatcher");
        //final ConceptMatcher iserveMatcher = iserve.getConceptMatcher("uk.ac.open.kmi.iserve.discovery.disco.impl.SparqlLogicConceptMatcher");
        //ConceptMatcher iserveMatcher = iserve.getConceptMatcher("uk.ac.open.kmi.iserve.discovery.infinispan.InfinispanIndexedConceptMatcher");

        final ConceptMatcher matcher = new ConceptMatcherMetrics(iserveMatcher);
        System.out.println("Matcher description: " + matcher.getMatcherDescription());

        final MatchGraph<URI, LogicConceptMatchType> matchGraph = new iServeMatchGraph(matcher,
                iserve.getRegistryManager().getKnowledgeBaseManager());
        final OperationTranslator opManager = new iServeIndexedOperationTranslator(serviceManager);
        //final OperationTranslator opManager = new iServeLazyOperationTranslator(serviceManager);

        final InputDiscoverer<URI> discoverer = new iServeMatchGraphBasedDiscoverer(opManager, serviceManager, matchGraph);
        //final InputDiscoverer<URI> discoverer = new iServeOperationDiscovererAdapter(new GenericLogicDiscoverer(serviceManager, matcher), opManager);
        //final InputDiscoverer<URI> discoverer = new DummyDiscoverer(opManager);

        CompositionProblem<URI, LogicConceptMatchType> problem = new CompositionProblem<URI, LogicConceptMatchType>() {
            @Override
            public MatchGraph<URI, LogicConceptMatchType> getMatchGraph() {
                return matchGraph;
            }

            @Override
            public InputDiscoverer<URI> getInputDiscoverer() {
                return discoverer;
            }
        };

        ComposIT<URI, LogicConceptMatchType> composit = new ComposIT<URI, LogicConceptMatchType>(problem);
        composit.addOptimization(new BackwardMinimizationOptimizer<URI, LogicConceptMatchType>());
        composit.addOptimization(new FunctionalDominanceOptimizer<URI, LogicConceptMatchType>());

        Metrics.get().reset();

        composit.search(translate(test.dataset().getRequest(), ontoUrl));

        System.out.println(Metrics.get());
        iserve.shutdown();
    }
}
