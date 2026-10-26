package com.mashro3ai.ai;
import com.mashro3ai.ai.provider.LocalRetrievalOrderExtractor;
import com.mashro3ai.ai.model.ExtractionContext;
import com.mashro3ai.catalog.CatalogService;
import com.mashro3ai.knowledge.KnowledgeController;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.*;
class LocalRetrievalTest {
 @Test void bilingualCatalogueRetrieval() throws Exception {
  var e=new LocalRetrievalOrderExtractor(new CatalogService(new JsonMapper()));
  var c=new ExtractionContext(UUID.randomUUID(),Instant.parse("2026-10-26T08:00:00Z"),ZoneId.of("Asia/Kuwait"),Currency.getInstance("KWD"));
  var en=e.extract("I am Sara. 2 cookie boxes and 1 brownie box for delivery tomorrow to Salmiya. 55500000",c);
  assertThat(en.items()).hasSize(2);assertThat(en.items().getFirst().quantity()).isEqualTo(2);
  assertThat(en.delivery().requestedDate()).isEqualTo(LocalDate.of(2026,10,27));
  var ar=e.extract("أنا سارة. أبي علبتين كوكيز، توصيل بكرة للسالمية. رقمي ٥٥٥٠٠٠٠٠",c);
  assertThat(ar.items().getFirst().quantity()).isEqualTo(2);assertThat(ar.customer().phone()).isEqualTo("55500000");
  assertThat(e.extract("cookies please",c).items().getFirst().quantity()).isNull();
 }
 @Test void knowledgeCitesSourcesAndAbstains() throws Exception {
  var k=new KnowledgeController(new JsonMapper());
  assertThat(k.search("delivery address").sources()).extracting(KnowledgeController.Passage::id).contains("delivery");
  assertThat(k.search("astronomy").sources()).isEmpty();
 }
}
