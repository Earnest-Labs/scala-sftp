package com.earnest.util.sftp

import io.github.andrebeat.pool.{Lease, Pool}

final case class SFTPDataSource(pool: Pool[SFTPSession]) {
  def acquire: Lease[SFTPSession] = pool.acquire()
  def shutdown(): Unit = pool.close()
}
