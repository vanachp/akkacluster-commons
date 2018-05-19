package termination.data

import akka.cluster.Member

case class DownSchedulerMessage(nodes: Set[Member])
