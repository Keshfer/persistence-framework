package com.ecs160;


import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.List;

import com.ecs160.persistence.Session;

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

        Post fetchPost = new Post();
        fetchPost.setPostId(4);
        session.load(fetchPost);
        // fetchPost should be populated
        System.out.println("Printing post of id " + fetchPost.getPostId());
        System.out.println("Post content: " + fetchPost.getPostContent());
        System.out.println("replies:");
        List<Post> replyList = fetchPost.getReplies();
        if(replyList == null) {
            System.out.println("No replies");
        } else {
            for (Post reply : replyList) {
                System.out.println("\treply ID: " + reply.getPostId());
                System.out.println("\treply content: " + reply.getPostContent());
            }
        }







    }
}
