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

        //absolute filepath to your json file
        //String filepath = "/Users/georgeburin/persistence-framework/src/main/resources/input.json";
        String filepath = "src/main/resources/input.json";


        try (FileReader reader = new FileReader(filepath)) {
            //read the file into JsonObject using gson library
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            System.out.println("Json file loaded");

            // Extract "feed" array
            JsonArray feed = jsonObject.getAsJsonArray("feed");

            //value to generate new posts ids.
            int postId = 1; // Start from 1

            //loop through it to get all threads
            for (JsonElement element : feed){
                JsonObject thread = element.getAsJsonObject().getAsJsonObject("thread");

                // Check if "thread" exists and is not null
                if (thread == null) {
                    //System.out.println("Skipping because missing 'thread'");
                    continue; // Skip this and move to the next one
                }

                JsonObject post = thread.getAsJsonObject("post");

                //extract the text of the post
                JsonObject record = post.getAsJsonObject("record");
                String text = record.get("text").getAsString();
                //String createdAt = record.get("createdAt").getAsString(); //for the future if needed



                //add all of the replies posts first, but keep track of the ids of the posts
                JsonArray replies = thread.getAsJsonArray("replies");
                List<Post> repliesIDs = new ArrayList<>();
                if (replies != null) {
                    for (JsonElement replyElement : replies) {
                        // Extract reply text:
                        JsonObject reply = replyElement.getAsJsonObject();
                        JsonObject replyPost = reply.getAsJsonObject("post");
                        JsonObject replypostrecord = replyPost.getAsJsonObject().getAsJsonObject("record");
                        String replyText = replypostrecord.get("text").getAsString();


                        Post replyObject = new Post(postId,replyText, new ArrayList<>());
                        session.add(replyObject);
                        repliesIDs.add(replyObject);
                        postId+=1;
                    }
                }

                //add the root post to the list
                Post rootPost = new Post(postId,text,repliesIDs);
                session.add(rootPost);
                postId+=1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        session.persistAll();






    }
}
