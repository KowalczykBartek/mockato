[![](https://img.shields.io/badge/unicorn-approved-ff69b4.svg)](https://www.youtube.com/watch?v=9auOCbH5Ns4)

## why ?
If you need to create some HTTP mock dynamically (and easily), this is what mockato allows you to do.

## mockato.ovh
you can access mockato here http://mockato.ovh/ (http only)

## run locally
You need GraalVM (https://github.com/oracle/graal).
Embedded Graal is responsible for execution javascript code (not safe too much, but easy to implement :)).

to build.
```
./gradlew fatjar
```

also, you need following exports

```
export DATABASE_USER=<database user>
export DATABASE_PASSWORD=<password>
export DATABASE_NAME=<database name>
export HTTP_LISTEN_PORT=<port you want to listen to (default 8080)>
```

and, if you run it against some domain (not locally), you need to export
```
export APPLICATION_DOMAIN=.yourdomain.com
```

and of course, tables, that is described in [MockRepository](src/main/java/com.mockato.repo/MockRepository)

## UI
its nasty.