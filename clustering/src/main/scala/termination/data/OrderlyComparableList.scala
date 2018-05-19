package termination.data

/**
  * Compare 2 sortable lists index per index until find some not equal elements or one of the list become empty
  */
class OrderlyComparableList[A <: Comparable[A]](list: Seq[A]) extends Comparable[OrderlyComparableList[A]] {

  protected val ordering: Ordering[A] = implicitly[Ordering[A]]
  protected val sortedList: Seq[A] = list.sorted

  override def compareTo(target: OrderlyComparableList[A]): Int = {
    (sortedList.isEmpty, target.sortedList.isEmpty) match {
      case (true, true)  => 0
      case (true, false) => -1
      case (false, true) => 1
      case (false, false) =>
        sortedList.head.compareTo(target.sortedList.head) match {
          case 0 => getTails().compareTo(target.getTails())
          case x => x
        }
    }
  }

  def getTails(): OrderlyComparableList[A] = {
    new OrderlyComparableList(sortedList.tail)
  }

}
