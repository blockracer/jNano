package uk.oczadly.karl.jnano.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * <p>This class represents the currency units and denominations used to represent an amount of Nano, and can be
 * used to natively convert between the different units.</p>
 * <p>If you are intending to parse or display an amount of Nano to the user, it is recommended that you use the
 * {@link #BASE_UNIT} constant, rather than explicitly specifying the unit. This constant represents the unit that users
 * of Nano will be most familiar with.</p>
 * <p>Below are a few currency conversion examples:</p>
 * <pre>
 *   // Convert 1.337 knano (KILO) to the base unit (currently MEGA, or "Nano")
 *   BigDecimal conv1 = CurrencyDivisor.BASE_UNIT
 *          .convertFrom(CurrencyDivisor.KILO, new BigDecimal("1.337"));
 *   System.out.println("1337 knano = " + conv1.toPlainString() + " Nano"); // Prints 1337 knano = 0.001337 Nano
 *
 *   // Convert 250 unano (MICRO) to raw (RAW)
 *   BigInteger conv2 = CurrencyDivisor.RAW
 *          .convertIntFrom(CurrencyDivisor.MICRO, BigInteger.valueOf(250));
 *   System.out.println("250 unano = " + conv2.toString() + " raw"); // Prints 250 unano = 250000000000000000000 raw
 * </pre>
 */
public enum CurrencyDivisor {
    
    /**
     * The largest divisor, equivalent to {@code 10^33} raw.
     */
    GIGA(33, "Gnano", "Gxrb"),
    
    /**
     * The 2nd largest divisor, equivalent to {@code 10^30} raw.
     */
    MEGA(30, "Nano", "Mxrb"),
    
    /**
     * The 3rd largest divisor, equivalent to {@code 10^27} raw.
     */
    KILO(27, "knano", "kxrb"),
    
    /**
     * The 4th largest divisor, equivalent to {@code 10^24} raw.
     */
    XRB(24, "nano", "xrb"),
    
    /**
     * The 5th largest divisor, equivalent to {@code 10^21} raw.
     */
    MILLI(21, "mnano", "mxrb"),
    
    /**
     * The 6th largest divisor, equivalent to {@code 10^18} raw.
     */
    MICRO(18, "μnano", "uxrb"),
    
    /**
     * The smallest possible representable unit.
     */
    RAW(0, "raw", "raw");
    
    
    /**
     * <p>The standard base unit currently used by most services, block explorers and exchanges.</p>
     * <p>End-users are likely to be most familiar with this unit, and it is recommended that this constant is used so
     * your application can be automatically updated should the units system ever change.</p>
     * <p>As of this current version, this is equal to the {@link #MEGA} unit.</p>
     */
    public static final CurrencyDivisor BASE_UNIT = CurrencyDivisor.MEGA;
    
    private static final DecimalFormat FRIENDLY_DECIMAL_FORMAT = new DecimalFormat("#,##0.######");
    
    int exponent;
    BigInteger rawValue;
    String displayName, classicName;
    
    CurrencyDivisor(int exponent, String displayName, String classicName) {
        this.exponent = exponent;
        this.rawValue = BigInteger.TEN.pow(exponent);
        this.displayName = displayName;
        this.classicName = classicName;
    }
    
    
    /**
     * <p>Returns the exponent of the unit as a power of 10.</p>
     * <p>For instance, 10<sup>x</sup>, with {@code x} being the value returned by this method.</p>
     */
    public int getExponent() {
        return exponent;
    }
    
    
    /**
     * Returns the equivalent value of a single unit in raw.
     *
     * @return the equivalent raw value of 1 unit
     */
    public BigInteger getRawValue() {
        return rawValue;
    }
    
    
    /**
     * Returns the human-readable name for this currency unit.
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Returns the classic legacy name used within previous versions of the node.
     */
    public String getClassicName() {
        return classicName;
    }
    
    
    @Override
    public String toString() {
        return getDisplayName();
    }
    
    
    /**
     * <p>Converts the specified unit and amount into this unit.</p>
     * <p>If you are converting from a smaller unit and fractional digits are lost, then an {@link ArithmeticException}
     * will be thrown. If you wish to bypass this, use {@link #convertFrom(CurrencyDivisor, BigInteger)} and transform
     * the retrieved value into a BigInteger using the {@link BigDecimal#toBigInteger()} method.</p>
     *
     * @param sourceAmount the source amount to convert from
     * @param sourceUnit   the source unit to convert from
     * @return the converted value in this unit
     *
     * @throws ArithmeticException if the conversion would result in a loss of information
     */
    public BigInteger convertIntFrom(CurrencyDivisor sourceUnit, BigInteger sourceAmount) {
        return convertIntFrom(sourceUnit, new BigDecimal(sourceAmount));
    }
    
    /**
     * <p>Converts the specified unit and amount into this unit.</p>
     * <p>If you are converting from a smaller unit and fractional digits are lost, then an {@link ArithmeticException}
     * will be thrown. If you wish to bypass this, use {@link #convertFrom(CurrencyDivisor, BigDecimal)} and transform
     * the retrieved value into a BigInteger using the {@link BigDecimal#toBigInteger()} method.</p>
     *
     * @param sourceAmount the source amount to convert from
     * @param sourceUnit   the source unit to convert from
     * @return the converted value in this unit
     *
     * @throws ArithmeticException if the conversion would result in a loss of information
     */
    public BigInteger convertIntFrom(CurrencyDivisor sourceUnit, BigDecimal sourceAmount) {
        try {
            return this.convertFrom(sourceUnit, sourceAmount).toBigIntegerExact();
        } catch (ArithmeticException e) {
            throw new ArithmeticException(
                    String.format("Converting %s %s to %s is not permitted, as fractional amounts would be truncated." +
                                    " Use convert(sourceAmount, sourceUnit).toBigInteger() if you are okay with " +
                                    "losing this information.",
                            sourceAmount, sourceUnit.getDisplayName(), this.getDisplayName()));
        }
    }
    
    
    /**
     * Converts the specified unit and amount into this unit.
     *
     * @param sourceAmount the source amount to convert from
     * @param sourceUnit   the source unit to convert from
     * @return the converted value in this unit
     */
    public BigDecimal convertFrom(CurrencyDivisor sourceUnit, BigInteger sourceAmount) {
        return this.convertFrom(sourceUnit, new BigDecimal(sourceAmount));
    }
    
    /**
     * Converts the specified unit and amount into this unit.
     *
     * @param sourceAmount the source amount to convert from
     * @param sourceUnit   the source unit to convert from
     * @return the converted value in this unit
     */
    public BigDecimal convertFrom(CurrencyDivisor sourceUnit, BigDecimal sourceAmount) {
        // Argument checks
        if (sourceAmount == null)
            throw new IllegalArgumentException("Source amount cannot be null");
        if (sourceUnit == null)
            throw new IllegalArgumentException("Source unit cannot be null");
        if (sourceAmount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Source amount cannot be negative");
        
        if (sourceUnit == this)
            return sourceAmount; // Same unit
        
        if (sourceUnit.exponent > this.exponent) { // Source is higher, multiply (shift right)
            return sourceAmount.movePointRight(sourceUnit.exponent - this.exponent).stripTrailingZeros();
        } else { // Source is lower, divide (shift left)
            return sourceAmount.movePointLeft(this.exponent - sourceUnit.exponent).stripTrailingZeros();
        }
    }
    
    /**
     * <p>Converts a given value of <i>raw</i> to the current base unit ({@link #BASE_UNIT}), and formats the number to
     * up to 6 decimal places (rounding up truncated digits), along with a suffix of the unit name. The value will
     * also be formatted to contain separating commas for every 3 digits.</p>
     * <p>For instance, a value of {@code 1234567000000000000000000000000001} will return {@code 1,234.567001 Nano}.</p>
     * <p>This value should not be used for any computations, and should only be used for displaying quantities of
     * the currency to a user.</p>
     * @param rawAmount the amount of raw to convert from
     * @return a friendly string of a given currency amount
     */
    public static String toFriendlyString(BigInteger rawAmount) {
        BigDecimal nanoAmount = BASE_UNIT.convertFrom(RAW, rawAmount)
                .setScale(6, RoundingMode.CEILING);
        return FRIENDLY_DECIMAL_FORMAT.format(nanoAmount) + " " + BASE_UNIT.getDisplayName();
    }
    
    
    /**
     * @deprecated Method renamed. Use {@link #convertFrom(CurrencyDivisor, BigDecimal)} instead.
     */
    @Deprecated(forRemoval = true)
    public BigDecimal convert(BigDecimal sourceAmount, CurrencyDivisor sourceUnit) {
        return convertFrom(sourceUnit, sourceAmount);
    }
    
    /**
     * @deprecated Method renamed. Use {@link #convertFrom(CurrencyDivisor, BigInteger)} instead.
     */
    @Deprecated(forRemoval = true)
    public BigDecimal convert(BigInteger sourceAmount, CurrencyDivisor sourceUnit) {
        return convertFrom(sourceUnit, sourceAmount);
    }
    
    /**
     * This method is potentially unsafe, as fractional values can be lost when converting to a larger unit.
     *
     * @deprecated Method unsafe. Use {@link #convertFrom(CurrencyDivisor, BigInteger)} instead.
     */
    @Deprecated(forRemoval = true)
    public BigInteger convertInt(BigDecimal sourceAmount, CurrencyDivisor sourceUnit) {
        return this.convertFrom(sourceUnit, sourceAmount).toBigInteger();
    }
    
    /**
     * This method is potentially unsafe, as fractional values can be lost when converting to a larger unit.
     *
     * @deprecated Method unsafe. Use {@link #convertFrom(CurrencyDivisor, BigInteger)} instead.
     */
    @Deprecated(forRemoval = true)
    public BigInteger convertInt(BigInteger sourceAmount, CurrencyDivisor sourceUnit) {
        return this.convertFrom(sourceUnit, sourceAmount).toBigInteger();
    }
    
}
