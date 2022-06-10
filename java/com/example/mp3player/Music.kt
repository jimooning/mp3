package com.example.mp3player

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import java.io.IOException
import java.io.Serializable

class Music (id:String, title:String?, artist:String?, albumId:String, likes:Int, duration:Long?):Serializable{
    //멤버변수
    var id:String=""
    var title:String?=null
    var artist:String?=null
    var albumId:String?=null
    var likes:Int?=null
    var duration:Long?=0

    //생성자 맴버변수 초기화
    init {
        this.id=id
        this.title=title
        this.artist=artist
        this.albumId=albumId
        this.likes=likes
        this.duration=duration
    }

    //음악정보를 가져오기 위한 경로 Uri 얻기(음악정보)
    fun getMusicUri(): Uri {
        return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id)
    }

    //컨텐츠리졸버 이용해 앨범정보을 가져오기 위해 해당 경로 Uri정보를 가져오기 위한 함수
    fun getAlbumUri(): Uri {
        return Uri.parse("content://media/external/audio/albumart/"+albumId)
    }

    //해당되는 음악에 이미지를 내가 원하는 사이즈로 비트맵 만들어 돌려주기
    fun getAlbumImage(context: Context, albumImageSize:Int): Bitmap?{
        val contentResolver: ContentResolver =context.getContentResolver()
        //앨범경로uri
        val uri=getAlbumUri()
        //앨범정보를 저장하기 위한 options
        val options = BitmapFactory.Options()

        if(uri != null) {
            var parcelFileDescriptor: ParcelFileDescriptor? = null
            try {
                //외부파일에 있는 이미지파일을 가져오기 위한 스트림
                parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")

                var bitmap = BitmapFactory.decodeFileDescriptor(
                    parcelFileDescriptor!!.fileDescriptor, null, options)

                //비트맵을 가져와 사이즈를 결정한다(원본사이즈가 맞지 않을 경우 내가 원하는 사이즈로 조절한다)
                if (bitmap != null) {
                    if (options.outHeight !== albumImageSize || options.outWidth !== albumImageSize) {
                        val tempBitmap =
                            Bitmap.createScaledBitmap(bitmap, albumImageSize, albumImageSize, true)
                        bitmap.recycle()
                        bitmap = tempBitmap
                    }
                }
                return bitmap
            } catch (e: Exception) {
                Log.d("moon", "getAlbumImage() ${e.toString()}")
            } finally {
                try {
                    parcelFileDescriptor?.close()
                }catch (e: IOException){
                    Log.d("moon", "parcelFileDescriptor?.close() ${e.toString()}")
                }
            }
        }//end of if(uri != null)
        return null
    }
}