package com.byteslounge.slickrepo.test.hsql

import com.byteslounge.slickrepo.test.{HsqlConfig, JodaTimeVersionedRepositoryTest}

class HsqlJodaTimeVersionedRepositoryTest extends JodaTimeVersionedRepositoryTest(HsqlConfig.config)
