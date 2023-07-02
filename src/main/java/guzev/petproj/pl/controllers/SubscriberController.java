package guzev.petproj.pl.controllers;

import guzev.petproj.bl.services.SubscriberService;
import guzev.petproj.dao.entities.Subscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("pet-proj/api/subscriber")
@RequiredArgsConstructor
public class SubscriberController {

    private final SubscriberService subscriberService;

    @Value("${page.result.size}")
    private Integer defaultSize;

    @PostMapping
    public ResponseEntity<Subscriber> createSubscriber(@RequestBody Subscriber subscriber) {

        return ResponseEntity.ok(subscriberService.create(subscriber));
    }

    @GetMapping("/{email}")
    public ResponseEntity<Subscriber> readSubscriberByEmail(@PathVariable("email") String email) {

        return ResponseEntity.ok(subscriberService.readByEmail(email));
    }

    @GetMapping
    public ResponseEntity<List<Subscriber>> read(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                                 @RequestParam(value = "size", required = false) Optional<Integer> size) {

        return ResponseEntity.ok(subscriberService.readAll(page, size.orElse(defaultSize)));
    }

    @PutMapping
    public ResponseEntity<Subscriber> updateSubscriber(@RequestBody Subscriber subscriber) {

        return ResponseEntity.ok(subscriberService.update(subscriber));
    }


    @DeleteMapping("/{email}")
    public ResponseEntity<?> deleteSubscriber(@PathVariable("email") String email) {

        subscriberService.delete(email);

        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
