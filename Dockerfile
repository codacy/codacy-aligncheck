FROM golang:1.17.2-alpine3.14 as builder

RUN apk add --no-cache git
RUN go get -u gitlab.com/opennota/check/cmd/aligncheck

FROM amazoncorretto:8-alpine3.14-jre

RUN apk add bash

# install go as it is required by aligncheck
COPY --from=builder /usr/local/go/ /usr/local/go/
ENV PATH="/usr/local/go/bin:${PATH}"

COPY --from=builder /go/bin/aligncheck /app/aligncheck
COPY src/main/resources/docs /docs
COPY target/universal/stage/ /opt/docker/

RUN adduser -u 2004 -D docker && chmod +x /opt/docker/bin/codacy-aligncheck

USER docker

WORKDIR /src
ENTRYPOINT ["/opt/docker/bin/codacy-aligncheck"]
