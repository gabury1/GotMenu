package org.TUK.gotMenu.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Entity
@Table(name="comment")
public class Comment
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column(name="comment_no", nullable = false)
    int commentNo; // 기본키

    @Column(nullable = false)
    String content;
    String recomment;
    @NotNull
    String resistDate;

    @ManyToOne(cascade = CascadeType.DETACH)
    User user;
    @ManyToOne(cascade = CascadeType.DETACH)
    Menu menu;

}
