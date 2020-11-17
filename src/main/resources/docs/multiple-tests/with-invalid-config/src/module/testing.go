package module

import "io"

type example2 struct {
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

func testing() string {
	return "testing"
}
