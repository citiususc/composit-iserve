package es.usc.citius.composit.iserve;

import es.usc.citius.composit.core.composition.network.ServiceMatchNetwork;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;

import java.net.URI;
import java.util.Set;

/**
 * CompositIserveEngine is the basic interface for the composition engine
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 02/04/2014
 */
public interface CompositIserveEngine {
    ServiceMatchNetwork<URI, LogicConceptMatchType> compose(Set<URI> inputs, Set<URI> outputs);
}
