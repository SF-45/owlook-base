package space.sadfox.owlook.base.jaxb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;

public class ChangeHistoryTest {
  static class ChangeHistoryKeepingImpl implements ChangeHistoryKeeping {

    StringProperty strProp = new SimpleStringProperty("defaultStringProperty");
    IntegerProperty intProp = new SimpleIntegerProperty();
    ObservableList<StringProperty> obsListStrProps = FXCollections.observableArrayList();
    ObservableMap<IntegerProperty, DoubleProperty> obsMapIntDoubProps = FXCollections.observableHashMap();
    ObservableSet<DoubleProperty> obsSetDoubProps = FXCollections.observableSet();
    ChangeHistoryKeepingImpl2 chChild = new ChangeHistoryKeepingImpl2();

    @Override
    public List<Object> getProperties() {
      return Arrays.asList(strProp, intProp, obsListStrProps, obsMapIntDoubProps, obsSetDoubProps, chChild);
    }

  }

  static class ChangeHistoryKeepingImpl2 implements ChangeHistoryKeeping {

    StringProperty strProp = new SimpleStringProperty("testValue1");

    @Override
    public List<Object> getProperties() {
      return Arrays.asList(strProp);
    }
  }

  ChangeHistoryKeepingImpl chParent;
  ChangeHistory<ChangeHistoryKeepingImpl> changeHistory;

  @BeforeEach
  void setUp() {
    chParent = new ChangeHistoryKeepingImpl();
    changeHistory = new ChangeHistory<>(chParent);
  }

  @Test
  void testListener() {
    ChangeHistoryListener<ChangeHistoryKeepingImpl> changeListener = change -> {
      assertTrue(change.wasModify());
      assertFalse(change.wasRedo());
      assertFalse(change.wasUndo());
    };
    changeHistory.addListener(changeListener);
    chParent.intProp.set(10);
    changeHistory.removeListener(changeListener);

    changeListener = change -> {
      assertFalse(change.wasModify());
      assertFalse(change.wasRedo());
      assertTrue(change.wasUndo());
    };
    changeHistory.addListener(changeListener);
    changeHistory.back();
    changeHistory.removeListener(changeListener);

    changeListener = change -> {
      assertFalse(change.wasModify());
      assertTrue(change.wasRedo());
      assertFalse(change.wasUndo());
    };
    changeHistory.addListener(changeListener);
    changeHistory.forward();
  }

  @Test
  void testEmptyBackForward() {
    assertEquals(changeHistory.getBackSize(), 0);
    assertEquals(changeHistory.getForwardSize(), 0);

    changeHistory.back();
    assertEquals(changeHistory.getBackSize(), 0);
    changeHistory.forward();
    assertEquals(changeHistory.getForwardSize(), 0);
  }

  @Test
  void testSimplePropertiesHistory() {
    chParent.intProp.set(10);
    chParent.strProp.set("testStr1");
    chParent.intProp.set(15);
    chParent.strProp.set("testStr2");
    assertEquals(changeHistory.getBackSize(), 4);

    changeHistory.back();
    assertEquals(chParent.strProp.get(), "testStr1");
    assertEquals(chParent.intProp.get(), 15);
    assertEquals(changeHistory.getBackSize(), 3);

    changeHistory.back();
    assertEquals(chParent.strProp.get(), "testStr1");
    assertEquals(chParent.intProp.get(), 10);
    assertEquals(changeHistory.getBackSize(), 2);
    assertEquals(changeHistory.getForwardSize(), 2);

    chParent.intProp.set(1);
    assertEquals(changeHistory.getForwardSize(), 0);
    assertEquals(changeHistory.getBackSize(), 3);

    changeHistory.back();
    assertEquals(chParent.intProp.get(), 10);
  }

