package com.littlearphone.mini.arco;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.littlearphone.mini.arco.extend.CodecExtend.nameOf;
import static com.littlearphone.mini.arco.extend.ObjectExtend.sneakyBiConvert;
import static com.littlearphone.mini.arco.extend.ObjectExtend.sneakyConvert;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Slf4j
class ConvertRmvb {
    private static final String TYPE = "application/vnd.rn-realmedia";
    private static final Path CODEC = extractCodec();
    private static final Tika TIKA = new Tika();

    @Test
    void testConvert() {
        search(get("H:\\Movies"));
    }

    private void search(Path folder) {
        sneakyConvert(folder, Files::list)
            .forEach(path -> {
                if (isDirectory(path)) {
                    search(path);
                    return;
                }
                final String type = sneakyConvert(path.toFile(), TIKA::detect);
                if (equalsIgnoreCase(type, TYPE)) {
                    convert(path);
                }
            });
    }

    private void convert(Path file) {
        final String realPath = sneakyConvert(file, Path::toRealPath).toString();
        String[] commands = new String[]{CODEC.toString(),
            "-i", realPath, "-y",
            "-qscale", "0",
            "-movflags", "faststart",
            "-vcodec", "libx264",
            nameOf(file, ".mp4")};
        long startTime = currentTimeMillis();
        ProcessBuilder builder = new ProcessBuilder()
            .redirectErrorStream(true)
            .command(commands);
        Process process = sneakyConvert(builder, ProcessBuilder::start);
        log.info(sneakyConvert(process.getInputStream(), IOUtils::toString));
        log.info("Took {}s finish [{}] conversion", (currentTimeMillis() - startTime) / 1000.0, file.getFileName());
    }

    private static Path extractCodec() {
        InputStream stream = ConvertRmvb.class.getResourceAsStream("/codec/ffmpeg.exe");
        Path path = get(getProperty("java.io.tmpdir")).resolve("ffmpeg.exe");
        sneakyBiConvert(stream, path, (a, b) -> Files.copy(a, b, REPLACE_EXISTING));
        return path;
    }
}
