/*
 * Copyright (c) 2014.
 * Centro de Investigación en Tecnoloxías da Información (CITIUS), University of Santiago de Compostela (USC)
 * Knowledge Media Institute (KMi) - The Open University (OU)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.usc.citius.composit.iserve;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import es.usc.citius.composit.core.composition.InputDiscoverer;
import es.usc.citius.composit.core.composition.optimization.BackwardMinimizationOptimizer;
import es.usc.citius.composit.core.composition.optimization.FunctionalDominanceOptimizer;
import es.usc.citius.composit.core.composition.search.ComposIT;
import es.usc.citius.composit.core.composition.search.CompositionProblem;
import es.usc.citius.composit.core.knowledge.Concept;
import es.usc.citius.composit.core.matcher.graph.MatchGraph;
import es.usc.citius.composit.core.model.impl.SignatureIO;
import es.usc.citius.composit.iserve.discovery.DummyDiscoverer;
import es.usc.citius.composit.iserve.discovery.WSCInputDiscoveryUriAdapter;
import es.usc.citius.composit.iserve.discovery.iServeMatchGraphBasedDiscoverer;
import es.usc.citius.composit.iserve.discovery.iServeOperationDiscovererAdapter;
import es.usc.citius.composit.iserve.match.ConceptMatcherMetrics;
import es.usc.citius.composit.iserve.match.iServeMatchGraph;
import es.usc.citius.composit.iserve.util.WSCImportUtils;
import es.usc.citius.composit.wsc08.data.WSCTest;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.api.iServeEngine;
import uk.ac.open.kmi.iserve.api.iServeEngineFactory;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.discovery.disco.impl.GenericLogicDiscoverer;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.iserve.sal.util.metrics.Metrics;

import java.net.URI;
import java.net.URL;
import java.util.*;

public class Cli {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Cli.class);

    @Parameter(names = {"-m", "--index-matcher"}, description = "Use an indexed concept matcher. By default it uses SPARQL.")
    private boolean indexMatcher = false;
    @Parameter(names = {"-i", "--discovery-engine"}, description = "Select the preferred discovery mechanism (DUMMY, INDEXED, SPARQL)")
    private DiscoveryEngine discoveryEngine = DiscoveryEngine.DUMMY;
    @Parameter(names = {"-d", "--index-documents"}, description = "Use an indexed concept matcher")
    private boolean indexDocuments = false;
    @Parameter(names={"-c", "--match-cache-size"}, description = "Size of the internal cache for matchmaking. 0 = disabled")
    private int matchCacheSize = 0;
    @Parameter(names = "--help", help = true, description = "Print general command usage options")
    private boolean showHelp = false;
    @Parameter(names = {"-in", "--inputs"}, variableArity = true, description = "List of request inputs (separated by spaces)")
    private List<String> requestInputs = new ArrayList<String>();
    @Parameter(names = {"-out", "--outputs"}, variableArity = true, description = "List of request outputs (separated by spaces)")
    private List<String> requestOutputs = new ArrayList<String>();
    @Parameter(names = {"-wsc", "--wsc-dataset"}, description = "WSC'08 dataset to be used")
    private WSCTest test = WSCTest.TESTSET_2008_01;
    @Parameter(names={"-t", "--run-times"}, description = "Run the composition many times (for benchmark purposes)")
    private int runtimes = 1;

    private static final int port = 15000;
    private JCommander jcommander;

    public enum DiscoveryEngine {
        DUMMY, WSC_ADHOC, CONCEPT_LEVEL, CACHED_CONCEPT_LEVEL, INDEXED_CONCEPT_LEVEL, ISERVE_GENERIC
    }

    public static void main(String[] args) throws Exception {
        log.info("ComposIT / iServe integrated composition engine.");
        new Cli().parse(args);
    }

    public Cli() {
        this.jcommander = new JCommander(this);
        this.jcommander.setProgramName("ComposIT/iServe");
    }

    private void parse(String[] args) throws Exception {
        log.info(Arrays.toString(args));
        // Parse options
        try {
            //if (args.length == 0) throw new Exception("No arguments specified");
            jcommander.parse(args);
        }catch(Exception e){
            log.error(e.getMessage(), e);
            log.info("To see the commands and options available, use --help");
            System.exit(-1);
        }
        if (showHelp) {
            jcommander.usage();
            System.exit(0);
        }

        // Generate the default ontology url pointing to the model reference of the WSC services
        URL ontoUrl = new URL("http://localhost:" + port + "/wsc/ontology/ontology.owl");
        log.info("Local URL ontology: {}", ontoUrl);
        SignatureIO<URI> request = createRequest(this.requestInputs, this.requestOutputs, ontoUrl);

        log.info("Request inputs: {}; Request outputs: {}", this.requestInputs, this.requestOutputs);
        // Run composition
        composition(request, ontoUrl);

        // Terminate
        System.exit(0);
    }

    private static SignatureIO<URI> createRequest(List<String> strInputs, List<String> strOutputs, URL ontologyUrl){
        Set<URI> inputs = new HashSet<URI>();
        for(String input : strInputs){
            inputs.add(URI.create(ontologyUrl.toString() + "#" + input));
        }
        Set<URI> outputs = new HashSet<URI>();
        for(String output : strOutputs){
            outputs.add(URI.create(ontologyUrl.toString() + "#" + output));
        }
        return new SignatureIO<URI>(inputs, outputs);
    }

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

    public void composition(SignatureIO<URI> request, URL ontoUrl) throws Exception {
        // Create a iServe engine
        // Import the dataset to iServe
        final iServeEngine iserve = iServeEngineFactory.createEngine();

        // Import data
        log.info("Importing Dataset...");
        WSCImportUtils.importDataset(iserve, ontoUrl, test, false);

        log.info("iServe available concept matchers: {}", iserve.listAvailableMatchers());

        ConceptMatcher iserveMatcher;

        if (indexMatcher){
            iserveMatcher = iserve.getConceptMatcher("uk.ac.open.kmi.iserve.discovery.disco.impl.SparqlIndexedLogicConceptMatcher");
        } else {
            iserveMatcher = iserve.getConceptMatcher("uk.ac.open.kmi.iserve.discovery.disco.impl.SparqlLogicConceptMatcher");
        }
        // Create a simple KB-Based MatchGraph that meets the WSC match rules (exact/plugin match)
        //final MatchGraph<URI, LogicConceptMatchType> matchGraph = new iServePluginKBMatchGraph(ontoUrl.toURI(), iserve.getRegistryManager().getKnowledgeBaseManager());

        // Use the default iServe service manager
        final ServiceManager serviceManager = iserve.getRegistryManager().getServiceManager();

        final ConceptMatcher matcher = new ConceptMatcherMetrics(iserveMatcher);
        log.info("Selected matcher description: " + matcher.getMatcherDescription());

        final MatchGraph<URI, LogicConceptMatchType> matchGraph = new iServeMatchGraph(matcher,
                iserve.getRegistryManager().getKnowledgeBaseManager(), matchCacheSize);

        OperationTranslator opManager;

        if (indexDocuments){
            opManager = new iServeIndexedOperationTranslator(serviceManager);
        } else {
            opManager = new iServeLazyOperationTranslator(serviceManager);
        }

        final InputDiscoverer<URI> discoverer;

        switch(discoveryEngine){
            case DUMMY:
                discoverer = new DummyDiscoverer(opManager);
                break;
            case WSC_ADHOC:
                WSCTest.Dataset dataset = test.dataset();
                discoverer = new WSCInputDiscoveryUriAdapter(dataset.getDefaultCompositionProblem().getInputDiscoverer(), dataset.getKb(), ontoUrl.toString());
                break;
            case CONCEPT_LEVEL:
                discoverer = new iServeMatchGraphBasedDiscoverer(opManager, serviceManager, matchGraph);
                break;
            case CACHED_CONCEPT_LEVEL:
                discoverer = new iServeMatchGraphBasedDiscoverer(opManager, serviceManager, matchGraph, true);
                break;
            case INDEXED_CONCEPT_LEVEL:
                discoverer = new iServeMatchGraphBasedDiscoverer(opManager, serviceManager, matchGraph, true).index(iserve.getRegistryManager().getKnowledgeBaseManager());
                break;
            case ISERVE_GENERIC:
                discoverer = new iServeOperationDiscovererAdapter(new GenericLogicDiscoverer(serviceManager, matcher), opManager);
                break;
            default:
                discoverer = new DummyDiscoverer(opManager);
        }

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

        Metrics.get().reset();

        ComposIT<URI, LogicConceptMatchType> composit = new ComposIT<URI, LogicConceptMatchType>(problem);
        composit.addOptimization(new BackwardMinimizationOptimizer<URI, LogicConceptMatchType>());
        composit.addOptimization(new FunctionalDominanceOptimizer<URI, LogicConceptMatchType>());

        for(int i=0; i < runtimes; i++){
            log.info("Running composition " + (i+1) + "/" + runtimes);
            Metrics.get().reset();
            composit.search(request);
            log.info("Metrics:\n " + Metrics.get().toString());
        }

        iserve.shutdown();
    }

}
