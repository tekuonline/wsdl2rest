package org.jboss.fuse.wsdl2rest.impl;

import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.apache.cxf.tools.common.model.JavaModel;
import org.jboss.fuse.wsdl2rest.EndpointInfo;
import org.jboss.fuse.wsdl2rest.ResourceMapper;
import org.jboss.fuse.wsdl2rest.WSDLProcessor;
import org.jboss.fuse.wsdl2rest.impl.codegen.CamelContextGenerator;
import org.jboss.fuse.wsdl2rest.impl.codegen.JavaTypeGenerator;
import org.jboss.fuse.wsdl2rest.util.IllegalArgumentAssertion;

public class Wsdl2Rest {

    private final URL wsdlUrl;
    private final Path outpath;

    private URL targetAddress;
    private Path targetContext;
    
    public Wsdl2Rest(URL wsdlUrl, Path outpath) {
        IllegalArgumentAssertion.assertNotNull(wsdlUrl, "wsdlUrl");
        IllegalArgumentAssertion.assertNotNull(outpath, "outpath");
        this.wsdlUrl = wsdlUrl;
        this.outpath = outpath;
    }

    public void setTargetAddress(URL targetAddress) {
        this.targetAddress = targetAddress;
    }

    public void setTargetContext(Path targetContext) {
        this.targetContext = targetContext;
    }

    public List<EndpointInfo> process() throws Exception {
        
        WSDLProcessor wsdlProcessor = new WSDLProcessorImpl();
        wsdlProcessor.process(wsdlUrl);
        
        List<EndpointInfo> clazzDefs = wsdlProcessor.getClassDefinitions();
        ResourceMapper resMapper = new ResourceMapperImpl();
        resMapper.assignResources(clazzDefs);

        JavaTypeGenerator typeGen = new JavaTypeGenerator(outpath, wsdlUrl);
        JavaModel javaModel = typeGen.execute();
        
        if (targetContext != null) {
            CamelContextGenerator camelGen = new CamelContextGenerator(outpath);
            camelGen.setTargetContext(targetContext);
            camelGen.setTargetAddress(targetAddress);
            camelGen.process(clazzDefs, javaModel);
        }
        
        return Collections.unmodifiableList(clazzDefs);
    }
}
