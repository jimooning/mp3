
package com.example.mp3player

import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBHelper(context: Context,dbName:String, version:Int):SQLiteOpenHelper (context, dbName, null, version){
    override fun onCreate(db: SQLiteDatabase?) {
        //테이블설계
        val createQuery="create table musicTBL(id TEXT primary key, title TEXT, artist TEXT, albumId TEXT, likes INTEGER, duration INTEGER)"
        db?.execSQL(createQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        //테이블제거
        val dropQuery="drop table musicTBL"
        db?.execSQL(dropQuery)
        //onCreate()테이블 다시 생성
        this.onCreate(db)
    }

    //테이블 삽입
    fun insertMusic(music:Music): Boolean{
        var insertFlag=false
        val insertQuery="insert into musicTBL(id, title, artist, albumId, likes, duration)"+
                "values('${music.id}','${music.title}','${music.artist}','${music.albumId}',${music.likes},${music.duration})"
        val db=this.writableDatabase
        try {
            db.execSQL(insertQuery)
            insertFlag=true
        }catch (e:SQLException){
            Log.d("moon", e.toString())
        }finally {
            db.close()
        }
        return insertFlag
    }
    //모든것을 선택할 수 있는 기능
    fun selectMusicAll():MutableList<Music>?{
        var musicList: MutableList<Music>?= mutableListOf<Music>()
        val selectQuery="select * from musicTBL"
        val db=this.readableDatabase
        var cursor:Cursor?=null
        try {
            val cursor=db.rawQuery(selectQuery,null)
            if(cursor.count>0){
                while (cursor.moveToNext()){
                    val id=cursor.getString(0)
                    val title=cursor.getString(1)
                    val artist=cursor.getString(2)
                    val albumId=cursor.getString(3)
                    val likes=cursor.getInt(4)
                    val duration=cursor.getLong(5)
                    val music= Music(id, title, artist, albumId, likes, duration)
                    musicList?.add(music)
                }
            }else{
                musicList=null
            }
        }catch (e:Exception){
            Log.d("moon", e.toString())
            musicList=null
        }finally {
            cursor?.close()
            db.close()
        }
        return musicList
    }
    //조건에 맞는 선택기능
    fun selectMusic(id:String):Music?{
        var music:Music?=null
        val selectQuery="select * from musicTBL where id = '{$id}'"
        val db=this.readableDatabase
        var cursor:Cursor?=null
        try {
            cursor=db.rawQuery(selectQuery,null)
            if(cursor.count > 0){
                if(cursor.moveToFirst()){
                    val id=cursor.getString(0)
                    val title=cursor.getString(1)
                    val artist=cursor.getString(2)
                    val albumId=cursor.getString(3)
                    val likes=cursor.getInt(4)
                    val duration=cursor.getLong(5)
                    music=Music(id, title, artist, albumId, likes, duration)
                }
             }
            }catch (e:java.lang.Exception){
                Log.d("moon", e.toString())
            music=null
            }finally {
                cursor?.close()
            db.close()
            }
        return music
        }

  fun updateLike(music: Music) {

    }

}
