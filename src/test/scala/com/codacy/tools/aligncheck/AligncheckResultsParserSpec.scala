package com.codacy.tools.aligncheck

import com.codacy.plugins.api.Source
import com.codacy.plugins.api.results.{Pattern, Result}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AligncheckResultsParserSpec extends AnyWordSpec with Matchers {
  "AligncheckResultsParser" should {
    "return empty list of results" in {
      val listOfResultsAsString = List()
      val results = AligncheckResultsParser.parse(listOfResultsAsString)
      results shouldBe List()
    }

    "return list of issues" in {
      val listOfResultsAsString =
        List("/src: main.go:11:1: struct example could have size 128 (currently 152)", "aligncheck: dir/main.go:12:1: struct example2 could have size 128 (currently 152)")
      val resultsList = List(
        Result
          .Issue(
            Source.File("main.go"),
            Result.Message("struct example could have size 128 (currently 152)"),
            Pattern.Id("aligncheck"),
            Source.Line(11)
          ),
        Result.Issue(
          Source.File("dir/main.go"),
          Result.Message("struct example2 could have size 128 (currently 152)"),
          Pattern.Id("aligncheck"),
          Source.Line(12)
        ),
      )

      val results = AligncheckResultsParser.parse(listOfResultsAsString)

      results shouldBe resultsList
    }

    "return list of issues and ignore invalid" in {
      val listOfResultsAsString = List(
        "/src: main.go:11:1: struct example could have size 128 (currently 152)",
        "/src: dir/main.go:12:1: struct example2 could have size 128 (currently 152)",
        "this is invalid"
      )
      val resultsList = List(
        Result
          .Issue(
            Source.File("main.go"),
            Result.Message("struct example could have size 128 (currently 152)"),
            Pattern.Id("aligncheck"),
            Source.Line(11)
          ),
        Result.Issue(
          Source.File("dir/main.go"),
          Result.Message("struct example2 could have size 128 (currently 152)"),
          Pattern.Id("aligncheck"),
          Source.Line(12)
        ),
      )

      val results = AligncheckResultsParser.parse(listOfResultsAsString)

      results shouldBe resultsList
    }
  }

}
