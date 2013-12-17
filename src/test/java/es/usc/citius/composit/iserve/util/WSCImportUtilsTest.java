package es.usc.citius.composit.iserve.util;

import es.usc.citius.composit.wsc08.data.WSCTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.open.kmi.iserve.api.iServeEngine;
import uk.ac.open.kmi.iserve.api.iServeEngineFactory;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;


public class WSCImportUtilsTest {
    private iServeEngine engine;
    private URL ontoUrl;
    private int port = 15000;

    @Before
    public void setUp() throws Exception {
        engine = iServeEngineFactory.createEngine();
        ontoUrl = new URL("http://localhost:" + port + "/wsc/ontology/ontology.owl");
        WSCImportUtils.importDataset(engine, ontoUrl, WSCTest.TESTSET_2008_01, false);
    }

    @After
    public void tearDown(){
        engine.shutdown();
    }

    @Test
    public void testKb(){
        // Test if data is correctly initialized
        URI testConcept = URI.create(ontoUrl.toString() + "#con864995873");
        Set<URI> expected = new HashSet<URI>(Arrays.asList(
           URI.create(ontoUrl.toString() + "#con864995873"),
           URI.create(ontoUrl.toString() + "#con102879816")
        ));
        Set<URI> result = engine.getRegistryManager().getKnowledgeBaseManager().listSubClasses(testConcept, false);
        assertEquals(expected, result);
    }
}
