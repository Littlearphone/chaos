package com.littlearphone.fx;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.github.stuxuhai.jpinyin.ChineseHelper.convertToSimplifiedChinese;
import static java.lang.String.format;
import static java.nio.file.Files.list;
import static java.nio.file.Files.move;
import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.Objects.nonNull;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

class FxApplicationTests {
    @Test
    @SneakyThrows
    void testTxt() {
        final String original = readString(get("P:\\fdsadasd.txt"));
        StringBuilder modified = new StringBuilder();
        AtomicInteger lines = new AtomicInteger();
        AtomicInteger section = new AtomicInteger();
        AtomicBoolean split = new AtomicBoolean();
        for (char c : original.toCharArray()) {
            if (split.get() && (c == '\n' || c == ' ')) {
                if (c == '\n') {
                    lines.incrementAndGet();
                    if (modified.charAt(modified.length() - 1) != '\n') {
                        continue;
                    }
                    System.out.println(lines);
                }
                continue;
            }
            split.set(false);
            if (section.get() > 1) {
                System.out.print(c);
            }
            if (c == '\n') {
                lines.incrementAndGet();
                if (modified.length() > 0
                    && modified.charAt(modified.length() - 1) != '”'
                    && modified.charAt(modified.length() - 1) != '…'
                    && modified.charAt(modified.length() - 1) != '」'
                    && modified.charAt(modified.length() - 1) != '』'
                    && modified.charAt(modified.length() - 1) != '\n'
                    && modified.charAt(modified.length() - 1) != '。'
                    && modified.charAt(modified.length() - 1) != '？'
                    && modified.charAt(modified.length() - 1) != '！'
                    && modified.charAt(modified.length() - 1) != '）'
                    && modified.charAt(modified.length() - 1) != '日') {
                    split.set(true);
                    System.out.println(lines);
                    continue;
                }
            }
            if (section.get() > 0 && (c == '\n' || c == '\t' || c == ' ')) {
                continue;
            }
            if (modified.length() > 0 && (modified.charAt(modified.length() - 1) == '」' && c == '「'
                || modified.charAt(modified.length() - 1) == '”' && c == '“')) {
                modified.append('\n').append('\n').append("    ");
            }
            modified.append(c);
            if (c == '「' || c == '“') {
                section.incrementAndGet();
                continue;
            }
            if (c == '」' || c == '”') {
                if (section.get() > 1) {
                    System.out.println();
                }
                section.decrementAndGet();
            }
        }
        final String text = convertToSimplifiedChinese(modified.toString());
        writeString(get("P:\\asdasd.txt"), fullWidth2halfWidth(text), CREATE, TRUNCATE_EXISTING);
    }
    
    /**
     * 全角字符串转换半角字符串
     *
     * @param fullWidthStr 非空的全角字符串
     * @return 半角字符串
     */
    private static String fullWidth2halfWidth(String fullWidthStr) {
        char[] charArray = fullWidthStr.toCharArray();
        //对全角字符转换的char数组遍历
        for (int i = 0; i < charArray.length; ++i) {
            int charIntValue = charArray[i];
            //如果符合转换关系,将对应下标之间减掉偏移量65248;如果是空格的话,直接做转换
            if (charIntValue >= 65281 && charIntValue <= 65374) {
                charArray[i] = (char) (charIntValue - 65248);
            } else if (charIntValue == 12288) {
                charArray[i] = (char) 32;
            }
        }
        return new String(charArray);
    }
    
    @Test
    @SneakyThrows
    void testXXX() {
        final Pattern pattern = compile("^(.*[^a-zA-Z])?([a-zA-Z]{2,})-?(\\d{2,})(-?[aA]|-?[bB ])?.*$");
        @Cleanup final Stream<Path> list = list(get("Q:\\XXX\\新建文件夹"));
        list.filter(Files::isRegularFile).forEach(path -> {
            final String name = path.getFileName().toString();
            final int dotIndex = name.lastIndexOf('.');
            final String shortName = name.substring(0, dotIndex);
            final Matcher matcher = pattern.matcher(shortName);
            if (matcher.find()) {
                final StringBuilder nameBuilder = new StringBuilder();
                final String serial = matcher.group(2).toUpperCase();
                nameBuilder.append(serial).append("-");
                final String digits = matcher.group(3);
                if (digits.length() >= 5 && digits.startsWith("00")) {
                    nameBuilder.append(digits.substring(2));
                } else {
                    nameBuilder.append(digits);
                }
                final String part = matcher.group(4);
                nameBuilder.append(defaultIfBlank(part, EMPTY));
                nameBuilder.append(name.substring(dotIndex));
                final String completeName = nameBuilder.toString();
                if (name.equalsIgnoreCase(completeName)) {
                    return;
                }
                try {
                    System.out.printf("<%s>%s%n", nameBuilder, name);
                    // path.toFile().renameTo(path.resolveSibling(completeName).toFile());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    @Test
    @Disabled
    @SneakyThrows
    void testCodec() {
        final Path source = Path.of("Q:\\Series\\欧美de剧集\\Heroes\\Heroes.S01.EP01.avi");
        final String executor = "P:\\Portable\\Bat2Exe\\ffmpeg-n5.0-latest-win64-gpl-5.0\\bin\\ffmpeg.exe";
        final ProcessBuilder builder = new ProcessBuilder();
        builder.command(
            executor,
            "-i", format("\"%s\"", source),
            "-c:a", "copy",
            "-c:v", "copy",
            "P:\\test.mp4"
        );
        builder.redirectErrorStream(true);
        final Process process = builder.start();
        @Cleanup final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        for (String line = reader.readLine(); nonNull(line); line = reader.readLine()) {
            System.out.println(line);
        }
    }
    
    @Test
    @Disabled
    @SneakyThrows
    void testRename() {
        final String path =
            "P:\\新建文件夹\\新建文件夹";
        AtomicInteger count = new AtomicInteger(232);
        @Cleanup final Stream<Path> list = list(get(path));
        final List<Path> paths = list.collect(toList());
        paths.forEach(file -> {
            final String name = file.getFileName().toString();
            final String suffix = name.substring(name.lastIndexOf('.'));
            final String other = format("%03d", count.getAndIncrement()) + suffix;
            final Path target = file.resolveSibling(other);
            try {
                System.out.printf("<%s>%s%n", name, target.getFileName());
                move(file, target);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
