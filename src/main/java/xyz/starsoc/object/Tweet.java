package xyz.starsoc.object;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

public class Tweet {
    private String text;
    //考虑有多个文件
    //video后面再试
    private FileOutputStream video;

    private Set<ByteArrayOutputStream> image = new HashSet<>();

    public Tweet() {
    }

    public Tweet(String text, Set<ByteArrayOutputStream> image) {
        this.text = text;
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public FileOutputStream getVideo() {
        return video;
    }

    public void setVideo(FileOutputStream video) {
        this.video = video;
    }

    public Set<ByteArrayOutputStream> getImage() {
        return image;
    }

    public void setImage(Set<ByteArrayOutputStream> image) {
        this.image = image;
    }
}
