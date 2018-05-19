package termination.policies

import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.Member

private[policies] trait NoOpTerminationPolicy extends TerminationPolicy{
  override def chooseSelfCluster(state: CurrentClusterState): Boolean = false
  override def downHandler(currentClusterState: CurrentClusterState, nodes: Set[Member]): Unit = {
    //do nothing
  }
}
