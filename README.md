# Slick CRUD Repositories

|Database|CI |Build status|
|--------|---|------------|
|MySQL, Oracle, DB2, PostgreSQL, Derby, H2, Hsql|Travis CI|[![Build status](https://travis-ci.org/gonmarques/slick-repo.svg?branch=master)](https://travis-ci.org/gonmarques/slick-repo)|
|SQLServer|AppVeyor|[![Build status](https://ci.appveyor.com/api/projects/status/3httes30fa1foes1/branch/master?svg=true)](https://ci.appveyor.com/project/gonmarques/slick-repo)|

[![Coverage Status](https://coveralls.io/repos/github/gonmarques/slick-repo/badge.svg?branch=master)](https://coveralls.io/github/gonmarques/slick-repo)&nbsp;&nbsp;&nbsp;![Latest Release](https://img.shields.io/github/release/gonmarques/slick-repo.svg)&nbsp;&nbsp;&nbsp;[![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202.0-7c39ef.svg)](http://www.apache.org/licenses/LICENSE-2.0)

Slick Repositories is an aggregation of common database operations in ready-to-be-used generic and type-safe repositories, best known as DAOs.

**Main features**:
 - Provide common database operations like *save*, *update*, *find*, *delete* or *count* in a type-safe way
 - Other operations like Optimistic Locking (aka versioning), Pessimistic Locking or custom query/statement execution are also supported
 - In order to maximize performance, all provided operations are backed by Slick compiled queries, as recommended in [Slick Documentation](http://slick.lightbend.com/doc/3.1.1/queries.html)
