package com.example.mp3player

import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.mp3player.databinding.ActivityPlaymusicBinding
import kotlinx.coroutines.*
import java.text.SimpleDateFormat

class PlaymusicActivity : AppCompatActivity() {
    lateinit var binding: ActivityPlaymusicBinding
    //1. 뮤직플레이어 변수
    private var mediaPlayer: MediaPlayer?=null
    //2. 음악정보 객체변수
    private var music:Music?=null
    //3. 음악앨범이미지 사이즈
    private val ALBUM_IMAGE_SIZE=200
    //4. 코루틴 스코프 launch
    private var playerJob: Job?=null

    //private val int position=0
    // private ArrayList<Music>list;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityPlaymusicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        music=intent.getSerializableExtra("music") as Music

        if(music != null){
            binding.tvTitle.text=music?.title
            binding.tvArtist.text=music?.artist
            binding.tvDurationStart.text="00:00"
            binding.tvDrationStop.text= SimpleDateFormat("mm:ss").format(music?.duration)
            val bitmap: Bitmap?=music?.getAlbumImage(this,ALBUM_IMAGE_SIZE)
            if(bitmap != null){
                binding.ivAlbumArt.setImageBitmap(bitmap)
            }else{
                binding.ivAlbumArt.setImageResource(R.drawable.music)
            }
            //음원실행 및 재생
            mediaPlayer= MediaPlayer.create(this,music?.getMusicUri())
            binding.seekBar.max=music?.duration!!.toInt()

            //싱크바 이벤트 설정해 노래와 같이 동기화 처리한다
            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                //싱크바 터치해 이동할 때 발생하는 이벤트 :fromUser=사용자에 의한 터치 유무
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if(fromUser){
                        //미디어플레이어에 음악위치를 싱크바에 값을 가져와 셋팅한다
                        mediaPlayer?.seekTo(progress)
                    }
                }

                //싱크바를 터치하는 순간 발생하는 이벤트
                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                //싱크바를 터치하고 손을 떼는 순간 발생하는 이벤트
                override fun onStopTrackingTouch(p0: SeekBar?) {

                }
            })

        }
    }
    fun onClickView(view: View?){
        //var musicList:MutableList<Music>?= mutableListOf<Music>()



        when(view?.id){
            R.id.ivList->{  //음악정지, 코르틴취소, 음악객체해제, 음악객체=null
                mediaPlayer?.stop()
                playerJob?.cancel()
                //mediaPlayer?.release()
                // mediaPlayer=null
                finish()
            }
            R.id.ivStart->{
                if(mediaPlayer?.isPlaying==true){
                    mediaPlayer?.pause()
                    // binding.seekBar.progress=mediaPlayer?.currentPosition!!
                    binding.ivStart.setImageResource(R.drawable.play)
                }else{
                    mediaPlayer?.start()
                    binding.ivStart.setImageResource(R.drawable.pause)

                    //여기서 음악재생, 싱크바, 시작시간진행을 코루틴으로 진행
                    val backgroundScope= CoroutineScope(Dispatchers.Default+ Job())
                    playerJob=backgroundScope.launch {
                        //음악이 진행이 되면서 음악 진행 상황을 싱크바와 시작진행상황값을 변화시켜줘야한다
                        //***중요 : 사용자가 만든 스레드에서 화면에 있는 뷰값을 변경하게되면 문제가 발생한다
                        //해결방법 : 스레드 안에서 뷰값을 변경하고 싶을때는 runOnUiThread(~~~)호출해준다
                        while (mediaPlayer?.isPlaying==true){
                            //노래를 진행하면서 진행위치값을 싱크바에 적용시킨다
                            runOnUiThread {
                                var currentPosition=mediaPlayer?.currentPosition!!
                                binding.seekBar.progress=currentPosition
                                binding.tvDurationStart.text= SimpleDateFormat("mm:ss").format(currentPosition)
                            }
                            try {
                                delay(500)
                            }catch (e:Exception){
                                Log.d("moon","delay(500) = ${e.toString()}")
                            }
                        }//end of while
                        Log.d("moon", " currentPosition ${mediaPlayer!!.currentPosition}")

                        Log.d("moon", " .max ${binding.seekBar.max}")
                        runOnUiThread {
                            if (mediaPlayer!!.currentPosition >= (binding.seekBar.max - 1000)) {
                                binding.seekBar.progress = 0
                                binding.tvDurationStart.text = "00:00"
                            }
                            binding.ivStart.setImageResource(R.drawable.play)
                        }
//                        binding.seekBar.progress = 0
                    }

                }// end of backgroundScope.launch

            }//end of if(mediaPlayer?.isPlaying == true)else

            R.id.ivStop ->{
                mediaPlayer?.stop()

                playerJob?.cancel()
                mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())
                binding.seekBar.progress = 0
                binding.tvDurationStart.text = "00:00"
                binding.ivStart.setImageResource(R.drawable.play)
            }


        }
    }

}
