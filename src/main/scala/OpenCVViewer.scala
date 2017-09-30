package net.nineclue.opencv

import javafx.application.{Application, Platform}
import javafx.scene.{Group, Scene}
import javafx.scene.image.{Image, ImageView}
import javafx.stage.{Screen, Stage}

import scala.concurrent.ExecutionContext.Implicits.global
import org.opencv.core.{Core, Mat}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future

object OpenCVViewer {
  var initialized = false
  var viewer: Option[CVViewer] = None

  def setup() = {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
    initialized = true
  }
  def view(mat: Mat): Unit =
    view(mat, "OPENCV Viewer")
  def view(mat: Mat, title: String): Unit = {
    if (!initialized) throw new Exception("view called before OpenCVViewer.setup() method")
    if (viewer.isEmpty) {
      viewer = Some(new CVViewer())
      CVViewer.title = title
      CVViewer.setMat(mat)
      // Application.launch(classOf[CVViewer], "")
      Future {
        Application.launch(viewer.map(_.getClass).get, "")
      }
      Thread.sleep(1000)
    } else {
      viewer.foreach(v => Platform.runLater(() => v.newStage(mat, title)))
    }
  }
}

object CVViewer {
  var mat: Mat = _
  var title = ""
  private val stages = ArrayBuffer.empty[Stage]
  def appendStage(s: Stage) = stages += s
  def arrangeStages() = {
    val bounds = Screen.getPrimary.getVisualBounds
    val ystep = bounds.getHeight / stages.length
    stages.zip(Range(0, stages.length)).foreach({ t => t._1.setY(t._2 * ystep) })
  }
  def setMat(m: Mat) = mat = m
}

class CVViewer extends Application {
  import CVViewer._
  override def start(ps: Stage): Unit = {
    ps.setTitle(title)
    ps.setScene(getScene(mat))
    appendStage(ps)
    ps.show()
  }
  def newStage(mat: Mat, sTitle: String): Unit = {
    val stage = new Stage()
    stage.setTitle(sTitle)
    stage.setScene(getScene(mat))
    appendStage(stage)
    stage.show
    arrangeStages()
  }
  private def getScene(mat: Mat): Scene = {
    assert(mat != null)
    val root = new Group()
    val viewer = new ImageView()
    val image = OpenCVUtil.mat2Image(mat)
    viewer.setImage(image)
    root.getChildren.add(viewer)
    new Scene(root, image.getWidth, image.getHeight)
  }
}