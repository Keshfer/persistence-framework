package com.ecs160;

import com.ecs160.persistence.Persistable;
import com.ecs160.persistence.PersistableField;
import com.ecs160.persistence.PersistableId;
import com.ecs160.persistence.PersistableListField;

import java.util.List;

@Persistable
public class Post {
    @PersistableId
    private Integer postId; //id of the post
    @PersistableField
    private String postContent; // text of the post
    @PersistableListField(className = "Post")
    private List<Post> replies; // list of post ids that are replies to this post

    public Post (int postId, String postContent, List<Post> replies){
        this.postId = postId;
        this.postContent = postContent;
        this.replies = replies;
    }
    public Post () {
        this.postId = null;
        this.postContent = null;
        this.replies = null;

    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(int id) {
        this.postId = id;
    }

    public String getPostContent() {
        return postContent;
    }

    public List<Post> getReplies() {
        return replies;
    }


}
