package termination.policies

import akka.actor.{Actor, ActorLogging, ActorRef, SupervisorStrategy, Terminated}
import akka.cluster.ClusterEvent._
import akka.cluster.{Cluster, Member}
import akka.remote.ThisActorSystemQuarantinedEvent
import termination.config.TerminationPolicySettings
import termination.data.DownSchedulerMessage

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

trait TerminationPolicy extends Actor with ActorLogging {
  import context.dispatcher

  override val supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy

  protected val cluster: Cluster = Cluster(context.system)

  def policySetting: TerminationPolicySettings

  override def receive: Receive = Actor.emptyBehavior

  override def preStart(): Unit = {
    cluster.subscribe(self,InitialStateAsEvents, classOf[LeaderChanged], classOf[MemberExited], classOf[MemberJoined],
      classOf[UnreachableMember], classOf[ReachableMember], classOf[MemberLeft], classOf[MemberExited], classOf[MemberRemoved]
    )
    context.system.eventStream.subscribe(self, classOf[ThisActorSystemQuarantinedEvent])
    context.become(active(self))
  }

  private def active(machine: ActorRef): Receive = {
    policyHandler(machine) orElse customHandler(machine)
  }

  //  For polimorph behaviour
  protected def customHandler(machine:ActorRef): Receive

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self)
    cluster.unsubscribe(self)
  }

  private def policyHandler(machine: ActorRef): Receive = {
    case MemberExited(member) =>
      log.warning(s"Member $member has exited!")

    case MemberJoined(member) =>
      log.info(s"Member $member has joined")

    case LeaderChanged(member)=>
      log.warning(s"Leader has changed to $member")

    case UnreachableMember(member) =>
      log.warning(s"Member $member is unreachable")
      val unreachable = cluster.state.getUnreachable.asScala.toSet
      if (unreachable.nonEmpty) {
        context.system.scheduler.scheduleOnce(policySetting.stableTime, self, DownSchedulerMessage(unreachable))
      }

    case ReachableMember(member) =>
      log.warning(s"Member $member become reachable")

    case DownSchedulerMessage(nodes) =>
      val state = cluster.state
      downHandler(state, nodes)

    case Terminated(`machine`) =>
      log.error("Terminating the system, because constructr-machine has terminated!")
      terminateSelf()

    case MemberRemoved(member, previousStatus) =>
      if( member.address == Cluster(context.system).selfAddress){
        log.error("Terminating the system, because member has been removed!")
        terminateSelf()
      }else{
        log.warning(s"Member $member is removed (was $previousStatus)")
      }
    case ThisActorSystemQuarantinedEvent(localAddress, uid) =>
      log.error(s"Terminating the system, because member has been quanrantined! address: $localAddress")
      terminateSelf()
  }

  protected def downHandler(currentClusterState: CurrentClusterState, nodes: Set[Member]): Unit = {
    currentClusterState.unreachable.equals(nodes) && nodes.nonEmpty match {
      case true if chooseSelfCluster(currentClusterState) =>
        //Down the rest
        log.warning(s"We are selected, downing unreachable nodes $nodes")
        nodes.foreach(mem => cluster.down(mem.address))

      case true =>
        //Down self
        log.warning(s"We are inferior, will terminate")
        terminateSelf()

      case false =>
        log.warning(s"Do nothing, cluster's state has changed")
    }
  }

  protected def chooseSelfCluster(state: CurrentClusterState): Boolean

  protected def terminateSelf(): Unit = {
    // exit JVM when ActorSystem has been terminated
    log.error(s"Shutting down self...")
    context.system.registerOnTermination(System.exit(0))

    // shut down ActorSystem
    context.system.terminate()

    // In case ActorSystem shutdown takes longer than 10 seconds,
    // exit the JVM forcefully anyway.
    // We must spawn a separate thread to not block current thread,
    // since that would have blocked the shutdown of the ActorSystem.
    new Thread {
      override def run(): Unit = {
        if (Try(Await.ready(context.system.whenTerminated, 10.seconds)).isFailure) {
          System.exit(-1)
        }
      }
    }.start()
  }
}

object TerminationPolicy{
  val MAJORITY = "majority"
  val ROLE_RATIO = "roleratio"
  val NOOP = "off"
}