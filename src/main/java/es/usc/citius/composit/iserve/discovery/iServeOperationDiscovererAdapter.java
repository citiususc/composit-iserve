package es.usc.citius.composit.iserve.discovery;


import es.usc.citius.composit.core.composition.InputDiscoverer;
import es.usc.citius.composit.core.model.Operation;
import es.usc.citius.composit.iserve.OperationTranslator;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.OperationDiscoverer;
import uk.ac.open.kmi.iserve.sal.util.metrics.Metrics;

import javax.inject.Inject;
import java.net.URI;
import java.util.*;

public class iServeOperationDiscovererAdapter implements InputDiscoverer<URI> {
    private OperationDiscoverer discoverer;
    private OperationTranslator opManager;

    @Inject
    public iServeOperationDiscovererAdapter(OperationDiscoverer discoverer, OperationTranslator opManager) {
        this.discoverer = discoverer;
        this.opManager = opManager;
    }

    @Override
    public Set<Operation<URI>> findOperationsConsuming(URI input) {
        Metrics.get().increment("iServeOperationDiscovererAdapter.findOperationsConsuming");
        Map<URI, MatchResult> result = discoverer.findOperationsConsumingSome(Collections.singleton(input));
        // Apply filtering?
        Set<Operation<URI>> ops = new HashSet<Operation<URI>>();
        // Convert
        for(URI operationUri : result.keySet()){
            ops.add(opManager.getOperation(operationUri));
        }
        return ops;
    }

    @Override
    public Set<Operation<URI>> findOperationsConsumingSome(Collection<URI> inputs) {
        Metrics.get().increment("iServeOperationDiscovererAdapter.findOperationsConsumingSome");
        Set<Operation<URI>> ops = new HashSet<Operation<URI>>();
        for(URI input : inputs){
            ops.addAll(findOperationsConsuming(input));
        }
        return ops;
    }
}
