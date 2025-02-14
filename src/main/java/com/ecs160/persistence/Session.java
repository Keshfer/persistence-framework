package com.ecs160.persistence;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;



// Assumption - only support int/long/and string values
public class Session {

    private Jedis jedisSession;
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
                    String idString = ""; // holds IDs of child posts
                    PersistableListField annot = field.getAnnotation(PersistableListField.class);
                    String className = annot.className(); // name of class stored in annotation's className
                    try {
                        List<?> fieldList = (List<?>) field.get(obj);
                        String fieldListName = field.getName();

                        //System.out.println(fieldList.size());

                        // loops over all elements in the list
                        for (int j =0; j < fieldList.size(); j++) {
                            Object eleObj = fieldList.get(j); // the class of object in the list
                            Class<?> eleObjClass = eleObj.getClass();

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
                                        if(idString.equals("")) {
                                            idString += extractedId.toString();
                                        } else {
                                            String idAddition = ", " + extractedId.toString();
                                            idString += idAddition;
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
                        objectMap.put(fieldListName, idString);
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
        return null;
    }

}
