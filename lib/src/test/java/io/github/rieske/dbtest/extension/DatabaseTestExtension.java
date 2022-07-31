package io.github.rieske.dbtest.extension;

import org.junit.jupiter.api.extension.Extension;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class DatabaseTestExtension implements Extension {
    public abstract DataSource getDataSource();

    public void executeUpdateSql(String sql) {
        try (var connection = getDataSource().getConnection();
             var stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T executeQuerySql(String sql, ResultSetMapper<T> resultSetMapper) {
        try (var connection = getDataSource().getConnection();
             var stmt = connection.createStatement();
             var rs = stmt.executeQuery(sql)) {
            return resultSetMapper.map(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}
