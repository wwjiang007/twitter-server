package com.twitter.server.handler

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.server.util.HttpUtils.newOk

class ReplyHandler(msg: String) extends Service[Request, Response] {
  def apply(req: Request) = newOk(msg)
}
