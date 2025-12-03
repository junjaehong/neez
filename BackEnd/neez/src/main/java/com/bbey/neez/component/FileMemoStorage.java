package com.bbey.neez.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class FileMemoStorage implements MemoStorage {

    private final Path memoDir;

    public FileMemoStorage(@Value("${app.memo-dir}") String memoDirStr) throws IOException {
        this.memoDir = Paths.get(memoDirStr);
        if (!Files.exists(this.memoDir)) {
            Files.createDirectories(this.memoDir);
        }
    }

    @Override
    public void write(String fileName, String content) throws IOException {
        Path target = memoDir.resolve(fileName);

        // 폴더 자동 생성 (Memo/ 또는 Meet/)
        if (target.getParent() != null && !Files.exists(target.getParent())) {
            Files.createDirectories(target.getParent());
        }

        Files.write(
                target,
                content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    @Override
    public String read(String fileName) throws IOException {
        Path target = memoDir.resolve(fileName);
        if (Files.exists(target)) {
            byte[] bytes = Files.readAllBytes(target);
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return "";
    }

    @Override
    public void delete(String fileName) throws IOException {
        Path target = memoDir.resolve(fileName);
        if (Files.exists(target)) {
            Files.delete(target);
        }
    }

    /**
     * prefix 에 따라 Memo 또는 Meet 디렉토리에 자동 저장
     *
     * 예)
     * "Memo/memo-"   → Memo/memo-20251203T130501.txt
     * "Meet/meeting-" → Meet/meeting-20251203T130501.txt
     */
    @Override
    public String save(String prefix, String content) throws IOException {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));

        // prefix 에 디렉토리 포함되어 있음 (예: Memo/memo- / Meet/meeting-)
        String fileName = prefix + timestamp + ".txt";

        write(fileName, content);

        return fileName;
    }
}
