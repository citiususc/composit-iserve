package es.usc.citius.composit.iserve;

import es.usc.citius.composit.core.model.Operation;

import java.net.URI;
import java.util.Set;

public interface OperationTranslator {
    Set<Operation<URI>> getOperations();
    Operation<URI> getOperation(URI operation);
    Set<Operation<URI>> getOperationsOfService(URI service);
}
