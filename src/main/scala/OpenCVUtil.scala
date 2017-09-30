package net.nineclue.opencv

import java.awt.image.{BufferedImage, DataBufferByte}
import javafx.scene.image.{Image, PixelFormat, WritableImage}

import org.opencv.core.{CvType, Mat}
import org.opencv.imgproc.Imgproc

trait OpenCVConverter {
  def mat2BufferedImage(mat: Mat, bi: Option[BufferedImage]): BufferedImage
  def mat2Image(mat: Mat, im: Option[WritableImage]): Image
  def bufferedImage2Mat(bi: BufferedImage, mat: Option[Mat]): Mat
  def image2Mat(im: Image, mat: Option[Mat]): Mat
}

object OpenCVUtil extends OpenCVConverter {
  import java.awt.image.BufferedImage._

  private def sameType(im: BufferedImage, mat: Mat) =
    (im.getType() == TYPE_3BYTE_BGR && mat.channels() == 3) ||
      (im.getType() == TYPE_BYTE_GRAY && mat.channels() == 1)

  private def is3channel(im: BufferedImage) = im.getType() == TYPE_3BYTE_BGR

  def mat2BufferedImage(mat: Mat, bi: Option[BufferedImage] = None):BufferedImage = {

    def createBufferedImage() = mat.channels() match {
      case 1 =>
        new BufferedImage(mat.cols, mat.rows, TYPE_BYTE_GRAY)
      case 3 =>
        new BufferedImage(mat.cols, mat.rows, TYPE_3BYTE_BGR)
    }

    val (cols, rows) = (mat.cols, mat.rows)
    val data: Array[Byte] = Array.ofDim[Byte](cols * rows * mat.channels)
    val dest = new Mat()
    Imgproc.cvtColor(mat, dest, Imgproc.COLOR_BGR2RGB)
    dest.get(0, 0, data)
    val result =
      bi match {
        case Some(im) =>
          if (im.getWidth == cols && im.getHeight == rows && sameType(im, mat))
            im
          else createBufferedImage()
        case None =>
          createBufferedImage
      }
    result.getRaster.setDataElements(0, 0, cols, rows, data)
    result
  }

  def mat2Image(mat: Mat, im: Option[WritableImage] = None): Image = {
    val (cols, rows) = (mat.cols, mat.rows)
    val dest = new Mat(mat.rows, mat.cols, CvType.CV_8UC3)
    if (mat.channels == 3)
      Imgproc.cvtColor(mat, dest, Imgproc.COLOR_BGR2RGB)
    else
      Imgproc.cvtColor(mat, dest, Imgproc.COLOR_GRAY2RGB)
    val data: Array[Byte] = Array.ofDim[Byte](cols * rows * 3)
    dest.get(0, 0, data)
    val result: WritableImage = im match {
      case Some(image) if (image.getWidth == cols && image.getHeight == rows) =>
         image
      case _ =>
        new WritableImage(cols, rows)
    }
    result.getPixelWriter.setPixels(0, 0, dest.cols, dest.rows, PixelFormat.getByteRgbInstance, data, 0, dest.step1.toInt)
    result
  }

  def bufferedImage2Mat(bi: BufferedImage, mat: Option[Mat]): Mat = {
    // reuse argument mat if possible
    val result: Mat = mat match {
      case Some(m) if (m.cols == bi.getWidth() && m.rows == bi.getHeight() && sameType(bi, m)) =>
        m
      case _ =>
        new Mat(bi.getHeight(), bi.getWidth(), if (is3channel(bi)) CvType.CV_8UC3 else CvType.CV_8UC1)
    }
    val im: BufferedImage =
      if (is3channel(bi)) {
        val buff = new BufferedImage(bi.getWidth, bi.getHeight, TYPE_3BYTE_BGR)
        buff.getGraphics.drawImage(bi, 0, 0, null)
        buff
      } else
        bi
    result.put(0, 0, im.getRaster.getDataBuffer.asInstanceOf[DataBufferByte].getData)
    result
  }

  def image2Mat(im: Image, mat: Option[Mat]): Mat = ???

  def testRunBufferedImage(mat: Mat, run: Int = 1000) = {
    val bi = new BufferedImage(mat.cols, mat.rows, TYPE_3BYTE_BGR)
    val data: Array[Byte] = Array.ofDim[Byte](mat.cols * mat.rows * mat.channels)
    def run1() = {
      Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB)
      mat.get(0, 0, data)
      bi.getRaster.setDataElements(0, 0, mat.cols, mat.rows, data)
    }
    def run2() = {
      mat.get(0, 0, data)
      var b: Byte = 0
      Range(0, data.length, 3).foreach({ i =>
        b = data(i)
        data(i) = data(i+2)
        data(i+2) = b
      })
      bi.getRaster.setDataElements(0, 0, mat.cols, mat.rows, data)
    }
    val t1 = System.nanoTime()
    Range(0, run).foreach(_ => run1)
    val t2 = System.nanoTime()
    Range(0, run).foreach(_ => run2)
    val t3 = System.nanoTime()
    println(s"Mat : ${mat.cols} X ${mat.rows}")
    println(s"Method1 : ${(t2 - t1) / 1000} miliseconds")
    println(s"Method2 : ${(t3 - t2) / 1000} miliseconds")
  }
}
