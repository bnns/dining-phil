package controllers

import models._
import play.api._
import play.api.mvc._
import play.api.libs.concurrent._
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import play.api.libs.iteratee._
import akka.dispatch.Await
import akka.dispatch.Future
import akka.util._
import akka.util.duration._
import java.util.concurrent.TimeUnit

object Application extends Controller {

	def index = Action { implicit request =>
 		Ok(views.html.index())
	}

	def chatRoom(username: Option[String]) = Action { implicit request =>
		username.filterNot(_.isEmpty).map { username =>
		  Ok(views.html.chatRoom(username))
		}.getOrElse {
		  Redirect(routes.Application.index).flashing(
		    "error" -> "Please choose a valid username."
		  )
		}
	}
  
	def chat(username: String) = WebSocket.async[JsValue] { request  =>
		ChatRoom.join(username)
	}

}