package es.usc.citius.composit.iserve;

import com.tinkerpop.blueprints.Graph;
import es.usc.citius.composit.blueprints.TinkerGraphSMNFactory;
import es.usc.citius.composit.core.composition.network.ServiceMatchNetwork;

/**
 * @author Pablo Rodríguez Mier <<a href="mailto:pablo.rodriguez.mier@usc.es">pablo.rodriguez.mier@usc.es</a>>
 */
public class CompositionResult<E, T extends Comparable<T>> {
    private ServiceMatchNetwork<E,T> network;
    private Graph graph;

    public CompositionResult(ServiceMatchNetwork<E, T> network) {
        this.network = network;
        this.graph = TinkerGraphSMNFactory.create(network);
    }

    public ServiceMatchNetwork<E, T> getNetwork() {
        return network;
    }

    public Graph getGraph() {
        return graph;
    }
}
