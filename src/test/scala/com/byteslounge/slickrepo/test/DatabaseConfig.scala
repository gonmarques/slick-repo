/*
 * MIT License
 *
 * Copyright (c) 2016 Gonçalo Marques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.byteslounge.slickrepo.test

import com.byteslounge.slickrepo.scalaversion.JdbcProfile
import com.byteslounge.slickrepo.test.scalaversion._

case class Error(errorCode: Int, sqlState: String)
case class Config(driver: JdbcProfile, dbConfig: String, rollbackTxError: Error, rowLockTimeoutError: Error, validationQuery: String)

abstract class DatabaseConfig {
  def config: Config
}

object H2Config extends DatabaseConfig {
  override def config: Config = Config(H2Profile, "h2", Error(23505, "23505"), Error(50200, "HYT00"), "select 1")
}

object MySQLConfig extends DatabaseConfig {
  override def config: Config = Config(MySQLProfile, "mysql", Error(1062, "23000"), Error(1213, "40001"), "select 1")
}

object OracleConfig extends DatabaseConfig {
  override def config: Config = Config(OracleProfile, "oracle", Error(1, "23000"), Error(60, "61000"), "select 1 from dual")
}

object DB2Config extends DatabaseConfig {
  override def config: Config = Config(DB2Profile, "db2", Error(-803, "23505"), Error(-911, "40001"), "select 1 from sysibm.sysdummy1")
}

object PostgresConfig extends DatabaseConfig {
  override def config: Config = Config(PostgresProfile, "postgres", Error(0, "23505"), Error(0, "40P01"), "select 1")
}

object SQLServerConfig extends DatabaseConfig {
  override def config: Config = Config(SQLServerProfile, "sqlserver", Error(2627, "23000"), Error(1205, "40001"), "select 1")
}

object DerbyConfig extends DatabaseConfig {
  override def config: Config = Config(DerbyProfile, "derby", Error(20000, "23505"), Error(30000, "40001"), "values 1")
}

object HsqlConfig extends DatabaseConfig {
  override def config: Config = Config(HsqldbProfile, "hsql", Error(-104, "23505"), Error(-4861, "40001"), "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS")
}
