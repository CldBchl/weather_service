/*
 * This is the API definition file of the Thrift server for distributed systems SS:2019.
 * 
 * The Server is using Binary Serialisation
 * It's Accessable via the URL: 141.100.70.110:8080/weather
 *
 * Use These Ressources for help:
 * https://thrift.apache.org/docs/idl
 * https://diwakergupta.github.io/thrift-missing-guide/
 */

/**
 * Define Namespace for generated files.
 * "Asterisk" should work for C, C++, C#, Go, Lua, Java, Python, Perl...
 * See https://thrift.apache.org/docs/idl#namespace
 */
namespace *  weatherService

/**
 * This Enum defines different weather warnings the system can return when a warning is requested
 */
enum WeatherWarning {
  NONE		= 0,
  BLIZZARD	= 1,
  FLOOD		= 2,
  HURRICANE = 3,
  STORM		= 4,
  TORNADO	= 5,
  UV 		= 6,
  // APOCALYPSE = 666
}

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
 * WeatherForecast definiton.
 *    WeatherForecast is a synonym for WeatherReport.
 *    They were once different and might be again some day (?).
 */
typedef WeatherReport WeatherForecast


/**
 * This Exception gets thrown when the server does not know about the user.
 */
exception UnknownUserException {
  1: i64 SessionToken,
  2: string why
}

/**
 * This Exception gets thrown when a unknown Location is passed to the server.
 */
exception LocationException {
  1: Location location,
  2: string why
}

/**
 * This Exception gets thrown when a wrong report is send to the system.
 */
exception ReportException {
  1: Report report,
  2: string why
}

/**
 * This Exception gets thrown when a wrong report is send to the system.
 */
exception DateException {
  1: string time,
  2: string why
}


/**
 * Weather service definition
 */
service Weather{
    
    /* Login
     * This call will login into the server. It returns a new sessionToken.
     * The UserId will be linked to the send location
     * A location is unique.
     *
     * Note: User will be logged out every day at 2 o'click.
     *
     * param: 
     * location: current location of the User.
     * return The UserId that's linked to the location
     *
     * Throws:
     * LocationException: 
     * This exception is thrown when the location is not unique or another problem is detected with the location.
    */
    i64 login(1: Location location) throws (1: LocationException locationException),

    /* logout
     * This call will logout the User.
     * 
     * param:
     * sessionToken: the id of the User That should be logged out.
     * 
     * return: 
     * Will return True if the user is correctly logout
     * Will return False if the user could not be logout correctly
     * Throw:
     * UnknownUserException will be thrown when a unknown user tries to logout
    */
    bool logout(1:i64 sessionToken) throws (1: UnknownUserException unknownUserException),

    /* Send Weather Report
     * Sends a weather report to the server
     *
     * param:
     * report: The report that the server should be receive.
     * userId: The User id that sends in the report
     *
     * return: 
     * Will return True if report was send in correctly.
     * Will return False if report was not send in correctly.
     * Throw
     * UnknownUserException will be thrown when a unknown user try's to send a report
     * ReportExeption will be thrown when a problem with the report is detected
     */
    bool sendWeatherReport(1:WeatherReport report, 2: i64 sessionToken) throws (1: UnknownUserException unknownUserException,2: ReportException reportException,3: DateException dateException,4: LocationException locationException),

    /* receiveForecastFor
     * Requests a forecast from the server for the Location of a User,
     * 
     * param:
     * userId: the Id of the user that requests a forecast.
     * time: the time the weather is forcasted. Shorter ISO 8601 is OK,
     *       e.g. "2019-04-18"
     *
     * return: 
     * A weather forecast with the
     * Throw
     * UnknownUserException will be thrown when a unknown user try's to send a report
     */
    WeatherForecast receiveForecastFor(1:i64 userId, 2: dateTime time ) throws (1:UnknownUserException unknownUserException,2: DateException dateException),
   
    /* checkWarnings
     * Checks if a Weather warning is present for the User.
     *
     * param:
     * userId: The id that receives a Warning.
     *
     * return: 
     * A weather warning for the location requested 
     * Throw
     * LocationException will be thrown when a location is unknown.
    */
    WeatherWarning checkWeatherWarnings(1:i64 userId) throws (1:UnknownUserException unknownUserException),
    
    /* sendWarning
     * Send system Warnings to the Server.
     *
     * return: 
     * Will return True when warning was received corretly
     * Will return False if report was not received corretly
     * Throw
     * UnknownUserException will be thrown when a unknown user try's to report a System Warning
    */
    bool sendWarning(1:SystemWarning systemWarning,2: i64 userId) throws (1:UnknownUserException unknownUserException)
}

