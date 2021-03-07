package balancer.utils


final case class ParseError(private val message: String = "",
                            private val cause: Throwable = None.orNull)
  extends Exception(message, cause)


final case class InvalidInput(private val message: String = "",
                              private val cause: Throwable = None.orNull)
  extends Exception(message, cause)

final case class OccupiedPosition(private val message: String = "",
                              private val cause: Throwable = None.orNull)
  extends Exception(message, cause)
