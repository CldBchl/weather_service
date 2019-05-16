/**
 * Autogenerated by Thrift Compiler (0.12.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package weatherservice.thrift;


/**
 * This Enum is used to send different system warnings
 */
@javax.annotation.processing.Generated(value = "Autogenerated by Thrift Compiler (0.12.0)", date = "2019-05-09")
public enum SystemWarning implements org.apache.thrift.TEnum {
  SHUTDOWN(1),
  BATTERY_LOW(2),
  NETWORK_UNSTABLE(3),
  INTERNAL_FAILURE(4),
  EXTERNAL_FAILURE(5);

  private final int value;

  private SystemWarning(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  @org.apache.thrift.annotation.Nullable
  public static SystemWarning findByValue(int value) { 
    switch (value) {
      case 1:
        return SHUTDOWN;
      case 2:
        return BATTERY_LOW;
      case 3:
        return NETWORK_UNSTABLE;
      case 4:
        return INTERNAL_FAILURE;
      case 5:
        return EXTERNAL_FAILURE;
      default:
        return null;
    }
  }
}