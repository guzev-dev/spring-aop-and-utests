package guzev.petproj.dao.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Data
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "publisher")
public class Publisher {

    //main properties

    @Id
    @NonNull
    private String name;

    @NonNull
    private String link;

    @NonNull
    private String redactor;

    @NonNull
    private String contactNumber;

    @NonNull
    private String address;

    @EqualsAndHashCode.Exclude
    @DBRef(lazy = true)
    private Set<Subscriber> subscribers = new HashSet<>();

    //statistical properties

    @EqualsAndHashCode.Exclude
    @Setter(value = AccessLevel.PROTECTED)
    private Long views = 0L;

    @EqualsAndHashCode.Exclude
    @Setter(value = AccessLevel.PROTECTED)
    private Long mailsSent = 0L;

    public Publisher(String name, String link, String redactor, String contactNumber, String address, Set<Subscriber> subscribers) {
        this(name, link, redactor, contactNumber, address);
        this.subscribers.addAll(subscribers);
    }

    protected Publisher(Publisher publisher) {
        this.name = publisher.name;
        this.link = publisher.link;
        this.redactor = publisher.redactor;
        this.contactNumber = publisher.contactNumber;
        this.address = publisher.address;
        this.subscribers = publisher.subscribers;
        this.views = publisher.views;
        this.mailsSent = publisher.mailsSent;
    }
}
