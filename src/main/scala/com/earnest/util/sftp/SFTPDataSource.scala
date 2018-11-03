package com.earnest.util.sftp

import cats.effect.ContextShift
import io.github.andrebeat.pool.{Lease, Pool}

import scala.concurrent.ExecutionContext

final case class SFTPDataSource[F[_]](
  pool: Pool[SFTPSession],
  blockingEc: ExecutionContext,
  cs: ContextShift[F]) {

  def acquire: Lease[SFTPSession] = pool.acquire()
  def shutdown(): Unit = pool.close()
  def eval[A](fa: F[A]): F[A] = cs.evalOn(blockingEc)(fa)
}
