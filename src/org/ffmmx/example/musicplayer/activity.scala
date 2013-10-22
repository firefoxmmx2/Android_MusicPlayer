package org.ffmmx.example.musicplayer

import org.scaloid.common._


class MenuActivity extends SActivity{
  override def basis: SActivity = this

  implicit override val ctx: SActivity = basis

  onCreate({
    setContentView(R.layout.main)
  })
}
