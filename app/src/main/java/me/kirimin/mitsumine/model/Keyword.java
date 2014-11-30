package me.kirimin.mitsumine.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "keyword")
public class Keyword extends Model {

    @Column(name = "word", unique = true)
    public String word;
}
