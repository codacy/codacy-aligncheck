FROM golang:1.15.3-alpine3.12 as builder

RUN apk add --no-cache git
RUN go get -u gitlab.com/opennota/check/cmd/aligncheck

FROM golang:1.15.3-alpine3.12

RUN apk add --no-cache openjdk8-jre
COPY --from=builder /go/bin/aligncheck /app/aligncheck

