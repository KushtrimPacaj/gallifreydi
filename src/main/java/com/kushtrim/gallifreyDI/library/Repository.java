package com.kushtrim.gallifreyDI.library;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Repository {

    private static Repository repositoryInstance;

    private Map<String, Class> namedEntities;     //all classes that are found
    private Map<Class, Object> singletons;    //all classes annotated with @Singleton
    private Set<Class> objectsCurrentlyInitializing = new HashSet<Class>(); // se we can check for circular dependencies

    private Repository(Map<String, Class> namedEntities, Map<Class, Object> singletons) {
        this.namedEntities = namedEntities;
        this.singletons = singletons;
    }

    public static synchronized Repository getInstance(Map<String, Class> namedEntities, Map<Class, Object> singletons) {
        if (repositoryInstance == null) {
            repositoryInstance = new Repository(namedEntities, singletons);
        }
        return repositoryInstance;
    }

    public Object getEntity(String alias) throws InstantiationException, InvocationTargetException, IllegalAccessException {
        Class clazz = getClass(alias);
        return getEntity(clazz);
    }

    public Class getClass(String alias) throws InstantiationException {
        Class clazz = namedEntities.get(alias);
        if (clazz == null) throw new InstantiationException(
                String.format("There exists no class with annotation @Named(\"%s\")", alias));
        return clazz;
    }

    @SuppressWarnings("unchecked")
    public <T> T getEntity(Class clazz) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        T entity;

        if (singletons.containsKey(clazz)) {
            Object singletonObject = singletons.get(clazz);
            if (singletonObject == null) {
                singletonObject = instantiateObject(clazz);
                singletons.put(clazz, singletonObject);
            }
            entity = (T) singletonObject;
        } else {
            entity = (T) instantiateObject(clazz);
        }

        return entity;
    }

    @SuppressWarnings("unchecked")
    private <T> T instantiateObject(Class<T> clazz) throws IllegalAccessException, InstantiationException, InvocationTargetException {

        T newInstance = clazz.newInstance();

        if (objectsCurrentlyInitializing.contains(clazz)) {
            throw new InstantiationException(
                    String.format(
                            "Circular reference has been detected during the instantiation of class %s",
                            clazz.getCanonicalName()));
        }
        try {
            objectsCurrentlyInitializing.add(clazz);
            Injector.inject(newInstance);
            // i injektim edhe varesite e klases se krijuar
        } finally {
            objectsCurrentlyInitializing.remove(clazz);
        }
        return newInstance;
    }

}
