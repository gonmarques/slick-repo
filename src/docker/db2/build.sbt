enablePlugins(DockerPlugin)

dockerfile in docker := {
  new sbtdocker.mutable.Dockerfile {
    from("ibmcom/db2express-c:10.5.0.5-3.10.0")
    expose(50000)
  }
}
