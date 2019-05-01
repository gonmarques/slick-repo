import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions

/*
 * MIT License
 *
 * Copyright (c) 2019 Gonçalo Marques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

def extractScripts(scriptsDir: String, imagesHome: String): Unit = {
  unzipImageDir(scriptsDir)
  chmodExecutable(imagesHome)
}

def unzipImageDir(scriptsDir: String): Unit = {
  IO.unzip(file(s"$scriptsDir.zip"), file(s"$scriptsDir"))
}

def chmodExecutable(imagesHome: String): Unit = {
  val permissions = PosixFilePermissions.fromString("rwxr-xr-x")
  Files.setPosixFilePermissions(file(s"$imagesHome/buildDockerImage.sh").toPath, permissions);
}

lazy val buildOracle = taskKey[Unit]("Build Oracle")
buildOracle := {
  val scriptsDir = "./src/docker/oracle-build/docker-images"
  val imagesHome = s"$scriptsDir/OracleDatabase/SingleInstance/dockerfiles"
  val fileName = "b3JhY2xlLXhlLTExLjIuMC0xLjAueDg2XzY0LnJwbS56aXAK"
  val destination = file(s"$imagesHome/11.2.0.2/$fileName.rpm.zip")
  extractScripts(scriptsDir, imagesHome)
  if (!destination.exists()) {
    IO.download(
      new URL(s"https://wonderkit.herokuapp.com/$fileName"),
      destination
    )
  }
  scala.sys.process.Process(Seq("./buildDockerImage.sh", "-v", "11.2.0.2"), file(imagesHome)).!
}
