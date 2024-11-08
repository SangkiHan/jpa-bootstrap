package boot;

import hibernate.AnnotationBinder;
import jakarta.persistence.OneToMany;
import persistence.CollectionPersister;
import persistence.EntityPersister;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class MetamodelImpl implements Metamodel {

    private static final String DOT = ".";

    private final Map<String, EntityPersister> entityPersisterMap = new HashMap<>();
    private final Map<String, CollectionPersister> collectionPersisterMap = new HashMap<>();

    @Override
    public void init() {
        AnnotationBinder annotationBinder = new AnnotationBinder("entity");
        List<Class<?>> entityClasses = annotationBinder.getEntityClasses();

        for (Class<?> entityClass : entityClasses) {
            entityPersisterMap.put(entityClass.getSimpleName(), new EntityPersister(entityClass));
            confirmOneToMany(entityClass);
        }
    }

    @Override
    public EntityPersister entityPersister(Class<?> entityClass) {
        return entityPersisterMap.get(entityClass.getSimpleName());
    }

    @Override
    public EntityPersister collectionPersister(String roll) {
        return entityPersisterMap.get(roll);
    }

    @Override
    public List<String> getEntityClasses() {
        List<String> allKeys = new ArrayList<>();
        allKeys.addAll(entityPersisterMap.keySet());  // entityPersisterMap의 키 추가
        allKeys.addAll(collectionPersisterMap.keySet());
        return allKeys;
    }

    private void confirmOneToMany(Class<?> entityClass) {
        Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToMany.class))
                .forEach(field -> {
                    CollectionPersister collectionPersister = new CollectionPersister(getRelatedEntityClass(field));
                    collectionPersisterMap.put(entityClass.getSimpleName() + DOT + collectionPersister.getSimpleName(), collectionPersister);
                });
    }

    private Class<?> getRelatedEntityClass(Field field) {
        Type type = field.getGenericType();
        Type[] types = ((ParameterizedType) type).getActualTypeArguments();
        return (Class<?>) types[0];
    }

}
