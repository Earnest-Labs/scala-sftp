package com.earnest.util.sftp.syntax

trait AllSyntax
  extends ToSessionOps
    with ToTransferOps
    with ToQueryOps
    with ToSFTPSessionOps
    with ToDirOps

object all extends AllSyntax
