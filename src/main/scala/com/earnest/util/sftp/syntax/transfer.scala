package com.earnest.util.sftp.syntax

import java.io.InputStream

import com.jcraft.jsch.ChannelSftp
import com.earnest.util.sftp.syntax.query._
import cats.effect.Effect
import cats.syntax.flatMap._
import cats.syntax.traverse._
import cats.instances.list._
import com.earnest.util.sftp.{LeasedInputStream, SFTPDataSource}
import scala.language.implicitConversions

final class TransferOps[F[_]](val sds: SFTPDataSource[F]) extends AnyVal {
  def openStreamToFile(location: String)(implicit F: Effect[F]): F[LeasedInputStream] =
    sds.eval(F.delay(sds.acquire) >>= (lease => F.delay(LeasedInputStream(lease.get().channel.get(location), () => lease.release()))))

  def openStreamsToFilesInDir(dir: String, predicate: String => Boolean)(implicit F: Effect[F]): F[List[LeasedInputStream]] =
    sds.lsFiles(dir) >>= (xs => xs.filter(e => predicate(e.getFilename))
      .traverse(e => openStreamToFile(dir + "/" + e.getFilename)))

  def upload(location: String, is: InputStream)(implicit F: Effect[F]): F[Unit] =
    sds.eval(F.delay(sds.acquire(sess => sess.channel.put(is, location, ChannelSftp.OVERWRITE))))
}

trait ToTransferOps {
  implicit def toTransferOps[F[_]](sds: SFTPDataSource[F]): TransferOps[F] =
    new TransferOps(sds)
}

object transfer extends ToTransferOps
