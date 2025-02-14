package com.ecs160;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private List<Object> posts = new ArrayList<>();

    public Parser(){

    }

    public List<Object> parse (String path){
        try (FileReader reader = new FileReader(path)) {
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
                        this.posts.add(replyObject);
                        repliesIDs.add(replyObject);
                        postId+=1;
                    }
                }

                //add the root post to the list
                Post rootPost = new Post(postId,text,repliesIDs);
                this.posts.add(rootPost);
                postId+=1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.posts;
    }
}
