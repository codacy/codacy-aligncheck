package com.codacy.tools.aligncheck

import com.codacy.plugins.api.results.Pattern

object AligncheckPatterns {
  val aligncheckPatternId: String = "aligncheck"
  val defaultPatterns: List[String] = List(aligncheckPatternId)

  val defaultPatternsDefinitions: List[Pattern.Definition] =
    defaultPatterns.map(patternId => Pattern.Definition(Pattern.Id(patternId)))

  def isValidPattern(patternsDefinition: Pattern.Definition): Boolean = {
    defaultPatternsDefinitions.contains(patternsDefinition)
  }
}
