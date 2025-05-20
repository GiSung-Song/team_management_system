package com.manager.taskmanager.config;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public class DBContainerExtension implements Extension, BeforeAllCallback {

    static MySQLContainer mySQLContainer = new MySQLContainer(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("testdb")
            .withUsername("root")
            .withPassword("password");

    static GenericContainer redisContainer = new GenericContainer(DockerImageName.parse("redis:7.2.5"))
            .withExposedPorts(6379);

    @Override
    public void beforeAll(ExtensionContext context) {
        mySQLContainer.start();
        redisContainer.start();

        System.setProperty("spring.datasource.url", mySQLContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", mySQLContainer.getUsername());
        System.setProperty("spring.datasource.password", mySQLContainer.getPassword());

        System.setProperty("spring.data.redis.port", String.valueOf(redisContainer.getFirstMappedPort()));
    }

}
