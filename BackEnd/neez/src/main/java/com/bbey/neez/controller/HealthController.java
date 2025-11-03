package com.bbey.neez;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class HealthController {

    private final DbMapper dbMapper;

    public HealthController(DbMapper dbMapper) {
        this.dbMapper = dbMapper;
    }

    @GetMapping("/health")
    public String health() {
        return "ok";
    }

    @GetMapping("/db/ping")
    public String ping() {
        Integer one = dbMapper.selectOne();
        return "select 1 = " + one;
    }

    @GetMapping("/db/tables")
    public List<String> tables() {
        return dbMapper.tables();
    }
}

@Mapper
interface DbMapper {
    @Select("SELECT 1")
    Integer selectOne();

    @Select("SELECT table_name FROM information_schema.tables")
    List<String> tables();
}
