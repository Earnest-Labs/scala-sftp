package com.earnest.util.sftp

import cats.data.{Validated, ValidatedNel, NonEmptyList => Nel}
import cats.effect.Effect
import cats.syntax.apply._
import cats.syntax.flatMap._

object config {

  final case class EnvironmentKeys(
    host: String,
    username: String,
    password: String,
    port: String,
    hostKeyChecking: String,
    privateKey: String
  )
  object EnvironmentKeys {
    def default: EnvironmentKeys = EnvironmentKeys(
      host = "SFTP_HOST",
      username = "SFTP_USER",
      password = "SFTP_PASSWORD",
      port = "SFTP_PORT",
      hostKeyChecking = "SFTP_STRICT_HOST_KEY_CHECKING",
      privateKey = "SFTP_PRIVATE_KEY"
    )
  }

  sealed abstract case class SFTPConnectionConfig (
    hostname: String,
    username: String,
    password: Option[String],
    keyChecking: String,
    port: Option[Int],
    privateKey: Option[String]
  )

  def getFromEnvironment[F[_]](
    keys: EnvironmentKeys = EnvironmentKeys.default,
    env: Map[String, String] = sys.env)(implicit F: Effect[F]): F[SFTPConnectionConfig] =
    F.delay(validate(keys, env)) >>= (validationRes =>
      validationRes.fold(errs =>
        F.raiseError[SFTPConnectionConfig](new RuntimeException(errs.toList.mkString("Invalid SFTP config: ", "\n", "\n"))),
        F.delay(_))
      )

  private def validate(keys: EnvironmentKeys, envMap: Map[String, String]): ValidatedNel[String, SFTPConnectionConfig] = {
    def error(name: String): Nel[String] = Nel.of(s"SFTP config validation error: Required environment variable '$name' not set")

    def validatePort(port: Option[String]) = port match {
      case r@Some(p) if p.forall(_.isDigit) => Validated.valid(r)
      case Some(_) => Validated.invalid("Port has to be an integer value")
      case n@None => Validated.valid(n)
    }

    (Validated.fromOption(envMap.get(keys.host), error(keys.host)),
      Validated.fromOption(envMap.get(keys.username), error(keys.username)),
      Validated.valid(envMap.get(keys.password)).toValidatedNel,
      Validated.fromOption(envMap.get(keys.hostKeyChecking), error(keys.hostKeyChecking)),
      validatePort(envMap.get(keys.port)).map(_.map(_.toInt)).toValidatedNel,
      Validated.valid(envMap.get(keys.privateKey)).toValidatedNel).mapN((host, user, pass, keychecking, port, privateKey) =>
        new SFTPConnectionConfig(host, user, pass, keychecking, port, privateKey) {})
  }
}
