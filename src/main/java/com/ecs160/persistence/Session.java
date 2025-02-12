package com.ecs160.persistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import redis.clients.jedis.Jedis;



// Assumption - only support int/long/and string values
public class Session {

    private Jedis jedisSession;
    private List<Object> objList = new ArrayList<>();
    private Session() {
        jedisSession = new Jedis("localhost", 6379);
    }


    public void add(Object obj) {
        objList.add(obj);
    }


    public void persistAll()  {
        for(int i =0; i < objList.size(); i++) {
            Object obj = objList.get(i);
            Class objClass = obj.getClass();
            if(!objClass.isAnnotationPresent((Persistable.class))) {
                System.out.println("The class " + objClass.getName() + " isn't persistable");
                continue;
            }
            Object id;
            Map<String, Object> objectMap = new HashMap<>(); //holds field name and value as a pair
            for(Field field : objClass.getDeclaredFields()) {
                field.setAccessible(true);
                if(field.isAnnotationPresent(PersistableId.class)) {
                    try {
                        id = field.get(obj);
                    } catch (IllegalAccessException e) {
                        System.out.println("Can't access " + field.getName() + "'s value");
                        e.printStackTrace();
                    }
                } else if(field.isAnnotationPresent(PersistableField.class)) {
                    //put field key and value into object map

                    String fieldName = field.getName();
                    try {
                        Object fieldValue = field.get(obj);
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
                        List fieldList = (List) field.get(objClass);
                        for (int j =0; j < fieldList.size(); j++) {
                            Object eleObj = fieldList.get(i); // the class of object in the list
                            Class eleObjClass = eleObj.getClass();
                            if(!eleObjClass.isAnnotationPresent((Persistable.class))) {
                                System.out.println("The class " + objClass.getName() + " isn't persistable");
                                continue;
                            }
                            for (Field eleField : eleObjClass.getDeclaredFields()) {
                                eleField.setAccessible(true);
                                if(eleField.isAnnotationPresent(PersistableId.class)) {
                                    try {
                                        Object eleId = eleField.get(eleObj);
                                        if(idString.equals("")) {
                                            idString += eleId.toString();
                                        } else {
                                            String idAddition = ", " + eleId.toString();
                                            idString += idAddition;
                                        }
                                    }catch (IllegalAccessException e) {
                                        System.out.println("Can't access " + eleField.getName() + "'s value");
                                        e.printStackTrace();
                                    }
                                } else if (eleField.isAnnotationPresent(PersistableField.class)) {
                                    String eleFieldName = eleField.getName();
                                    try {
                                        eleField.get(eleObj);

                                    }catch(IllegalAccessException e) {
                                        System.out.println("Can't access " + eleFieldName + "'s value");
                                        e.printStackTrace();
                                        objectMap.put(eleFieldName, "");
                                    }
                                }
                            }
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                }
            }

        }


    }


    public Object load(Object object)  {
        return null;
    }

}
