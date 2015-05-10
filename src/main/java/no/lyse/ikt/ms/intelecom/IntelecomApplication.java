package no.lyse.ikt.ms.intelecom;

import no.lyse.ikt.ms.intelecom.schemas.Authenticate;
import no.lyse.ikt.ms.intelecom.schemas.InboundRequestAddRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.integration.ws.MarshallingWebServiceInboundGateway;
import org.springframework.integration.ws.MarshallingWebServiceOutboundGateway;
import org.springframework.messaging.Message;

@SpringBootApplication
public class IntelecomApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntelecomApplication.class, args);
    }

    @Autowired
    MarshallingWebServiceInboundGateway entryPoint;

    @Autowired
    GenericTransformer<Object,Object> requestAddTransformer;

    @Bean
    IntegrationFlow flow() {
        return IntegrationFlows.from(entryPoint)
                .enrich(e -> e.requestChannel("authenticateChannel"))
                .transform(requestAddTransformer)
                .get();
    }

    @Autowired
    GenericTransformer<Message<InboundRequestAddRequest>, Message<Authenticate>> authenticateTransformer;

    @Autowired
    MarshallingWebServiceOutboundGateway authenticateGateway;

    @Bean
    IntegrationFlow authenticateFlow() {
        return IntegrationFlows.from("authenticateChannel")
                .transform(authenticateTransformer)
                .handle(authenticateGateway)
                .get();
    }
}
