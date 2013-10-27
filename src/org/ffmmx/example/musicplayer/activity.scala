package org.ffmmx.example.musicplayer

import org.scaloid.common._
import android.widget.ImageButton
import android.view.{View, KeyEvent}
import android.content.{Intent, Context, BroadcastReceiver}


class MainActivity extends SActivity {
  override def basis: SActivity = this

  implicit override val ctx: SActivity = basis

  var receiver:BroadcastReceiver = _
  onCreate({
    setContentView(R.layout.main)

    //播放暂停按钮
    val playPauseButton = find[ImageButton](R.id.playPauseButton)
      .onClick({
      // 发送播放或者停止请求到播放服务
      sendBroadcast(
        SIntent(Constants.MUSIC_SERVICE_ACTION)
          .putExtra("action", Constants.PLAY_ACTION_PLAYPAUSE)
          .putExtra("song", "")
      )
    })

    //上一首按钮
    val previousButton = find[ImageButton](R.id.previousButton)
      .onClick({
      // 发送上一首请求到播放服务
      sendBroadcast(
        SIntent(Constants.MUSIC_SERVICE_ACTION)
          .putExtra("action", Constants.PLAY_ACTION_PREVIOUS)
          .putExtra("song", "")

      )
    })

    //下一首按钮
    val nextButton = find[ImageButton](R.id.nextButton)
      .onClick({
      // 发送下一首请求到播放服务
      sendBroadcast(
        SIntent(Constants.MUSIC_SERVICE_ACTION)
          .putExtra("action", Constants.PLAY_ACTION_NEXT)
          .putExtra("song", "")
      )
    })

    // TODO 播放列表
    // 注册播放器广播
    broadcastReceiver(Constants.MUSIC_PLAYER_ACTION) {
      (context, intent) => {
        // TODO 更新界面图标
        println("="*13+Constants.MUSIC_PLAYER_ACTION+"="*13)
        intent.getIntExtra("status",Constants.PLAY_STATUS_STOP) match {
          case Constants.PLAY_STATUS_PAUSE =>
            playPauseButton.imageResource(R.drawable.pause_sel)
          case _ =>
            playPauseButton.imageResource(R.drawable.play_sel)
        }

        // TODO 更新界面歌曲

        // TODO 更新界面时间

      }
    }

//    receiver = new BroadcastReceiver {
//      def onReceive(context: Context, intent: Intent) {
//        println("="*13+Constants.MUSIC_PLAYER_ACTION+"="*13)
//        intent.getIntExtra("status",Constants.PLAY_STATUS_STOP) match {
//          case Constants.PLAY_STATUS_PAUSE =>
//            playPauseButton.imageResource(R.drawable.pause_sel)
//          case _ =>
//            playPauseButton.imageResource(R.drawable.play_sel)
//        }
//
//      }
//    }
//
//    registerReceiver(receiver,Constants.MUSIC_PLAYER_ACTION)

    sendBroadcast(SIntent(Constants.MUSIC_PLAYER_ACTION))
    //开始播放服务
    startService(SIntent[MusicPlayService])
  })


//  onDestroy({
//    unregisterReceiver(receiver)
//  })
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
}