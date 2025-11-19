package com.bbey.neez.component;

import java.io.IOException;

public interface MemoStorage {
    
    void write(String fileName, String content) throws IOException;
    String read(String fileName) throws IOException;
    void delete(String memo) throws IOException;

}
