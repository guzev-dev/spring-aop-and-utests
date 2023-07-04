package guzev.petproj.dao.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "article")
@CompoundIndexes({
        @CompoundIndex(name = "unique_title_publisher", unique = true, def = "{'title': 1, 'publisher': 1}")
})
public class Article {

    //main properties

    @Id
    private String id;

    private String title;

    private String content;
    
    @DBRef
    private Publisher publisher;

    //statistical properties

    @EqualsAndHashCode.Exclude
    @Setter(value = AccessLevel.PROTECTED)
    private Long views = 0L;

    @JsonCreator
    public Article(@JsonProperty(required = true, value = "title") @NonNull String title,
                   @JsonProperty(required = true, value = "content") @NonNull String content) {
        this.title = title;
        this.content = content;
    }

    protected Article(Article article) {
        this.id = article.id;
        this.title = article.title;
        this.content = article.content;
        this.publisher = article.publisher;
        this.views = article.views;
    }
}
