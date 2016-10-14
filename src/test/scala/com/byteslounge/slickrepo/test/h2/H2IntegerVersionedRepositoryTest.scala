package com.byteslounge.slickrepo.test.h2

import com.byteslounge.slickrepo.test.IntegerVersionedRepositoryTest
import slick.driver.H2Driver

class H2IntegerVersionedRepositoryTest extends IntegerVersionedRepositoryTest(H2Driver, "h2")