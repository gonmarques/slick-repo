enablePlugins(DockerPlugin)

dockerfile in docker := {
  new sbtdocker.mutable.Dockerfile {
    from("postgres:9.6.0")
    expose(5432)
  }
}
