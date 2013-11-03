package org.ffmmx.example.musicplayer

import org.scaloid.common._
import android.content.{Context, BroadcastReceiver, Intent}
import android.os.IBinder
import android.media.MediaPlayer


class MusicPlayService extends SService{
  var status = Constants.PLAY_STATUS_PAUSE
  var receiver:BroadcastReceiver = _
  def onBind(intent: Intent): IBinder = null
  var player:MediaPlayer = _
  onCreate({
    // 注册音乐服务广播
    receiver = new MusicPlayServiceBroadcastReceiver
    registerReceiver(receiver,Constants.MUSIC_SERVICE_ACTION)

  })


  onDestroy({
//    注销音乐服务接收器
    unregisterReceiver(receiver)
  })

  def playPause(intent:Intent) {
    status match {
      case Constants.PLAY_STATUS_STOP =>
        status = Constants.PLAY_STATUS_PLAY
        val song = intent.getSerializableExtra("song").asInstanceOf[Song]
        play(song)
        sendBroadcast(
          new Intent(Constants.MUSIC_PLAYER_ACTION)
          .putExtra("status",status)
          .putExtra("songTitle",song.title)
          .putExtra("songAuthor",song.author)
        )
      case Constants.PLAY_STATUS_PLAY =>
        status=Constants.PLAY_STATUS_PAUSE
        pause()
        sendBroadcast(
          new Intent(Constants.MUSIC_PLAYER_ACTION)
            .putExtra("status",status)
        )

      case Constants.PLAY_STATUS_PAUSE =>
        status=Constants.PLAY_STATUS_PLAY
        val song = intent.getSerializableExtra("song").asInstanceOf[Song]
        play(song)
        sendBroadcast(
          new Intent(Constants.MUSIC_PLAYER_ACTION).putExtra("status",status)
            .putExtra("songTitle",song.title)
            .putExtra("songAuthor",song.author)
        )
    }
  }

  def stopPlay() {
    status = Constants.PLAY_STATUS_STOP
    sendBroadcast(new Intent(Constants.MUSIC_PLAYER_ACTION).putExtra("status",status))
  }

  def playPrevious(){
    status = Constants.PLAY_STATUS_PLAY

  }
  def playNext(){
    status = Constants.PLAY_STATUS_PLAY
  }

  def play(song:Song) {
    player = MediaPlayer.create(this,song.songId)
//    player.prepare()
    player.start()
  }

  def pause() {
    if(player!=null&&player.isPlaying){
      player.pause()
    }
  }

}

class MusicPlayServiceBroadcastReceiver extends BroadcastReceiver {
  def onReceive(context: Context, intent: Intent) {
    val service=context.asInstanceOf[MusicPlayService]
    intent.getStringExtra("action") match {
      case Constants.PLAY_ACTION_PLAYPAUSE =>
        // 播放暂停实现
        service.playPause(intent)
      case Constants.PLAY_ACTION_STOP =>
        // TODO 实现停止
        service.stopPlay()
      case Constants.PLAY_ACTION_PREVIOUS =>
        // TODO 实现上一首
        service.playPrevious()
      case Constants.PLAY_ACTION_NEXT  =>
        // TODO 实现下一首
        service.playNext()

    }
  }
}