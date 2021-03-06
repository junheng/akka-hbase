package io.github.junheng.akka.hbase.proxy

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import io.github.junheng.akka.accessor.access.{AccessorAdapter, Accessor}
import io.github.junheng.akka.hbase.{HService, HTable}
import io.github.junheng.akka.locator.{LoadMonitor, Service, ServiceLocator}
import io.github.junheng.akka.monitor.dispatcher.MonitoredForkJoinPool
import io.github.junheng.akka.monitor.mailbox.SafeMailboxMonitor
import org.apache.log4j.{Level, Logger}

object APP extends App {

  Logger.getRootLogger.setLevel(Level.OFF)

  implicit val system = ActorSystem("hbase")

  implicit val config = ConfigFactory.load()

  MonitoredForkJoinPool.logger(system.log)

  LoadMonitor.monitorActorRef = system.actorOf(Props(new SafeMailboxMonitor(config.getConfig("safe-mailbox-monitor"))), "safe-mailbox-monitor")

  ServiceLocator.initialize("phb01,phb02,phb03")

  Accessor.start(config.getString("accessor.host"), config.getInt("accessor.port"))

  HService.start(config.getConfig("hbase"), (conn, name) => Props(new HTable(conn, name) with Service with AccessorAdapter))

}