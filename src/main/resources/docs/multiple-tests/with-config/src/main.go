package main

import (
	"fmt"
	"io"
)

type example struct {
	StringTest           string
	Reader             io.Reader
	Closer       io.Closer
	Int64    int64
	Boolean   bool
	Checking            bool
	ListOfExamples []string
	IsResponse       bool
	Err    error // any non-EOF error from reading Body

	IsCheck bool            // flush headers to network before body
	ComChannel   chan channel // non-nil if probeRequestBody called
}

func main() {
	if false {
		fmt.Println("HERE")
	}
}

func example() {}
