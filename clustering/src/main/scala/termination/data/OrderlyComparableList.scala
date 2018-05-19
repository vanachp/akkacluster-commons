package termination.data

/**
  * Compare 2 sortable lists index per index until find some not equal elements or one of the list become empty
  */
class OrderlyComparableList[A <: Comparable[A]](list: List[A], sorted: Boolean = false) extends Comparable[OrderlyComparableList[A]] {

  protected val ordering: Ordering[A] = implicitly[Ordering[A]]
  protected val sortedList: List[A] = if(sorted) list else list.sorted

  override def compareTo(target: OrderlyComparableList[A]): Int = {
    (sortedList, target.sortedList) match {
      case (head1 :: sortedList, head2 :: sortedList2) => {
        val compareResult = head1.compareTo(head2)
        if(compareResult!=0){
          compareResult
        }else{
          getTails().compareTo(target.getTails())
        }
      }
      case (head1 :: sortedList, sortedList2) => 1
      case (sortedList1, head2:: sortedList2) => -1
      case _ => 0
    }
  }

  def getTails(): OrderlyComparableList[A] = {
    new OrderlyComparableList(sortedList.tail, true)
  }

}
