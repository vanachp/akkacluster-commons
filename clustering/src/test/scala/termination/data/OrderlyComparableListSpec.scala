package termination.data

import org.scalatest.{FunSuite, Matchers}

class OrderlyComparableListSpec extends FunSuite with Matchers{

  test("1) OrderlyComparableListSpec should compare correctly if both lists are empty"){
    val list1: List[Integer] = List.empty
    val list2: List[Integer] = List.empty
    new OrderlyComparableList(list1).compareTo(new OrderlyComparableList(list2)) should be(0)
  }

  test("2) OrderlyComparableListSpec should compare correctly if list1 is empty"){
    val list1: List[Integer] = List.empty
    val list2: List[Integer] = List(1)
    new OrderlyComparableList(list1).compareTo(new OrderlyComparableList(list2)) should be(-1)
  }

  test("3) OrderlyComparableListSpec should compare correctly if list2 is empty"){
    val list1: List[Integer] = List(1)
    val list2: List[Integer] = List.empty
    new OrderlyComparableList(list1).compareTo(new OrderlyComparableList(list2)) should be(1)
  }

  test("4) OrderlyComparableListSpec should compare correctly if list1 first element is less than list2"){
    val list1: List[Integer] = List(1)
    val list2: List[Integer] = List(2)
    new OrderlyComparableList(list1).compareTo(new OrderlyComparableList(list2)) should be(-1)
  }

  test("5) OrderlyComparableListSpec should compare correctly if list1 first element is greater than list2"){
    val list1: List[Integer] = List(2)
    val list2: List[Integer] = List(1)
    new OrderlyComparableList(list1).compareTo(new OrderlyComparableList(list2)) should be(1)
  }

  test("6) OrderlyComparableListSpec should compare correctly if list1 first elements are equal, but list1 has less element"){
    val list1: List[Integer] = List(1)
    val list2: List[Integer] = List(1, 2)
    new OrderlyComparableList(list1).compareTo(new OrderlyComparableList(list2)) should be(-1)
  }

  test("7) OrderlyComparableListSpec should compare correctly if list1 first elements are equal, but second element is different"){
    val list1: List[Integer] = List(1, 1)
    val list2: List[Integer] = List(1, 2)
    new OrderlyComparableList(list1).compareTo(new OrderlyComparableList(list2)) should be(-1)
  }

  test("8) OrderlyComparableListSpec should compare correctly if list1 first two elements are equal, but third element is different"){
    val list1: List[Integer] = List(1, 2, 4)
    val list2: List[Integer] = List(1, 2, 3)
    new OrderlyComparableList(list1).compareTo(new OrderlyComparableList(list2)) should be(1)
  }
}
