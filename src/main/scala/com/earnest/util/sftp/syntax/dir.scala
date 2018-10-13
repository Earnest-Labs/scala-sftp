package com.earnest.util.sftp.syntax

import cats.effect.Effect
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.traverse._
import cats.instances.list._

import com.earnest.util.sftp.SFTPDataSource
import com.earnest.util.sftp.syntax.query._

import scala.language.implicitConversions

final class DirOps(val sds: SFTPDataSource) extends AnyVal {
  def changeDir[F[_]](location: String)(implicit F: Effect[F]): F[Unit] =
    F.delay(sds.acquire(sess => sess.channel.cd(location)))

  def rmdir[F[_]](location: String)(implicit F: Effect[F]): F[Unit] =
    F.delay(sds.acquire(sess => sess.channel.rmdir(location)))

  def rmFile[F[_]](location: String)(implicit F: Effect[F]): F[Unit] =
    F.delay(sds.acquire(sess => sess.channel.rm(location)))

  def rmFilesInDir[F[_]](dir: String)(implicit F: Effect[F]): F[Unit] =
    sds.lsFiles(dir) >>= (_.traverse(e => rmFile(dir + "/" + e.getFilename)).void)

  def mkdir[F[_]](location: String)(implicit F: Effect[F]): F[Unit] =
    F.delay(sds.acquire(sess => sess.channel.mkdir(location)))

  def pwd[F[_]](implicit F: Effect[F]): F[String] =
    F.delay(sds.acquire(sess => sess.channel.pwd()))
}

trait ToDirOps {
  implicit def toDirOps(sds: SFTPDataSource): DirOps =
    new DirOps(sds)
}

object dir extends ToDirOps
