package org.ffmmx.example.musicplayer

import org.scaloid.common._
import android.widget._
import android.content.{Intent, Context, BroadcastReceiver}
import android.widget.SeekBar.OnSeekBarChangeListener


class MainActivity extends SActivity {
  override def basis: SActivity = this

  implicit override val ctx: SActivity = basis

  var receiver: BroadcastReceiver = _

  var nowPlay: Song = new Song(R.raw.test_music, "测试", "semon", 290000)
  var playList: List[Song] = _
  var songTitleView: TextView = _
  var songAuthorView: TextView = _
  var songTimeLengthView: TextView = _
  var playPauseButton: ImageButton = _
  var previousButton: ImageButton = _
  var nextButton: ImageButton = _
  var seekBar: SeekBar = _
  onCreate({
    setContentView(R.layout.main)

    songTitleView = find[TextView](R.id.songTitleView)
    songAuthorView = find[TextView](R.id.songAuthorView)
    songTimeLengthView = find[TextView](R.id.songTimeLengthView)

    //播放暂停按钮
    playPauseButton = find[ImageButton](R.id.playPauseButton)
      .onClick({
      playPause()
    })

    //上一首按钮
    previousButton = find[ImageButton](R.id.previousButton)
      .onClick({
      previous()
    })

    //下一首按钮
    nextButton = find[ImageButton](R.id.nextButton)
      .onClick({
      next()
    })
    // 进度条
    seekBar = find[SeekBar](R.id.seekBar).onSeekBarChangeListener(new OnSeekBarChangeListener {
      def onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        //在进度改变的时候
      }

      def onStopTrackingTouch(seekBar: SeekBar) {
        //在停止拖动的时候
        nowPlay.curTime = (seekBar.progress / 100.0 * nowPlay.length).toInt
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

  def updateSeekBar(song: Song) {
    runOnUiThread {
      seekBar.progress = song.curTime * 100 / song.length
      seekBar.secondaryProgress = seekBar.progress
    }

  }

  def play(song: Song) {
    nowPlay = song
    nowPlay.curTime = 0
  }

  def previous() {
    // 发送上一首请求到播放服务
    sendBroadcast(
      new Intent(Constants.MUSIC_SERVICE_ACTION)
        .putExtra("action", Constants.PLAY_ACTION_PREVIOUS)
        .putExtra("song", nowPlay)

    )
  }

  def next() {
    // 发送下一首请求到播放服务
    sendBroadcast(
      new Intent(Constants.MUSIC_SERVICE_ACTION)
        .putExtra("action", Constants.PLAY_ACTION_NEXT)
        .putExtra("song", nowPlay)
    )
  }

  def playPause() {
    if (nowPlay == null) {
      try {
        play(playList(0))
      }
    }

    if (nowPlay != null)
    // 发送播放或者停止请求到播放服务
      sendBroadcast(
        new Intent(Constants.MUSIC_SERVICE_ACTION)
          .putExtra("action", Constants.PLAY_ACTION_PLAYPAUSE)
          .putExtra("song", nowPlay)
      )


  }
}

class MusicPlayerBroadcastReceiver extends BroadcastReceiver {
  def onReceive(context: Context, intent: Intent) {
    // 更新界面图标
    val container = context.asInstanceOf[MainActivity]
    val songTitleView: TextView = container.songTitleView
    val songAuthorView: TextView = container.songAuthorView
    val songTimeLengthView: TextView = container.songTimeLengthView

    val playPauseButton = container.playPauseButton

    intent.getIntExtra("status", Constants.PLAY_STATUS_STOP) match {
      case Constants.PLAY_STATUS_PLAY =>
        playPauseButton.imageResource(R.drawable.pause_sel)
      case _ =>
        playPauseButton.imageResource(R.drawable.play_sel)
    }

    // 更新界面歌曲
    val song = intent.getSerializableExtra("song").asInstanceOf[Song]
    if (song.author != null)
      songAuthorView.text = song.author
    if (song.title != null)
      songTitleView.text = song.title
    // 更新界面时间
    if (song.curTime != 0) {
      if (container.nowPlay != null)
        container.nowPlay.curTime = song.curTime
      songTimeLengthView.text = song.curTime / 1000 / 60 + ":" + song.curTime / 1000 % 60 + " / " + song.length / 1000 / 60 + ":" + song.length / 1000 % 60
    }
    // 更新进度条
    if (!container.seekBar.isPressed)
      container.updateSeekBar(song)
  }
}

class Song(val songId: Int, val title: String, val author: String) extends Serializable {
  var length: Int = 0
  var bitrate: Int = 0
  var star: Int = 0
  var playTimes: Int = 0
  var curTime: Int = 0

  def this(songId: Int, title: String, author: String, length: Int, bitrate: Int, star: Int, playTimes: Int, curTime: Int) {
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