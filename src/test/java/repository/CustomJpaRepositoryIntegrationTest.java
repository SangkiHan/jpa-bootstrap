package repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import database.DatabaseServer;
import database.H2;
import jakarta.persistence.Entity;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.entity.EntityManager;
import persistence.entity.impl.EntityManagerImpl;
import persistence.entity.impl.context.DefaultPersistenceContext;
import persistence.entity.impl.event.EntityEventDispatcher;
import persistence.entity.impl.event.EntityEventPublisher;
import persistence.entity.impl.event.dispatcher.EntityEventDispatcherImpl;
import persistence.entity.impl.event.listener.DeleteEntityEventListenerImpl;
import persistence.entity.impl.event.listener.FlushEntityEventListenerImpl;
import persistence.entity.impl.event.listener.LoadEntityEventListenerImpl;
import persistence.entity.impl.event.listener.MergeEntityEventListenerImpl;
import persistence.entity.impl.event.listener.PersistEntityEventListenerImpl;
import persistence.entity.impl.event.publisher.EntityEventPublisherImpl;
import persistence.entity.impl.retrieve.EntityLoaderImpl;
import persistence.entity.impl.store.EntityPersisterImpl;
import persistence.sql.ddl.generator.CreateDDLQueryGenerator;
import persistence.sql.ddl.generator.DropDDLQueryGenerator;
import persistence.sql.dialect.H2Dialect;
import persistence.sql.dml.Database;
import persistence.sql.dml.JdbcTemplate;
import registry.EntityMetaRegistry;

@DisplayName("Repository 통합 테스트")
class CustomJpaRepositoryIntegrationTest {

    private static DatabaseServer server;
    private static EntityMetaRegistry entityMetaRegistry;
    private static final Class<?> testClazz = TestEntity.class;
    private static Database jdbcTemplate;
    private static Connection connection;
    private EntityManager entityManager;
    private JpaRepository<TestEntity, Long> jpaRepository;

    @BeforeAll
    static void setServer() throws SQLException {
        server = new H2();
        server.start();
        connection = server.getConnection();
        entityMetaRegistry = EntityMetaRegistry.of(new H2Dialect());
        entityMetaRegistry.addEntityMeta(testClazz);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void setUp() throws SQLException {
        final EntityPersisterImpl persister = new EntityPersisterImpl(connection, entityMetaRegistry);
        final EntityLoaderImpl loader = new EntityLoaderImpl(connection, entityMetaRegistry);

        EntityEventDispatcher entityEventDispatcher = new EntityEventDispatcherImpl(
            new LoadEntityEventListenerImpl(loader),
            new MergeEntityEventListenerImpl(persister),
            new PersistEntityEventListenerImpl(persister),
            new DeleteEntityEventListenerImpl(persister),
            new FlushEntityEventListenerImpl()
        );
        EntityEventPublisher entityEventPublisher = new EntityEventPublisherImpl(entityEventDispatcher);

        entityManager = EntityManagerImpl.of(
            connection,
            new DefaultPersistenceContext(entityMetaRegistry),
            entityEventPublisher,
            entityMetaRegistry,
            FlushModeType.AUTO
        );

        jdbcTemplate = new JdbcTemplate(server.getConnection());
        CreateDDLQueryGenerator createDDLQueryGenerator = new CreateDDLQueryGenerator();
        jdbcTemplate.execute(createDDLQueryGenerator.create(entityMetaRegistry.getEntityMeta(testClazz)));

        jpaRepository = new CustomJpaRepository<>(entityManager, TestEntity.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        DropDDLQueryGenerator dropDDLQueryGenerator = new DropDDLQueryGenerator();
        jdbcTemplate.execute(dropDDLQueryGenerator.drop(testClazz));
    }

    @Test
    @DisplayName("Repository를 통해 저장할 수 있다.")
    void repositoryCanSaveEntity() {
        final TestEntity entity = new TestEntity(null, "test", "test@gamil.com");
        final TestEntity savedEntity = jpaRepository.save(entity);

        assertAll(
            () -> assertThat(entity.getName()).isEqualTo(savedEntity.getName()),
            () -> assertThat(entity.getEmail()).isEqualTo(savedEntity.getEmail())
        );
    }

    @Test
    @DisplayName("Repository를 통해 캐싱된 엔티티를 불러올 수 있다.")
    void repositoryCanFindSavedEntity() {
        final TestEntity entity = new TestEntity(null, "test", "test@gamil.com");
        final TestEntity savedEntity = jpaRepository.save(entity);

        final Optional<TestEntity> findEntity = jpaRepository.findById(1L);

        assertAll(
            () -> assertThat(entity.getName()).isEqualTo(savedEntity.getName()),
            () -> assertThat(entity.getEmail()).isEqualTo(savedEntity.getEmail()),
            () -> assertThat(findEntity).hasValueSatisfying(e ->
                assertThat(savedEntity == e).isTrue()
            )
        );
    }

    @Entity
    private static class TestEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        private String email;

        public TestEntity(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        protected TestEntity() {

        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }
}
