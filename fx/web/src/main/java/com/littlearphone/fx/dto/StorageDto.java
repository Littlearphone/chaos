package com.littlearphone.fx.dto;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.littlearphone.fx.entity.BaseStorage;
import com.littlearphone.fx.entity.FSStorage;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Set;

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;
import static com.littlearphone.fx.utils.UUIDExtend.uuid;
import static java.io.File.separator;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class StorageDto {
    private String title;
    private String type;
    private String path;
    private Set<String> excludes;
    
    public StorageDto(final String title, final String type, final String path) {
        this.title = title;
        this.type = type;
        this.path = path;
    }
    
    public BaseStorage toFSStorage() {
        final ArrayNode excludes = instance.arrayNode()
            .add("**" + separator + "$RECYCLE.BIN" + separator + "**")
            .add("**" + separator + "System Volume Information" + separator + "**")
            .add("**" + separator + "desktop.ini");
        ofNullable(getExcludes())
            .orElse(emptySet())
            .forEach(excludes::add);
        return new FSStorage()
            .setUuid(uuid())
            .setTitle(getTitle())
            .setType(getType())
            .setPath(getPath())
            .setExcludes(excludes.toString());
    }
}
