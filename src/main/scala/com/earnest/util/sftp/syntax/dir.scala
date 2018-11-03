package com.earnest.util.sftp.syntax

import cats.effect.Effect
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.traverse._
import cats.instances.list._

import com.earnest.util.sftp.SFTPDataSource
import com.earnest.util.sftp.syntax.query._

import scala.language.implicitConversions

final class DirOps[F[_]](val sds: SFTPDataSource[F]) extends AnyVal {
  def changeDir(location: String)(implicit F: Effect[F]): F[Unit] =
    sds.eval(F.delay(sds.acquire(sess => sess.channel.cd(location))))

  def rmdir(location: String)(implicit F: Effect[F]): F[Unit] =
    sds.eval(F.delay(sds.acquire(sess => sess.channel.rmdir(location))))

  def rmFile(location: String)(implicit F: Effect[F]): F[Unit] =
    sds.eval(F.delay(sds.acquire(sess => sess.channel.rm(location))))

  def rmFilesInDir(dir: String)(implicit F: Effect[F]): F[Unit] =
    sds.lsFiles(dir) >>= (_.traverse(e => rmFile(dir + "/" + e.getFilename)).void)

  def mkdir(location: String)(implicit F: Effect[F]): F[Unit] =
    sds.eval(F.delay(sds.acquire(sess => sess.channel.mkdir(location))))

  def pwd(implicit F: Effect[F]): F[String] =
    sds.eval(F.delay(sds.acquire(sess => sess.channel.pwd())))
}

trait ToDirOps {
  implicit def toDirOpsF[F[_]](sds: SFTPDataSource[F]): DirOps[F] =
    new DirOps(sds)
}

object dir extends ToDirOps
