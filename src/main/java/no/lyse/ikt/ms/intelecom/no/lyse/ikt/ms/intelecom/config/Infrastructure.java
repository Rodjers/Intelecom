package no.lyse.ikt.ms.intelecom.no.lyse.ikt.ms.intelecom.config;

import no.lyse.ikt.ms.intelecom.schemas.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.ContentEnricher;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.integration.ws.MarshallingWebServiceInboundGateway;
import org.springframework.integration.ws.MarshallingWebServiceOutboundGateway;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.config.annotation.EnableWs;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
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
    MarshallingWebServiceOutboundGateway requestAddGateway() {
        MarshallingWebServiceOutboundGateway requestAddGateway = new MarshallingWebServiceOutboundGateway(
                "https://api.intele.com/Connect/ContactCentre/Agent.svc", jaxb2Marshaller(), jaxb2Marshaller()
        );

        return requestAddGateway;
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
    GenericTransformer<Message<Map<String,String>>, Message<RequestAdd>> requestAddTransformer() {
        return new GenericTransformer<Message<Map<String,String>>, Message<RequestAdd>>() {
            @Override
            public Message<RequestAdd> transform(Message<Map<String,String>> messageSource) {

                Map<String,String> source = messageSource.getPayload();
                ObjectFactory factory = new ObjectFactory();
                RequestAdd requestAdd = factory.createRequestAdd();

                requestAdd.setAccessToken(factory.createRequestAddAccessToken(source.get("accesstoken")));
                requestAdd.setCustomerKey(factory.createRequestAddCustomerKey(source.get("customerKey")));

                StringBuilder sb = new StringBuilder();

                sb.append("<info>");
                sb.append("<from>" + source.get("from") + "</from>");
                sb.append("<to>" + source.get("to") + "</to>");
                sb.append("<sentTime>" + source.get("sentTime") + "</sentTime>");
                sb.append("<receivedTime>" + source.get("receivedTime") + "</receivedTime>");
                sb.append("<subject>" + source.get("subject") + "</subject>");
                sb.append("<uri>" + source.get("uri") + "</uri>");
                sb.append("</info>");

                requestAdd.setInfoXml(factory.createRequestAddInfoXml(sb.toString()));

                MessageBuilder mb = MessageBuilder.withPayload(requestAdd);
                mb.setHeader("RequestId", source.get("requestId"));
                mb.setHeader("ws_soapAction", "ContactCentreWebServices/IAgent/RequestAdd");

                return mb.copyHeadersIfAbsent(messageSource.getHeaders()).build();
            }
        };
    }

    @Bean
    GenericTransformer<Object, Object> debugTransformer() {
        return new GenericTransformer<Object, Object>() {
            @Override
            public Object transform(Object source) {

                ObjectFactory factory = new ObjectFactory();

                InboundRequestAddResponse inboundRequestAddResponse = factory.createInboundRequestAddResponse();
                JAXBElement<ResponseType> jaxbResponseType = inboundRequestAddResponse.getRequestAddResult();

                ResponseType responseType = jaxbResponseType.getValue();

        //        responseType.setCode(source.getPayload().getRequestAddResult().getValue().getResult());
     //           responseType.setRequestId((String) source.getHeaders().get("RequestId"));

                jaxbResponseType.setValue(responseType);
                inboundRequestAddResponse.setRequestAddResult(jaxbResponseType);

                MessageBuilder<InboundRequestAddResponse> mb = MessageBuilder.withPayload(inboundRequestAddResponse);

                return mb.build();
            }
        };
    }

    @Bean
    GenericTransformer<Message<RequestAddResponse>,Message<InboundRequestAddResponse>> requestAddReplyTransformer() {
        return new GenericTransformer<Message<RequestAddResponse>, Message<InboundRequestAddResponse>>() {
            @Override
            public Message<InboundRequestAddResponse> transform(Message<RequestAddResponse> source) {

                ObjectFactory factory = new ObjectFactory();

                InboundRequestAddResponse inboundRequestAddResponse = factory.createInboundRequestAddResponse();

                ResponseType responseType = factory.createResponseType();
                responseType.setCode(source.getPayload().getRequestAddResult().getValue().getCode());
                responseType.setResult(source.getPayload().getRequestAddResult().getValue().getResult());
                responseType.setMessage(source.getPayload().getRequestAddResult().getValue().getMessage().getValue());
                responseType.setRequestId((String) source.getHeaders().get("RequestId"));
                responseType.setId(source.getPayload().getRequestAddResult().getValue().getId().getValue());

                QName responsTypeQName = new QName("http://www.example.org/IntelecomRequestAdd", "ResponseType");
                JAXBElement<ResponseType> responseTypeJAXBElement = new JAXBElement<ResponseType>(responsTypeQName, ResponseType.class, responseType);

                inboundRequestAddResponse.setRequestAddResult(responseTypeJAXBElement);

                MessageBuilder<InboundRequestAddResponse> mb = MessageBuilder.withPayload(inboundRequestAddResponse);

                return mb.copyHeadersIfAbsent(source.getHeaders()).build();
            }
        };
    }
}
