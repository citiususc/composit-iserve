package es.usc.citius.composit.iserve.discovery;


import es.usc.citius.composit.core.composition.InputDiscoverer;
import es.usc.citius.composit.core.model.Operation;
import es.usc.citius.composit.iserve.OperationTranslator;
import uk.ac.open.kmi.iserve.sal.util.metrics.Metrics;

import java.net.URI;
import java.util.Collection;
import java.util.Set;

public class DummyDiscoverer implements InputDiscoverer<URI> {
    private OperationTranslator op;

    public DummyDiscoverer(OperationTranslator op) {
        this.op = op;
    }

    @Override
    public Set<Operation<URI>> findOperationsConsuming(URI input) {
        Metrics.get().increment("DummyDiscoverer.findOperationsConsuming");
        return op.getOperations();
    }

    @Override
    public Set<Operation<URI>> findOperationsConsumingSome(Collection<URI> inputs) {
        Metrics.get().increment("DummyDiscoverer.findOperationsConsumingSome");
        return op.getOperations();
    }
}
