package guzev.petproj.dao.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

/**
 * <h3>Class represents a publisher that posts {@link Article}.</h3>*/

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "publisher")
public class Publisher {

    //main properties

    @Id
    private String name;

    private String link;

    private String redactor;

    private String contactNumber;

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

    @JsonCreator
    public Publisher(@JsonProperty(required = true, value = "name") @NonNull String name,
                     @JsonProperty(required = true, value = "link") @NonNull String link,
                     @JsonProperty(required = true, value = "redactor") @NonNull String redactor,
                     @JsonProperty(required = true, value = "contactNumber") @NonNull String contactNumber,
                     @JsonProperty(required = true, value = "address") @NonNull String address) {
        this.name = name;
        this.link = link;
        this.redactor = redactor;
        this.contactNumber = contactNumber;
        this.address = address;
    }

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
