package persistence.entity;

import boot.action.ActionQueue;
import boot.metamodel.MyMetaModel;
import database.dialect.H2Dialect;
import domain.Person;
import event.EventListenerGroup;
import jdbc.JdbcTemplate;
import jdbc.RowMapperFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistence.sql.ddl.CreateQueryBuilder;
import persistence.sql.ddl.DropQueryBuilder;
import persistence.sql.dml.SelectAllQueryBuilder;
import persistence.support.DatabaseSetup;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DatabaseSetup
class MyEntityPersisterTest {
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        CreateQueryBuilder createQueryBuilder = new CreateQueryBuilder(new H2Dialect());
        String createQuery = createQueryBuilder.build(Person.class);
        jdbcTemplate.execute(createQuery);
    }

    @AfterEach
    void tearDown(JdbcTemplate jdbcTemplate) {
        DropQueryBuilder dropQueryBuilder = new DropQueryBuilder(new H2Dialect());
        String dropQuery = dropQueryBuilder.build(Person.class);
        jdbcTemplate.execute(dropQuery);
    }

    @Test
    void insert() {
        //given
        EntityPersister<Person> entityPersister = new MyEntityPersister<>(jdbcTemplate, EntityMeta.from(Person.class));

        //when
        Person person = new Person(1L, "ABC", 10, "ABC@email.com", 10);
        entityPersister.insert(person);
        Person anotherPerson = new Person(2L, "DEF", 20, "DEF@email.com", 20);
        entityPersister.insert(anotherPerson);

        //then
        String selectAllQuery = new SelectAllQueryBuilder().build(Person.class);
        List<Person> result = jdbcTemplate.query(selectAllQuery, RowMapperFactory.create(Person.class));
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    void update() {
        //given
        EntityPersister<Person> entityPersister = new MyEntityPersister<>(jdbcTemplate, EntityMeta.from(Person.class));

        //when
        Person person = new Person(1L, "ABC", 10, "ABC@email.com", 10);
        entityPersister.insert(person);
        String updateName = "DEF";
        int updateAge = 20;
        String updateEmail = "DEF@email.com";
        Person updatePerson = new Person(1L, updateName, updateAge, updateEmail, 20);
        entityPersister.update(updatePerson);

        //then
        MyMetaModel metaModel = new MyMetaModel(jdbcTemplate);
        ActionQueue actionQueue = new ActionQueue();
        EntityManager entityManager = new MyEntityManager(metaModel, EventListenerGroup.createDefaultGroup(metaModel, actionQueue), actionQueue);
        Person result = entityManager.find(Person.class, 1L);
        assertAll(
                () -> assertThat(result).extracting("name").isEqualTo(updateName),
                () -> assertThat(result).extracting("age").isEqualTo(updateAge),
                () -> assertThat(result).extracting("email").isEqualTo(updateEmail)
        );
    }

    @Test
    void delete() {
        //given
        EntityPersister<Person> entityPersister = new MyEntityPersister<>(jdbcTemplate, EntityMeta.from(Person.class));

        //when
        Person person = new Person(1L, "ABC", 10, "ABC@email.com", 10);
        entityPersister.insert(person);
        entityPersister.delete(person);

        //then
        String selectAllQuery = new SelectAllQueryBuilder().build(Person.class);
        List<Person> result = jdbcTemplate.query(selectAllQuery, RowMapperFactory.create(Person.class));
        assertThat(result.size()).isEqualTo(0);
    }
}
