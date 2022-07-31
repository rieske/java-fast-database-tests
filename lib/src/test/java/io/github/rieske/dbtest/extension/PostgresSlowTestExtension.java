package io.github.rieske.dbtest.extension;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class PostgresSlowTestExtension extends DatabaseTestExtension implements BeforeEachCallback, AfterEachCallback {
    private static final PostgreSQLContainer<?> DB_CONTAINER =
            new PostgreSQLContainer<>("postgres:14.4-alpine").withReuse(true);

    private static final String JDBC_URI;

    static {
        DB_CONTAINER.start();

        JDBC_URI = "jdbc:postgresql://" + DB_CONTAINER.getHost() + ":" + DB_CONTAINER.getMappedPort(5432);
    }

    private final String databaseName = "testdb_" + UUID.randomUUID().toString().replace('-', '_');

    @Override
    public DataSource getDataSource() {
        return dataSourceForDatabase(databaseName);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        executeInPostgresSchema("CREATE DATABASE " + databaseName);
        Flyway.configure()
                .dataSource(dataSourceForDatabase(databaseName))
                .load()
                .migrate();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        executeInPostgresSchema("DROP DATABASE " + databaseName);
    }

    private static DataSource dataSourceForDatabase(String databaseName) {
        var dataSource = new PGSimpleDataSource();
        dataSource.setUrl(JDBC_URI + "/" + databaseName);
        dataSource.setUser(DB_CONTAINER.getUsername());
        dataSource.setPassword(DB_CONTAINER.getPassword());
        return dataSource;
    }

    private void executeInPostgresSchema(String sql) {
        var dataSource = dataSourceForDatabase("postgres");
        try (Connection conn = dataSource.getConnection()) {
            conn.createStatement().execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
