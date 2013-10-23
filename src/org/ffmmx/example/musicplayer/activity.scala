package org.ffmmx.example.musicplayer

import org.scaloid.common._
import android.widget.ImageButton
import android.view.{View, KeyEvent}


class MainActivity extends SActivity {
  override def basis: SActivity = this

  implicit override val ctx: SActivity = basis

  onCreate({
    setContentView(R.layout.main)

    //播放暂停按钮
    val playPauseButton = find[ImageButton](R.id.playPauseButton)
      .onClick({
        // TODO 发送播放或者停止请求到播放服务
    })
      .onKey((v, id, event) => {
      // TODO 播放暂停按钮图标状态改变
      false
    })
    //上一首按钮
    val previousButton = find[ImageButton](R.id.previousButton)
      .onClick({
      // TODO 发送上一首请求到播放服务
    })
      .onKey((v,id,event) => {
      // TODO  上一首按钮图标状态改变
      false
    })
    //下一首按钮
    val nextButton = find[ImageButton](R.id.nextButton)
      .onClick({
      // TODO 发送下一首请求到播放服务
    })
      .onKey((v,id,event) => {
      // TODO  下一首按钮图标状态改变
      false
    })

    // TODO 播放列表
    broadcastReceiver(Constants.MUSIC_PLAYER_ACTION) {
      (context, intent) => {
        // TODO 更新界面信息
      }
    }
  })


}

object Constants {
  val MUSIC_PLAYER_ACTION = "org.ffmmx.example.musicplayer.MusicPlayerActivity"
  val MUSIC_SERVICE_ACTION = "org.ffmmx.example.musicplayer.MusicPlayerService"
}