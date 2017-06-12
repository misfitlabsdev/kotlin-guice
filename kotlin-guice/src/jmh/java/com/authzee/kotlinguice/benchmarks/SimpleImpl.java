package com.authzee.kotlinguice.benchmarks;

/**
 * @author John Leacox
 */
public class SimpleImpl implements Simple {
  @Override
  public String value() {
    return "Impl of Simple";
  }
}
