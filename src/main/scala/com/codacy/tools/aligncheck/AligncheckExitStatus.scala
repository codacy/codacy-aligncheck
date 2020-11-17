package com.codacy.tools.aligncheck

sealed abstract class AligncheckExitStatus

object AligncheckExitStatus extends Enumeration {

  def fromExitCode(code: Int): AligncheckExitStatus = code match {
    case 0 | 1 => Success // Tool returns exit code 1 when there are results
    case _ => Error
  }

  final case object Success extends AligncheckExitStatus

  final case object Error extends AligncheckExitStatus
}
