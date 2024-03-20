package persistence.sql.meta;

import domain.Person;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Table class 의")
class TableTest {

    @DisplayName("of 메서드는")
    @Nested
    class Of {
        @DisplayName("Entity 클래스로부터 Table 객체를 생성한다.")
        @Test
        void testTableOf() {
            Table table = Table.getInstance(Person.class);
            assertNotNull(table);
        }

        @DisplayName("Entity 클래스가 아닐 경우 예외를 던진다.")
        @Test
        void testTableOf_WhenNonEntity() {
            class NonEntity {
                private int id;
                private String name;
            }

            assertThrows(IllegalArgumentException.class, () -> Table.getInstance(NonEntity.class));
        }

        @DisplayName("Id 필드가 없을 경우 예외를 던진다.")
        @Test
        void testTableOf_WhenNoIdField() {
            @Entity
            class EntityWithNoId {
                private String name;
            }

            assertThrows(IllegalArgumentException.class, () -> Table.getInstance(EntityWithNoId.class));
        }

        @DisplayName("Id 필드가 2개 이상일 경우 예외를 던진다.")
        @Test
        void testTableOf_WhenMultipleIdFields() {
            @Entity
            class EntityWithMultipleId {
                @Id
                private int id;
                @Id
                private String name;
            }

            assertThrows(IllegalArgumentException.class, () -> Table.getInstance(EntityWithMultipleId.class));
        }
    }

    @DisplayName("getColumns 메서드는")
    @Nested
    class GetColumns {
        @DisplayName("Entity 클래스의 필드에 대한 Column 객체 목록을 반환한다.")
        @Test
        void testGetColumns() {
            Table table = Table.getInstance(Person.class);
            List<Column> columns = table.getSelectColumns();
            assertEquals(4, columns.size());
        }
    }

    @DisplayName("getTableName 메서드는")
    @Nested
    class GetTableName {
        @DisplayName("Entity 클래스의 테이블 이름을 반환한다.")
        @Test
        void testGetTableName() {
            Table table = Table.getInstance(Person.class);
            assertEquals("users", table.getTableName());
        }

        @DisplayName("Entity 클래스의 @Table 어노테이션이 없을 경우 클래스 이름을 반환한다.")
        @Test
        void testGetTableName_WhenNoTableAnnotation() {
            @Entity
            class EntityWithoutTableAnnotation {
                @Id
                private int id;
                private String name;
            }

            Table table = Table.getInstance(EntityWithoutTableAnnotation.class);
            assertEquals("EntityWithoutTableAnnotation", table.getTableName());
        }
    }


    @DisplayName("getIdColumn 메서드는")
    @Nested
    class GetIdColumn {
        @DisplayName("Entity 클래스의 Id 필드에 대한 Column 객체를 반환한다.")
        @Test
        void testGetIdColumn() {
            Table table = Table.getInstance(Person.class);
            Column idColumn = table.getIdColumn();
            assertEquals("id", idColumn.getColumnName());
        }
    }

    @DisplayName("getIdValue 메서드는")
    @Nested
    class GetIdValue {
        @DisplayName("Entity 클래스의 Id 필드의 값을 반환한다.")
        @Test
        void testGetIdValue() {
            Table table = Table.getInstance(Person.class);
            Person person = Person.of(1L, "name", 20, "email");
            Object idValue = table.getIdValue(person);
            assertEquals(1L, idValue);
        }
    }
}