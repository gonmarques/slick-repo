package com.byteslounge.slickrepo.test.mysql

import com.byteslounge.slickrepo.test.IntegerVersionedRepositoryTest
import slick.driver.MySQLDriver

class MySQLIntegerVersionedRepositoryTest extends IntegerVersionedRepositoryTest(MySQLDriver, "mysql")