enablePlugins(DockerPlugin)
enablePlugins(com.tapad.docker.DockerComposePlugin)

dockerfile in docker := {
  new sbtdocker.mutable.Dockerfile {
    from("ibmcom/db2express-c:10.5.0.5-3.10.0")
    expose(50000)
    copy(baseDirectory(_ / "start.sh").value, file("start.sh"))
  }
}

dockerImageCreationTask := docker.value
