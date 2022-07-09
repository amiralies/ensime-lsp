package ensimelsp

import java.nio.file.Files
import java.nio.file.Path

import scala.collection.concurrent.TrieMap

class Buffers() {
  private val map: TrieMap[Path, String] = TrieMap.empty

  def put(key: Path, value: String): Unit = map.put(key, value)
  def get(key: Path): Option[String] = map.get(key)
  def remove(key: Path): Unit = map.remove(key)
  def contains(key: Path): Boolean = map.contains(key)
}
