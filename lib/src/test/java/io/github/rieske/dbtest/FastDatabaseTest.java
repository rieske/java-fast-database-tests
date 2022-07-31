package io.github.rieske.dbtest;

import io.github.rieske.dbtest.extension.DatabaseTestExtension;
import io.github.rieske.dbtest.extension.PostgresTestExtension;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FastDatabaseTest {
    @RegisterExtension
    private final DatabaseTestExtension database = new PostgresTestExtension();

    @RepeatedTest(1000)
    void foo() {
        var id = UUID.randomUUID();
        var foo = UUID.randomUUID().toString();
        database.executeUpdateSql("INSERT INTO some_table(id, foo) VALUES('%s', '%s')".formatted(id, foo));

        database.executeQuerySql("SELECT COUNT(*) FROM some_table", rs -> {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getLong(1)).isEqualTo(1);
            return null;
        });
        database.executeQuerySql("SELECT * FROM some_table", rs -> {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getObject(1, UUID.class)).isEqualTo(id);
            assertThat(rs.getString(2)).isEqualTo(foo);
            return null;
        });
    }

    @RepeatedTest(1000)
    void bar() {
    }
}
