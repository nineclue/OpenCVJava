package net.nineclue.opencv

import javafx.application.{Application, Platform}
import javafx.scene.{Group, Scene}
import javafx.scene.image.ImageView
import javafx.stage.Stage

import org.opencv.core.{Core, Mat, Point, Scalar}
import org.opencv.imgproc.Imgproc

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object OpenCVFramesViewer {
  var initialized = false
  var (matWidth, matHeight) = (0, 0)
  var lastTime: Long = 0L
  var viewer: Option[OpenCVFramesViewer] = None
  type MatProcessor = () => Mat
  var matp: MatProcessor = _
  var fFlag = false

  def setup(w: Int, h: Int, showFrames: Boolean): Unit = {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
    initialized = true
    matWidth = w
    matHeight = h
    fFlag = showFrames
  }
  def apply(f: () => Mat): Unit = {
    if (!initialized) throw new Exception("view called before OpenCVFramesViewer.setup() method")
    if (viewer.isEmpty) {
      matp = f
      viewer = Some(new OpenCVFramesViewer())
      Future {
        Application.launch(viewer.map(_.getClass).get, "")
      }
      Thread.sleep(1000)
    } else {
      matp = f
    }

  }
}

class OpenCVFramesViewer extends Application {
  import OpenCVFramesViewer._

  override def start(ps: Stage) = {
    val group = new Group()
    val ffviewer = new ImageView()
    group.getChildren.addAll(ffviewer)
    ps.setScene(new Scene(group, matWidth, matHeight))
    ps.setTitle("OpenCV Frames Viewer")
    ps.show

    def job(): Unit = {
      Future {
        val mat = matp()
        if (fFlag) {
          if (lastTime != 0L) {
            val fps = (1000000000L / (System.nanoTime() - lastTime)).toInt
            Imgproc.putText(mat, s"$fps fps", new Point(20, 30), Core.FONT_HERSHEY_PLAIN, 1.0, new Scalar(0, 255, 0))
          }
          lastTime = System.nanoTime()
        }
        val im = OpenCVUtil.mat2Image(mat)
        ffviewer.setImage(im)
      } onComplete(_ => job())
    }

    job()
  }
}
