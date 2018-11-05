package com.earnest.util.sftp

import java.io.{BufferedReader, ByteArrayInputStream, InputStreamReader}
import java.util.concurrent.Executors

import cats.effect.IO
import cats.syntax.flatMap._
import com.earnest.util.sftp.implicits._
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

import scala.concurrent.ExecutionContext

object sftpEnv {
  val tp = Executors.newFixedThreadPool(2)
  val blockingEc = ExecutionContext.fromExecutor(tp)
  implicit val cs = IO.contextShift(ExecutionContext.global)
  val sftpDS: SFTPDataSource[IO] = (config.getFromEnvironment[IO]() >>=
    (config => connection.createSFTPDataSource[IO](config, blockingEc))).unsafeRunSync()
}

final class SFTPIntreactionTest extends FreeSpec with Matchers with BeforeAndAfterAll {
  import sftpEnv._

  override def afterAll() = {
    sftpDS.shutdown()
  }

  s"SFTP client" - {
    val dir = "/share/"

    "should be able to upload to and download from SFTP server" in {

      sftpDS.rmFilesInDir(dir).unsafeRunSync()

      val goodCatFile = "goodCat.txt"
      val badCatFile = "badCat.txt"

      val goodCatContent = "I am a good, meowing cat"
      val badCatContent = "I am a bad, meowing cat"

      val fileNames = Set(goodCatFile, badCatFile)

      val goodCatFileLocation = dir + goodCatFile
      val badCatFileLocation = dir + badCatFile


      sftpDS.doesFileExist(goodCatFileLocation).unsafeRunSync() shouldBe false

      sftpDS.upload(goodCatFileLocation, new ByteArrayInputStream(goodCatContent.getBytes)).unsafeRunSync()
      sftpDS.upload(badCatFileLocation, new ByteArrayInputStream(badCatContent.getBytes)).unsafeRunSync()

      sftpDS.doesFileExist(goodCatFileLocation).unsafeRunSync() shouldBe true

      sftpDS.lstatFile(goodCatFileLocation).unsafeRunSync() shouldBe a[Some[_]]

      val lis1 = sftpDS.openStreamToFile(goodCatFileLocation).unsafeRunSync()
      val bfr = new BufferedReader(new InputStreamReader(lis1.stream))
      bfr.readLine() shouldBe goodCatContent
      bfr.read() shouldBe -1 // check if all contents were read
      bfr.close()
      lis1.releaseLease()

      val lis2 = sftpDS.openStreamToFile(badCatFileLocation).unsafeRunSync()
      val bfr2 = new BufferedReader(new InputStreamReader(lis2.stream))
      bfr2.readLine() shouldBe badCatContent
      bfr2.read() shouldBe -1
      bfr2.close()
      lis2.releaseLease()

      sftpDS.lsFiles(dir).unsafeRunSync().forall(e => fileNames.contains(e.getFilename)) shouldBe true
    }

    "should be able to create new folder and delete it" in {
      val newFolder = dir + "new"
      sftpDS.doesFolderExist(newFolder).unsafeRunSync() shouldBe false
      noException should be thrownBy sftpDS.mkdir(newFolder).unsafeRunSync()
      sftpDS.doesFolderExist(newFolder).unsafeRunSync() shouldBe true
      noException should be thrownBy sftpDS.changeDir(newFolder).unsafeRunSync()
      sftpDS.pwd.unsafeRunSync() shouldBe newFolder
      noException should be thrownBy sftpDS.rmdir(newFolder).unsafeRunSync()
    }
  }
}

