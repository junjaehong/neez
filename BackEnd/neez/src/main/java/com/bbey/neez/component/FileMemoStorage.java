package com.bbey.neez.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

@Component
public class FileMemoStorage implements MemoStorage {

    private final Path memoDir;

    public MemoStorage(@Value("${app.memo-dir}") String memoDirStr) throws IOException {
        // Java 8에서는 Path.of(...) 없음 → Paths.get(...)
        this.memoDir = Paths.get(memoDirStr);
        if (!Files.exists(this.memoDir)) {
            Files.createDirectories(this.memoDir);
        }
    }

    public void write(String fileName, String content) throws IOException {
        Path target = memoDir.resolve(fileName);
        // Java 8에서는 writeString 없음 → Files.write(...)
        Files.write(
                target,
                content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    public String read(String fileName) throws IOException {
        Path target = memoDir.resolve(fileName);
        if (Files.exists(target)) {
            byte[] bytes = Files.readAllBytes(target);
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return "";
    }
}
