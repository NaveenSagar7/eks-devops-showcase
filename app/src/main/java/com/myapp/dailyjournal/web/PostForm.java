package com.myapp.dailyjournal.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PostForm {

    @NotBlank(message = "Write something before saving")
    @Size(max = 5000, message = "That's a lot to say - keep it under 5000 characters")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
