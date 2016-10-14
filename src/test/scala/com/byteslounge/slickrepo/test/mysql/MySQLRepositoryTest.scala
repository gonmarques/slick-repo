package com.byteslounge.slickrepo.test.mysql

import com.byteslounge.slickrepo.test.RepositoryTest
import slick.driver.MySQLDriver

class MySQLRepositoryTest extends RepositoryTest(MySQLDriver, "mysql")