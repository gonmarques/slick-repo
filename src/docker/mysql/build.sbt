enablePlugins(DockerPlugin)
enablePlugins(com.tapad.docker.DockerComposePlugin)

dockerfile in docker := {
  new sbtdocker.mutable.Dockerfile {
    from("mysql/mysql-server:5.7.15")
    expose(3306)
  }
}

dockerImageCreationTask := docker.value
