package org.ffmmx.example.musicplayer

import org.scaloid.common._
import android.content.{Context, BroadcastReceiver, Intent}
import android.os.IBinder


class MusicPlayService extends SService{
  var status = Constants.PLAY_STATUS_PAUSE
  var receiver:BroadcastReceiver = _
  def onBind(intent: Intent): IBinder = null

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
        play(intent.getStringExtra("song"))
        sendBroadcast(
          new Intent(Constants.MUSIC_PLAYER_ACTION)
          .putExtra("status",status)
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
//        play()
        sendBroadcast(
          new Intent(Constants.MUSIC_PLAYER_ACTION).putExtra("status",status)
        )
    }
  }

  def stopPlay() {
    status = Constants.PLAY_STATUS_STOP
    sendBroadcast(new Intent(Constants.MUSIC_PLAYER_ACTION).putExtra("status",status))
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

class MusicPlayServiceBroadcastReceiver extends BroadcastReceiver {
  def onReceive(context: Context, intent: Intent) {
    println("="*13+Constants.MUSIC_SERVICE_ACTION+"="*13)
    val service=context.asInstanceOf[MusicPlayService]
    intent.getStringExtra("action") match {
      case Constants.PLAY_ACTION_PLAYPAUSE =>
        // TODO 播放暂停实现
        println("="*13+"PLAY_ACTION_PLAYPAUSE"+"="*13)
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