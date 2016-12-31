/*
 * Copyright 2016 byteslounge.com (Gonçalo Marques).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.byteslounge.slickrepo.test

import com.typesafe.slick.driver.db2.DB2Driver
import com.typesafe.slick.driver.ms.SQLServerDriver
import com.typesafe.slick.driver.oracle.OracleDriver
import slick.driver._

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

object DerbyConfig extends DatabaseConfig {
  override def config: Config = Config(DerbyDriver, "derby", Error(20000, "23505"), Error(30000, "40001"), "values 1")
}

object HsqlConfig extends DatabaseConfig {
  override def config: Config = Config(HsqldbDriver, "hsql", Error(-104, "23505"), Error(-4861, "40001"), "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS")
}
