package org.ffmmx.example.musicplayer

import org.scaloid.common._
import android.widget._
import android.content.{Intent, Context, BroadcastReceiver}
import android.widget.SeekBar.OnSeekBarChangeListener


class MainActivity extends SActivity {
  override def basis: SActivity = this

  implicit override val ctx: SActivity = basis

  var receiver: BroadcastReceiver = _

  var nowPlay:Song = new Song(R.raw.test_music, "测试", "semon",290000)
  var playList: List[Song] = _

  onCreate({
    setContentView(R.layout.main)

    //播放暂停按钮
    find[ImageButton](R.id.playPauseButton)
      .onClick({
      // 发送播放或者停止请求到播放服务

      sendBroadcast(
        new Intent(Constants.MUSIC_SERVICE_ACTION)
          .putExtra("action", Constants.PLAY_ACTION_PLAYPAUSE)
          .putExtra("song", nowPlay)

      )
    })

    //上一首按钮
    find[ImageButton](R.id.previousButton)
      .onClick({
      // 发送上一首请求到播放服务
      sendBroadcast(
        new Intent(Constants.MUSIC_SERVICE_ACTION)
          .putExtra("action", Constants.PLAY_ACTION_PREVIOUS)
          .putExtra("song", nowPlay)

      )
    })

    //下一首按钮
    find[ImageButton](R.id.nextButton)
      .onClick({
      // 发送下一首请求到播放服务
      sendBroadcast(
        new Intent(Constants.MUSIC_SERVICE_ACTION)
          .putExtra("action", Constants.PLAY_ACTION_NEXT)
          .putExtra("song", nowPlay)
      )
    })
    // 进度条
    find[SeekBar](R.id.seekBar).onSeekBarChangeListener(new OnSeekBarChangeListener {
      def onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
         //在进度改变的时候
      }

      def onStopTrackingTouch(seekBar: SeekBar) {
        //在停止拖动的时候
        nowPlay.curTime=seekBar.progress/100 * nowPlay.length
        sendBroadcast(
          new Intent(Constants.MUSIC_SERVICE_ACTION)
            .putExtra("action", Constants.PLAY_ACTION_SEEK)
            .putExtra("song", nowPlay)
        )
      }

      def onStartTrackingTouch(seekBar: SeekBar) {
         //开始拖动的时候
      }
    })

    // TODO 播放列表
    // 注册播放器广播
    receiver = new MusicPlayerBroadcastReceiver()
    registerReceiver(receiver, Constants.MUSIC_PLAYER_ACTION)
    //开始播放服务
    startService(SIntent[MusicPlayService])
  })

  onDestroy({
    //注销广播
    unregisterReceiver(receiver)
  })

}

class MusicPlayerBroadcastReceiver extends BroadcastReceiver {
  def onReceive(context: Context, intent: Intent) {
    // 更新界面图标
    val container = context.asInstanceOf[MainActivity]
    val songTitleView: TextView = container.find[TextView](R.id.songTitleView)
    val songAuthorView: TextView = container.find[TextView](R.id.songAuthorView)
    val songTimeLengthView: TextView = container.find[TextView](R.id.songTimeLengthView)

    val playPauseButton = container.find[ImageButton](R.id.playPauseButton)
    intent.getIntExtra("status", Constants.PLAY_STATUS_STOP) match {
      case Constants.PLAY_STATUS_PLAY =>
        playPauseButton.imageResource(R.drawable.pause_sel)
      case _ =>
        playPauseButton.imageResource(R.drawable.play_sel)
    }

    // 更新界面歌曲

    //    val songTitle=intent.getStringExtra("songTitle")
    //    val songAuthor=intent.getStringExtra("songAuthor")
    //    if(songTitle!=null)
    //      songAuthorView.text=songAuthor
    //    if(songAuthor!=null)
    //      songTitleView.text=songTitle
    val song = intent.getSerializableExtra("song").asInstanceOf[Song]
    if (song.author != null)
      songAuthorView.text = song.author
    if (song.title != null)
      songTitleView.text = song.title
    // 更新界面时间
    if (song.length != 0)
      songTimeLengthView.text = song.length
  }
}

class Song(val songId: Int, val title: String, val author: String) extends Serializable {
  var length: Int = 0
  var bitrate: Int = 0
  var star: Int = 0
  var playTimes: Int = 0
  var curTime:Int = 0

  def this(songId: Int, title: String, author: String, length: Int, bitrate: Int, star: Int, playTimes: Int,curTime:Int) {
    this(songId, title, author)
    this.length = length
    this.bitrate = bitrate
    this.star = star
    this.playTimes = playTimes
    this.curTime = curTime
  }

  def this(songId: Int, title: String, author: String, length: Int) {
    this(songId, title, author)
    this.length = length
  }
}

object Constants {
  //播放器广播action
  val MUSIC_PLAYER_ACTION = "org.ffmmx.example.musicplayer.MusicPlayerActivity"
  //播放服务广播action
  val MUSIC_SERVICE_ACTION = "org.ffmmx.example.musicplayer.MusicPlayerService"

  //播放状态 停止
  val PLAY_STATUS_STOP = 0
  //播放状态 播放
  val PLAY_STATUS_PLAY = 1
  //播放状态 暂停
  val PLAY_STATUS_PAUSE = 2

  //播放动作
  val PLAY_ACTION_PLAYPAUSE = "playpause"
  val PLAY_ACTION_STOP = "stop"
  val PLAY_ACTION_PREVIOUS = "previous"
  val PLAY_ACTION_NEXT = "next"
  val PLAY_ACTION_SEEK = "seek"
}