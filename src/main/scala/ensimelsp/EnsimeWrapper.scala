package ensimelsp

object EnsimeWrapper {
  def runCmdAtOffset(command: String, filePath: String, offset: Int) = {
    val ensimeLauncher =
      s"""${sys.props("user.home")}/.cache/ensime${filePath}"""
    val stdout = sys.process
      .Process(List(ensimeLauncher, command, filePath, offset).mkString(" "))
      .!!
    stdout.trim
  }

  def source(filePath: String, offset: Int) =
    runCmdAtOffset("source", filePath, offset)

  def typ(filePath: String, offset: Int) =
    runCmdAtOffset("type", filePath, offset)

}
