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
@ApiModel(description = "entity representing a webpage")
@Table(name = "pages", indexes = {@Index(name = "path", columnList = "path")})
public class PageEntity implements Serializable {
    @Id
    @ApiModelProperty("id generated by db automatically")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false, referencedColumnName = "id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JsonIgnore
    private SiteEntity site;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    @ApiModelProperty("relative path to webpage")
    private String path;

    @Column(columnDefinition = "INT NOT NULL")
    @ApiModelProperty("response code")
    private int code;

    @Column(columnDefinition = "TEXT NOT NULL")
    @ApiModelProperty("content of webpage")
    private String content;

    @OneToMany(mappedBy = "page", cascade = CascadeType.REMOVE)
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<IndexEntity> indexEntities;

}
