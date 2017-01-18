/*
 * Copyright 2017 byteslounge.com (Gonçalo Marques).
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

package object scalaversion {
  type H2Profile = slick.jdbc.H2Profile
  val DB2Profile = slick.jdbc.DB2Profile
  val SQLServerProfile = slick.jdbc.SQLServerProfile
  val OracleProfile = slick.jdbc.OracleProfile
  val H2Profile = slick.jdbc.H2Profile
  val MySQLProfile = slick.jdbc.MySQLProfile
  val PostgresProfile = slick.jdbc.PostgresProfile
  val DerbyProfile = slick.jdbc.DerbyProfile
  val HsqldbProfile = slick.jdbc.HsqldbProfile
}
