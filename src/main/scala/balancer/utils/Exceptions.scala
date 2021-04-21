package balancer.utils

/**
 * Exceptions that might occur during the parsing of the saved file or
 * when placing the weight (only in the Text version of the game)
 */
final case class ParseError(private val message: String = "",
                            private val cause: Throwable = None.orNull)
  extends Exception(message, cause)


final case class InvalidInput(private val message: String = "",
                              private val cause: Throwable = None.orNull)
  extends Exception(message, cause)

final case class OccupiedPosition(private val message: String = "",
                                  private val cause: Throwable = None.orNull)
  extends Exception(message, cause)
