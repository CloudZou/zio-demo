package movie

import com.typesafe.config.ConfigFactory

/**
 * Defines all the data model used in the system.
 * Included boundary check or formation validation as part of the last line of data sanity check
 */
package object model {

  //Imposing artificial limit for the seating in the system.
  val MAX_SEATING    =
    ConfigFactory.load().getConfig("application").getInt("max-seating")
  val IMDBID_REGEX   = """tt\d{7}"""
  val SCREENID_REGEX = """screen_\d{6}"""

  type IMDBID   = String
  type ScreenID = String
  type UserId   = String

  final case class MovieDetail(
    imdbId: IMDBID,
    movieTitle: String)

  final case class MovieRegistration(
    imdbId: IMDBID,
    availableSeats: Int = MAX_SEATING,
    screenId: ScreenID) {
    require(imdbId.matches(IMDBID_REGEX), "Invalid IMDB Id")
    require(screenId.matches(SCREENID_REGEX), "Invalid Screen Id")
    require(
      availableSeats >= 0 && availableSeats <= MAX_SEATING,
      "Available seats exceeded maximum capacity."
    )
  }

  final case class ReservationRequest(
    imdbId: IMDBID,
    screenId: ScreenID) {
    require(imdbId.matches(IMDBID_REGEX), "Invalid IMDB Id")
    require(screenId.matches(SCREENID_REGEX), "Invalid Screen Id")
  }

  final case class ReservationStatus(
    imdbId: IMDBID,
    screenId: ScreenID,
    movieTitle: String,
    availableSeats: Int = MAX_SEATING,
    reservedSeats: Int = 0) {
    require(imdbId.matches(IMDBID_REGEX), "Invalid IMDB Id")
    require(screenId.matches(SCREENID_REGEX), "Invalid Screen Id")
    require(
      availableSeats >= 0 && availableSeats <= MAX_SEATING,
      "Available seats exceeded maximum capacity."
    )
    require(
      reservedSeats >= 0 && reservedSeats <= availableSeats,
      "Reserved Seats can not be more than available seats"
    )
  }

}
