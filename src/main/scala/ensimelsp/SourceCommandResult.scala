package ensimelsp

import javax.xml.transform.Source

case class SourceCommandResult(
    archive: Option[String],
    path: Option[String],
    line: Int
)

object SourceCommandResult {
  def apply(resultStr: String): SourceCommandResult = {

    if (resultStr.startsWith(":")) {
      SourceCommandResult(None, None, resultStr.split(":").last.toInt)
    } else if (resultStr.contains("!/")) {
      val archivePathLine = resultStr.split("!")
      val archive = archivePathLine.head
      val pathLine = archivePathLine.last.split(":")
      val path = pathLine.head
      val line = pathLine.last.toInt

      SourceCommandResult(Some(archive), Some(path), line)
    } else {
      val pathLine = resultStr.split(":")
      val path = pathLine.head
      val line = pathLine.last.toInt

      SourceCommandResult(None, Some(path), line)
    }
  }
}
