package com.mashro3ai.ai.provider;

import com.mashro3ai.ai.application.OrderExtractionPort;
import com.mashro3ai.ai.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Deterministic fixture adapter. Accepts labelled lines, not arbitrary natural language. */
@Component
@Profile({"local", "demo"})
public class LocalDemoOrderParser implements OrderExtractionPort {
    @Override public OrderExtractionResult extract(String message, ExtractionContext context) {
        Map<String,String> fields=new HashMap<>();
        List<OrderExtractionResult.ItemCandidate> items=new ArrayList<>();
        for(String line:message.split("\\R")) {
            int colon=line.indexOf(':'); if(colon<0) continue;
            var key=line.substring(0,colon).strip().toLowerCase(Locale.ROOT);
            var value=line.substring(colon+1).strip();
            if(key.equals("item")) {
                var parts=value.split("\\|",-1);
                if(parts.length!=3) throw new IllegalArgumentException("Item format: description | quantity | unit price");
                items.add(new OrderExtractionResult.ItemCandidate(parts[0].strip(),parts[1].isBlank()?null:Integer.valueOf(parts[1].strip()),parts[2].isBlank()?null:new BigDecimal(parts[2].strip()),null));
            } else fields.put(key,value);
        }
        String method=fields.get("method");
        return new OrderExtractionResult(
            new OrderExtractionResult.CustomerCandidate(fields.get("name"),fields.get("phone"),fields.get("email")),
            new OrderExtractionResult.DeliveryCandidate(method==null?null:method.toUpperCase(Locale.ROOT),fields.get("area"),fields.get("address"),fields.containsKey("date")&&!fields.get("date").isBlank()?LocalDate.parse(fields.get("date")):null,fields.get("date"),null),
            items,List.of(new OrderExtractionResult.Warning("DEMO_PARSER","sourceMessage","Structured demo parser, not an AI model")));
    }
}
