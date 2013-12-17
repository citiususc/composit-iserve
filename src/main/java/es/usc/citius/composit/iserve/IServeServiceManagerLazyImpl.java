//package es.usc.citius.composit.iserve;
//
//
//import uk.ac.open.kmi.iserve.sal.exception.SalException;
//import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
//
//import java.net.URI;
//import java.util.HashSet;
//import java.util.Set;
//
///**
// * @author Pablo Rodríguez Mier
// */
//
//public class IServeServiceManagerLazyImpl implements OperationManager {
//    private final ServiceManager delegatedManager;
//    private Set<ServiceModel> modelServices = null;
//
//    public IServeServiceManagerLazyImpl(ServiceManager manager) {
//        this.delegatedManager = manager;
//    }
//
//    @Override
//    public Set<ServiceModel> getServices() {
//        if (modelServices!=null){
//            return modelServices;
//        }
//
//        modelServices = new HashSet<ServiceModel>();
//
//        Set<URI> serviceUris = delegatedManager.listServices();
//        // Convert services
//        for (final URI serviceUri : serviceUris) {
//            modelServices.add(getService(serviceUri));
//        }
//
//        return modelServices;
//    }
//
//    @Override
//    public ServiceModel getService(final URI serviceUri) {
//        return new AbstractService(serviceUri) {
//            @Override
//            public Set<OperationModel> getOperations() {
//                Set<OperationModel> operations = new HashSet<OperationModel>();
//
//                for (final URI opUri : delegatedManager.listOperations(serviceUri)) {
//                    operations.add(new AbstractOperation(opUri) {
//                        @Override
//                        public Set<Resource> getInputs() {
//                            Set<URI> inputs = null;
//                            try {
//                                inputs = loadInputs(opUri);
//                            } catch (SalException e) {
//                                e.printStackTrace();
//                            }
//                            return Resources.fromURIs(inputs);
//                        }
//
//                        @Override
//                        public Set<Resource> getOutputs() {
//                            Set<URI> outputs = null;
//                            try {
//                                outputs = loadOutputs(opUri);
//                            } catch (SalException e) {
//                                e.printStackTrace();
//                            }
//                            return Resources.fromURIs(outputs);
//                        }
//                    });
//                }
//
//                return operations;
//            }
//
//        };
//    }
//
//    private Set<URI> loadInputs(URI operation) throws SalException {
//        Set<URI> models = new HashSet<URI>();
//        for (URI input : delegatedManager.listInputs(operation)) {
//            for (URI p : delegatedManager.listMandatoryParts(input)) {
//                models.addAll(delegatedManager.listModelReferences(p));
//            }
//        }
//        return models;
//    }
//
//    private Set<URI> loadOutputs(URI operation) throws SalException {
//        Set<URI> models = new HashSet<URI>();
//        for (URI output : delegatedManager.listOutputs(operation)) {
//            for (URI p : delegatedManager.listMandatoryParts(output)) {
//                models.addAll(delegatedManager.listModelReferences(p));
//            }
//        }
//        return models;
//    }
//}
