package com.ecs160;


import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.ecs160.persistence.Session;
import com.google.gson.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

public class MyApp {
    public static void main(String[] args) throws FileNotFoundException, URISyntaxException, NoSuchFieldException {
        Session session = new Session();
        Parser parser = new Parser();

        //absolute or relative filepath to your json file
        //String filepath = "/Users/georgeburin/persistence-framework/src/main/resources/input.json";
        String filepath = "src/main/resources/input.json";

        List<Object> objects = parser.parse(filepath);

        //put all posts to the session class
        for (Object obj : objects) {
            if (obj instanceof Post) {  // Ensure it's actually a Post
                session.add((Post) obj);   // Cast and add to the new list
            } else {
                System.out.println("Warning: Found non-Post object in list.");
            }
        }

        session.persistAll();






    }
}
