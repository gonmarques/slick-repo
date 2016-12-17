package com.byteslounge.slickrepo.test.h2

import com.byteslounge.slickrepo.test.{H2Config, JodaTimeVersionedRepositoryTest}

class H2JodaTimeVersionedRepositoryTest extends JodaTimeVersionedRepositoryTest(H2Config.config)
