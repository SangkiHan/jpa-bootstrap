package boot;

import persistence.EntityPersister;

import java.util.List;

public interface Metamodel {
    void init();

    EntityPersister entityPersister(Class<?> entityClass);

    EntityPersister collectionPersister(String roll);

    List<String> getEntityClasses();
}
