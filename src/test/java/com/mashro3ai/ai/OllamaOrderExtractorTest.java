package com.mashro3ai.ai;

import com.mashro3ai.ai.model.ExtractionContext;
import com.mashro3ai.ai.provider.OllamaOrderExtractor;
import com.mashro3ai.catalog.CatalogService;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.*;

class OllamaOrderExtractorTest {
    @Test void sendsSchemaAndMapsRealProviderContract() throws Exception {
        var mapper = new JsonMapper();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var captured = new java.util.concurrent.atomic.AtomicReference<String>();
        String content = """
            {"customer":{"name":"سارة","phone":"55500000","email":null},
            "delivery":{"method":"DELIVERY","area":"السالمية","address":null,"requestedDate":"2026-10-27","originalDateText":"بكرة","notes":null},
            "items":[{"sku":"COOKIE-BOX","description":"كوكيز","quantity":2,"notes":null}]}
            """;
        server.createContext("/api/chat", exchange -> {
            captured.set(new String(exchange.getRequestBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
            byte[] body=mapper.writeValueAsBytes(Map.of("done",true,"message",Map.of("content",content)));
            exchange.sendResponseHeaders(200,body.length);exchange.getResponseBody().write(body);exchange.close();
        });
        server.start();
        try {
            var extractor=new OllamaOrderExtractor(mapper,new CatalogService(mapper),"http://127.0.0.1:"+server.getAddress().getPort(),"test-model",5);
            var result=extractor.extract("أبي علبتين كوكيز",new ExtractionContext(UUID.randomUUID(),Instant.parse("2026-10-26T08:00:00Z"),ZoneId.of("Asia/Kuwait"),Currency.getInstance("KWD")));
            assertThat(result.customer().name()).isEqualTo("سارة");
            assertThat(result.items().getFirst().sku()).isEqualTo("COOKIE-BOX");
            assertThat(result.items().getFirst().unitPrice()).isNull();
            assertThat(result.delivery().requestedDate()).isEqualTo(LocalDate.of(2026,10,27));
            assertThat(mapper.readTree(captured.get()).path("format").path("type").asText()).isEqualTo("object");
            assertThat(captured.get()).contains("2026-10-26");
        } finally { server.stop(0); }
    }
    @Test void unavailableModelNeverFallsBackToDemo() throws Exception {
        var mapper=new JsonMapper();
        var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/api/chat",e->{e.sendResponseHeaders(404,-1);e.close();});server.start();
        try {
            var extractor=new OllamaOrderExtractor(mapper,new CatalogService(mapper),"http://127.0.0.1:"+server.getAddress().getPort(),"missing",5);
            assertThatThrownBy(()->extractor.extract("test",new ExtractionContext(UUID.randomUUID(),Instant.now(),ZoneId.of("Asia/Kuwait"),Currency.getInstance("KWD"))))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class).hasMessageContaining("503");
        } finally {server.stop(0);}
    }
}
