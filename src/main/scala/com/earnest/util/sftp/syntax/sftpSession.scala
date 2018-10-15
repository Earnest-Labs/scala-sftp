package com.earnest.util.sftp.syntax

import com.earnest.util.sftp.SFTPSession
import scala.language.implicitConversions

final class SFTPSessionOps(val sftpSess: SFTPSession) extends AnyVal {
  def close(): Unit = {
    sftpSess.channel.disconnect()
    sftpSess.session.disconnect()
  }

  def checkHealth(): Boolean =
    sftpSess.session.isConnected && sftpSess.channel.isConnected
}

trait ToSFTPSessionOps {
  implicit def toTransferOps(sftpSess: SFTPSession): SFTPSessionOps =
    new SFTPSessionOps(sftpSess)
}

object sftpSession extends ToSFTPSessionOps
