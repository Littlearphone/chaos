package com.littlearphone.fx.controller;

import com.littlearphone.fx.dto.AudioDto;
import com.littlearphone.fx.dto.FileDto;
import com.littlearphone.fx.dto.StorageDto;
import com.littlearphone.fx.entity.BaseStorage;
import com.littlearphone.fx.service.EverythingService;
import com.littlearphone.fx.service.StorageService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.nio.file.Path;
import java.util.List;

import static com.littlearphone.fx.utils.IOExtend.detectCharset;
import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readAllBytes;
import static java.util.Base64.getDecoder;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.jaudiotagger.audio.AudioFileIO.read;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping("/api")
public class StorageController {
    @Resource
    private StorageService storageService;
    @Resource
    private EverythingService everythingService;
    
    @ResponseBody
    @PostMapping("/storage")
    public BaseStorage storage(@RequestBody StorageDto dto) {
        return storageService.save(dto);
    }
    
    @ResponseBody
    @DeleteMapping("/storage/{storageId}")
    public BaseStorage storage(@PathVariable Long storageId) {
        return storageService.delete(storageId);
    }
    
    @ResponseBody
    @GetMapping("/everything/status")
    public boolean esStatus() {
        return everythingService.supportEverything();
    }
    
    @ResponseBody
    @GetMapping("/storages")
    public List<BaseStorage> storages() {
        return storageService.findAll();
    }
    
    @ResponseBody
    @GetMapping(value = "/storage/{storageId}/files", produces = APPLICATION_JSON_VALUE)
    public List<FileDto> storageFiles(@PathVariable Long storageId, String relativePath) {
        final String path = new String(getDecoder().decode(relativePath), UTF_8);
        return storageService.listFiles(storageId, decode(path, UTF_8));
    }
    
    @ResponseBody
    @GetMapping(value = "/storage/{storageId}/es", produces = APPLICATION_JSON_VALUE)
    public Object searchFiles(@PathVariable Long storageId, String relativePath) {
        final String path = new String(getDecoder().decode(relativePath), UTF_8);
        return storageService.searchFiles(storageId, decode(path, UTF_8));
    }
    
    @SneakyThrows
    @ResponseBody
    @GetMapping("/audio/metadata")
    public AudioDto audioMetadata(Long storageId, String location) {
        final String path = new String(getDecoder().decode(location), UTF_8);
        final Path realPath = storageService.resolveFile(storageId, decode(path, UTF_8));
        final AudioFile file = read(realPath.toFile());
        final AudioDto audioDto = new AudioDto(file.getTag());
        if (isNotBlank(audioDto.getLrc())) {
            return audioDto;
        }
        final String name = realPath.getFileName().toString();
        final Path lrcPath = realPath.resolveSibling(name.substring(0, name.lastIndexOf('.')) + ".lrc");
        if (exists(lrcPath)) {
            final byte[] data = readAllBytes(lrcPath);
            audioDto.setLrc(new String(data, detectCharset(data)));
        }
        return audioDto;
    }
}
