package persistence;

import boot.EntityManagerFactory;
import boot.MyEntityManagerFactory;
import database.DatabaseServer;
import database.H2;
import database.dialect.H2Dialect;
import domain.Person;
import jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.entity.EntityManager;
import persistence.entity.EntityMeta;
import persistence.sql.ddl.CreateQueryBuilder;
import persistence.sql.ddl.DropQueryBuilder;
import repository.CustomJpaRepository;

public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        logger.info("Starting application...");
        try {
            final DatabaseServer server = new H2();
            server.start();

            final JdbcTemplate jdbcTemplate = new JdbcTemplate(server.getConnection());
            CreateQueryBuilder createQueryBuilder = new CreateQueryBuilder(new H2Dialect());
            jdbcTemplate.execute(createQueryBuilder.build(Person.class));

            Person person = new Person(null, "John", 25, "email", 1);
            Person person2 = new Person(1L, "James", 45, "james@asdf.com", 10);
            EntityManagerFactory entityManagerFactory = new MyEntityManagerFactory(jdbcTemplate);
            EntityManager entityManager = entityManagerFactory.openEntityManager();
            CustomJpaRepository<Person, Long> personLongCustomJpaRepository = new CustomJpaRepository<>(entityManager, EntityMeta.from(Person.class));
            personLongCustomJpaRepository.save(person);
            personLongCustomJpaRepository.save(person2);

            DropQueryBuilder dropQuery = new DropQueryBuilder(new H2Dialect());
            jdbcTemplate.execute(dropQuery.build(Person.class));

            server.stop();
        } catch (Exception e) {
            logger.error("Error occurred", e);
        } finally {
            logger.info("Application finished");
        }
    }
}
