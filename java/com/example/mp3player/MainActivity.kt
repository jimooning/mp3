package com.example.mp3player



import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mp3player.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    //데이터베이스 상수화
    companion object{
        val DB_NAME="musicDB"
        val VERSOIN=1
    }
    lateinit var binding: ActivityMainBinding
    //승인받아야 할 항목 permission요청
    val permission= arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    val REQUEST_READ=200

    //데이터베이스 객체화
    //val dbHelper:DBHelper by lazy { DBHelper(this,"musicDB",1) }
    val dbHelper:DBHelper by lazy { DBHelper(this, DB_NAME, VERSOIN) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //승인 > 음악파일기져옴,  승인거절 > 재요청
        if(isPermitted()==true){
            //실행 (외부파일을 가져와 컬렉션프레임워크에 저장 후 어뎁터를 호출한다)
            startProcess()
        }else{
            //승인거절시 승인요청 다시한다(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            // 요청이 승인이 되면 콜백함수(onRequestPermissionsResult)로 승인 결과값을 알려준다
            ActivityCompat.requestPermissions(this, permission, REQUEST_READ)

        }
    }

    //승인요청시 승인결과의 대한 콜백함수
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==REQUEST_READ){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                //실행 (외부파일을 가져와 컬렉션프레임워크에 저장 후 어뎁터를 호출한다)
                startProcess()
            }else{
                Toast.makeText(this,"권한요청이 승인이되어야 앱 실행 가능함",Toast.LENGTH_SHORT).show()
            }
        }
    }


    //외부파일 읽기 승인요청을 확인한다
    fun isPermitted():Boolean{
        if(ContextCompat.checkSelfPermission(this,permission[0]) != PackageManager.PERMISSION_GRANTED){
            return false
        }else{
            return true
        }
    }

    //외부파일로부터 모든 음악정보를 가져오는 함수
    private fun startProcess() {
        var musicList:MutableList<Music>?= mutableListOf<Music>()

        //1. musicTBL에서 정보를 가져온다(musicTBL에있으면 > 리사이클러뷰에 보여주고,
        //없으면 > getMusicList 가져와 musicTBL에 저장 후 리사이클러뷰에 보여준다

        musicList=dbHelper.selectMusicAll()

        if(musicList == null || musicList!!.size <= 0){
            //getMusicList 가져와 musicTBL에 저장
            musicList=getMusicList()
            //musicTBL에 모두 저장하고
            for(i in 0..(musicList!!.size-1)){
                val music=musicList.get(i)
                if(dbHelper.insertMusic(music)==false){
                    Log.d("moon","insert error ${music.toString()}")
                }
            }
            Log.d("moon","테이블에 없어 getMusicList()가져옴")
        }else{
            Log.d("moon","테이블에 있어서 내용을 가져와 보여줌")
        }
        val musicRecyclerAdapter=MusicRecyclerAdapter(this,musicList)
        binding.recyclerView.adapter=musicRecyclerAdapter
        binding.recyclerView.layoutManager=LinearLayoutManager(this)


    }
    private fun getMusicList():MutableList<Music>?{
        //1. 외부파일에 음악정보주소를 준다
        val ListUri=MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        //2. 요청해야할 음원정보 컬럼들
        val proj= arrayOf(
            MediaStore.Audio.Media._ID,         //음악id
            MediaStore.Audio.Media.TITLE,       //음악title
            MediaStore.Audio.Media.ARTIST,      //가수
            MediaStore.Audio.Media.ALBUM_ID,    //음악이미지

            MediaStore.Audio.Media.DURATION     //음악시간
        )
        //3. contentResolver.query에 Uri, 요청음원정보컬럼을 요구하고 결과값을 cursor반환
        val cursor=contentResolver.query(ListUri, proj, null, null, null)
        //Music= mp3정보 5가지를 기억하고,mp3파일경로,mp3이미지경로,이미지경로를 내가 원하는 사이즈로 비트맵
        val musicList:MutableList<Music>?= mutableListOf<Music>()
        while (cursor?.moveToNext()==true){
            val id=cursor.getString(0)
            val title=cursor.getString(1)
            val artist=cursor.getString(2)
            val albumId=cursor.getString(3)
            val duration=cursor.getLong(4)

            val music=Music(id, title, artist, albumId, 0, duration)
            musicList?.add(music)
        }
        cursor?.close()
        return musicList
    }
}