package com.littlearphone.fx.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.flac.FlacTag;

import static com.littlearphone.fx.utils.IOExtend.toBase64Text;
import static com.littlearphone.fx.utils.StreamExtend.asStream;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.jaudiotagger.tag.FieldKey.ARTIST;
import static org.jaudiotagger.tag.FieldKey.LYRICS;
import static org.jaudiotagger.tag.FieldKey.TITLE;

@Data
@Accessors(chain = true)
public class AudioDto {
    private String lrc;
    private String name;
    private String cover;
    private String artist;
    
    public AudioDto(final Tag tag) {
        setArtist(tag.getFirst(ARTIST));
        setName(tag.getFirst(TITLE));
        if (tag instanceof FlacTag) {
            final FlacTag flacTag = (FlacTag) tag;
            setCover(asStream(flacTag.getImages()).findAny()
                .map(picture -> toBase64Text(picture.getImageData(), picture.getMimeType()))
                .orElse(EMPTY));
        }
        if (tag.hasField(LYRICS)) {
            setLrc(tag.getFirst(LYRICS));
        }
    }
}
