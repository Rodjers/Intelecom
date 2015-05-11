package no.lyse.ikt.ms.intelecom.no.lyse.ikt.ms.intelecom.config;

import no.lyse.ikt.ms.intelecom.schemas.*;
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
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.config.annotation.EnableWs;

import java.util.HashMap;
import java.util.Map;

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
    GenericTransformer<Message<Map<String,String>>, Message<Authenticate>> authenticateTransformer() {
        return new GenericTransformer<Message<Map<String, String>>, Message<Authenticate>>() {
            @Override
            public Message<Authenticate> transform(Message<Map<String, String>> source) {
                ObjectFactory factory = new ObjectFactory();
                Authenticate authenticate = factory.createAuthenticate();
                Map<String, String> payload = source.getPayload();

                authenticate.setCustomerKey(factory.createAuthenticateCustomerKey(payload.get("customerKey")));
                authenticate.setUserName(factory.createAuthenticateUserName(payload.get("userName")));
                authenticate.setPassword(factory.createAuthenticatePassword(payload.get("password")));

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
    GenericTransformer<AuthenticateResponse,Map<String,String>> authenticateReplyTransformer() {
        return new GenericTransformer<AuthenticateResponse, Map<String, String>>() {
            @Override
            public Map<String, String> transform(AuthenticateResponse source) {
                Map<String,String> result = new HashMap<>();
                result.put("authenticateResult", source.getAuthenticateResult().getValue().getResult().getValue());

                return result;
            }
        };
    }

    @Bean
    GenericTransformer<Map<String,String>, Message<RequestAdd>> requestAddTransformer() {
        return new GenericTransformer<Map<String,String>, Message<RequestAdd>>() {
            @Override
            public Message<RequestAdd> transform(Map<String,String> source) {
                ObjectFactory factory = new ObjectFactory();
                RequestAdd requestAdd = factory.createRequestAdd();

                requestAdd.setAccessToken(factory.createRequestAddAccessToken(source.get("accesstoken")));
                requestAdd.setCustomerKey(factory.createRequestAddCustomerKey(source.get("customerKey")));

                InfoType infoXml = factory.createInfoType();
                infoXml.setFrom(source.get("from"));
                infoXml.setReceivedTime(source.get("receivedTime"));
                infoXml.setSentTime(source.get("sentTime"));
                infoXml.setSubject(source.get("subject"));
                infoXml.setTo(source.get("to"));
                infoXml.setUri("uri");


                return null;
            }
        };
    }
}
