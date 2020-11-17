package com.codacy.tools.aligncheck

import better.files._
import com.codacy.plugins.api.results.{Pattern, Result, Tool}
import com.codacy.plugins.api.{Options, Source}
import com.codacy.tools.scala.seed.utils.{CommandResult, CommandRunner}

import scala.util.{Success, Try}

object Aligncheck extends Tool {
  private val aligncheckCommand = "/app/aligncheck"
  private val directoriesMaxDepth = 5

  override def apply(
      source: Source.Directory,
      configuration: Option[List[Pattern.Definition]],
      filesOpt: Option[Set[Source.File]],
      options: Map[Options.Key, Options.Value]
  )(implicit specification: Tool.Specification): Try[List[Result]] = {
    val sourcePath = File(source.path)

    val patternsToAnalyse = patternsToRun(configuration)
    val results = patternsToAnalyse.fold[List[Result]](List.empty)(_ => runTool(sourcePath))
    val filteredResults = filterResultsForFiles(results, filesOpt)

    Success(filteredResults)
  }

  private def patternsToRun(configuration: Option[List[Pattern.Definition]]): Option[List[Pattern.Definition]] = {
    val patternsConfiguration =
      configuration.getOrElse(AligncheckPatterns.defaultPatternsDefinitions).filter(AligncheckPatterns.isValidPattern)
    Option(patternsConfiguration).filter(_.nonEmpty)
  }

  private def filterResultsForFiles(results: List[Result], filesOpt: Option[Set[Source.File]]): List[Result] = {
    filesOpt.fold(results) { files =>
      results.collect {
        case res: Result.Issue if files.contains(res.file) => res
      }
    }
  }

  private def runTool(root: File): List[Result] = {
    root
      .walk(directoriesMaxDepth)
      .collect {
        case directory if directory.isDirectory =>
          runToolOnSingleDirectory(directory)
      }
      .flatten
      .toList
  }

  private def runToolOnSingleDirectory(directory: File): List[Result] = {
    val command = List(aligncheckCommand)

    CommandRunner.exec(command, Some(directory.path.toFile)) match {
      case Right(commandResults) =>
        handleToolResults(commandResults)
      case Left(failure) =>
        throw failure
    }
  }

  private def handleToolResults(commandResults: CommandResult) = {
    AligncheckExitStatus.fromExitCode(commandResults.exitCode) match {
      case AligncheckExitStatus.Success =>
        AligncheckResultsParser.parse(commandResults.stdout)
      case _ =>
        throw new scala.Exception(
          s"Exit code: ${commandResults.exitCode}\n" +
            s"stdout: ${commandResults.stdout}\n" +
            s"sterr: ${commandResults.stdout}\n"
        )
    }
  }
}
