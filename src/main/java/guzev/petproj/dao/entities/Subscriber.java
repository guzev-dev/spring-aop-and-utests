package guzev.petproj.dao.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "subscriber")
public class Subscriber {

    @Id
    @NonNull
    private String email;

    @NonNull
    private String username;

}
