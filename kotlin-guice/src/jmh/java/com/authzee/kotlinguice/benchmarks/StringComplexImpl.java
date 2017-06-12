package com.authzee.kotlinguice.benchmarks;

/**
 * @author John Leacox
 */
public class StringComplexImpl implements Complex<String> {
  @Override
  public String value() {
    return "String Impl of Complex";
  }
}
