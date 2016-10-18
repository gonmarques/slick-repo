package com.byteslounge.slickrepo.test

import com.typesafe.slick.driver.db2.DB2Driver
import com.typesafe.slick.driver.oracle.OracleDriver
import slick.driver.{H2Driver, JdbcProfile, MySQLDriver}

case class Config(driver: JdbcProfile, dbConfig: String, rollbackTxError: Int, rowLockTimeoutError: Int, validationQuery: String)

abstract class DatabaseConfig {
  def config: Config
}

object H2Config extends DatabaseConfig {
  override def config: Config = Config(H2Driver, "h2", 23505, 50200, "select 1")
}

object MySQLConfig extends DatabaseConfig {
  override def config: Config = Config(MySQLDriver, "mysql", 1062, 1213, "select 1")
}

object OracleConfig extends DatabaseConfig {
  override def config: Config = Config(OracleDriver, "oracle", 1, 60, "select 1 from dual")
}

object DB2Config extends DatabaseConfig {
  override def config: Config = Config(DB2Driver, "db2", 1, 60, "select 1 from sysibm.sysdummy1")
}
