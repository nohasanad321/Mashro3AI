package com.mashro3ai.ai.provider;

import com.mashro3ai.ai.application.OrderExtractionPort;
import com.mashro3ai.ai.model.*;
import com.mashro3ai.catalog.CatalogService;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.*;
import org.springframework.stereotype.Component;

/** Offline lexical retrieval plus explicit extraction rules. No language model or generation. */
@Component
public class LocalRetrievalOrderExtractor implements OrderExtractionPort {
    private final CatalogService catalog;
    public LocalRetrievalOrderExtractor(CatalogService catalog) { this.catalog = catalog; }
    public record Status(boolean ready, String model, String message) {}

    public static String normalize(String text) {
        String s=text.toLowerCase(Locale.ROOT).replaceAll("[\\p{M}ـ]", "").replaceAll("[أإآ]", "ا");
        for(int i=0;i<10;i++) s=s.replace((char)('٠'+i),(char)('0'+i)).replace((char)('۰'+i),(char)('0'+i));
        return s;
    }
    private static String capture(String text,String expression) {
        var m=Pattern.compile(expression,Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE).matcher(text);
        return m.find()?m.group(1).strip():null;
    }
    @Override public OrderExtractionResult extract(String message, ExtractionContext context) {
        String text=normalize(message);
        var items=new ArrayList<OrderExtractionResult.ItemCandidate>();
        for(var product:catalog.products()) {
            int start=-1,end=-1;
            for(String alias:product.names()) {
                var match=Pattern.compile("(?<![\\p{L}])"+Pattern.quote(normalize(alias))+"(?![\\p{L}])").matcher(text);
                if(match.find() && (start<0||match.start()<start||match.start()==start&&match.end()>end)) {start=match.start();end=match.end();}
            }
            if(start<0) continue;
            String before=text.substring(Math.max(0,start-35),start);
            String quantity=capture(before,"(\\d+|one|two|three|four|five|علبتين|علبة|علب|اثنين|ثلاث|ثلاثة)\\s*(?:boxes?\\s+(?:of\\s+)?|علب(?:ة)?\\s*)?$");
            Integer q=null;
            if(quantity!=null) q=switch(quantity){case "one","علبة"->1;case "two","علبتين","اثنين"->2;case "three","ثلاث","ثلاثة"->3;case "four"->4;case "five"->5;default->quantity.matches("\\d+")?Integer.valueOf(quantity):null;};
            items.add(new OrderExtractionResult.ItemCandidate(product.name(),q,null,null,product.sku()));
        }
        String name=capture(message,"(?:name\\s*:\\s*|I am\\s+|I'm\\s+|أنا\\s+|انا\\s+)([^.,،\\n]+)");
        String phone=capture(text,"(?<!\\d)(\\+?965[ -]?\\d{8}|[569]\\d{7})(?!\\d)");
        String area=capture(message,"(?:area\\s*:\\s*|to\\s+|ل)(Salmiya|Hawally|Jabriya|السالمية|سالمية|حولي|الجابرية)");
        String address=capture(message,"(?:address|عنوان)\\s*:\\s*([^\\n]+)");
        String method=null;
        if(Pattern.compile("pickup|pick up|collection|استلام").matcher(text).find())method="PICKUP";
        else if(Pattern.compile("delivery|delivered|توصيل").matcher(text).find())method="DELIVERY";
        String dateText=capture(text,"(\\d{4}-\\d{2}-\\d{2}|tomorrow|بكرة|بكره|غدا|today|اليوم)");
        LocalDate date=null;
        if(dateText!=null) {
            LocalDate today=context.referenceTimestamp().atZone(context.businessTimezone()).toLocalDate();
            date=dateText.matches("\\d{4}-.*")?LocalDate.parse(dateText):Set.of("tomorrow","بكرة","بكره","غدا").contains(dateText)?today.plusDays(1):today;
        }
        return new OrderExtractionResult(new OrderExtractionResult.CustomerCandidate(name,phone,null),
            new OrderExtractionResult.DeliveryCandidate(method,area,address,date,dateText,null),items,
            List.of(new OrderExtractionResult.Warning("LOCAL_RETRIEVAL","sourceMessage","Offline catalogue alias retrieval and extraction rules, not generative AI. Review quantities and unmatched requests against the source.")));
    }
}
