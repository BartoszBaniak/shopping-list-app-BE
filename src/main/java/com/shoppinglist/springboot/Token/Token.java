package com.shoppinglist.springboot.Token;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "token")

public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false)
    private String id;
    @Column(name = "user_id", updatable = false)
    private String userID;
    @Column(name = "content", nullable = false)
    private String content;

    public Token() {
    }

    public Token(String userID, String content) {
        this.userID = userID;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String id) {
        this.userID = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return Objects.equals(id, token.id) && Objects.equals(userID, token.userID) && Objects.equals(content, token.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userID, content);
    }

    @Override
    public String toString() {
        return "Token{" +
                "id=" + id +
                ", userID=" + userID +
                ", content='" + content + '\'' +
                '}';
    }
}