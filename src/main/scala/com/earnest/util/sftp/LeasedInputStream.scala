package com.earnest.util.sftp

import java.io.InputStream

final case class LeasedInputStream(stream: InputStream, releaseLease: () => Unit)
