/*
 * This is the API definition file of the Thrift server for exchanging data
 * between weatherService servers.
 *
 * The Server is using Binary Serialisation
 *
 */

/**
 * Define Namespace for generated files.
 * "Asterisk" should work for C, C++, C#, Go, Lua, Java, Python, Perl...
 * See https://thrift.apache.org/docs/idl#namespace
 */
namespace *  weatherservice.weatherSync

/**
 * This Enum is used to send different system warnings
 */
enum SystemWarning {
  SHUTDOWN = 1,			// Panic: About to shut down without logout
  BATTERY_LOW = 2,		// Reducing QoS to save battery
  NETWORK_UNSTABLE = 3,	// Jitter too large, ping too long, etc.
  INTERNAL_FAILURE = 4,	// Report that internal tests failed
  EXTERNAL_FAILURE = 5, // Report that received data failed tests
}

enum Report {
  SUNNY = 1,
  CLOUDY = 2,
  RAINY = 3,
  SNOW = 4,
}

/**
 * This Struct defines a Location in the Real world.
 */
struct Location {
  1: byte locationID,
  2: string name,
  // Latitude and Longitude using ISO 6709, where we require fractions
  // of degrees (i.e. sexagesimal notation is not tolerated).
  // See https://en.wikipedia.org/wiki/ISO_6709#Order,_sign,_and_units
  3: double latitude, // between -90 (South Pole) and 90 (North Pole)
  4: double longitude, // between -180 (Far West) and 180 (Far East)
  // Examples: Darmstadt          Lat  49.866, Long =   8.641
  // Chatham Island, New Zealand  Lat -44.013, Long: -176.547
  5: optional string description,
}

/**
 * Thrift doesn't give us a date time type, so we leverage ISO 8601.
 * Example date time: "2019-04-18T08:35:17+00:00"
 */
typedef string dateTime

/**
 * WeatherReport definiton.
 * Attention Values will be checked and has to be in a natural range.
 */
struct WeatherReport {
  1: Report report,
  2: Location location,
  3: double temperature, //in °C
  4: byte humidity, //in Percent
  5: byte windStrength, //in km/h
  6: double rainfall //in mm
  7: i16 atmosphericpressure//in Pa
  8: i16 windDirection // 0 = North, 90 = East, 180 = South, 270 = West
  9: string dateTime // ISO 8601 e.g. "2019-04-18T08:35:17+00:00"
}

/**
 * This Exception gets thrown when the server does not know about the user.
 */
exception UnknownUserException {
  1: i64 SessionToken,
  2: string why
}

/**
 * This Exception gets thrown when a location is already registered as a logged in location.
 */
exception LoggedInUserException {
  1: i64 userId,
  2: string why
}


/**
 * Weather synchronization definition
 */
service WeatherSync{

    /*
    * syncLoginData
    * This call sends the location and the userId of a recently logged in location.
    * It returns true if the user data was updated successfully.
    *
    * param:
    * location: location of the recently logged in user.
    * userId: userId of the recently logged in user.
    */
    bool syncLoginData(1: Location location, 2: i64 userId) throws (1: LoggedInUserException loggedInUserException),

   /*
    * syncLogoutData
    * This call sends the userId of a recently logged out location.
    * It returns true if the user data was updated successfully.
    *
    * param:
    * userId: userId of the recently logged in user.
    */
    bool syncLogoutData(1: i64 userId) throws (1: UnknownUserException unknownUserException),

   /*
    * syncWeatherReport
    * This call sends a new weatherReport.
    * It returns true if the report was stored successfully.
    *
    * param:
    * weatherReport: a new weatherReport.
    * userId: userId of the location which generated the weatherReport.
    */
    bool syncWeatherReport(1: WeatherReport weatherReport, 2: i64 userId) throws (1: UnknownUserException unknownUserException),

   /*
    * syncSystemWarning
    * This call sends a system warning.
    * It returns true if the system warning was stored successfully.
    *
    * param:
    * systemWarning: SystemWarning sent by the client.
    * userId: userId of the location which generated the SystemWarning.
    */
    bool syncSystemWarning(1: SystemWarning systemWarning, 2: i64 userId) throws (1: UnknownUserException unknownUserException)

}

