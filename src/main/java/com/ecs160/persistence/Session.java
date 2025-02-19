package com.ecs160.persistence;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;



// Assumption - only support int/long/and string values
public class Session {

    public Jedis jedisSession;
    private List<Object> objList = new ArrayList<>(); //List
    public Session() {
        jedisSession = new Jedis("localhost", 6379);
    }


    public void add(Object obj) {
        objList.add(obj);
    }


    public void persistAll()  {
        //for each object in the objects list
        for(int i =0; i < objList.size(); i++) {
            Object obj = objList.get(i);
            Class<?> objClass = obj.getClass();

            if(!objClass.isAnnotationPresent((Persistable.class))) {
                System.out.println("The class " + objClass.getName() + " isn't persistable");
                continue;
            }

            String id = null;
            Map<String, String> objectMap = new HashMap<>(); //holds field name and value as a pair

            // loops over the class' fields
            for(Field field : objClass.getDeclaredFields()) {
                field.setAccessible(true);
                if(field.isAnnotationPresent(PersistableId.class)) {
                    try {
                        id = field.get(obj).toString();
                    } catch (IllegalAccessException e) {
                        System.out.println("Can't access " + field.getName() + "'s value");
                        e.printStackTrace();
                    }

                } else if(field.isAnnotationPresent(PersistableField.class)) {
                    //put field key and value into object map
                    String fieldName = field.getName();
                    try {
                        String fieldValue = field.get(obj).toString();
                        objectMap.put(fieldName, fieldValue);
                    } catch(IllegalAccessException e) {
                        System.out.println("Can't access " + fieldName + "'s value");
                        e.printStackTrace();
                        objectMap.put(fieldName, "");
                    }

                } else if (field.isAnnotationPresent(PersistableListField.class)) {
                    StringBuilder idString = new StringBuilder(); // holds IDs of child posts

                    try {
                        List<?> fieldList = (List<?>) field.get(obj);
                        String fieldListName = field.getName();

                        //System.out.println(fieldList.size());

                        // loops over all elements in the list
                        for (int j =0; j < fieldList.size(); j++) {
                            Object eleObj = fieldList.get(j);
                            Class<?> eleObjClass = eleObj.getClass(); // the class of object in the list

                            if(!eleObjClass.isAnnotationPresent((Persistable.class))) {
                                System.out.println("The class " + objClass.getName() + " isn't persistable");
                                continue;
                            }


                            Map<String, String> eleObjMap = new HashMap<>();
                            String eleId = null;
                            // loops over the fields in the element
                            for (Field eleField : eleObjClass.getDeclaredFields()) {
                                eleField.setAccessible(true);

                                if(eleField.isAnnotationPresent(PersistableId.class)) {
                                    try {
                                        Object extractedId = eleField.get(eleObj);
                                        if(idString.toString().isEmpty()) {
                                            idString.append(extractedId.toString());
                                        } else {
                                            String idAddition = "," + extractedId.toString();
                                            idString.append(idAddition);
                                        }
                                        eleId = extractedId.toString();
                                    }catch (IllegalAccessException e) {
                                        System.out.println("Can't access " + eleField.getName() + "'s value");
                                        e.printStackTrace();
                                    }
                                } else if (eleField.isAnnotationPresent(PersistableField.class)) {
                                    String eleFieldName = eleField.getName();
                                    try {
                                        Object eleFieldValue = eleField.get(eleObj);
                                        eleObjMap.put(eleFieldName, eleFieldValue.toString());

                                    } catch(IllegalAccessException e) {
                                        System.out.println("Can't access " + eleFieldName + "'s value");
                                        e.printStackTrace();
                                        objectMap.put(eleFieldName, "");
                                    }
                                }
                            }
                            if (eleId != null) {
                                jedisSession.hset(eleId, eleObjMap);
                            } else {
                                System.out.println("Missing eleId");
                            }
                        }
                        objectMap.put(fieldListName, idString.toString());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(id != null) {
                jedisSession.hset(id, objectMap);
            } else {
                System.out.print("Object is missing ID");
            }

        }


    }

    public Object load(Object object)  {
        // object should only have @PersistableId field filled out
        Class<?> objClass = object.getClass();
        Map<String, String> map = null;
        //search for the id field
        for (Field field : objClass.getDeclaredFields()) {
            String fieldName = field.getName();
            if(field.isAnnotationPresent(PersistableId.class)) {
                try {
                    field.setAccessible(true);
                    String fieldObj = field.get(object).toString();
                    map = jedisSession.hgetAll(fieldObj);
                    break;

                } catch (IllegalAccessException e) {
                    System.out.println("Can't access " + fieldName + "'s value");
                    e.printStackTrace();
                }
            }
        }
        if(map == null) {
            System.out.println("Nothing retrieved from Redis database");
            return null;
        }
        //fill in the @PersistableField fields of object
        for (Field field : objClass.getDeclaredFields()) {
            String fieldName = field.getName();
            if(field.isAnnotationPresent(PersistableField.class)) {
                String fieldValue = map.get(fieldName);
                try {
                    if(fieldValue != null) {
                        field.setAccessible(true);
                        if(field.getType().equals(Integer.class)) {
                            field.set(object, Integer.valueOf(fieldValue));
                        } else {
                            //field is of String type
                            field.set(object, fieldValue);
                        }
                    }

                } catch (IllegalAccessException e) {
                    System.out.println("Can't add value to " + fieldName);
                    e.printStackTrace();
                }

            } else if (field.isAnnotationPresent(PersistableListField.class)) {
                String fieldValue = map.get(fieldName); // this should be the string of reply ids
                String[] childIds = null;
                if (!fieldValue.isEmpty()) {
                    childIds = fieldValue.split(",");
                }
                //PersistableListField annot = field.getAnnotation(PersistableListField.class);
                //String className = annot.className(); // name of class stored in annotation's className
                List<Object> childList = new ArrayList<>();
                Constructor[] objConstructs = objClass.getConstructors();
                Constructor zeroConstruct = null; // This will hold the constructor that takes 0 arguements
                for (Constructor con : objConstructs) {
                    zeroConstruct = con;
                    if (zeroConstruct.getGenericParameterTypes().length == 0) {
                        break; // we found the 0 arg constructor
                    }
                }
                if(zeroConstruct == null) {
                    System.out.println("zeroConstruct is null");
                    System.out.println("Skipping this persistable list field");
                    continue;
                }
                if(childIds == null) {
                    System.out.println("There are no replies ");
                    continue;
                }
                for(String childId : childIds) {
                    Map<String, String> childMap = jedisSession.hgetAll(childId);
                    Object childObj = null;
                    try {
                        zeroConstruct.setAccessible(true);
                        childObj = zeroConstruct.newInstance();
                    } catch (InvocationTargetException e) {
                        System.out.println("InvocationTarget error occurred while instantiating");
                        System.out.println("Skipping this childId");
                        e.printStackTrace();
                        continue;
                    } catch (InstantiationException e) {
                        System.out.println("Can't instantiate");
                        System.out.println("Skipping this childId");
                        e.printStackTrace();
                        continue;
                    } catch (IllegalAccessException e) {
                        System.out.println("Can't access constructor");
                        System.out.println("Skipping this childId");
                        e.printStackTrace();
                        continue;
                    }
                    // at this point, childObj is instantiated
                    Class<?> childObjClass = childObj.getClass();
                    for(Field childField : childObjClass.getDeclaredFields()) {
                        if(childField.isAnnotationPresent(PersistableId.class)) {
                            childField.setAccessible(true);
                            try {
                                childField.set(childObj, Integer.parseInt(childId));
                            } catch (IllegalAccessException e) {
                                System.out.println("Can't set childField: " + childField.getName() + " with " + childId);
                                e.printStackTrace();
                            }
                        } else if (childField.isAnnotationPresent(PersistableField.class)) {
                            childField.setAccessible(true);
                            String childFieldName = childField.getName();
                            String childFieldValue = childMap.get(childFieldName);
                            try {
                                if (childField.getType().equals(Integer.class)) {
                                    childField.set(childObj, Integer.parseInt(childFieldValue));
                                } else {
                                    //childField is a String type
                                    childField.set(childObj, childFieldValue);
                                }
                            } catch (IllegalAccessException e) {
                                System.out.println("Can't set childField: " + childFieldName + " with " + childFieldValue);
                                e.printStackTrace();
                            }
                        }
                    }
                    // childObj has been populated. Put it in the childList
                    childList.add(childObj);
                }
                //all child objects are in the list. Attach the list to the persistable list field
                field.setAccessible(true);
                try {
                    field.set(object, childList);
                } catch (IllegalAccessException e) {
                    System.out.println("Can't set field: " + fieldName + " with childList of type " + childList.getClass());
                    e.printStackTrace();
                }
            }
        }
        return object;
    }

}
