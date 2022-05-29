package com.littlearphone.fx.entity;

import com.littlearphone.fx.dto.FileDto;
import lombok.Cleanup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.littlearphone.fx.dto.FileDto.from;
import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Path.of;
import static java.nio.file.Paths.get;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

@Data
@Entity
// 等于这个值就会创建为这个类型
@DiscriminatorValue("fs")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class FSStorage extends BaseStorage {
    @Override
    @SneakyThrows
    public Path resolve(final String path) {
        final String base = getPath();
        final Path location = of(base, defaultIfBlank(decode(path, UTF_8), EMPTY));
        if (anyExclude(location.toAbsolutePath().toString())) {
            return null;
        }
        return location;
    }
    
    @Override
    @SneakyThrows
    public List<FileDto> list(final String path) {
        final Path parent = get(getPath());
        final Path dir = parent.resolve(defaultIfBlank(decode(path, UTF_8), EMPTY));
        final Path realPath = dir.toRealPath();
        if (exists(realPath) && isDirectory(realPath)) {
            @Cleanup final Stream<Path> list = Files.list(realPath);
            return list.map(line -> from(line, parent))
                .filter(Objects::nonNull)
                .filter(this::noneExclude)
                .collect(toList());
        }
        return emptyList();
    }
}
