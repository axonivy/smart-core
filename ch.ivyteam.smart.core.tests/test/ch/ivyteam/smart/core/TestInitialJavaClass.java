package ch.ivyteam.smart.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class TestInitialJavaClass {

  @Test
  void run() {
    assertThat(InitialJavaClass.run()).isEqualTo("Hello, World!");
  }
}
