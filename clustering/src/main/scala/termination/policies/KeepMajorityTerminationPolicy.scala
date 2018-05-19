package termination.policies

import akka.cluster.ClusterEvent.CurrentClusterState

private[policies] trait KeepMajorityTerminationPolicy extends TerminationPolicy{

  override def chooseSelfCluster(state: CurrentClusterState): Boolean = {
    isMajority(state.members.size, state.unreachable.size)
  }

  private def majority(n: Int): Int = (n + 1)/2 + (n + 1) % 2

  private def isMajority(total: Int, dead: Int): Boolean = {
    require(total > 0)
    require(dead >= 0)
    (total - dead) >= majority(total)
  }
}
