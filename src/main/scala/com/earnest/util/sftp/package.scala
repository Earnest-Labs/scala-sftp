package com.earnest.util

import com.earnest.util.sftp.syntax.AllSyntax

package object sftp {
  object connection extends Connection
  object implicits extends AllSyntax
}
