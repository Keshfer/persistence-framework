package com.ecs160;

import com.ecs160.persistence.Persistable;
import com.ecs160.persistence.PersistableField;
import com.ecs160.persistence.PersistableId;
import com.ecs160.persistence.PersistableListField;

import java.util.List;

@Persistable
public class Post {
    @PersistableId
    private int postId; //id of the post
    @PersistableField
    private String postContent; // text of the post
    //@PersistableListField(className = "")
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
