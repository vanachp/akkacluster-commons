package termination.config

import java.util.concurrent.TimeUnit

import com.typesafe.config.{Config, ConfigFactory}
import termination.policies.TerminationPolicy

import scala.collection.JavaConverters._
import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps
import scala.util.Try


case class TerminationPolicySettings(policy: String, stableTime: FiniteDuration, roleRatioMapping: Option[Map[String, Int]] = None){
  if(policy == TerminationPolicy.ROLE_RATIO){
    require(roleRatioMapping.nonEmpty, "Role Ratio policy must contain config field ratio")
  }
}

object TerminationPolicySettings extends Settings[TerminationPolicySettings]("akka.cluster.termination-policy"){
  override def fromSubConfig(c: Config): TerminationPolicySettings = {
    val policy: String = Try(c.getString("policy")).getOrElse(TerminationPolicy.MAJORITY)
    val stableTime: FiniteDuration = Try(c.getDuration("stableTime", TimeUnit.SECONDS).seconds).getOrElse(20 seconds)
    val roleRatioMapping: Option[Map[String, Int]] = Try(c.getConfig("ratio").entrySet()
      .asScala.map{ entry => entry.getKey -> entry.getValue.unwrapped().asInstanceOf[Int]}.toMap).toOption
    TerminationPolicySettings(policy, stableTime, roleRatioMapping)
  }

  def apply(): TerminationPolicySettings = TerminationPolicySettings(ConfigFactory.load())
}
