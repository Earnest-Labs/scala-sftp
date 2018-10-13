package com.earnest.util.sftp

import cats.effect.{Effect, IO, Resource}
import com.earnest.util.sftp.syntax.session._
import com.earnest.util.sftp.syntax.sftpSession._
import com.jcraft.jsch.{JSch, Session}
import io.github.andrebeat.pool.Pool
import cats.syntax.flatMap._
import com.earnest.util.sftp.config.SFTPConnectionConfig

trait Connection {
  def createSftpDataSourceResource[F[_]](config: SFTPConnectionConfig)(implicit F: Effect[F]): Resource[F, SFTPDataSource] =
    Resource.make(createSFTPDataSource(config))(p => F.delay(p.shutdown()))

  def createSFTPDataSource[F[_]](config: SFTPConnectionConfig, maxSessionCount: Int = 10)(implicit F: Effect[F]): F[SFTPDataSource] =
    F.delay {
      SFTPDataSource(
        Pool[SFTPSession](
          capacity = maxSessionCount, // sessions lazily created as needed
          factory = () =>
            (establishSession[IO](config) >>= (sess =>
              sess.createSFTPChannel[IO] >>= (chan =>
                IO(SFTPSession(sess, chan))))).unsafeRunSync(),
          dispose = _.close(), healthCheck = _.checkHealth()))
    }


  def establishSession[F[_]](config: SFTPConnectionConfig)(implicit F: Effect[F]): F[Session] = {
    F.delay {
      val jsch = new JSch
      config.privateKey.foreach{key => jsch.addIdentity("pk", key.getBytes, null, null)} // nulls necessary to pass key from variable
      val session = jsch.getSession(config.username, config.hostname)
      config.port.foreach(session.setPort)
      config.password.foreach(session.setPassword)
      session.setConfig("StrictHostKeyChecking", config.keyChecking)
      config.password.foreach(session.setPassword)
      session.connect()
      session
    }
  }
}
