/**
 * Autogenerated by Thrift Compiler (0.12.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package weatherSync;


@javax.annotation.processing.Generated(value = "Autogenerated by Thrift Compiler (0.12.0)", date = "2019-05-24")
public enum Report implements org.apache.thrift.TEnum {
  SUNNY(1),
  CLOUDY(2),
  RAINY(3),
  SNOW(4);

  private final int value;

  private Report(int value) {
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
  public static Report findByValue(int value) { 
    switch (value) {
      case 1:
        return SUNNY;
      case 2:
        return CLOUDY;
      case 3:
        return RAINY;
      case 4:
        return SNOW;
      default:
        return null;
    }
  }
}
