package searchengine.model.page;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import searchengine.model.site.SiteEntity;

import javax.persistence.*;
import java.io.Serializable;


@Entity
@Getter
@Setter
@ApiModel(description = "entity representing a webpage")
@Table(name = "pages", indexes = {@Index(name = "path", columnList = "path")})
public class PageEntity implements Serializable {
    @Id
    @ApiModelProperty("id generated by db automatically")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false, referencedColumnName = "id")
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

    @OneToOne(mappedBy = "pageEntity")
    private searchengine.model.index.Index index;

}
