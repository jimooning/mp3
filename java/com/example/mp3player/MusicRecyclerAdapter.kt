package com.example.mp3player

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mp3player.databinding.ItemViewBinding


//1. 매개변수 (context, 컬렉션 프레임 워크)     3. 상속처리 RecyclerView.Adapter<MusicRecyclerAdapter.CustomViewHolder>()
class MusicRecyclerAdapter(val context:Context, val musicList: MutableList<Music>?):RecyclerView.Adapter<MusicRecyclerAdapter.CustomViewHolder>() {
    //이미지 사이즈 정의
    val ALBUM_IMAGE_SIZE=80
    val dbHelper: DBHelper by lazy { DBHelper(context, "musicDB", 1) }
    //4. 오버라이딩(자동으로처리ctrl+i)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding= ItemViewBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding=(holder as CustomViewHolder).binding
        val music=musicList?.get(position)
        binding.tvArtist.text=music?.artist      //가수명
        binding.tvTitle.text=music?.title        //노래제목
       // binding.ivLike.setImageResource(R.drawable.like)
        val bitmap:Bitmap?=music?.getAlbumImage(context, ALBUM_IMAGE_SIZE)
        if(bitmap != null){
            binding.ivAlbumArt.setImageBitmap(bitmap)
        }else{
            //앨범이미지가 없을때는 디폴트 이미지를 준다
            binding.ivAlbumArt.setImageResource(R.drawable.music)
        }
        //해당항목을 클릭하면 해당되는 music객채를 가지고 PlaymusicActivity 화면으로 넘어간다
        binding.root.setOnClickListener {
            //액티비티로 음악정보를 보내 음악을 재생해 주는 액티비티를 설계
           val intent= Intent(binding.root.context,PlaymusicActivity::class.java)
            intent.putExtra("music",music)
            binding.root.context.startActivity(intent)

        }
        when(music?.likes){
            0->{
                binding.ivLike.setImageResource(R.drawable.like)
            }
            1->{
                binding.ivLike.setImageResource(R.drawable.like2)
            }

        }

        binding.ivLike.setOnClickListener {
            if(music?.likes == 1){
                binding.ivLike.setImageResource(R.drawable.like2)
                music?.likes = 0
                if (music != null) {
                    dbHelper.updateLike(music)
                   // Toast.makeText(this,"좋아하는 음악으로 등록",Toast.LENGTH_SHORT).show()
                }
                notifyDataSetChanged()
            }else{
                binding.ivLike.setImageResource(R.drawable.like)
                music?.likes = 1
                if (music != null) {
                    dbHelper.updateLike(music)
                }
                notifyDataSetChanged()
            }

        }

    }

    override fun getItemCount(): Int {
        return musicList?.size ?:0
    }

    //2. 뷰홀더 내부선언(바인딩)
    class CustomViewHolder(val binding:ItemViewBinding): RecyclerView.ViewHolder(binding.root)


}