package no.lyse.ikt.ms.intelecom;

import no.lyse.ikt.ms.intelecom.schemas.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.integration.transformer.ObjectToMapTransformer;
import org.springframework.integration.ws.MarshallingWebServiceInboundGateway;
import org.springframework.integration.ws.MarshallingWebServiceOutboundGateway;
import org.springframework.messaging.Message;

import java.util.Map;

@SpringBootApplication
public class IntelecomApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntelecomApplication.class, args);
    }

    @Autowired
    MarshallingWebServiceInboundGateway entryPoint;

    @Autowired
    GenericTransformer<Message<Map<String,String>>,Message<RequestAdd>> requestAddTransformer;

    @Autowired
    GenericTransformer<Message<RequestAddResponse>,Message<InboundRequestAddResponse>> requestAddReplyTransformer;

    @Bean
    IntegrationFlow flow() {
        return IntegrationFlows.from(entryPoint)
                .transform(new ObjectToMapTransformer())
                .enrich(e -> e.requestChannel("authenticateChannel")
                                .propertyExpression("accesstoken", "payload.authenticateResult"))
                .transform(requestAddTransformer)
                .handle(requestAddGateway)
                .transform(requestAddReplyTransformer)
                .get();
    }

    @Autowired
    GenericTransformer<Message<Map<String,String>>, Message<Authenticate>> authenticateTransformer;

    @Autowired
    MarshallingWebServiceOutboundGateway authenticateGateway;

    @Autowired
    MarshallingWebServiceOutboundGateway requestAddGateway;

    @Autowired
    GenericTransformer<AuthenticateResponse, Map<String,String>> authenticateReplyTransformer;

    @Bean
    IntegrationFlow authenticateFlow() {
        return IntegrationFlows.from("authenticateChannel")
                .transform(authenticateTransformer)
                .handle(authenticateGateway)
                .transform(authenticateReplyTransformer)
                .get();
    }
}
