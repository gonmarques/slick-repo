package com.byteslounge.slickrepo.test.mysql

import com.byteslounge.slickrepo.test.{IntegerVersionedRepositoryAutoPkTest, MySQLConfig}

class MySQLIntegerVersionedRepositoryTest extends IntegerVersionedRepositoryAutoPkTest(MySQLConfig.config)
