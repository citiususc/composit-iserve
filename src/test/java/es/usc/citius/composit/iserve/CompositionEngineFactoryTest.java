package es.usc.citius.composit.iserve;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import es.usc.citius.composit.core.composition.network.ServiceMatchNetwork;
import es.usc.citius.composit.core.composition.optimization.NetworkOptimizer;
import es.usc.citius.composit.iserve.util.WSCImportUtils;
import es.usc.citius.composit.wsc08.data.WSCTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.api.iServeEngine;
import uk.ac.open.kmi.iserve.api.iServeEngineModule;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;

import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * CompositionEngineFactoryTest
 * TODO: Provide Description
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 04/04/2014
 */
public class CompositionEngineFactoryTest {

    private static final Logger log = LoggerFactory.getLogger(CompositIserveEngineTest.class);
    private static final String WSC_01_ONTOLOGY_URL = "http://localhost:15000/wsc/ontology/ontology.owl";
    private static final String WSC_01_ONTOLOGY_NS = WSC_01_ONTOLOGY_URL + "#";

    private static WSCTest test = WSCTest.TESTSET_2008_01;

    @BeforeClass
    public static void setUp() throws Exception {

        Injector injector = Guice.createInjector(new iServeEngineModule());
        iServeEngine iserve = injector.getInstance(iServeEngine.class);

        // Clear registry
        iserve.getRegistryManager().clearRegistry();

        // Import data
        WSCImportUtils.importDataset(iserve, new URL(WSC_01_ONTOLOGY_URL), test, false);
    }

    @Test
    public void testCreateEngine() throws Exception {

        log.info("Testing composition...");

        ImmutableSet.Builder<URI> inputsBuilder = new ImmutableSet.Builder<URI>();
        inputsBuilder.add(new URI(WSC_01_ONTOLOGY_NS + "con1233457844")).
                add(new URI(WSC_01_ONTOLOGY_NS + "con1849951292")).
                add(new URI(WSC_01_ONTOLOGY_NS + "con864995873"));

        ImmutableSet.Builder<URI> outputsBuilder = new ImmutableSet.Builder<URI>();
        outputsBuilder.add(new URI(WSC_01_ONTOLOGY_NS + "con1220759822")).
                add(new URI(WSC_01_ONTOLOGY_NS + "con2119691623"));

        List<NetworkOptimizer<URI, LogicConceptMatchType>> optimizers =
                new LinkedList<NetworkOptimizer<URI, LogicConceptMatchType>>();

        CompositIserveEngine compositionEngine = CompositionEngineFactory.createEngine();

        ServiceMatchNetwork<URI, LogicConceptMatchType> result = compositionEngine.compose(inputsBuilder.build(), outputsBuilder.build());
        log.info("Composition Result: {}", result.getLeveledList());
    }

}
