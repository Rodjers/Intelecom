package no.lyse.ikt.ms.intelecom;

import no.lyse.ikt.ms.intelecom.schemas.Authenticate;
import no.lyse.ikt.ms.intelecom.schemas.AuthenticateResponse;
import no.lyse.ikt.ms.intelecom.schemas.InboundRequestAddRequest;
import no.lyse.ikt.ms.intelecom.schemas.RequestAdd;
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
    GenericTransformer<Map<String,String>,Message<RequestAdd>> requestAddTransformer;

    @Bean
    IntegrationFlow flow() {
        return IntegrationFlows.from(entryPoint)
                .transform(new ObjectToMapTransformer())
                .enrich(e -> e.requestChannel("authenticateChannel")
                                .propertyExpression("accesstoken", "payload.authenticateResult"))
                .transform(requestAddTransformer)
                .get();
    }

    @Autowired
    GenericTransformer<Message<Map<String,String>>, Message<Authenticate>> authenticateTransformer;

    @Autowired
    MarshallingWebServiceOutboundGateway authenticateGateway;

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
