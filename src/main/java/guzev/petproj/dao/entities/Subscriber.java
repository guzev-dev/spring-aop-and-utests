package guzev.petproj.dao.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "subscriber")
public class Subscriber {

    @Id
    private String email;

    private String username;

    @JsonCreator
    public Subscriber(@JsonProperty(required = true) @NonNull String email,
                      @JsonProperty(required = true) @NonNull String username) {
        this.email = email;
        this.username = username;
    }
}
