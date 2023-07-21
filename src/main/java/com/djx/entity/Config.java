package com.djx.entity;

import lombok.Data;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Data
public class Config {
    private static Config config;
    private String hostname;
    private Integer port;
    private String dbName;
    private String username;
    private String password;
    private List<String> exportTableNames;

    private Config() {
    }

    public synchronized static Config getInstance() {
        if (config == null) {
            config = new Yaml().loadAs(Config.class.getClassLoader().getResourceAsStream("config.yaml"), Config.class);
        }
        return config;
    }

    public static Config load(String configPath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(configPath));
        return new Yaml().loadAs(new String(bytes), Config.class);
    }
}
