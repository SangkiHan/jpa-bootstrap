package database.sql.ddl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class DropTest {

    @ParameterizedTest
    @CsvSource(value = {
            "database.sql.ddl.OldPerson1:DROP TABLE OldPerson1",
            "database.sql.ddl.OldPerson2:DROP TABLE OldPerson2",
            "database.sql.ddl.OldPerson3:DROP TABLE users"
    }, delimiter = ':')
    void buildDeleteQuery(Class<?> clazz, String expected) {
        String actual = new Drop(clazz).buildQuery();

        assertThat(actual).isEqualTo(expected);
    }
}
