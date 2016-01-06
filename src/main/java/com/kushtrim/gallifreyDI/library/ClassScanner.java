package com.kushtrim.gallifreyDI.library;


import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.net.URL;
import java.util.*;

public class ClassScanner {

    Map<String, Class> namedEntities;
    Map<Class, Object> singletons;
    Set<String> foundAnnotatedClassNames;

    @SuppressWarnings("unchecked")
    public Repository scan(String packageName) {
        foundAnnotatedClassNames = new HashSet<String>();
        namedEntities = new HashMap<String, Class>();
        singletons = new HashMap<Class, Object>();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(packageName.replace(".", "/") + "/");
            while (resources.hasMoreElements()) {
                File folder = new File(resources.nextElement().getFile());
                scanFolderForClasses(folder, packageName);
            }
            return Repository.getInstance(namedEntities, singletons);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void scanFolderForClasses(File folder, String packageName) throws ClassNotFoundException {

        File[] folderContent = folder.listFiles();
        assert folderContent != null;
        for (File file : folderContent) {
            String fileName = file.getName();
            if (fileName.endsWith(".class")) {
                // shto klasen ne listë
                String filenameWithoutClass = fileName.substring(0, fileName.lastIndexOf('.'));
                String completeClassName = packageName + "." + filenameWithoutClass;
                boolean added = foundAnnotatedClassNames.add(completeClassName);
                if (added) {
                    processClass(Class.forName(completeClassName));
                }
            } else if (file.isDirectory()) {
                //thirrje rekurzive qe ta analizojme edhe nënfolderin
                scanFolderForClasses(file, packageName + "." + fileName);
            }
        }
    }

    private void processClass(Class clazz) {

        if (clazz.isAnnotationPresent(Named.class) || clazz.isAnnotationPresent(Singleton.class)) {
            Named named = (Named) clazz.getAnnotation(Named.class);
            String name = named != null ? named.value() : null;
            if (name == null || name.length() == 0) {
                name = clazz.getSimpleName();
            }
            if (namedEntities.get(name) != null) {
                throw new RuntimeException(
                        String.format("There are two classes with the same @Named(\"%s\") : %s and %s",
                                name, namedEntities.get(name).getCanonicalName(), clazz
                                        .getCanonicalName()));
            }
            namedEntities.put(name, clazz);
            System.out.format("%s class has been registered with alias \"%s\"\n", clazz.getCanonicalName(), name);

            // register singletons:
            if (clazz.isAnnotationPresent(Singleton.class)) {
                singletons.put(clazz, null);
                System.out.format("%s class (alias \"%s\") is singleton\n", clazz.getCanonicalName(), name);
            }

        }
    }

}