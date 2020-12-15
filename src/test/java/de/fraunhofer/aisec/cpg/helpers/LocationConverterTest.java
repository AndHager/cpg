package de.fraunhofer.aisec.cpg.helpers;

import com.google.common.base.Objects;
import de.fraunhofer.aisec.cpg.sarif.PhysicalLocation;
import de.fraunhofer.aisec.cpg.sarif.Region;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.neo4j.ogm.typeconversion.CompositeAttributeConverter;

public class LocationConverterTest extends BaseAttributeConverterTest<PhysicalLocation> {

  private static final String URI_STRING = "test://test:1234";
  private static final URI URI_TO_TEST = URI.create(URI_STRING);

  CompositeAttributeConverter<PhysicalLocation> getSut() {
    return new LocationConverter();
  }

  @Test
  void toEntityAttributeWithNullValueLine() {
    // arrange
    final CompositeAttributeConverter<PhysicalLocation> sut = getSut();
    // act
    final PhysicalLocation have = sut.toEntityAttribute(null);
    // assert
    Assertions.assertNull(have);
  }

  @Test
  void toEntityAttributeWithNullStartLine() {
    // arrange
    final CompositeAttributeConverter<PhysicalLocation> sut = getSut();
    final Map<String, ?> value = new HashMap<>();
    value.put(LocationConverter.START_LINE, null);
    // act
    final PhysicalLocation have = sut.toEntityAttribute(value);
    // assert
    Assertions.assertNull(have);
  }

  @Test
  void toEntityAttributeWithInteger() {
    // arrange
    final CompositeAttributeConverter<PhysicalLocation> sut = getSut();
    final Map<String, Object> value = new HashMap<>();
    final int startLineValue = 1;
    value.put(LocationConverter.START_LINE, startLineValue); // autoboxing to Integer
    final int endLineValue = 2;
    value.put(LocationConverter.END_LINE, endLineValue);
    final int startColumnValue = 3;
    value.put(LocationConverter.START_COLUMN, startColumnValue);
    final int endColumnValue = 4;
    value.put(LocationConverter.END_COLUMN, endColumnValue);
    value.put(LocationConverter.ARTIFACT, URI_STRING);
    final Region region =
        new Region(startLineValue, startColumnValue, endLineValue, endColumnValue);
    final PhysicalLocation want = new PhysicalLocation(URI_TO_TEST, region);
    // act
    final PhysicalLocation have = sut.toEntityAttribute(value);
    // assert
    Assertions.assertEquals(want, have);
  }

  @Test
  void toEntityAttributeWithLong() {
    // arrange
    final CompositeAttributeConverter<PhysicalLocation> sut = getSut();
    final Map<String, Object> value = new HashMap<>();
    final long startLineValue = 1;
    value.put(LocationConverter.START_LINE, startLineValue); // autoboxing to Long
    final long endLineValue = 2;
    value.put(LocationConverter.END_LINE, endLineValue);
    final long startColumnValue = 3;
    value.put(LocationConverter.START_COLUMN, startColumnValue);
    final long endColumnValue = 4;
    value.put(LocationConverter.END_COLUMN, endColumnValue);
    value.put(LocationConverter.ARTIFACT, URI_STRING);
    final Region region =
        new Region(
            (int) startLineValue, (int) startColumnValue,
            (int) endLineValue, (int) endColumnValue);
    final PhysicalLocation want = new PhysicalLocation(URI_TO_TEST, region);
    // act
    final PhysicalLocation have = sut.toEntityAttribute(value);
    // assert
    Assertions.assertEquals(want, have);
  }

  @Test
  void toEntityAttributeWithIntegerAndLong() {
    // arrange
    final CompositeAttributeConverter<PhysicalLocation> sut = getSut();
    final Map<String, Object> value = new HashMap<>();
    final int startLineValue = 1;
    value.put(LocationConverter.START_LINE, startLineValue); // autoboxing to Integer
    final int endLineValue = 2;
    value.put(LocationConverter.END_LINE, endLineValue);
    final long startColumnValue = 3;
    value.put(LocationConverter.START_COLUMN, startColumnValue); // autoboxing to Long
    final long endColumnValue = 4;
    value.put(LocationConverter.END_COLUMN, endColumnValue);
    value.put(LocationConverter.ARTIFACT, URI_STRING);
    final Region region =
        new Region(
            startLineValue, (int) startColumnValue,
            endLineValue, (int) endColumnValue);
    final PhysicalLocation want = new PhysicalLocation(URI_TO_TEST, region);
    // act
    final PhysicalLocation have = sut.toEntityAttribute(value);
    // assert
    Assertions.assertEquals(want, have);
  }

  @Test
  void toEntityAttributeWithStrings() {
    // arrange
    final CompositeAttributeConverter<PhysicalLocation> sut = getSut();
    final Map<String, Object> value = new HashMap<>();
    final String startLineValue = "1";
    value.put(LocationConverter.START_LINE, startLineValue);
    final String endLineValue = "2";
    value.put(LocationConverter.END_LINE, endLineValue);
    final String startColumnValue = "3";
    value.put(LocationConverter.START_COLUMN, startColumnValue);
    final String endColumnValue = "4";
    value.put(LocationConverter.END_COLUMN, endColumnValue);
    value.put(LocationConverter.ARTIFACT, URI_STRING);
    final Region region =
        new Region(
            Integer.parseInt(startLineValue),
            Integer.parseInt(startColumnValue),
            Integer.parseInt(endLineValue),
            Integer.parseInt(endColumnValue));
    final PhysicalLocation want = new PhysicalLocation(URI_TO_TEST, region);
    // act
    final PhysicalLocation have = sut.toEntityAttribute(value);
    // assert
    Assertions.assertEquals(want, have);
  }

  @Test
  void toEntityAttributeWithCustomTypes() {
    // arrange
    final CompositeAttributeConverter<PhysicalLocation> sut = getSut();
    final Map<String, Object> value = new HashMap<>();
    final Object startLineValue = new CustomNumber(1);
    value.put(LocationConverter.START_LINE, startLineValue);
    final Object endLineValue = new CustomNumber(2);
    value.put(LocationConverter.END_LINE, endLineValue);
    final Object startColumnValue = new CustomNumber(3);
    value.put(LocationConverter.START_COLUMN, startColumnValue);
    final Object endColumnValue = new CustomNumber(4);
    value.put(LocationConverter.END_COLUMN, endColumnValue);
    value.put(LocationConverter.ARTIFACT, URI_STRING);
    final Region region =
        new Region(
            Integer.parseInt(startLineValue.toString()),
            Integer.parseInt(startColumnValue.toString()),
            Integer.parseInt(endLineValue.toString()),
            Integer.parseInt(endColumnValue.toString()));
    final PhysicalLocation want = new PhysicalLocation(URI_TO_TEST, region);
    // act
    final PhysicalLocation have = sut.toEntityAttribute(value);
    // assert
    Assertions.assertEquals(want, have);
  }

  private static class CustomNumber extends Number {

    private final int value;

    private CustomNumber(final int value) {
      this.value = value;
    }

    @Override
    public int intValue() {
      return value;
    }

    @Override
    public long longValue() {
      return intValue();
    }

    @Override
    public float floatValue() {
      return longValue();
    }

    @Override
    public double doubleValue() {
      return floatValue();
    }

    @Override
    public String toString() {
      return Integer.toString(intValue());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof CustomNumber)) return false;
      CustomNumber that = (CustomNumber) o;
      return value == that.value;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(value);
    }
  }
}