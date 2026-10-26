package com.mashro3ai.knowledge;
import com.mashro3ai.ai.provider.LocalRetrievalOrderExtractor;
import java.util.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.json.JsonMapper;

/** Extractive, source-cited lookup. No generated answers or network requests. */
@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {
    private final List<Passage> passages;
    public KnowledgeController(JsonMapper mapper) throws java.io.IOException {
        try(var input=new ClassPathResource("knowledge.json").getInputStream()) {
            passages=List.of(mapper.readValue(input,Passage[].class));
        }
    }
    @GetMapping public Answer search(@RequestParam String question) {
        if(question.isBlank()||question.length()>1000)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Enter a question of 1–1000 characters");
        String q=LocalRetrievalOrderExtractor.normalize(question);
        var matches=passages.stream().filter(p->p.keywords().stream().anyMatch(k->q.contains(LocalRetrievalOrderExtractor.normalize(k)))).limit(3).toList();
        return new Answer(matches.isEmpty()?"No supported answer found in the local knowledge base.":"Retrieved source passages; no AI-generated answer.",matches);
    }
    public record Passage(String id,String title,List<String> keywords,String text){}
    public record Answer(String message,List<Passage> sources){}
}
