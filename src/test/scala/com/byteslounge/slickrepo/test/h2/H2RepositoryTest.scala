package com.byteslounge.slickrepo.test.h2

import com.byteslounge.slickrepo.test.RepositoryTest
import slick.driver.H2Driver

class H2RepositoryTest extends RepositoryTest(H2Driver, "h2")