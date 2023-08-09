package searchengine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@ApiModel(description = "entity representing lemmas")
@Table(name = "lemmas", indexes = {@Index(name = "site_lemma_index", columnList = "site_id, lemma"),
        @Index(name = "site_id", columnList = "site_id")})
public class LemmaEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty("id generated by db automatically")
    private int id;

    @ManyToOne
    @JoinColumn(name = "site_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private SiteEntity site;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    @ApiModelProperty("normal form of a word")
    private String lemma;

    @Column(columnDefinition = "INT NOT NULL")
    @ApiModelProperty("the number of pages where the word occurs at least once")
    private int frequency;

    @OneToMany(mappedBy = "lemma", cascade = CascadeType.ALL)
//    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<IndexEntity> indexEntities;

    public LemmaEntity(SiteEntity site, String lemma, int frequency) {
        this.site = site;
        this.lemma = lemma;
        this.frequency = frequency;
    }

    public LemmaEntity() {
        this(null, null, 0);
    }
}
