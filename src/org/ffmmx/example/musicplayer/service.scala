package org.ffmmx.example.musicplayer

import org.scaloid.common._
import android.content.Intent
import android.os.IBinder


class MusicPlayService extends SService{
  var status = Constants.PLAY_STATUS_PAUSE

  def onBind(intent: Intent): IBinder = null

  override def onCreate(): Unit = {
    // TODO 播放服务实现

    // 音乐服务广播
    broadcastReceiver(Constants.MUSIC_SERVICE_ACTION) {
      (context,intent) => {
        intent.getStringExtra("action") match {
          case Constants.PLAY_ACTION_PLAYPAUSE =>
            // TODO 播放暂停实现
          case Constants.PLAY_ACTION_STOP =>
            // TODO 实现停止
          case Constants.PLAY_ACTION_PREVIOUS =>
            // TODO 实现上一首
          case Constants.PLAY_ACTION_NEXT  =>
            // TODO 实现下一首

        }
      }
    }
  }
}
