package me.kirimin.mitsumine.model

import com.activeandroid.Model
import com.activeandroid.annotation.Column
import com.activeandroid.annotation.Table

Table(name = "userid")
public class UserId : Model() {

    Column(name = "word", unique = true)
    public var word: String = ""
}
