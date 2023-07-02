package guzev.petproj.pl.controllers;

import guzev.petproj.bl.services.PublisherService;
import guzev.petproj.dao.entities.Publisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("pet-proj/api/publisher")
@RequiredArgsConstructor
public class PublisherController {

    private final PublisherService publisherService;

    @Value("${page.result.size}")
    private Integer defaultSize;

    @PostMapping
    public ResponseEntity<Publisher> createPublisher(@RequestBody Publisher publisher) {

        return ResponseEntity.ok(publisherService.create(publisher));
    }

    @GetMapping("/{name}")
    public ResponseEntity<Publisher> readByName(@PathVariable(value = "name") String publisherName) {

        return ResponseEntity.ok(publisherService.readByName(publisherName));
    }

    @GetMapping
    public ResponseEntity<List<Publisher>> read(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                                @RequestParam(value = "size", required = false) Optional<Integer> size) {

        return ResponseEntity.ok(publisherService.readAll(page, size.orElse(defaultSize)));
    }

    @GetMapping("/subscribed")
    public ResponseEntity<List<Publisher>> readSubscribedPublishers(@RequestParam(value = "email") String subscriberEmail,
                                                                    @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                                    @RequestParam(value = "size", required = false) Optional<Integer> size) {

        return ResponseEntity.ok(publisherService.readSubscribedPublishers(subscriberEmail, page, size.orElse(defaultSize)));
    }

    @PutMapping
    public ResponseEntity<Publisher> updatePublisher(@RequestBody Publisher publisher) {

        return ResponseEntity.ok(publisherService.update(publisher));
    }

    @PatchMapping("/{name}/subscribe")
    public ResponseEntity<?> subscribePublisher(@PathVariable(value = "name") String publisherName,
                                                @RequestParam(value = "email") String subscriberEmail) {

        return ResponseEntity.ok(Map.of("subscribed", publisherService.subscribe(publisherName, subscriberEmail)));
    }

    @PatchMapping("/{name}/unsubscribe")
    public ResponseEntity<?> unsubscribePublisher(@PathVariable(value = "name") String publisherName,
                                                  @RequestParam(value = "email") String subscriberEmail) {

        return ResponseEntity.ok(Map.of("unsubscribed", publisherService.unsubscribe(publisherName, subscriberEmail)));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<?> delete(@PathVariable(value = "name") String publisherName) {

        publisherService.delete(publisherName);

        return ResponseEntity.ok(Map.of("deleted", true));
    }

}
