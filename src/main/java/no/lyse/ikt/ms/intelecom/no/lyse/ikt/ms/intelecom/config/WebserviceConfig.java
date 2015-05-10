package no.lyse.ikt.ms.intelecom.no.lyse.ikt.ms.intelecom.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.ws.MarshallingWebServiceInboundGateway;
import org.springframework.messaging.MessageHandler;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.server.endpoint.PayloadEndpoint;
import org.springframework.ws.server.endpoint.adapter.MessageEndpointAdapter;
import org.springframework.ws.server.endpoint.mapping.PayloadRootAnnotationMethodEndpointMapping;
import org.springframework.ws.server.endpoint.mapping.UriEndpointMapping;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

/**
 * Created by henrikwl on 10.05.15.
 */

@EnableWs
@Configuration
@ComponentScan
public class WebserviceConfig {

    @Bean
    public ServletRegistrationBean messageDispatcherServlet(ApplicationContext context) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(context);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean(servlet, "/intelecom/*");
    }

    @Bean(name = "intelecomrequestadd")
    SimpleWsdl11Definition simpleWsdl11Definition() {
        SimpleWsdl11Definition definition = new SimpleWsdl11Definition(new ClassPathResource("WSDL/IntelecomRequestAdd.wsdl"));
        /*definition.setPortTypeName("IntelecomRequestAdd");
        definition.setLocationUri("/intelecom");
        definition.setTargetNamespace("http://www.example.org/IntelecomRequestAdd");

        definition.setSchema(requestSchema);*/
        return definition;
    }

    @Autowired
    MarshallingWebServiceInboundGateway entryPoint;

    @Bean
    UriEndpointMapping uriEndpointMapping() {
        UriEndpointMapping uriEndpointMapping = new UriEndpointMapping();
        uriEndpointMapping.setDefaultEndpoint(entryPoint);
        return uriEndpointMapping;
    }

    @Bean
    MessageEndpointAdapter messageEndpointAdapter() {
        MessageEndpointAdapter adapter = new MessageEndpointAdapter();
        return adapter;
    }

    @Bean
    XsdSchema requestSchema() {
        return new SimpleXsdSchema(new ClassPathResource("XSD/IntelecomRequestAdd.xsd"));
    }
}
