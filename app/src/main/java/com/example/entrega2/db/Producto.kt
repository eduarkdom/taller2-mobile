package com.example.entrega2.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Producto(
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    var producto:String,
    var comprado:Boolean
)