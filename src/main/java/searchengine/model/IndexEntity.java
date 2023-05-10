package searchengine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@ApiModel(description = "entity representing search indexes")
@Table(name = "Indexes")
public class IndexEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty("id generated by db automatically")
    private long id;

    @Column(columnDefinition = "FLOAT NOT NULL")
    @ApiModelProperty("rank of lemma")
    private float rank;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "page_id", referencedColumnName = "id")
    @JsonIgnore
    private PageEntity pageEntity;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "lemma_id", referencedColumnName = "id")
    @JsonIgnore
    private LemmaEntity lemmaEntity;
}
