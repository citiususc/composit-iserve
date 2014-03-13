package es.usc.citius.composit.iserve.discovery;


import es.usc.citius.composit.core.composition.InputDiscoverer;
import es.usc.citius.composit.core.matcher.graph.MatchGraph;
import es.usc.citius.composit.core.model.Operation;
import es.usc.citius.composit.iserve.OperationTranslator;
import es.usc.citius.composit.iserve.util.Metrics;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class iServeMatchGraphBasedDiscoverer implements InputDiscoverer<URI> {

    private OperationTranslator mgr;
    private ServiceManager serviceManager;
    private MatchGraph<URI, LogicConceptMatchType> matchGraph;

    public iServeMatchGraphBasedDiscoverer(OperationTranslator mgr, ServiceManager serviceManager, MatchGraph<URI, LogicConceptMatchType> matchGraph) {
        this.mgr = mgr;
        this.serviceManager = serviceManager;
        this.matchGraph = matchGraph;
    }

    @Override
    public Set<Operation<URI>> findOperationsConsuming(URI input) {
        Metrics.get().increment("iServeMatchGraphBasedDiscoverer.findOperationsConsuming");
        Set<URI> operationUris = new HashSet<URI>();
        for(URI compatibleInput : matchGraph.getTargetElementsMatchedBy(input).keySet()){
            // Get service op with output
            operationUris.addAll(serviceManager.listOperationsWithInputType(compatibleInput));
        }
        Set<Operation<URI>> ops = new HashSet<Operation<URI>>();
        for(URI opUri : operationUris){
            ops.add(mgr.getOperation(opUri));
        }
        return ops;
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
