package space.sadfox.owlook.base.moduleapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class VersionFormatTest {

  @Test
  void versionOfTest() {
    var ver = new VersionFormat(1, 2, 3, "beta");
    var verOf = VersionFormat.of("1.2.3-beta");
    assertEquals(ver, verOf);

    assertThrows(IllegalArgumentException.class, () -> {
      VersionFormat.of("Hello World");
    });

    assertThrows(NumberFormatException.class, () -> {
      VersionFormat.of("a.b.c");
    });
  }

  @Test
  void structVersionTest() {
    var ver = new VersionFormat(1, 2, 3, "beta");
    assertEquals(1, ver.compatibility());
    assertEquals(2, ver.primary());
    assertEquals(3, ver.secondary());
    assertEquals("beta", ver.suffix());
  }

  @Test
  void isReleaseTest() {
    var releaseVer = new VersionFormat(1, 2, 3, "");
    assertTrue(releaseVer.isRelease());

    var betaVer = new VersionFormat(1, 2, 3, "beta");
    assertFalse(betaVer.isRelease());
  }

  @Test
  void compatibilityVersionTest() {
    var ver1 = VersionFormat.of("1.0.0");
    var ver2 = VersionFormat.of("1.4.2-beta");
    assertTrue(ver1.compatibleWith(ver2));

    ver1 = VersionFormat.of("0.2.3");
    assertFalse(ver1.compatibleWith(ver2));
  }

  @Test
  void compareVersionsTest() {
    var ver1 = VersionFormat.of("1.9.9");
    var ver2 = VersionFormat.of("1.0.0-beta");

    assertEquals(1, ver1.compareTo(ver2));
    assertEquals(-1, ver2.compareTo(ver1));
    assertEquals(0, ver1.compareTo(ver1));

    ver2 = VersionFormat.of("0.0.0");
    assertEquals(1, ver1.compareTo(ver2));

    ver1 = VersionFormat.of("0.0.0");
    assertEquals(0, ver1.compareTo(ver2));
  }

  @Test
  void toStringTest() {
    var ver = new VersionFormat(0, 1, 4, "beta");
    assertEquals("0.1.4-beta", ver.toString());
    ;
  }

}
