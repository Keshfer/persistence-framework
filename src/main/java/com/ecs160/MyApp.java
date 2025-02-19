package com.ecs160;


import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

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

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter the ID: (0 to quit)");
            int id = -1;
            try {
                id = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
                continue;
            }

            if (id == -1){
                System.out.println("provide a valid id");
                continue;
            }

            if (id==0){
                System.out.println("Quitting...");
                break;
            }


            Post fetchPost = new Post();
            fetchPost.setPostId(id);
            Object statusPost = session.load(fetchPost);

            //statusPost is used to check if post data was loaded into fetchPost successfully.
            //Use fetchPost tp print out its content
            if(statusPost == null) {
                System.out.println("the load function returned a null object");
                continue;
            }

            // fetchPost should be populated
            System.out.println("Printing post of id " + fetchPost.getPostId());
            System.out.println("Post content: " + fetchPost.getPostContent());
            System.out.println("replies:");
            List<Post> replyList = fetchPost.getReplies();
            if (replyList == null) {
                System.out.println("No replies");
            } else {
                for (Post reply : replyList) {
                    System.out.println("\treply ID: " + reply.getPostId());
                    System.out.println("\treply content: " + reply.getPostContent());
                }
            }
        }

        scanner.close();
        
    }
}
