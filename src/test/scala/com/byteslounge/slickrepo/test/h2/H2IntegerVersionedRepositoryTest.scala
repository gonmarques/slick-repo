package com.byteslounge.slickrepo.test.h2

import com.byteslounge.slickrepo.test.{H2Config, IntegerVersionedRepositoryAutoPkTest}

class H2IntegerVersionedRepositoryTest extends IntegerVersionedRepositoryAutoPkTest(H2Config.config)
