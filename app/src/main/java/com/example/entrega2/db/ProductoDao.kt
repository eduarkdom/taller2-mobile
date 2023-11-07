package com.example.entrega2.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProductoDao {

    @Query("SELECT COUNT(*) FROM producto")
    fun count():Int
    @Query("SELECT * FROM producto ORDER BY comprado ASC")
    fun getAll():List<Producto>
    @Query("SELECT * FROM producto WHERE id = :id")
    fun findById(id:Int):Producto
    @Insert
    fun insert(producto:Producto):Long
    @Insert
    fun insertAll(vararg productos:Producto)
    @Update
    fun update(vararg productos:Producto)
    @Delete
    fun delete(producto:Producto)
}
