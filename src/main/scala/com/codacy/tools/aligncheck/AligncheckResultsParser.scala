package com.codacy.tools.aligncheck

import com.codacy.plugins.api.Source
import com.codacy.plugins.api.results.{Pattern, Result}

import scala.util.{Success, Try}

object AligncheckResultsParser {

  def parse(results: List[String]): List[Result] = {
    results.map(parseSingleResult).collect { case Success(issue) => issue }
  }

  def parseSingleResult(result: String): Try[Result] = {
    Try {
      val Array(_, filename, line, _, message) = result.split(":")
      Result.Issue(
        Source.File(filename.trim),
        Result.Message(message.trim),
        Pattern.Id(AligncheckPatterns.aligncheckPatternId),
        Source.Line(line.toInt)
      )
    }
  }
}
