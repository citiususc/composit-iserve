package es.usc.citius.composit.iserve.util;


import es.usc.citius.composit.transformer.wsc.wscxml.InstanceResolver;
import es.usc.citius.composit.transformer.wsc.wscxml.WSCTransformer;
import es.usc.citius.composit.wsc08.data.WSCTest;
import es.usc.citius.composit.wsc08.data.knowledge.WSCXMLKnowledgeBase;
import es.usc.citius.composit.wsc08.data.util.OWLTransformer;
import uk.ac.open.kmi.iserve.api.iServeEngine;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.msm4j.Service;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.jadler.Jadler.*;

public class WSCImportUtils {

    public static void importDataset(iServeEngine engine, URL ontologyUrl, WSCTest test, boolean force) throws Exception {
        int services = engine.getRegistryManager().getServiceManager().listServices().size();
        int concepts = engine.getRegistryManager().getKnowledgeBaseManager().listConcepts(ontologyUrl.toURI()).size();
        // TODO: Add #services and #concepts to WSCTest
        if (force || services != test.getServices() || concepts != test.getConcepts()) {
            // Clear registry
            engine.getRegistryManager().clearRegistry();
            WSCImportUtils.importServices(
                    test.openServicesStream(),
                    ontologyUrl,
                    test.createKnowledgeBase(),
                    engine.getRegistryManager().getServiceManager());
        }
    }



    public static Map<URI, Service> importServices(InputStream servicesStream, URL ontoUrl, final WSCXMLKnowledgeBase kb, ServiceManager mgr) throws Exception {
        // Export kb to OWL
        String ontoOwl = OWLTransformer.convertKbToOwl(kb, ontoUrl.toString());
        // Transform the services to iserve model and import
        InstanceResolver resolver = new InstanceResolver() {
            @Override
            public String resolveToConcept(String s) {
                return kb.resolveInstance(kb.getInstance(s)).getID();
            }
        };
        List<Service> services = new WSCTransformer().transform(servicesStream, ontoUrl, resolver);
        System.out.println("OK");

        try {
            initJadlerListeningOn(ontoUrl.getPort());
            onRequest()
                    .havingMethodEqualTo("GET")
                    .havingPathEqualTo(ontoUrl.getPath())
                    .respond()
                    .withBody(ontoOwl)
                    .withContentType("application/rdf+xml; charset=UTF-8");

            // Import services
            Map<URI, Service> mapping = new HashMap<URI, Service>();
            for (Service s : services) {
                mapping.put(mgr.addService(s), s);
            }
            return mapping;
        } finally {
            closeJadler();
        }
    }
}
