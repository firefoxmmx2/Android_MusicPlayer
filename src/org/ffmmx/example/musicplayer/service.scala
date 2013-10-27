package org.ffmmx.example.musicplayer

import org.scaloid.common._
import android.content.Intent
import android.os.IBinder


class MusicPlayService extends SService{
  var status = Constants.PLAY_STATUS_PAUSE

  def onBind(intent: Intent): IBinder = null

  override def onCreate(): Unit = {

    // 音乐服务广播
    broadcastReceiver(Constants.MUSIC_SERVICE_ACTION) {
      (context,intent) => {
        intent.getStringExtra("action") match {
          case Constants.PLAY_ACTION_PLAYPAUSE =>
            // TODO 播放暂停实现
            println("="*13+"PLAY_ACTION_PLAYPAUSE"+"="*13)
            playPause(intent)
          case Constants.PLAY_ACTION_STOP =>
            // TODO 实现停止
            stopPlay()
          case Constants.PLAY_ACTION_PREVIOUS =>
            // TODO 实现上一首
            playPrevious()
          case Constants.PLAY_ACTION_NEXT  =>
            // TODO 实现下一首
            playNext()

        }
      }
    }
  }

  def playPause(intent:Intent) {
    status match {
      case Constants.PLAY_STATUS_STOP =>
        status = Constants.PLAY_STATUS_PLAY
        play(intent.getStringExtra("song"))
        sendBroadcast(
          SIntent(Constants.MUSIC_PLAYER_ACTION)
          .putExtra("status",status)
        )
      case Constants.PLAY_STATUS_PLAY =>
        status=Constants.PLAY_STATUS_PAUSE
        pause()
        sendBroadcast(
          SIntent(Constants.MUSIC_PLAYER_ACTION)
            .putExtra("status",status)
        )

      case Constants.PLAY_STATUS_PAUSE =>
        status=Constants.PLAY_STATUS_PLAY
//        play()
        sendBroadcast(
          SIntent(Constants.MUSIC_PLAYER_ACTION).putExtra("status",status)
        )
    }
  }

  def stopPlay() {
    status = Constants.PLAY_STATUS_STOP
    sendBroadcast(SIntent(Constants.MUSIC_PLAYER_ACTION).putExtra("status",status))
  }

  def playPrevious(){

  }
  def playNext(){

  }

  def play(musicPath:String) {

  }

  def pause() {

  }

}
