package hibernate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ComponentScanner {

    private static final String DOT = ".";
    private static final String SLASH = "/";
    private static final String PACKAGE_NOT_FOUND_MESSAGE = "패키지를 찾을 수 없습니다.";
    private static final String CLASS_NOT_FOUNT_MESSAGE = "클래스를 찾을 수 없습니다.";
    private static final String CLASS_EXTENSION = ".class";


    public static List<Class<?>> scan(String basePackage) {
        List<Class<?>> classes = new ArrayList<>();
        String path = basePackage.replace(DOT, SLASH);
        File baseDir = getBaseDir(path);

        if (baseDir.exists() && baseDir.isDirectory()) {
            classes.addAll(scanDirectory(basePackage, baseDir));
        }

        return classes;
    }

    private static File getBaseDir(String path) {
        try {
            return new File(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(path)).getFile());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(PACKAGE_NOT_FOUND_MESSAGE);
        }
    }

    private static List<Class<?>> scanDirectory(String basePackage, File directory) {
        List<Class<?>> classes = new ArrayList<>();

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                classes.addAll(scan(basePackage + DOT + file.getName()));
            } else if (file.getName().endsWith(CLASS_EXTENSION)) {
                classes.add(getClassFromFile(basePackage, file));
            }
        }

        return classes;
    }

    private static Class<?> getClassFromFile(String basePackage, File file) {
        String className = basePackage + DOT + file.getName().substring(0, file.getName().length() - 6);
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(CLASS_NOT_FOUNT_MESSAGE);
        }
    }
}
