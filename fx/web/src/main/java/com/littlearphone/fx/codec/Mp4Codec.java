package com.littlearphone.fx.codec;

import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class Mp4Codec extends AbstractCodec {
    @Override
    public boolean testStreaming(final Path path) {
        return true;
    }
}
