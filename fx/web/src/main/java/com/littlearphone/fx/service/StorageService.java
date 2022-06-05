package com.littlearphone.fx.service;

import com.littlearphone.fx.dto.FileDto;
import com.littlearphone.fx.dto.StorageDto;
import com.littlearphone.fx.entity.BaseStorage;
import com.littlearphone.fx.entity.FSStorage;
import com.littlearphone.fx.repository.StorageRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.nio.file.Path;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.hibernate.Hibernate.unproxy;

@Service
public class StorageService {
    @Resource
    private StorageRepository storageRepository;
    @Resource
    private EverythingService everythingService;
    
    @PostConstruct
    @Transactional
    public void initial() {
        if (storageRepository.findAll().isEmpty()) {
            storageRepository.save(new StorageDto("System", "fs", "C:/Users").toFSStorage());
            storageRepository.save(new StorageDto("Passport", "fs", "P:/").toFSStorage());
            storageRepository.save(new StorageDto("WDBook", "fs", "Q:/").toFSStorage());
            storageRepository.save(new StorageDto("Seagate", "fs", "S:/").toFSStorage());
        }
    }
    
    @Transactional
    public BaseStorage save(StorageDto dto) {
        if (isNull(dto)) {
            return new FSStorage();
        }
        return storageRepository.save(dto.toFSStorage());
    }
    
    @Transactional
    @CacheEvict("cache-storage-by-id")
    public BaseStorage delete(Long storageId) {
        if (isNull(storageId)) {
            return new FSStorage();
        }
        try {
            return storageRepository.findById(storageId).orElseGet(FSStorage::new);
        } finally {
            storageRepository.deleteById(storageId);
        }
    }
    
    @Cacheable("cache-storage-by-id")
    public Path resolveFile(Long id, final String relativePath) {
        return storageRepository.findById(id).map(storage -> storage.resolve(relativePath)).orElseThrow();
    }
    
    public List<FileDto> listFiles(Long id, final String relativePath) {
        return storageRepository.findById(id).map(storage -> storage.list(relativePath)).orElse(emptyList());
    }
    
    public Object searchFiles(Long id, final String path) {
        return storageRepository.findById(id).map(storage -> everythingService.search(storage, path)).orElseThrow();
    }
    
    public List<BaseStorage> findAll() {
        return storageRepository.findAll()
            .stream()
            .map(storage -> unproxy(storage, BaseStorage.class))
            .collect(toList());
    }
}
