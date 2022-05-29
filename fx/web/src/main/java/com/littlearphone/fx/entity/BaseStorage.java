package com.littlearphone.fx.entity;

import com.littlearphone.fx.dto.FileDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.nio.file.Path;
import java.util.List;

import static com.littlearphone.fx.utils.JSONExtend.toList;
import static com.littlearphone.fx.utils.StreamExtend.asStream;
import static java.io.File.separator;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static javax.persistence.DiscriminatorType.STRING;
import static javax.persistence.GenerationType.IDENTITY;
import static javax.persistence.InheritanceType.SINGLE_TABLE;

@Data
@Entity
@Table(name = "STORAGE")
@Accessors(chain = true)
@RequiredArgsConstructor
@Inheritance(strategy = SINGLE_TABLE)
@DiscriminatorColumn(name = "STORAGE_TYPE", discriminatorType = STRING)
// 等于这个值就会创建为这个类型
@DiscriminatorValue("storage")
// 拆成多表结构，父类和子类都建表，子类具有父类所有列
// @Inheritance(strategy = TABLE_PER_CLASS)
// 拆成多表关联，父类和子类都建表，子类只有自己特有列
// @Inheritance(strategy = JOINED)
public abstract class BaseStorage {
    private static final PathMatcher MATCHER = createMatcher();
    
    private static AntPathMatcher createMatcher() {
        final AntPathMatcher matcher = new AntPathMatcher(separator);
        matcher.setCachePatterns(true);
        matcher.setCaseSensitive(false);
        return matcher;
    }
    
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Column(name = "uuid", unique = true, nullable = false)
    private String uuid;
    @Column(name = "storage_title", nullable = false)
    private String title;
    @Column(name = "storage_type", nullable = false, insertable = false, updatable = false)
    private String type;
    @Column(name = "storage_path", nullable = false, columnDefinition = "varchar(255) default ''")
    private String path;
    @Column(name = "excludes", nullable = false, columnDefinition = "text default '{}'")
    private String excludes;
    @Transient
    private List<String> excludeList;
    
    public boolean anyExclude(String location) {
        if (isNull(getExcludeList())) {
            setExcludeList(toList(getExcludes(), String.class));
        }
        return asStream(getExcludeList()).anyMatch(pattern -> MATCHER.match(pattern, location));
    }
    
    public boolean noneExclude(final FileDto dto) {
        return !anyExclude(dto.getPath());
    }
    
    public Path resolve(final String path) {
        return null;
    }
    
    public List<FileDto> list(final String path) {
        return emptyList();
    }
}
