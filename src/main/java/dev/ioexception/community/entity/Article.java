package dev.ioexception.community.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    @Column(name = "image_url")
    private String imageUrl;
    private int view = 0;
    @Column(name = "`like`")
    private int like = 0;
    private LocalDateTime date;
    @Column(name = "delete_flag")
    private boolean deleteFlag = false;

    public void markAsDeleted() {
        this.deleteFlag = true;
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeContent(String content) {
        this.content = content;
    }

    public void modifyDate() {
        this.date = LocalDateTime.now();
    }

    public void incrementView() {
        this.view += 1;
    }

    public void incrementLike() {
        this.like += 1;
    }

    public void decrementLike() {
        this.like -= 1;
    }
}
