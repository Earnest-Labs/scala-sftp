package com.earnest.util.sftp.syntax

import cats.effect.Effect
import com.jcraft.jsch.{ChannelSftp, Session}
import scala.language.implicitConversions

final class SessionOps(val sess: Session) extends AnyVal {
  def createSFTPChannel[F[_]](implicit F :Effect[F]): F[ChannelSftp] =
    F.delay {
      val channel = sess.openChannel("sftp")
      channel.connect()
      channel.asInstanceOf[ChannelSftp]
    }
}

trait ToSessionOps {
  implicit def toChannelOps(sess: Session): SessionOps =
    new SessionOps(sess)
}

object session extends ToSessionOps
