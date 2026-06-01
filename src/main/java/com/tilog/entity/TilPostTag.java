//package com.tilog.entity;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//@Entity
//@Table(name = "til_post_tag",
//        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "tag_id"}))
//@Getter
//@NoArgsConstructor
//public class TilPostTag {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "post_tag_id")
//    private Long postTagId;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "post_id", nullable = false)
//    private TilPost post;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "tag_id", nullable = false)
//    private Tag tag;
//
//    public TilPostTag(TilPost post, Tag tag) {
//        this.post = post;
//        this.tag = tag;
//    }
//}