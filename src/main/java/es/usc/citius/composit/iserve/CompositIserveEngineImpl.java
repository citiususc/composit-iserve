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

import com.google.inject.assistedinject.Assisted;
import es.usc.citius.composit.core.composition.InputDiscoverer;
import es.usc.citius.composit.core.composition.network.ServiceMatchNetwork;
import es.usc.citius.composit.core.composition.optimization.NetworkOptimizer;
import es.usc.citius.composit.core.composition.search.ComposIT;
import es.usc.citius.composit.core.composition.search.CompositionProblem;
import es.usc.citius.composit.core.composition.search.DefaultCompositionProblem;
import es.usc.citius.composit.core.matcher.graph.MatchGraph;
import es.usc.citius.composit.core.model.impl.SignatureIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.api.iServeEngine;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.sal.util.metrics.Metrics;

import javax.inject.Inject;
import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * CompositIserveEngineImpl provides the main implementation of the interface to the composition engine.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 01/04/2014
 */
public class CompositIserveEngineImpl implements CompositIserveEngine {

    private static final Logger log = LoggerFactory.getLogger(CompositIserveEngineImpl.class);

    // The discovey engine this composition engine will use. To be extended for several engines
    private final iServeEngine discoveryEngine;
    private final OperationTranslator operationTranslator;
    private final InputDiscoverer<URI> inputDiscoverer;
    private final MatchGraph<URI, LogicConceptMatchType> matchGraph;

    // Ordered list of optimsations to be applied while performing compositions. Note: the order matters
    private final List<NetworkOptimizer<URI, LogicConceptMatchType>> optimisations;

    private final Integer matchCacheSize;

    @Inject
    public CompositIserveEngineImpl(iServeEngine discoveryEngine,
                                    // TODO: the next three should be replaced with an instance of ComposIT
                                    OperationTranslator operationTranslator,
                                    InputDiscoverer<URI> inputDiscoverer,
                                    MatchGraph<URI, LogicConceptMatchType> matchGraph,
                                    @Assisted List<NetworkOptimizer<URI, LogicConceptMatchType>> optimisations,
                                    @Assisted Integer matchCacheSize) {
        this.discoveryEngine = discoveryEngine;
        this.operationTranslator = operationTranslator;
        this.inputDiscoverer = inputDiscoverer;
        this.matchGraph = matchGraph;
        this.optimisations = optimisations;
        this.matchCacheSize = matchCacheSize;
    }

    public List<NetworkOptimizer<URI, LogicConceptMatchType>> getOptimisations() {
        return optimisations;
    }

    @Override
    public ServiceMatchNetwork<URI, LogicConceptMatchType> compose(Set<URI> inputs, Set<URI> outputs) {

        log.info("Computing compositions based on signature:\n inputs {} \n outputs {}", inputs, outputs );

        CompositionProblem<URI, LogicConceptMatchType> problem =
                new DefaultCompositionProblem<URI, LogicConceptMatchType>(matchGraph, this.inputDiscoverer);

        // TODO: This should be changed in ComposIT
        ComposIT<URI, LogicConceptMatchType> composit = new ComposIT<URI, LogicConceptMatchType>(problem);
        for (NetworkOptimizer<URI, LogicConceptMatchType> optimisation : optimisations) {
            composit.addOptimization(optimisation);
        }

        // create Composit request
        SignatureIO<URI> request = new SignatureIO<URI>(inputs, outputs);

        Metrics.get().reset();
        ServiceMatchNetwork<URI, LogicConceptMatchType> result = composit.searchComposition(request);
        log.info("Metrics:\n " + Metrics.get().toString());

        return result;

    }


//    private MatchGraph<URI, LogicConceptMatchType> createMatchGraph() {
//
//        // Get default concept matcher
//        ConceptMatcher conceptMatcher = this.discoveryEngine.getDefaultConceptMatcher();
//
//        MatchGraph<URI, LogicConceptMatchType> matchGraph = null;
//        if (conceptMatcher != null) {
//            matchGraph = new iServeMatchGraph(conceptMatcher,
//                this.discoveryEngine.getRegistryManager().getKnowledgeBaseManager(), matchCacheSize);
//        } else {
//            log.error("The discovery engine does not have a concept matcher configured. Stopping.");
//            // TODO: handle better
//        }
//
//        return matchGraph;
//    }


}
