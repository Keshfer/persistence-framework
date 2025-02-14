package com.ecs160;

import java.util.List;

public class Post {
    private int postId; //id of the post
    private String postContent; // text of the post
    private List<Integer> replies; // list of post ids that are replies to this post

    Post (int postId, String postContent, List<Integer> replies){
        this.postId = postId;
        this.postContent = postContent;
        this.replies = replies;
    }

    public int getPostId() {
        return postId;
    }

    public String getPostContent() {
        return postContent;
    }

    public List<Integer> getReplies() {
        return replies;
    }


}
