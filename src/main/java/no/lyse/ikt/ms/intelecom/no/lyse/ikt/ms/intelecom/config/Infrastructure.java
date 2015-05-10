package no.lyse.ikt.ms.intelecom.no.lyse.ikt.ms.intelecom.config;

import no.lyse.ikt.ms.intelecom.schemas.Authenticate;
import no.lyse.ikt.ms.intelecom.schemas.InboundRequestAddRequest;
import no.lyse.ikt.ms.intelecom.schemas.ObjectFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.integration.ws.MarshallingWebServiceInboundGateway;
import org.springframework.integration.ws.MarshallingWebServiceOutboundGateway;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.config.annotation.EnableWs;

/**
 * Created by henrikwl on 09.05.15.
 */

@EnableWs
@Configuration
@ComponentScan
@IntegrationComponentScan
public class Infrastructure {

    @Bean
    MarshallingWebServiceInboundGateway entryPoint() {
        MarshallingWebServiceInboundGateway entryPoint = new MarshallingWebServiceInboundGateway(jaxb2Marshaller());
        return entryPoint;
    }

    @Bean
    Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setContextPath("no.lyse.ikt.ms.intelecom.schemas");
        return jaxb2Marshaller;
    }

    @Bean
    GenericTransformer<Message<InboundRequestAddRequest>, Message<Authenticate>> authenticateTransformer() {
        return new GenericTransformer<Message<InboundRequestAddRequest>, Message<Authenticate>>() {
            @Override
            public Message<Authenticate> transform(Message<InboundRequestAddRequest> source) {
                ObjectFactory factory = new ObjectFactory();
                Authenticate authenticate = factory.createAuthenticate();

                authenticate.setCustomerKey(factory.createAuthenticateCustomerKey(source.getPayload().getCustomerKey()));
                authenticate.setPassword(factory.createAuthenticatePassword(source.getPayload().getPassword()));
                authenticate.setUserName(factory.createAuthenticateUserName(source.getPayload().getUserName()));

                MessageBuilder<Authenticate> mb = MessageBuilder.withPayload(authenticate);
                mb.setHeader("ws_soapAction", "ContactCentreWebServices/IAgent/Authenticate");

                return mb.copyHeadersIfAbsent(source.getHeaders()).build();
            }
        };
    }

    @Bean(name = "authenticateChannel")
    MessageChannel authenticateChannel() {
        return new DirectChannel();
    }

    @Bean
    MarshallingWebServiceOutboundGateway authenticateGateway() {
        MarshallingWebServiceOutboundGateway authenticateGateway = new MarshallingWebServiceOutboundGateway(
                "https://api.intele.com/Connect/ContactCentre/Agent.svc", jaxb2Marshaller(), jaxb2Marshaller()
        );

        return authenticateGateway;
    }

    @Bean
    GenericTransformer<Object, Object> requestAddTransformer() {
        return new GenericTransformer<Object, Object>() {
            @Override
            public Object transform(Object source) {
                return null;
            }
        };
    }
}
