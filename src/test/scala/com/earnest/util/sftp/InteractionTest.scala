package com.earnest.util.sftp

import java.io.{BufferedReader, ByteArrayInputStream, InputStreamReader}

import cats.effect.IO
import cats.syntax.flatMap._
import com.earnest.util.sftp.implicits._
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

object sftpEnv {
  val sftpDS: SFTPDataSource = (config.getFromEnvironment[IO]() >>= (config => connection.createSFTPDataSource[IO](config))).unsafeRunSync()
}

final class SFTPIntreactionTest extends FreeSpec with Matchers with BeforeAndAfterAll {
  import sftpEnv._

  override def afterAll() = {
    sftpDS.shutdown()
  }

  s"SFTP client" - {
    val dir = "/share/"

    "should be able to upload to and download from SFTP server" in {

      sftpDS.rmFilesInDir[IO](dir).unsafeRunSync()

      val goodCatFile = "goodCat.txt"
      val badCatFile = "badCat.txt"

      val goodCatContent = "I am a good, meowing cat"
      val badCatContent = "I am a bad, meowing cat"

      val fileNames = Set(goodCatFile, badCatFile)

      val goodCatFileLocation = dir + goodCatFile
      val badCatFileLocation = dir + badCatFile


      sftpDS.doesFileExist[IO](goodCatFileLocation).unsafeRunSync() shouldBe false

      sftpDS.upload[IO](goodCatFileLocation, new ByteArrayInputStream(goodCatContent.getBytes)).unsafeRunSync()
      sftpDS.upload[IO](badCatFileLocation, new ByteArrayInputStream(badCatContent.getBytes)).unsafeRunSync()

      sftpDS.doesFileExist[IO](goodCatFileLocation).unsafeRunSync() shouldBe true

      sftpDS.lstatFile[IO](goodCatFileLocation).unsafeRunSync() shouldBe a[Some[_]]

      val lis1 = sftpDS.openStreamToFile[IO](goodCatFileLocation).unsafeRunSync()
      val bfr = new BufferedReader(new InputStreamReader(lis1.stream))
      bfr.readLine() shouldBe goodCatContent
      bfr.read() shouldBe -1 // check if all contents were read
      bfr.close()
      lis1.releaseLease()

      val lis2 = sftpDS.openStreamToFile[IO](badCatFileLocation).unsafeRunSync()
      val bfr2 = new BufferedReader(new InputStreamReader(lis2.stream))
      bfr2.readLine() shouldBe badCatContent
      bfr2.read() shouldBe -1
      bfr2.close()
      lis2.releaseLease()

      sftpDS.lsFiles[IO](dir).unsafeRunSync().forall(e => fileNames.contains(e.getFilename)) shouldBe true
    }

    "should be able to create new folder and delete it" in {
      val newFolder = dir + "new"
      sftpDS.doesFolderExist[IO](newFolder).unsafeRunSync() shouldBe false
      noException should be thrownBy sftpDS.mkdir[IO](newFolder).unsafeRunSync()
      sftpDS.doesFolderExist[IO](newFolder).unsafeRunSync() shouldBe true
      noException should be thrownBy sftpDS.changeDir[IO](newFolder).unsafeRunSync()
      sftpDS.pwd[IO].unsafeRunSync() shouldBe newFolder
      noException should be thrownBy sftpDS.rmdir[IO](newFolder).unsafeRunSync()
    }
  }
}

