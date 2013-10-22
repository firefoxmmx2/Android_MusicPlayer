name := "Android_MusicPlayer"

version := "0.1"

scalaVersion := "2.10.2"

android.Plugin.androidBuild

libraryDependencies ++= Seq(
	 "org.scaloid" % "scaloid_2.10" % "2.4-8-SNAPSHOT"
) 
