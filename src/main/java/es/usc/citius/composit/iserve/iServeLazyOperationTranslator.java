package es.usc.citius.composit.iserve;


import es.usc.citius.composit.core.model.Operation;
import es.usc.citius.composit.core.model.Signature;
import es.usc.citius.composit.core.model.impl.ResourceOperation;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class iServeLazyOperationTranslator implements OperationTranslator {

    private ServiceManager serviceMgr;

    public iServeLazyOperationTranslator(ServiceManager serviceMgr) {
        this.serviceMgr = serviceMgr;
    }

    @Override
    public Set<Operation<URI>> getOperations() {
        Set<Operation<URI>> ops = new HashSet<Operation<URI>>();
        for(URI serviceUri : serviceMgr.listServices()){
            ops.addAll(getOperationsOfService(serviceUri));
        }
        return ops;
    }

    @Override
    public Operation<URI> getOperation(final URI operation) {
        return new ResourceOperation<URI>(operation.toString(), new Signature<URI>() {
            @Override
            public Set<URI> getInputs() {
                Set<URI> models = new HashSet<URI>();
                for(URI opUri : serviceMgr.listInputs(operation)){
                    for(URI mandatoryPart : serviceMgr.listMandatoryParts(opUri)){
                        models.addAll(serviceMgr.listModelReferences(mandatoryPart));
                    }
                }
                return models;
            }

            @Override
            public Set<URI> getOutputs() {
                Set<URI> models = new HashSet<URI>();
                for(URI opUri : serviceMgr.listOutputs(operation)){
                    for(URI mandatoryPart : serviceMgr.listMandatoryParts(opUri)){
                        models.addAll(serviceMgr.listModelReferences(mandatoryPart));
                    }
                }
                return models;
            }
        });
    }

    @Override
    public Set<Operation<URI>> getOperationsOfService(URI service) {
        Set<Operation<URI>> ops = new HashSet<Operation<URI>>();
        for(URI op : serviceMgr.listOperations(service)){
            ops.add(getOperation(op));
        }
        return ops;
    }
}
