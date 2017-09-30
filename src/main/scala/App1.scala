import javafx.application.Application
import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.ImageView
import javafx.scene.{Group, Scene}
import javafx.stage.Stage

import org.opencv.core._
import org.opencv.imgcodecs.Imgcodecs
import net.nineclue.opencv.OpenCVUtil

object App1 {
  def main(as: Array[String]) =
    Application.launch(classOf[App1], as:_*)
}

class App1 extends Application {
  override def init():Unit =
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

  override def start(ps: Stage) = {
    val im = run()
    val root = new Group()
    val iv = new ImageView()
    iv.setImage(im)
    root.getChildren.add(iv)
    ps.setScene(new Scene(root, im.getWidth(), im.getHeight()))
    ps.show
  }

  def run() = {
    val image = Imgcodecs.imread("/Users/nineclue/Pictures/Image2.jpg")
    assert(!image.empty())
    // OpenCVUtil.testRunBufferedImage(image)
    // SwingFXUtils.toFXImage(OpenCVUtil.mat2BufferedImage(image, None), null)
    OpenCVUtil.mat2Image(image, None)
  }
}
