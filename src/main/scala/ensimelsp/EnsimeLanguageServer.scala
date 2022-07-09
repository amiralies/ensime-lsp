package ensimelsp

import org.eclipse.lsp4j.services.LanguageServer
import java.util.concurrent.CompletableFuture
import org.eclipse.lsp4j.{InitializeParams, InitializeResult}
import java.util.concurrent.CompletableFuture
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.ServerInfo
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import java.nio.file.Paths
import java.net.URI
import scala.jdk.CollectionConverters._
import org.eclipse.lsp4j.{Hover, HoverParams}
import org.eclipse.lsp4j.jsonrpc.messages
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification
import com.sourcegraph.semanticdb_javac.Semanticdb.TextDocument
import org.eclipse.lsp4j.TextDocumentSyncOptions
import org.eclipse.lsp4j.TextDocumentSyncKind
import org.eclipse.lsp4j.SaveOptions
import org.eclipse.lsp4j.DefinitionParams
import java.{util => ju}
import org.eclipse.lsp4j.Location

class EnsimeLanguageServer {
  private val buffers = new Buffers()

  @JsonNotification("textDocument/didOpen")
  def didOpen(params: DidOpenTextDocumentParams): Unit = {
    val path = Conversions.uriToPath(params.getTextDocument().getUri())
    buffers.put(path, params.getTextDocument().getText())
    CompletableFuture.completedFuture(())
  }

  @JsonNotification("textDocument/didChange")
  def didChange(params: DidChangeTextDocumentParams): Unit = {
    params.getContentChanges().asScala.headOption match {
      case None =>
        CompletableFuture.completedFuture(())
      case Some(change) =>
        val path = Conversions.uriToPath(params.getTextDocument().getUri())
        buffers.put(path, change.getText())
        CompletableFuture.completedFuture(())
    }
  }

  @JsonNotification("textDocument/didClose")
  def didClose(params: DidCloseTextDocumentParams): Unit = {
    val path = Conversions.uriToPath(params.getTextDocument().getUri())
    buffers.remove(path)
    CompletableFuture.completedFuture(())
  }

  @JsonNotification("textDocument/didSave")
  def didSave(params: DidSaveTextDocumentParams): Unit = {
    CompletableFuture.completedFuture(())
  }

  @JsonRequest("initialize")
  def initialize(
      params: InitializeParams
  ): CompletableFuture[InitializeResult] = {
    val capabilities = new ServerCapabilities()
    val textDocumentSyncOptions = new TextDocumentSyncOptions()
    textDocumentSyncOptions.setChange(TextDocumentSyncKind.Full)
    textDocumentSyncOptions.setSave(new SaveOptions(true))
    textDocumentSyncOptions.setOpenClose(true)
    capabilities.setTextDocumentSync(textDocumentSyncOptions)

    capabilities.setDefinitionProvider(true)
    capabilities.setHoverProvider(true)

    val serverInfo =
      new ServerInfo("Ensime", "0.1.0-SNAPSHOT") // TODO sbt buildinfo
    val result = new InitializeResult(capabilities, serverInfo)
    CompletableFuture.completedFuture(result)
  }

  @JsonRequest("shutdown")
  def shutdown(): CompletableFuture[Unit] = {
    CompletableFuture.completedFuture(())
  }

  @JsonNotification("exit")
  def exit(): Unit = {}

  @JsonRequest("textDocument/definition")
  def definition(
      params: DefinitionParams
  ): CompletableFuture[ju.List[Location]] = {
    val result = {
      val uri = params.getTextDocument().getUri()
      val path = Conversions.uriToPath(uri)
      val filePath = path.toString()
      val content = buffers.get(path).get
      val offset = Conversions.positionToOffset(params.getPosition(), content)
      val cmdResult = EnsimeWrapper.source(filePath, offset)
      if (cmdResult.isEmpty()) {
        Nil
      } else {
        cmdResult.split("\n").toList.map(SourceCommandResult.apply).flatMap {
          case SourceCommandResult(None, Some(path), line) =>
            Some(
              new Location(
                Conversions.pathToUri(path),
                Conversions.lineToRange(line - 1)
              )
            )

          case SourceCommandResult(None, None, line) =>
            Some(new Location(uri, Conversions.lineToRange(line - 1)))

          case SourceCommandResult(Some(_), _, _) => None
        }
      }

    }

    CompletableFuture.completedFuture(result.asJava)
  }

  @JsonRequest("textDocument/hover")
  def hover(params: HoverParams): CompletableFuture[Hover] = {
    val path = Conversions.uriToPath(params.getTextDocument().getUri())
    buffers.get((path)) match {
      case None =>
        CompletableFuture.completedFuture(new Hover())
      case Some(value) =>
        val fullPath =
          Conversions.uriToPath(params.getTextDocument().getUri()).toString()
        val offset = Conversions.positionToOffset(params.getPosition(), value)
        val cmdResult = EnsimeWrapper.typ(fullPath, offset)
        val content = Seq("```scala", cmdResult, "```").mkString("\n")
        val hover = new Hover(new MarkupContent("", content))
        CompletableFuture.completedFuture(hover)
    }
  }
}
