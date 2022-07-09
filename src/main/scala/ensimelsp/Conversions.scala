package ensimelsp

import java.nio.file.Paths
import java.net.URI
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import java.nio.file.Path
import java.io.File

object Conversions {
  def uriToPath(uri: String) = Paths.get(URI.create(uri))
  def pathToUri(path: String) = new File(path).toPath().toUri().toString

  private def peek(idx: Int, contents: String) =
    if (idx < contents.size) contents(idx) else -1

  def positionToOffset(pos: Position, contents: String): Int = {
    val line = pos.getLine
    val col = pos.getCharacter

    var i, l, c = 0
    while (i < contents.size && l < line) {
      contents(i) match {
        case '\r' =>
          l += 1
          if (peek(i + 1, contents) == '\n') i += 1

        case '\n' =>
          l += 1

        case _ =>
      }
      i += 1
    }

    if (l < line)
      throw new IllegalArgumentException(
        s"Can't find position $pos in contents of only $l lines long."
      )

    if (i + col < contents.size)
      i + col
    else
      throw new IllegalArgumentException(
        s"Invalid column. Position $pos in line '${contents.slice(i, contents.size).mkString}'"
      )

  }

  def lineToRange(line: Int): Range = {
    val pos = new Position(line, 0)
    new Range(pos, pos)
  }
}
