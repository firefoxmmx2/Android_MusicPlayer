package org.ffmmx.example.musicplayer.util

import java.text.SimpleDateFormat
import java.util.Date


object DateUtil {
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
  val timeFormat = new SimpleDateFormat("y" * 4 + "-MM-dd HH:mm:ss")

  def formatDate(date: Date, hasTime: Boolean = false) =
    if (hasTime) timeFormat.format(date)
    else dateFormat.format(date)

  def parseDate(dateString: String, hasTime: Boolean = false) =
    if (hasTime) timeFormat.parse(dateString)
    else dateFormat.parse(dateString)
}
