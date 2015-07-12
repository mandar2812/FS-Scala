package be.kuleuven.esat.fsscala

import java.io.{CharArrayWriter, PrintWriter}
import scala.tools.nsc.interpreter.ILoop
import scala.tools.nsc.Settings


object fsScala extends App {

  override def main(args: Array[String]) = {
    val repl = new ILoop {
      override def prompt = "FS-Scala>"

      addThunk {
        intp.beQuietDuring {
          intp.addImports("breeze.linalg._")
          intp.addImports("be.kuleuven.esat.fsscala.models._")
          intp.addImports("be.kuleuven.esat.fsscala.models.svm._")
          intp.addImports("be.kuleuven.esat.fsscala.utils")
          intp.addImports("be.kuleuven.esat.fsscala.kernels._")
          intp.addImports("be.kuleuven.esat.fsscala.examples._")
          intp.addImports("org.apache.spark.SparkContext")
          intp.addImports("org.apache.spark.SparkConf")
        }
      }

      override def printWelcome() {
        echo("\nWelcome to FS-Scala v 1.0\nInteractive Scala shell")
        echo("STADIUS ESAT KU Leuven (2015)\n")
      }
    }
    val settings = new Settings
    settings.Yreplsync.value = true

    if (isRunFromSBT) {
      settings.embeddedDefaults[fsScala.type]
    } else {
      settings.usejavacp.value = true
    }

    def isRunFromSBT = {
      val c = new CharArrayWriter()
      new Exception().printStackTrace(new PrintWriter(c))
      c.toString().contains("at sbt.")
    }

    repl.process(settings)
  }
}