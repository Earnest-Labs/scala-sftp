
# scala-sftp

An SFTP wrapper library for Jsch that embraces referential transparency via Cats.Effect.
The library has built-in pooling support.
  
## Documentation
http://www.jcraft.com/jsch/

## Environment Variables
These are the expected environment variable by default. However, the key names are overridable.

"SFTP_HOST"                     - required

"SFTP_USER"                     - required

"SFTP_STRICT_HOST_KEY_CHECKING" - required

"SFTP_PASSWORD"                 - optional

"SFTP_PORT"                     - optional

"SFTP_PRIVATE_KEY"              - optional
     
     
## Script Usage

A `go` wrapper is a proxy for all operations. All dependencies are managed by docker

Usage: `./go <command> [sub-command]`

```
Available commands are:
    sbt [cmd] SBT commands (http://www.scala-sbt.org/)
    test      Run tests    
```     
