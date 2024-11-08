package hibernate;

import jakarta.persistence.Entity;

import java.util.List;
import java.util.stream.Collectors;

public class AnnotationBinder {

    private final List<Class<?>> entityClasses;

    public AnnotationBinder(String basePackage) {
        this.entityClasses = getEntityClasses(basePackage);
    }

    private List<Class<?>> getEntityClasses(String basePackage) {
        List<Class<?>> classes = ComponentScanner.scan(basePackage);
        return classes.stream()
                .filter(clazz -> clazz.isAnnotationPresent(Entity.class))
                .collect(Collectors.toList());
    }

    public List<Class<?>> getEntityClasses() {
        return entityClasses;
    }
}
