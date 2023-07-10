package guzev.petproj.dao.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * <h3>Class represents a user who   can be subscribed to a {@link Publisher} articles</h3>*/

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "subscriber")
public class Subscriber {

    @Id
    private String email;

    private String username;

    @JsonCreator
    public Subscriber(@JsonProperty(required = true, value = "email") @NonNull String email,
                      @JsonProperty(required = true, value = "username") @NonNull String username) {
        this.email = email;
        this.username = username;
    }
}
