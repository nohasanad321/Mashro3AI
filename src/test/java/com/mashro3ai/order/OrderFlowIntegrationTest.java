package com.mashro3ai.order;

import com.mashro3ai.order.application.*;
import com.mashro3ai.order.api.dto.*;
import com.mashro3ai.order.domain.*;
import com.mashro3ai.order.persistence.OrderDraftRepository;
import com.mashro3ai.business.CurrentBusinessProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties={"spring.datasource.url=jdbc:h2:mem:orders;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "spring.datasource.driver-class-name=org.h2.Driver","spring.datasource.username=sa","spring.datasource.password=","spring.flyway.enabled=true"})
@ActiveProfiles("local")
class OrderFlowIntegrationTest {
    @org.springframework.boot.test.web.server.LocalServerPort int port;
    @Autowired OrderDraftService service;
    @Autowired DemoExtractionService extraction;
    @Autowired OrderDraftRepository repository;
    @Autowired com.mashro3ai.order.persistence.ConfirmedOrderRepository orders;
    @Test void httpValidationAndDemoPage() throws Exception {
        var client=java.net.http.HttpClient.newHttpClient();
        var home=client.send(java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://localhost:"+port+"/")).build(),java.net.http.HttpResponse.BodyHandlers.ofString());
        assertThat(home.statusCode()).isEqualTo(200);
        assertThat(home.body()).contains("Your next order, organized.");
        var invalid=client.send(java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://localhost:"+port+"/api/v1/order-drafts"))
            .header("Content-Type","application/json").POST(java.net.http.HttpRequest.BodyPublishers.ofString("{}" )).build(),java.net.http.HttpResponse.BodyHandlers.ofString());
        assertThat(invalid.statusCode()).isEqualTo(400);
        assertThat(invalid.body()).contains("errors");
    }
    @Test void extractReviewPersistConfirmAndReplay() {
        var draft=extraction.extract(new ExtractOrderDraftRequest("Name: Demo\nPhone: 55500000\nMethod: DELIVERY\nArea: Salmiya\nDate: 2026-11-01\nItem: Cookie box | 2 | 4.500",null));
        assertThat(draft.missingFields()).extracting(MissingFieldDto::path).containsExactly("delivery.address");
        assertThatThrownBy(()->service.confirm(draft.id(),new ConfirmOrderRequest(draft.version()))).isInstanceOf(ErrorResponseException.class).satisfies(e->assertThat(((ErrorResponseException)e).getStatusCode().value()).isEqualTo(422));
        var updated=service.update(draft.id(),new SaveOrderDraftRequest(draft.version(),draft.customer(),new DeliveryDetailsDto(DeliveryMethod.DELIVERY,"Salmiya","Demo block 1",LocalDate.of(2026,11,1),null,null),draft.items(),draft.currency(),"Reviewed"));
        assertThat(updated.version()).isGreaterThan(draft.version());
        assertThat(service.get(draft.id()).delivery().address()).isEqualTo("Demo block 1");
        assertThatThrownBy(()->service.update(draft.id(),new SaveOrderDraftRequest(draft.version(),draft.customer(),draft.delivery(),draft.items(),draft.currency(),null))).isInstanceOf(ResponseStatusException.class).satisfies(e->assertThat(((ResponseStatusException)e).getStatusCode().value()).isEqualTo(409));
        var confirmed=service.confirm(draft.id(),new ConfirmOrderRequest(updated.version()));
        assertThat(confirmed.created()).isTrue();
        assertThat(confirmed.order().total()).isEqualByComparingTo("9.000");
        var replay=service.confirm(draft.id(),new ConfirmOrderRequest(draft.version()));
        assertThat(replay.created()).isFalse();
        assertThat(replay.order().id()).isEqualTo(confirmed.order().id());
        assertThat(service.order(confirmed.order().id()).items()).hasSize(1);
        assertThatThrownBy(()->service.update(draft.id(),new SaveOrderDraftRequest(updated.version(),null,null,List.of(),"KWD",null))).isInstanceOf(ResponseStatusException.class);
        assertThat(service.list(0,20).content()).extracting(OrderResponse::id).contains(confirmed.order().id());
    }
    @Test void anotherBusinessCannotReadOrModifyDraft() {
        var d=service.create(new CreateOrderDraftRequest(null,null,List.of(),"KWD",null));
        var other=new OrderDraftService(repository,orders,UUID::randomUUID);
        assertThatThrownBy(()->other.get(d.id())).isInstanceOf(ResponseStatusException.class).satisfies(e->assertThat(((ResponseStatusException)e).getStatusCode().value()).isEqualTo(404));
    }
    @Test void invalidParserOutputIsRejectedBeforePersistence() {
        assertThatThrownBy(()->extraction.extract(new ExtractOrderDraftRequest("Item: Cookie box | -1 | 4.500",null))).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(()->extraction.extract(new ExtractOrderDraftRequest("Date: Thursday",null))).isInstanceOf(ResponseStatusException.class);
    }
}
