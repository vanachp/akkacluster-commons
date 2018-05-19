package termination.policies

import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.Member
import termination.data.OrderlyComparableList

private[policies] trait RoleRatioTerminationPolicy extends TerminationPolicy{

  type Role = String
  type RoleValue = Double

  case class RoleScore(role: Role, value: Double) extends Comparable[RoleScore]{
    override def compareTo(o: RoleScore): Int =  value.compareTo(o.value)
  }

  override def chooseSelfCluster(state: CurrentClusterState): Boolean = {
    val reachableMembers = state.members -- state.unreachable
    val unreachableMembers = state.unreachable
    val sortedReachableRatio = getSortedRoleRatio(state, reachableMembers, policySetting.roleRatioMapping.get)
    val sortedUnreachableRatio = getSortedRoleRatio(state, unreachableMembers, policySetting.roleRatioMapping.get)
    log.warning(s"Comparing between $sortedReachableRatio and $sortedUnreachableRatio")
    val result = sortedReachableRatio.compareTo(sortedUnreachableRatio) match {
      case 0 => reachableMembers.size.compareTo(unreachableMembers.size)
      case x => x
    }
    isGreater(result)
  }

  private def isGreater(compareResult: Int): Boolean = {
    compareResult == 1
  }

  private def getSortedRoleRatio(state: CurrentClusterState, members: Set[Member], ratioMapping: Map[String, Int]): OrderlyComparableList[RoleScore] = {
    val roles = ratioMapping.keySet
    val list = roles.map(x => RoleScore(x, members.count(_.hasRole(x)).toDouble/ ratioMapping.get(x).get)).toList.sortBy(_.value)
    new OrderlyComparableList[RoleScore](list)
  }
}
