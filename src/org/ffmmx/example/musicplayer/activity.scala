package org.ffmmx.example.musicplayer

import org.scaloid.common._
import android.widget._
import android.content.{Intent, Context, BroadcastReceiver}
import android.widget.SeekBar.OnSeekBarChangeListener
import android.view._
import scala.collection.mutable.ListBuffer
import scala.concurrent
import scala.concurrent.ExecutionContext.Implicits.global


class MainActivity extends SActivity {
  override def basis: SActivity = this

  implicit override val ctx: SActivity = basis

  var receiver: BroadcastReceiver = _

  var nowPlay: Song = new Song(R.raw.test_music, "测试", "semon", 290000)
  val playList: ListBuffer[Song] = ListBuffer(nowPlay)
  var previousPlay: Song = nowPlay
  var nextPlay: Song = nowPlay

  var songTitleView: TextView = _
  var songAuthorView: TextView = _
  var songTimeLengthView: TextView = _
  var playPauseButton: ImageButton = _
  var previousButton: ImageButton = _
  var nextButton: ImageButton = _
  var seekBar: SeekBar = _
  var playListView: ListView = _

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
        sendBroadcast(new Intent(Constants.MUSIC_SERVICE_ACTION)
          .putExtra("action", Constants.PLAY_ACTION_RESUME_UPDATE_SEEKBAR))
      }

      def onStartTrackingTouch(seekBar: SeekBar) {
        //开始拖动的时候
        sendBroadcast(new Intent(Constants.MUSIC_SERVICE_ACTION)
          .putExtra("action", Constants.PLAY_ACTION_SUSPEND_UPDATE_SEEKBAR))
      }
    })

    // todo 播放列表
    playListView = find[ListView](R.id.playListView)
    playListView.adapter(new PlayListAdapter(playList))

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

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.main_menu, menu)
    super.onCreateOptionsMenu(menu)
  }

  def updateSeekBar(song: Song) {
    seekBar.progress = song.curTime * 100 / song.length
    seekBar.secondaryProgress = seekBar.progress
  }

  def prepare(song: Song) {
    nowPlay = song
    nowPlay.curTime = 0
  }

  /**
   * 上一首
   */
  def previous() {
    prepare(previousPlay)
    playPause()
  }

  /**
   * 下一首
   */
  def next() {
    prepare(nextPlay)
    playPause()
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.mainmenu_add =>
        // todo 打开一个对话框,添加音乐文件
      case R.id.mainmenu_about =>
        alert("关于", "一个用于测试的简单播放器")
      case R.id.mainmenu_setting =>
      // todo 打开设置界面,然后设置媒体库或者搜索音乐文件的文件夹
      case R.id.mainmenu_quit =>
        stopService(SIntent[MusicPlayService])
        finish()
    }

    super.onOptionsItemSelected(item)
  }

  def playPause() {
    if (nowPlay == null) {
      try {
        prepare(playList(0))
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

  class PlayListItem(val seq: TextView, val author: TextView, val title: TextView, val length: TextView)

  /**
   * 播放列表配置器
   */
  class PlayListAdapter(val data: ListBuffer[Song])(implicit val context: Context) extends BaseAdapter {
    def getCount: Int = data.size

    def getItem(position: Int): Song = data(position)

    def getItemId(position: Int): Long = position

    def getView(position: Int, convertView: View, parent: ViewGroup): View = {
      var playListItem: PlayListItem = null
      var resultView: View = convertView
      if (resultView == null) {
        resultView = LayoutInflater.from(context).inflate(R.layout.playlist, null)
        playListItem = new PlayListItem(
          resultView.find[TextView](R.id.seq),
          resultView.find[TextView](R.id.author),
          resultView.find[TextView](R.id.title),
          resultView.find[TextView](R.id.length)
        )
        resultView.tag(playListItem)
      }
      else {
        playListItem = resultView.tag.asInstanceOf[PlayListItem]
      }
      if (playListItem != null) {
        playListItem.seq.text(position.toString)
        playListItem.author.text = data(position).author
        playListItem.title.text = data(position).title
        playListItem.length.text = data(position).length / 1000 / 60 + ":" + data(position).length / 1000 % 60
      }
      resultView
    }
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

    if (intent.getExtras.containsKey("status"))
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
  val PLAY_ACTION_PLAYPAUSE = 0
  val PLAY_ACTION_PAUSE = 1
  val PLAY_ACTION_STOP = 2
  val PLAY_ACTION_PREVIOUS = 3
  val PLAY_ACTION_NEXT = 4
  val PLAY_ACTION_SEEK = 5
  val PLAY_ACTION_SUSPEND_UPDATE_SEEKBAR = 6
  val PLAY_ACTION_RESUME_UPDATE_SEEKBAR = 7
}