package org.ffmmx.example.musicplayer

import org.scaloid.common._
import android.widget._
import android.content.{Intent, Context, BroadcastReceiver}
import android.widget.SeekBar.OnSeekBarChangeListener
import android.view._
import scala.collection.mutable.{ListBuffer, Stack}
import android.os.Environment
import java.io.{FileFilter, File}
import scala.collection.mutable
import scala.annotation.tailrec
import scala.collection.JavaConverters._


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

    //测试文件选择
    startActivity(SIntent[FileDialog])
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
        //  打开一个对话框,添加音乐文件
        startActivity(SIntent[FileDialog])
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
  //文件选择广播
  val FILE_DIALOG_ACTION = "org.ffmmx.example.musicplayer.FileDialog"
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

class FileDialog extends SActivity {
  val HOME_PATH = Environment.getExternalStorageDirectory.getPath
  val LOCATION_LABEL = "位置: "
  val locationList: ListBuffer[File] = ListBuffer[File]()
  var current: File = new File(HOME_PATH)

  override def basis = this

  implicit override val ctx: SActivity = this
  var location: TextView = _
  var enterButton: Button = _
  var fileListView: ListView = _
  var allSelectCheckbox: CheckBox = _
  val fileFilter = new FileFilter {
    def accept(file: File): Boolean = {
      file.getName match {
        case ".android_secure" => false
        case _ => true
      }
    }
  }
  onCreate {
    setContentView(R.layout.file_dialog)

    location = find[TextView](R.id.location)
    enterButton = find[Button](R.id.filedialog_enter)
      .onClick {
      val adapter = fileListView.adapter.asInstanceOf[FileListAdapter]
      sendBroadcast(new Intent(Constants.FILE_DIALOG_ACTION)
        .putExtra("selectFiles", selectFiles(adapter.selecteds.filter(_._2).map(m => adapter.data(m._1)).toList // 发送选择的文件列表
      )))
    }
    fileListView = find[ListView](R.id.fileListView)
    allSelectCheckbox = find[CheckBox](R.id.filedialog_checkbox).onCheckedChanged {
      //全选按钮实现
      val adapter = fileListView.adapter.asInstanceOf[FileListAdapter]
      if (allSelectCheckbox.isChecked)
        (0 to adapter.getCount).foreach(x => adapter.selecteds += (x -> true))
      else (0 to adapter.getCount).foreach(x => adapter.selecteds += (x -> false))
    }
    location.text(LOCATION_LABEL + HOME_PATH)

    if (Environment.getExternalStorageDirectory.canRead) {
      fileListView.adapter(new FileListAdapter(Environment.getExternalStorageDirectory.listFiles(fileFilter).toList))
    }

  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.filedialogmenu_back =>
        back()
      case R.id.filedialogmenu_home =>
        openDir(new File(HOME_PATH))
    }
    super.onOptionsItemSelected(item)
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.file_dialog_menu, menu)
    super.onCreateOptionsMenu(menu)
  }


  /**
   * 打开文件夹
   * @param dir 文件夹
   */
  def openDir(dir: File) {
    if (!dir.isDirectory)
      throw new RuntimeException("dir必须为文件夹")
    locationList += current
    jump(dir)

  }

  /**
   * 后退
   */
  def back() {
    if (!locationList.isEmpty) {
      jump(locationList.remove(locationList.size - 1))
    }

  }

  /**
   * 跳转
   * @param dir
   */
  private def jump(dir: File) {
    current = dir
    location.text(LOCATION_LABEL + dir.getPath)
    fileListView.adapter.asInstanceOf[FileListAdapter].data = dir.listFiles().toList
    fileListView.adapter.asInstanceOf[FileListAdapter].notifyDataSetChanged()
  }

  /**
   * 得到所选择的文件
   */
  def selectFiles(files: List[File]): Array[File] = {
    def findFiles(files: List[File]): List[File] = {
      files.flatMap {
        file => file.isDirectory match {
          case false => List(file)
          case true => findFiles(file.listFiles(fileFilter).toList)
        }
      }.toList
    }

    findFiles(files).toArray
  }

  class FileList(val checkbox: CheckBox, val img: ImageView, val filename: TextView)

  class FileListAdapter(var data: List[File])(implicit context: Context) extends BaseAdapter {
    var selecteds = Map[Int, Boolean]()

    def getCount: Int = data.size

    def getItem(position: Int): File = data(position)

    def getItemId(position: Int): Long = position

    def getView(position: Int, convertView: View, parent: ViewGroup): View = {
      var fileList: FileList = null
      var filelistView: View = convertView
      filelistView match {
        case null =>
          filelistView = LayoutInflater.from(context).inflate(R.layout.filelist, null)
          fileList = new FileList(filelistView.find[CheckBox](R.id.filelist_checkbox),
            filelistView.find[ImageView](R.id.filelist_img),
            filelistView.find[TextView](R.id.filelist_filename)
          )
          filelistView.tag(fileList)
        case _ =>
          fileList = filelistView.tag.asInstanceOf[FileList]
      }
      fileList.filename.text(data(position).getName)
      fileList.img.imageResource(data(position).isDirectory match {
        case true => R.drawable.gtk_directory
        case false => R.drawable.gtk_file
      })

      fileList.checkbox.onCheckedChanged {
        if (fileList.checkbox.isChecked)
          selecteds += (position -> true)
        else
          selecteds -= position
      }.setChecked(selecteds.getOrElse(position, false))
      filelistView.onClick {
        v =>
          if (v.id != R.id.filedialog_checkbox) {
            if (data(position).isDirectory)
              openDir(data(position))
            else if (fileList.checkbox.isChecked)
              fileList.checkbox.setChecked(true)
            else
              fileList.checkbox.setChecked(false)
          }
      }

    }
  }

}
