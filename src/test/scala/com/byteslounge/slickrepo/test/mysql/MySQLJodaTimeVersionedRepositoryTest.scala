package com.byteslounge.slickrepo.test.mysql

import com.byteslounge.slickrepo.test.{JodaTimeVersionedRepositoryTest, MySQLConfig}

class MySQLJodaTimeVersionedRepositoryTest extends JodaTimeVersionedRepositoryTest(MySQLConfig.config)
