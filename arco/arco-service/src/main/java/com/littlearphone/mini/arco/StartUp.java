package com.littlearphone.mini.arco;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import static com.littlearphone.mini.arco.extend.ObjectExtend.nullOrConvert;
import static com.littlearphone.mini.arco.extend.ObjectExtend.sneakyConvert;
import static java.nio.file.FileSystems.getDefault;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.util.Objects.nonNull;

/**
 * @author YaoJiamin
 * @description
 * @createDate 2020/5/1
 * @createTime 上午11:20
 */
@Slf4j
@SpringBootApplication
public class StartUp {
    // public static final String MOVIES_PATH = "/media/nuc/My Book/Movies";
    public static final String MOVIES_PATH = "H:/Movies";
    public static final WatchService WATCH_SERVICE = nullOrConvert(getDefault(), FileSystem::newWatchService);
    public static final Kind<?>[] EVENTS = {ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE};

    public static void main(String[] args) {
        SpringApplication.run(StartUp.class, args);
    }

    @Bean
    public CommandLineRunner watch() {
        return args -> {
            Path dir = Paths.get(MOVIES_PATH);
            WatchKey watchKey = sneakyConvert(() -> dir.register(WATCH_SERVICE, EVENTS));
            log.info("key={}", watchKey);
            for (WatchKey key; nonNull(key = sneakyConvert(WATCH_SERVICE, WatchService::take)); ) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    // 得到 监听的事件类型
                    Kind<?> kind = event.kind();
                    // 得到 监听的文件/目录的路径
                    Path pathName = (Path) event.context();
                    log.info(kind.name());
                    log.info(pathName.toString());
                }
                // 每次的到新的事件后，需要重置监听池
                key.reset();
            }
        };
    }
}
