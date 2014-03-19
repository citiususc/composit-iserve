package es.usc.citius.composit.iserve.discovery;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hp.hpl.jena.sparql.function.library.max;
import es.usc.citius.composit.core.composition.InputDiscoverer;
import es.usc.citius.composit.core.matcher.graph.MatchGraph;
import es.usc.citius.composit.core.model.Operation;
import es.usc.citius.composit.iserve.OperationTranslator;
import es.usc.citius.composit.iserve.util.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.sal.manager.KnowledgeBaseManager;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class iServeMatchGraphBasedDiscoverer implements InputDiscoverer<URI> {

    private static final int MAX_CACHE_SIZE = 100000;
    private final OperationTranslator mgr;
    private final ServiceManager serviceManager;
    private final MatchGraph<URI, LogicConceptMatchType> matchGraph;
    private LoadingCache<URI, Set<Operation<URI>>> cache;
    private static final Logger log = LoggerFactory.getLogger(iServeMatchGraphBasedDiscoverer.class);


    public iServeMatchGraphBasedDiscoverer(OperationTranslator mgr, ServiceManager serviceManager, MatchGraph<URI, LogicConceptMatchType> matchGraph) {
        this(mgr, serviceManager, matchGraph, null);
    }

    public iServeMatchGraphBasedDiscoverer(final OperationTranslator mgr, final ServiceManager serviceManager, final MatchGraph<URI, LogicConceptMatchType> matchGraph, KnowledgeBaseManager kb) {
        this.mgr = mgr;
        this.serviceManager = serviceManager;
        this.matchGraph = matchGraph;

        this.cache = CacheBuilder.newBuilder()
                .maximumSize(kb != null ? MAX_CACHE_SIZE : 0)
                .build(new CacheLoader<URI, Set<Operation<URI>>>() {
                    @Override
                    public Set<Operation<URI>> load(URI key) {
                        Set<URI> operationUris = new HashSet<URI>();
                        for(URI compatibleInput : matchGraph.getTargetElementsMatchedBy(key).keySet()){
                            // Get service op with output
                            operationUris.addAll(serviceManager.listOperationsWithInputType(compatibleInput));
                        }
                        Set<Operation<URI>> ops = new HashSet<Operation<URI>>();
                        for(URI opUri : operationUris){
                            ops.add(mgr.getOperation(opUri));
                        }
                        return ops;
                    }
                });

        if (kb != null){
            log.info("Populating discovery index...");
            Set<URI> concepts = kb.listConcepts(null);
            log.info("Indexing " + concepts.size() + " concepts...");
            // Populate
            int counter = 1;
            for(URI concept : kb.listConcepts(null)){
                this.cache.getUnchecked(concept);
                log.info("Indexing " + counter + "/" + concepts.size() + "...");
                counter++;
            }
        }
    }

    @Override
    public Set<Operation<URI>> findOperationsConsuming(URI input) {
        Metrics.get().increment("iServeMatchGraphBasedDiscoverer.findOperationsConsuming");
        return this.cache.getUnchecked(input);
    }

    @Override
    public Set<Operation<URI>> findOperationsConsumingSome(Collection<URI> inputs) {
        Metrics.get().increment("iServeMatchGraphBasedDiscoverer.findOperationsConsumingSome");
        Set<Operation<URI>> operations = new HashSet<Operation<URI>>();
        for(URI input : inputs){
            operations.addAll(findOperationsConsuming(input));
        }
        return operations;
    }
}
