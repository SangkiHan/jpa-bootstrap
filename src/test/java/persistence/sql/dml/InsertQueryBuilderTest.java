package persistence.sql.dml;

import domain.Person;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import persistence.sql.meta.Table;

@DisplayName("InsertQueryBuilder class 의")
class InsertQueryBuilderTest {

    @DisplayName("generateQuery 메서드는")
    @Nested
    class GenerateQuery {

        @DisplayName("Person Entity의 insert 쿼리가 만들어지는지 확인한다.")
        @Test
        void testGenerateQuery() {
            // given
            Person person = Person.of("user1", 1, "abc@test.com", 1);
            InsertQueryBuilder builder = InsertQueryBuilder.getInstance();

            // when
            String query = builder.generateQuery(Table.getInstance(Person.class), person);

            // then
            assertEquals("INSERT INTO users (nick_name,old,email) VALUES ('user1',1,'abc@test.com')", query);
        }
    }
}
