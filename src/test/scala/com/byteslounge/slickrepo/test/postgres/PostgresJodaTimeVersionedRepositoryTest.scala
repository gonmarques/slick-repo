package com.byteslounge.slickrepo.test.postgres

import com.byteslounge.slickrepo.test.{JodaTimeVersionedRepositoryTest, PostgresConfig}

class PostgresJodaTimeVersionedRepositoryTest extends JodaTimeVersionedRepositoryTest(PostgresConfig.config)
