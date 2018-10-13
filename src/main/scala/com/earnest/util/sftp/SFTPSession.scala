package com.earnest.util.sftp

import com.jcraft.jsch.{ChannelSftp, Session}

final case class SFTPSession(session: Session, channel: ChannelSftp)
