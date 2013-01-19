package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent._
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json
import play.api.libs.iteratee.Enumerator
import models._

object Application extends Controller {
  
	def index = Action {
		val data = Dining.runDining
		val dataContent: Enumerator[Array[Byte]] = data
		Ok.stream(dataContent.andThen(Enumerator.eof))
	}
	
	def start = Action {
		Dining.runDining
		Ok("Start")
	}
	
	def stop = {
		Dining.stopDining
		Ok("End")
	}
	
	def act = Action { implicit request =>
		// to get a Blah object from request content
		val blah = json.Json.parse(request.body.asText.get).as[String]
	
		// to return Blah as application/json, you just have to convert your Blah to a JsValue and give it to Ok()
		Ok(json.Json.toJson(blah))
	}

}
