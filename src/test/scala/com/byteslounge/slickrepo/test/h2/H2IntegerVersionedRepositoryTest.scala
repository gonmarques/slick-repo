package com.byteslounge.slickrepo.test.h2

import com.byteslounge.slickrepo.test.{H2Config, IntegerVersionedRepositoryTest}

class H2IntegerVersionedRepositoryTest extends IntegerVersionedRepositoryTest(H2Config.config)