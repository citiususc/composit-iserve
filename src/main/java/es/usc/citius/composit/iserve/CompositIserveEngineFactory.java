package es.usc.citius.composit.iserve;

import es.usc.citius.composit.core.composition.optimization.NetworkOptimizer;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;

import java.net.URI;
import java.util.List;

/**
 * CompositIserveEngineFactory  is a factory for creating CompositIserveEngines
 * It uses Assited Injection to that end. See http://google-guice.googlecode.com/svn/trunk/javadoc/com/google/inject/assistedinject/FactoryModuleBuilder.html
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 02/04/2014
 */
public interface CompositIserveEngineFactory {

    CompositIserveEngine create(List<NetworkOptimizer<URI, LogicConceptMatchType>> optimisations,
                                Integer matchCacheSize);
}
