package searchengine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;


@Entity
@Getter
@Setter
@ApiModel(description = "entity representing a webpage")
@Table(name = "pages", indexes = {@Index(name = "page_site_id_path_index", columnList = "site_id, path"),
        @Index(name = "pages_site_id_index", columnList = "site_id")})
public class PageEntity implements Serializable {
    @Id
    @ApiModelProperty("id generated by db automatically")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false, referencedColumnName = "id")
    @JsonIgnore
    private SiteEntity site;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    @ApiModelProperty("relative path to webpage")
    private String path;

    @Column(columnDefinition = "INT", nullable = false)
    @ApiModelProperty("response code")
    private int code;

    @Column(columnDefinition = "TEXT", nullable = false)
    @ApiModelProperty("content of webpage")
    private String content;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL)
//    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<IndexEntity> indexEntities;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PageEntity that = (PageEntity) o;

        if (id != that.id) return false;
        if (site.getId() != that.site.getId()) return false;
        if (code != that.code) return false;
        if (!Objects.equals(path, that.path)) return false;
        return Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + site.getId();
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + code;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }
}
