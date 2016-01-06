package com.kushtrim.gallifreyDI.library;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class Injector {

    private static Repository repository;

    public static void init(String packageName) {
        ClassScanner classScanner = new ClassScanner();
        repository = classScanner.scan(packageName);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getEntity(Class<T> clazz) {
        try {
            Object entity = repository.getEntity(clazz);
            return (T) entity;
        } catch (Exception e) {
            throw new RuntimeException(String.format("Could not instantiate class %s", clazz.getName()), e);
        }
    }


    @SuppressWarnings("unchecked")
    public static <T> T getEntity(String alias, Class<T> clazz) {
        try {
            Object entity = repository.getEntity(alias);
            return (T) entity;
        } catch (Exception e) {
            throw new RuntimeException(String.format("Could not instantiate class %s", clazz.getName()), e);
        }
    }

    /**
     * Ia injekton ketij objekti varesite e tij
     */

    public static <T> void inject(T object) {
        Class<?> clazz = object.getClass();

        try {
            for (Field field : getInjectedFields(clazz)) {

                String fieldName = field.getName();

                //psh per field coffeeMaker, emri i metodes del setCoffeeMaker
                String setterMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                Method setterMethod;
                try {
                    setterMethod = clazz.getMethod(setterMethodName, field.getType());
                } catch (Exception e) {
                    throw new IllegalAccessException(String.format("Cannot find (public) setter %s.%s(%s})",
                            clazz.getCanonicalName(), setterMethodName, field.getType().getCanonicalName()));
                }

                Class<?> fieldType = field.getType();
                String classThatWeAreWorkingOn = object.getClass().getName();

                Named named = field.getAnnotation(Named.class);
                Object objectToBeInjected;
                if (fieldType.isInterface()) {
                    //shikojme se a eshte interface kjo, nese po a e implementon klasa me @Named kete interface
                    if (named != null && named.value() != null) {
                        checkNamedObjectImplementsInterface(named.value(), field, classThatWeAreWorkingOn);
                        objectToBeInjected = repository.getEntity(named.value());
                    } else {
                        throw new InstantiationException(String.format(
                                "Class %s. Variable \'%s\'s type is of interface, and must be annotated with @Named(\"alias\")",
                                classThatWeAreWorkingOn,
                                field.getName()));
                    }

                } else if (Modifier.isAbstract(fieldType.getModifiers())) {
                    //kemi te bejme me klase abstrakte,nese s'ka annotation -> error, nese klasa me qat annotation nuk e extend -> error
                    if (named != null && named.value() != null) {
                        checkNamedObjectExtendsClass(named.value(), field, classThatWeAreWorkingOn);
                        objectToBeInjected = repository.getEntity(named.value());
                    } else {
                        throw new InstantiationException(String.format(
                                "Class %s. Variable \'%s\'s type is of abstract class, and must be annotated with @Named(\"alias\")",
                                classThatWeAreWorkingOn,
                                field.getName()));
                    }
                } else {
                    objectToBeInjected = repository.getEntity(fieldType);
                }


                try {
                    setterMethod.invoke(object, objectToBeInjected);
                } catch (Exception e) {
                    throw new InstantiationException(String.format(
                            "Unable to call setter function [{%s}] of object {%s} with argument type {%s}",
                            setterMethod.toString(),
                            object.getClass(),
                            objectToBeInjected.getClass()));
                }

                System.out.format("Injected %s.%s() successfully\n", clazz.getCanonicalName(), setterMethodName);
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("Could not instantiate class %s", clazz.getName()), e);

        }
    }

    private static void checkNamedObjectExtendsClass(String value, Field field, String classThatWeAreWorkingOn) throws InstantiationException {
        Class requiredSuperclass = field.getType();


        Class currentSuperClass = repository.getClass(value);
        boolean found = false;
        while (currentSuperClass != Object.class) {
            currentSuperClass = currentSuperClass.getSuperclass();
            if (currentSuperClass == requiredSuperclass) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new InstantiationException(String.format(
                    "Could not inject field %s in %s, because the class %s with annotation @Named(\"%s\") extend doesn't extend the abstract class %s",
                    field.getName(), classThatWeAreWorkingOn, requiredSuperclass, value, field.getType()
            ));
        }
    }

    private static void checkNamedObjectImplementsInterface(String value, Field field, String classThatWeAreWorkingOn) throws InstantiationException {

        boolean namedObjectImplementsInterface = false;
        Class classWithAnnotation = repository.getClass(value);
        Class<?>[] interfaces = classWithAnnotation.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            if (anInterface == field.getType()) {
                namedObjectImplementsInterface = true;
                break;
            }
        }
        if (!namedObjectImplementsInterface) {
            throw new InstantiationException(String.format(
                    "Could not inject field %s in %s, because the class %s with annotation @Named(\"%s\") annotation doesn't implement the interface",
                    field.getName(), classThatWeAreWorkingOn, classWithAnnotation, value
            ));
        }
    }

    /**
     * Returns all @Inject annotated fields of the class and parent classes.
     */
    private static List<Field> getInjectedFields(Class clazz) {
        List<Field> fieldList = new ArrayList<Field>();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                fieldList.add(field);
            }
        }

        Class superClass = clazz.getSuperclass();
        if (superClass != null) {
            List<Field> fieldsOfParent = getInjectedFields(superClass);
            fieldList.addAll(fieldsOfParent);
        }
        return fieldList;
    }
}
