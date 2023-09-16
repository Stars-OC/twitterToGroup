package xyz.starsoc.object;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

public class Tweet {
    private String text;
    //考虑有多个文件
    //TODO video后面再试
    private FileOutputStream video;

    private Set<String> image = new HashSet<>();

    public Tweet() {
    }

    public Tweet(String text, Set<String> image) {
        this.text = text;
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Set<String> getImage() {
        return image;
    }

    public void setImage(Set<String> image) {
        this.image = image;
    }
}
