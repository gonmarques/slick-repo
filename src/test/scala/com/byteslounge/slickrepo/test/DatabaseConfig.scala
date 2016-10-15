package com.byteslounge.slickrepo.test

import slick.driver.{H2Driver, JdbcProfile, MySQLDriver}

case class Config(driver: JdbcProfile, dbConfig: String, rollbackTxError: Int)

abstract class DatabaseConfig {
  def config: Config
}

object H2Config extends DatabaseConfig {
  override def config: Config = Config(H2Driver, "h2", 23505)
}

object MySQLConfig extends DatabaseConfig {
  override def config: Config = Config(MySQLDriver, "mysql", 1062)
}