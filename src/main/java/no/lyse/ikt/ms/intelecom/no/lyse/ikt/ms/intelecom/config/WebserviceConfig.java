package no.lyse.ikt.ms.intelecom.no.lyse.ikt.ms.intelecom.config;


import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

/**
 * Created by henrikwl on 10.05.15.
 */

@EnableWs
@Configuration
public class WebserviceConfig {

    @Bean
    public ServletRegistrationBean messageDispatcherServlet(ApplicationContext context) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(context);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean(servlet, "/intelecom/*");
    }

    @Bean(name = "intelecomrequestadd")
    DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema requestSchema) {
        DefaultWsdl11Definition definition = new DefaultWsdl11Definition();
        definition.setPortTypeName("IntelecomRequestAdd");
        definition.setLocationUri("/intelecom");
        definition.setTargetNamespace("http://www.example.org/IntelecomRequestAdd");
        definition.setSchema(requestSchema);
        return definition;
    }

    @Bean
    XsdSchema requestSchema() {
        return new SimpleXsdSchema(new ClassPathResource("XSD/IntelecomRequestAdd.xsd"));
    }
}
