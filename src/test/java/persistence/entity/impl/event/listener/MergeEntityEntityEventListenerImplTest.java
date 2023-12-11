package persistence.entity.impl.event.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.entity.EntityManager;
import persistence.entity.ContextSource;
import persistence.entity.impl.EntityManagerImpl;
import persistence.entity.impl.context.DefaultPersistenceContext;
import persistence.entity.impl.event.EntityEventDispatcher;
import persistence.entity.impl.event.EntityEventListener;
import persistence.entity.impl.event.EntityEventPublisher;
import persistence.entity.impl.event.dispatcher.EntityEventDispatcherImpl;
import persistence.entity.impl.event.listener.DeleteEntityEventListenerImpl;
import persistence.entity.impl.event.listener.FlushEntityEventListenerImpl;
import persistence.entity.impl.event.listener.LoadEntityEventListenerImpl;
import persistence.entity.impl.event.listener.MergeEntityEventListenerImpl;
import persistence.entity.impl.event.listener.PersistEntityEventListenerImpl;
import persistence.entity.impl.event.publisher.EntityEventPublisherImpl;
import persistence.entity.impl.event.type.MergeEntityEvent;
import persistence.entity.impl.retrieve.EntityLoaderImpl;
import persistence.entity.impl.store.EntityPersisterImpl;
import persistence.sql.ddl.generator.CreateDDLQueryGenerator;
import persistence.sql.ddl.generator.DropDDLQueryGenerator;
import persistence.sql.dialect.H2Dialect;
import persistence.sql.dml.Database;
import persistence.sql.dml.JdbcTemplate;
import registry.EntityMetaRegistry;

@DisplayName("MergeEventListener 테스트")
class MergeEntityEntityEventListenerImplTest {

    private ContextSource contextSource;

    private EntityEventListener mergeEntityEventListener;

    private EntityManager entityManager;

    private static DatabaseServer server;
    private static EntityMetaRegistry entityMetaRegistry;
    private static final Class<?> testClazz = MergeEventEntity.class;
    private static Database jdbcTemplate;
    private static Connection connection;

    @BeforeAll
    static void setServer() throws SQLException {
        server = new H2();
        server.start();
        connection = server.getConnection();
        entityMetaRegistry = EntityMetaRegistry.of(new H2Dialect());
        entityMetaRegistry.addEntityMeta(testClazz);
    }

    @BeforeEach
    void setUp() {
        final EntityPersisterImpl persister = new EntityPersisterImpl(connection, entityMetaRegistry);
        final EntityLoaderImpl loader = new EntityLoaderImpl(connection, entityMetaRegistry);
        final DefaultPersistenceContext persistenceContext = new DefaultPersistenceContext(entityMetaRegistry);

        mergeEntityEventListener = new MergeEntityEventListenerImpl(persister);
        contextSource = persistenceContext;

        EntityEventDispatcher entityEventDispatcher = new EntityEventDispatcherImpl(
            new LoadEntityEventListenerImpl(loader),
            mergeEntityEventListener,
            new PersistEntityEventListenerImpl(persister),
            new DeleteEntityEventListenerImpl(persister),
            new FlushEntityEventListenerImpl()
        );
        EntityEventPublisher entityEventPublisher = new EntityEventPublisherImpl(entityEventDispatcher);

        entityManager = EntityManagerImpl.of(connection, persistenceContext, entityEventPublisher, entityMetaRegistry, FlushModeType.AUTO);
        jdbcTemplate = new JdbcTemplate(connection);
        CreateDDLQueryGenerator createDDLQueryGenerator = new CreateDDLQueryGenerator();
        jdbcTemplate.execute(createDDLQueryGenerator.create(entityMetaRegistry.getEntityMeta(testClazz)));
    }

    @AfterEach
    void tearDown() {
        DropDDLQueryGenerator dropDDLQueryGenerator = new DropDDLQueryGenerator();
        jdbcTemplate.execute(dropDDLQueryGenerator.drop(MergeEventEntity.class));
    }

    @Test
    @DisplayName("병합 이벤트를 수신하면 병합 엔티티의 내용이 반영된다.")
    void mergeEvent() {
        // given
        final MergeEventEntity mergeEventEntity = new MergeEventEntity(null, "saved");
        final Object savedEntity = entityManager.persist(mergeEventEntity);
        final MergeEventEntity savedMergeEventEntity = (MergeEventEntity) savedEntity;
        savedMergeEventEntity.setName("merged");

        final MergeEntityEvent mergeEvent = MergeEntityEvent.of(savedMergeEventEntity, contextSource, entityManager);

        // when
        final MergeEventEntity mergeEventResultEvent = mergeEntityEventListener.onEvent(MergeEventEntity.class, mergeEvent);

        // then
        final MergeEventEntity foundEntity = entityManager.find(MergeEventEntity.class, 1L);
        assertAll(
            () -> assertThat(foundEntity).isNotNull(),
            () -> assertThat(mergeEventResultEvent.getId()).isEqualTo(foundEntity.getId()),
            () -> assertThat(mergeEventResultEvent.getName()).isEqualTo(foundEntity.getName())
        );
    }

    @Test
    @DisplayName("READ ONLY 상태의 엔티티에 병합 이벤트를 수신하면 변경된 엔티티의 내용이 반영되지 않는다.")
    void cannotMergeReadOnlyEntity() {
        // given
        final MergeEventEntity mergeEventEntity = new MergeEventEntity(null, "saved");
        final Object savedEntity = entityManager.persist(mergeEventEntity);
        final MergeEventEntity savedMergeEventEntity = (MergeEventEntity) savedEntity;
        savedMergeEventEntity.setName("merged");

        final MergeEntityEvent mergeEvent = MergeEntityEvent.of(savedMergeEventEntity, contextSource, entityManager);

        // when
        contextSource.readOnly(savedMergeEventEntity);

        // then
        assertThatThrownBy(
            () -> mergeEntityEventListener.onEvent(MergeEventEntity.class, mergeEvent)
        ).isInstanceOf(RuntimeException.class)
            .hasMessage("해당 Entity는 변경될 수 없습니다.");
    }

    @Entity
    static class MergeEventEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        public MergeEventEntity(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        protected MergeEventEntity() {
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
