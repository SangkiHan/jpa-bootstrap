package persistence.entity;

import database.DatabaseServer;
import database.H2;
import database.HibernateEnvironment;
import domain.Person;
import jdbc.JdbcTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.sql.column.Columns;
import persistence.sql.column.IdColumn;
import persistence.sql.column.TableColumn;
import persistence.sql.ddl.CreateQueryBuilder;
import persistence.sql.ddl.DropQueryBuilder;
import persistence.sql.dialect.Dialect;
import persistence.sql.dialect.MysqlDialect;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class EntityPersisterImplTest {

    private JdbcTemplate jdbcTemplate;
    private TableColumn table;
    private EntityPersister entityPersister;
    private EntityManager entityManager;


    @BeforeEach
    void setUp() throws SQLException {
        DatabaseServer server = new H2();
        server.start();
        jdbcTemplate = new JdbcTemplate(server.getConnection());

        Class<Person> personEntity = Person.class;
        table = new TableColumn(personEntity);
        Dialect dialect = new MysqlDialect();
        entityPersister = new EntityPersisterImpl(jdbcTemplate, dialect);
        EntityManagerFactory entityManagerFactory = new EntityManagerFactoryImpl(
                new HibernateEnvironment(dialect, server.getDataSourceProperties(), server.getConnection())
        );
        entityManager = entityManagerFactory.createEntityManager();
        createTable(personEntity, dialect);
    }

    private void createTable(Class<Person> personEntity, Dialect dialect) {
        Columns columns = new Columns(personEntity.getDeclaredFields());
        IdColumn idColumn = new IdColumn(personEntity.getDeclaredFields());
        CreateQueryBuilder createQueryBuilder = new CreateQueryBuilder(table, columns, idColumn, dialect);
        String createQuery = createQueryBuilder.build();
        jdbcTemplate.execute(createQuery);
    }

    @AfterEach
    void tearDown() {
        DropQueryBuilder dropQueryBuilder = new DropQueryBuilder(table);
        String dropQuery = dropQueryBuilder.build();
        jdbcTemplate.execute(dropQuery);
    }

    @DisplayName("엔티티의 이름을 수정한다.")
    @Test
    void update() {
        //given
        Person person = new Person(1L, "홍길동", 20, "Jon@test.com", 20);
        Person savedPerson =  entityManager.persist(person);

        //when
        savedPerson.setName("김길동");
        entityPersister.update(savedPerson, new IdColumn(savedPerson).getValue());

        //then
        Person foundPerson = entityManager.find(Person.class, savedPerson.getId());

        assertThat(foundPerson.getName()).isEqualTo("김길동");

    }

    @DisplayName("엔티티를 추가한다.")
    @Test
    void insert() {

        //given
        Person person = new Person(1L, "홍길동", 30, "Jon@test.com", 20);

        //when
        entityPersister.insert(person);

        //then
        Person foundPerson = entityManager.find(Person.class, person.getId());
        assertAll(
                () -> assertThat(foundPerson.getId()).isEqualTo(person.getId()),
                () -> assertThat(foundPerson.getName()).isEqualTo(person.getName()),
                () -> assertThat(foundPerson.getAge()).isEqualTo(person.getAge()),
                () -> assertThat(foundPerson.getEmail()).isEqualTo(person.getEmail())
        );
    }

    @DisplayName("엔티티를 삭제한다.")
    @Test
    void delete() {
        //given
        Person person = new Person(1L, "홍길동", 20, "jon@test.com", 20);
        entityPersister.insert(person);
        IdColumn idColumn = new IdColumn(person);
        //when
        entityPersister.delete(person, idColumn.getValue());

        //then
        assertThatThrownBy(() -> entityManager.find(Person.class, person.getId()))
                .isInstanceOf(RuntimeException.class);
    }
}
