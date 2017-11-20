package com.twitter.server.view

import com.twitter.finagle.http.{Request, Response, Status, Version}
import com.twitter.finagle.Service
import com.twitter.io.Buf
import com.twitter.server.util.HttpUtils.{newOk, newResponse}
import com.twitter.util.{Await, Future}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IndexViewTest extends FunSuite {
  test("wraps content based on http fragments") {
    val fragment = new Service[Request, Response] {
      def apply(req: Request) =
        newResponse(contentType = "text/html;charset=UTF-8", content = Buf.Utf8("<h1>hello</h1>"))
    }

    val nofragment = new Service[Request, Response] {
      def apply(req: Request) = newOk("hello")
    }

    val idx = new IndexView("test", "", () => Seq())
    val req = Request("/")
    req.headerMap.set("Accept", "text/html")

    val svc0 = idx andThen fragment
    val res0 = Await.result(svc0(req))
    assert(res0.headerMap.get("content-type") == Some("text/html;charset=UTF-8"))
    assert(res0.status == Status.Ok)
    assert(res0.contentString.contains("<html>"))

    val svc1 = idx andThen nofragment
    req.headerMap.set("Accept", "*/*")
    val res1 = Await.result(svc1(req))
    assert(res1.headerMap.get("content-type") == Some("text/plain;charset=UTF-8"))
    assert(res1.status == Status.Ok)
    assert(res1.contentString == "hello")
  }

  test("handles missing content-type header") {
    val svc = new Service[Request, Response] {
      def apply(req: Request) = {
        val response = Response(Version.Http11, Status.Ok)
        response.content = Buf.Utf8("string")
        Future.value(response)
      }
    }

    val idx = new IndexView("test", "", () => Seq())
    val req = Request("/")
    req.headerMap.set("Accept", "text/html")

    val res = Await.result(idx(req, svc))
    assert(res.status == Status.Ok)
    assert(res.contentType.isEmpty)
  }
}
