package com.byteslounge.slickrepo.test

import com.typesafe.slick.driver.db2.DB2Driver
import com.typesafe.slick.driver.ms.SQLServerDriver
import com.typesafe.slick.driver.oracle.OracleDriver
import slick.driver.{H2Driver, JdbcProfile, MySQLDriver, PostgresDriver}

case class Error(errorCode: Int, sqlState: String)
case class Config(driver: JdbcProfile, dbConfig: String, rollbackTxError: Error, rowLockTimeoutError: Error, validationQuery: String)

abstract class DatabaseConfig {
  def config: Config
}

object H2Config extends DatabaseConfig {
  override def config: Config = Config(H2Driver, "h2", Error(23505, "23505"), Error(50200, "HYT00"), "select 1")
}

object MySQLConfig extends DatabaseConfig {
  override def config: Config = Config(MySQLDriver, "mysql", Error(1062, "23000"), Error(1213, "40001"), "select 1")
}

object OracleConfig extends DatabaseConfig {
  override def config: Config = Config(OracleDriver, "oracle", Error(1, "23000"), Error(60, "61000"), "select 1 from dual")
}

object DB2Config extends DatabaseConfig {
  override def config: Config = Config(DB2Driver, "db2", Error(-803, "23505"), Error(-911, "40001"), "select 1 from sysibm.sysdummy1")
}

object PostgresConfig extends DatabaseConfig {
  override def config: Config = Config(PostgresDriver, "postgres", Error(0, "23505"), Error(0, "40P01"), "select 1")
}

object SQLServerConfig extends DatabaseConfig {
  override def config: Config = Config(SQLServerDriver, "sqlserver", Error(2627, "23000"), Error(1205, "40001"), "select 1")
}
