package com.earnest.util.sftp.syntax

import java.util.stream.Collectors

import com.jcraft.jsch.{ChannelSftp, SftpATTRS}
import java.util.{Vector => JVector}

import cats.effect.Effect
import com.earnest.util.sftp.SFTPDataSource

import scala.collection.JavaConverters._
import scala.language.implicitConversions

final class QueryOps[F[_]](val sds: SFTPDataSource[F]) extends AnyVal {
  def lsFiles(dir: String)(implicit F: Effect[F]): F[List[ChannelSftp#LsEntry]] =
    sds.eval(F.delay(sds.acquire(sess => sess.channel.ls(dir).asInstanceOf[JVector[ChannelSftp#LsEntry]])
      .stream()
      .filter(e => !e.getAttrs.isDir)
      .collect(Collectors.toList()) // convert Java Vector to Java List
      .asScala
      .toList // convert to Scala Lit
    ))

  // handling error below for all cases where the underlying library throws an exception if the file/folder is not there

  def lstatFile(location: String)(implicit F: Effect[F]): F[Option[SftpATTRS]] =
    sds.eval(F.handleError(F.delay(sds.acquire(sess => Option(sess.channel.lstat(location)))))(_ => None))

  def doesFileExist(location: String)(implicit F: Effect[F]): F[Boolean] =
    sds.eval(F.handleError(F.delay(sds.acquire(sess => !sess.channel.lstat(location).isDir)))(_ => false))

  def doesFolderExist(location: String)(implicit F: Effect[F]): F[Boolean] =
    sds.eval(F.handleError(F.delay(sds.acquire(sess => sess.channel.lstat(location).isDir)))(_ => false))
}

trait ToQueryOps {
  implicit def toChannelQueryOps[F[_]](sds: SFTPDataSource[F]): QueryOps[F] =
    new QueryOps(sds)
}

object query extends ToQueryOps
