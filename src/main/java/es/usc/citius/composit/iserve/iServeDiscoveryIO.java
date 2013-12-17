package es.usc.citius.composit.iserve;



import es.usc.citius.composit.core.composition.DiscoveryIO;
import es.usc.citius.composit.core.matcher.graph.MatchGraph;
import es.usc.citius.composit.core.model.Operation;
import es.usc.citius.composit.core.model.Signature;
import es.usc.citius.composit.core.model.impl.ResourceOperation;
import uk.ac.open.kmi.iserve.api.iServeEngineFactory;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.msm4j.Service;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class iServeDiscoveryIO implements DiscoveryIO<URI> {

    private OperationManager mgr;
    private ServiceManager serviceManager;
    private MatchGraph<URI, LogicConceptMatchType> matchGraph;

    public iServeDiscoveryIO(OperationManager mgr, ServiceManager serviceManager, MatchGraph<URI, LogicConceptMatchType> matchGraph) {
        this.mgr = mgr;
        this.serviceManager = serviceManager;
        this.matchGraph = matchGraph;
    }

    @Override
    public Set<Operation<URI>> discoverOperationsForInput(URI input) {
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
    public Set<Operation<URI>> discoverOperationsForOutput(URI output) {
        throw new UnsupportedOperationException();
    }
}
