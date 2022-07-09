package ensimelsp

import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.jsonrpc.Launcher

object Main {
  def main(args: Array[String]): Unit = {
    val systemIn = System.in
    val systemOut = System.out
    val server = new EnsimeLanguageServer()

    val launcher = new Launcher.Builder[EnsimeLanguageClient]()
      .setInput(systemIn)
      .setOutput(systemOut)
      .setRemoteInterface(classOf[EnsimeLanguageClient])
      .setLocalService(server)
      .create()

    val clientProxy = launcher.getRemoteProxy
    launcher.startListening().get()
  }
}
