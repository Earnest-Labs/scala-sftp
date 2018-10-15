# scala-sftp
An SFTP wrapper library for [Jsch](http://www.jcraft.com/jsch/) that embraces [referential transparency](https://www.reddit.com/r/scala/comments/8ygjcq/can_someone_explain_to_me_the_benefits_of_io/e2s29ym) via 
[Cats-effect](https://typelevel.org/cats-effect/).
The library has built-in pooling support. It heavily uses the "enrich my library pattern".
  
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
A `go` wrapper is a proxy for all operations. All dependencies are managed by docker.

Usage: `./go <command> [sub-command]`

```
Available commands are:
    sbt [cmd] SBT commands (http://www.scala-sbt.org/)
    test      Run tests    
```     
