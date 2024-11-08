package persistence;

import builder.dml.EntityData;
import builder.dml.JoinEntityData;
import builder.dml.builder.DeleteQueryBuilder;
import builder.dml.builder.InsertQueryBuilder;
import builder.dml.builder.UpdateQueryBuilder;
import database.H2DBConnection;
import jdbc.JdbcTemplate;

import java.sql.SQLException;

public class EntityPersister {

    private JdbcTemplate jdbcTemplate;

    private final InsertQueryBuilder insertQueryBuilder = new InsertQueryBuilder();
    private final UpdateQueryBuilder updateQueryBuilder = new UpdateQueryBuilder();
    private final DeleteQueryBuilder deleteQueryBuilder = new DeleteQueryBuilder();
    private final CollectionPersister collectionPersister = new CollectionPersister();
    private Class<?> entityClass;

    public EntityPersister() {
        this.jdbcTemplate = initializeJdbcTemplate();
    }

    public EntityPersister(Class<?> entityClass) {
        initializeJdbcTemplate();
        this.entityClass = entityClass;
    }

    private JdbcTemplate initializeJdbcTemplate() {
        try {
            return new H2DBConnection().start();
        } catch (SQLException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    //데이터를 반영한다.
    public void persist(EntityData entityData) {
        jdbcTemplate.execute(insertQueryBuilder.buildQuery(entityData.getTableName(), entityData.getEntityColumn()));
        if (entityData.checkJoin()) {
            collectionPersister.persist(entityData);
        }
    }

    //데이터를 수정한다.
    public void merge(EntityData entityData) {
        jdbcTemplate.execute(updateQueryBuilder.buildQuery(entityData));
    }

    //데이터를 제거한다.
    public void remove(EntityData entityData) {
        jdbcTemplate.execute(deleteQueryBuilder.buildQuery(entityData));
    }

}
