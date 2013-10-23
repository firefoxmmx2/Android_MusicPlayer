package org.ffmmx.example.musicplayer

import org.scaloid.common._
import android.content.Intent
import android.os.IBinder


class MusicPlayService extends SService{
  def onBind(intent: Intent): IBinder = null

  override def onCreate(): Unit = {
    // TODO 播放服务实现
  }
}
