package com.byteslounge.slickrepo.test.h2

import com.byteslounge.slickrepo.test.{H2Config, LongVersionedRepositoryTest}

class H2LongVersionedRepositoryTest extends LongVersionedRepositoryTest(H2Config.config)