  @Test
  void testOblervableListHistory() {
    final var obsList = chParent.obsListStrProps;
    final StringProperty testStr0 = strP("testStr0");
    final StringProperty testStr1 = strP("testStr1");
    final StringProperty testStr2 = strP("testStr2");
    obsList.addAll(testStr0, testStr1, testStr2);

    obsList.remove(testStr1);
    changeHistory.back();
    assertEquals(obsList.indexOf(testStr1), 1);

    changeHistory.forward();
    assertEquals(obsList.indexOf(testStr2), 1);
    changeHistory.back();

    obsList.removeAll(testStr0, testStr1);
    changeHistory.back();
    assertEquals(obsList.indexOf(testStr0), 0);
    assertEquals(obsList.indexOf(testStr1), 1);
    assertEquals(obsList.indexOf(testStr2), 2);
    changeHistory.forward();
    assertEquals(obsList.indexOf(testStr2), 0);

    changeHistory.allBack();
    assertEquals(obsList.size(), 0);
    changeHistory.allForward();
    assertEquals(obsList.size(), 1);
    changeHistory.back();
    assertEquals(obsList.size(), 3);

    testStr0.set("newTestStr0");
    testStr1.set("newTestStr1");
    testStr2.set("newTestStr2");
    changeHistory.back();
    assertEquals(testStr2.get(), "testStr2");
    changeHistory.back();
    assertEquals(testStr1.get(), "testStr1");
    changeHistory.back();
    assertEquals(testStr0.get(), "testStr0");
    changeHistory.allForward();
    assertEquals(testStr0.get(), "newTestStr0");
    assertEquals(testStr1.get(), "newTestStr1");
    assertEquals(testStr2.get(), "newTestStr2");
  }

  @Test
  void testUnSubscribe() {
    var strProp = strP("testStr0");
    var obsList = chParent.obsListStrProps;
    obsList.add(strProp);

    strProp.set("newTestStr0");
    changeHistory.back();
    assertEquals(strProp.get(), "testStr0");
    obsList.remove(strProp);
    strProp.set("newTestStr0");
    changeHistory.back();
    assertEquals(strProp.get(), "newTestStr0");
    assertTrue(obsList.contains(strProp));
  }

  @Test
  void testObservableMapHistory() {
    var obsMap = chParent.obsMapIntDoubProps;
    var key0 = intP(10);
    var value00 = doubP(0.5);
    var value01 = doubP(1.5);
    var key1 = intP(20);
    var value10 = doubP(2.5);

    obsMap.put(key0, value00);
    obsMap.put(key1, value10);
    value10.set(5d);
    obsMap.put(key0, value01);
    key0.set(20);
    obsMap.remove(key1);
    key1.set(30);
    obsMap.remove(key0);

    assertEquals(obsMap.size(), 0);
    changeHistory.back();
    assertEquals(obsMap.get(key0).get(), 1.5);
    changeHistory.back();
    assertEquals(key1.get(), 30);
    assertEquals(obsMap.get(key1).get(), 5d);
    changeHistory.back();
    assertEquals(key0.get(), 10);
    assertEquals(obsMap.get(key0).get(), 1.5);
    changeHistory.back();
    assertEquals(obsMap.get(key0).get(), 0.5);
    changeHistory.back();
    assertEquals(obsMap.get(key1).get(), 2.5);
    changeHistory.back();
    assertFalse(obsMap.containsKey(key1));
    assertTrue(obsMap.containsKey(key0));
    changeHistory.back();
    assertTrue(obsMap.isEmpty());
  }

  @Test
  void testObservableSetHistroy() {
    var obsSet = chParent.obsSetDoubProps;
    var key0 = doubP(1d);
    var key1 = doubP(2d);
    var key2 = doubP(3d);
    obsSet.addAll(Arrays.asList(key0, key1, key2));

    key0.set(1.5);
    key1.set(2.5);
    key2.set(3.5);

    obsSet.remove(key0);

    changeHistory.allBack();
    assertTrue(obsSet.isEmpty());

    changeHistory.forward();
    changeHistory.forward();
    changeHistory.forward();
    assertTrue(obsSet.contains(key0));
    assertTrue(obsSet.contains(key1));
    assertTrue(obsSet.contains(key2));

    assertEquals(key0.get(), 1d);
    assertEquals(key1.get(), 2d);
    assertEquals(key2.get(), 3d);

    changeHistory.allForward();
    assertEquals(key0.get(), 1.5d);
    assertEquals(key1.get(), 2.5d);
    assertEquals(key2.get(), 3.5d);
    assertFalse(obsSet.contains(key0));
  }

  @Test
  void testChildChangeHistoryKeepingImpl() {
    var childStrProp = chParent.chChild.strProp;
    childStrProp.set("newTestValue");
    assertEquals(childStrProp.get(), "newTestValue");
    changeHistory.back();
    assertEquals(childStrProp.get(), "testValue1");

  }

  private StringProperty strP(String str) {
    return new SimpleStringProperty(str);
  }

  private IntegerProperty intP(int i) {
    return new SimpleIntegerProperty(i);
  }

  private DoubleProperty doubP(double d) {
    return new SimpleDoubleProperty(d);
  }
}
