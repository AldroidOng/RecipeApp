package com.example.recipe.util

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.provider.Settings.System.getString

class SQLiteHelper(context: Context?, name: String?, factory: CursorFactory?, version: Int) : SQLiteOpenHelper(context, name, factory, version) {
    fun queryData(sql: String?) {
        val database = writableDatabase
        database.execSQL(sql)
    }

    fun insertData(
        recipeType: String?,
        recipeName: String?,
        ingredients: String?,
        steps: String?,
        image: ByteArray?
    ) {
        val database = writableDatabase
        val sql = "INSERT INTO RECIPE VALUES (NULL, ?, ?, ?, ?, ?)"
        val statement = database.compileStatement(sql)
        statement.clearBindings()
        statement.bindString(1, recipeType)
        statement.bindString(2, recipeName)
        statement.bindString(3, ingredients)
        statement.bindString(4, steps)
        statement.bindBlob(5, image)
        statement.executeInsert()
    }

    fun updateData(
        id: Int?,
        recipeType: String?,
        recipeName: String?,
        ingredients: String?,
        steps: String?,
        image: ByteArray?
    ) {
        val database = writableDatabase
        val sql = "UPDATE RECIPE SET recipeType = ?, recipeName = ?, ingredients = ? , steps = ? , image = ? WHERE id = ?"
        val statement = database.compileStatement(sql)

        if (id != null) {
            statement.bindDouble(6, id.toDouble())
            statement.bindString(1, recipeType)
            statement.bindString(2, recipeName)
            statement.bindString(3, ingredients)
            statement.bindString(4, steps)
            statement.bindBlob(5, image)
            statement.execute()
            database.close()
        }
    }

    fun deleteData(id: Int) {
        val database = writableDatabase
        val sql = "DELETE FROM RECIPE WHERE Id = ?"
        val statement = database.compileStatement(sql)
        statement.clearBindings()
        statement.bindDouble(1, id.toDouble())
        statement.execute()
        database.close()
    }

    fun getData(sql: String?): Cursor {
        val database = readableDatabase
        return database.rawQuery(sql, null)
    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {}
    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {}
}